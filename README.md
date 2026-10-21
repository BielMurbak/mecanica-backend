# Mecânica Backend

API REST (Spring Boot 3.5 + Java 17) para gestão de mecânicos e serviços de oficina.

## Rodando localmente com Docker Compose (app + Postgres)

```bash
docker compose up -d --build
```

Sobe o Postgres e a API em `http://localhost:8080`. Um usuário admin é criado automaticamente no primeiro start (`admin` / `admin123`, configurável via env vars).

Parar e remover:

```bash
docker compose down
```

## Build manual da imagem (sem compose)

```bash
docker build -t mecanica-backend .
docker run -d --name mecanica-backend -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://<host-do-banco>:5432/mecanica \
  -e SPRING_DATASOURCE_USERNAME=mecanica \
  -e SPRING_DATASOURCE_PASSWORD=mecanica \
  -e CORS_ALLOWED_ORIGINS=http://<host-do-frontend> \
  -e APP_ADMIN_USERNAME=admin \
  -e APP_ADMIN_PASSWORD=admin123 \
  mecanica-backend
```

Use esse comando na VM do backend, apontando `SPRING_DATASOURCE_URL` para o IP/DNS da VM do banco e `CORS_ALLOWED_ORIGINS` para o IP/DNS da VM do frontend.

## Variáveis de ambiente

Ver [.env.example](.env.example) para a lista completa. Principais:

| Variável | Descrição | Default |
|---|---|---|
| `PORT` | Porta HTTP da API | `8080` |
| `SPRING_DATASOURCE_URL` | URL JDBC do Postgres | `jdbc:postgresql://localhost:5432/mecanica` |
| `SPRING_DATASOURCE_USERNAME` / `_PASSWORD` | Credenciais do banco | `mecanica` / `mecanica` |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Estratégia de schema (`update`, `validate`, ...) | `update` |
| `APP_ADMIN_USERNAME` / `APP_ADMIN_PASSWORD` | Usuário admin criado no seed | `admin` / `admin123` |
| `CORS_ALLOWED_ORIGINS` | Origem(s) permitida(s) para o frontend | `http://localhost:5173` |

## Autenticação

HTTP Basic Auth. `POST /api/auth/login` valida usuário/senha e retorna `{username, role}` — usado pelo frontend para confirmar o login antes de guardar as credenciais e enviá-las em toda requisição via header `Authorization: Basic`.

## Endpoints

- `POST /api/auth/login`
- `GET/POST /api/mecanicos`, `GET/PUT/DELETE /api/mecanicos/{id}`
- `GET/POST /api/servicos`, `GET/PUT/DELETE /api/servicos/{id}`

## Build sem Docker

```bash
mvn package
java -jar target/app.jar
```

## Kubernetes com Minikube (Mensal 2)

Toda a aplicação roda dentro do cluster: PostgreSQL, backend (2 réplicas) e frontend (2 réplicas). Os manifestos ficam **neste repositório** (`k8s/`); o código do frontend está no repositório [mecanica-frontend](https://github.com/BielMurbak/mecanica-frontend), que deve estar clonado **ao lado** deste (`../mecanica-frontend`).

```
Navegador ──► Service frontend (NodePort 30080) ──► 2 Pods frontend (nginx)
                                                        │  proxy /api
                                                        ▼
                                  Service backend (ClusterIP :8080) ──► 2 Pods backend (Spring Boot)
                                                        │
                                                        ▼
                                  Service postgres (ClusterIP :5432) ──► 1 Pod postgres + PVC (1Gi)
```

| Arquivo | Recursos |
|---|---|
| `k8s/00-namespace.yaml` | Namespace `mecanica` |
| `k8s/postgres.yaml` | Secret, PVC, Deployment (1 réplica), Service ClusterIP |
| `k8s/backend.yaml` | ConfigMap, Secret, Deployment (2 réplicas, probes, initContainer), Service ClusterIP |
| `k8s/frontend.yaml` | ConfigMap, Deployment (2 réplicas), Service NodePort |

### Pré-requisitos

Docker Desktop (em execução), Minikube e kubectl.

### Passo a passo

Execute nesta pasta (`mecanica-backend`), com `mecanica-frontend` clonado ao lado.

**1. Iniciar o Minikube**

```bash
minikube start --driver=docker --cpus=2 --memory=3072
```

**2. Construir as imagens e disponibilizá-las ao Minikube**

```bash
docker build -t mecanica-backend:v1 .
docker build -t mecanica-frontend:v1 ../mecanica-frontend
minikube image load mecanica-backend:v1
minikube image load mecanica-frontend:v1
```

Os manifestos usam `imagePullPolicy: IfNotPresent` e tag fixa (`v1`), por isso o cluster usa as imagens carregadas e não tenta baixá-las de um registry.

**3. Aplicar os manifestos**

```bash
kubectl apply -f k8s/00-namespace.yaml
kubectl apply -f k8s/
```

**4. Aguardar os Pods ficarem prontos**

```bash
kubectl -n mecanica get pods -w
```

O backend pode levar alguns minutos na primeira subida (o Spring inicia devagar com pouca CPU). Aguarde `1/1 Running` em todos os Pods.

**5. Acessar a aplicação**

```bash
kubectl -n mecanica port-forward svc/frontend 8080:80
```

Abra http://localhost:8080 e entre com `admin` / `admin123` (definidos em `k8s/backend.yaml`). Deixe o terminal aberto. O `port-forward` fica preso a um Pod: se o frontend for recriado ou atualizado, rode o comando de novo. Alternativa: `minikube service frontend -n mecanica --url` (também precisa do terminal aberto com o driver docker no Windows).

### Roteiro da demonstração

```bash
kubectl get pods -n mecanica -o wide
kubectl get services -n mecanica
kubectl get deployments -n mecanica
```

**Balanceamento de carga.** O nginx devolve o header `X-Served-By` (Pod do frontend) e o backend devolve `X-Backend-Pod` (Pod do backend). O `port-forward` fixa um único Pod, então para ver a alternância use o NodePort por dentro do nó:

```bash
for i in $(seq 1 10); do minikube ssh "curl -s -I localhost:30080/" | grep -i x-served-by; done
```

```bash
for i in $(seq 1 10); do minikube ssh "curl -s -i -X POST localhost:30080/api/auth/login -H 'Content-Type: application/json' -d '{\"username\":\"admin\",\"password\":\"admin123\"}'" | grep -i x-backend-pod; done
```

**Recriação automática de Pod.** Apague um Pod do backend e veja o Deployment criar outro (a aplicação continua respondendo pelo outro Pod):

```bash
kubectl -n mecanica get pods -l app=backend
kubectl -n mecanica delete pod NOME-DO-POD
kubectl -n mecanica get pods -w
```

Troque `NOME-DO-POD` pelo nome real que apareceu no primeiro comando, sem os sinais `<` `>` (o PowerShell não os aceita).

**Persistência.** Cadastre um mecânico, apague o Pod do Postgres e confira que o dado continua lá (volume PVC):

```bash
kubectl -n mecanica delete pod -l app=postgres
```

### Adaptações feitas para o Kubernetes

- **Backend**: `spring-boot-starter-actuator` para os probes de liveness e readiness (`/actuator/health/**` liberado no Spring Security); `DataSeeder` tolera duas réplicas criando o admin ao mesmo tempo; filtro `PodHeaderFilter` devolve o header `X-Backend-Pod` para identificar o Pod.
- **Frontend** (repo `mecanica-frontend`): o nginx virou template configurável (`default.conf.template`): DNS detectado do ambiente, upstream via `BACKEND_UPSTREAM` (no cluster, o FQDN `backend.mecanica.svc.cluster.local:8080`, porque o nginx não usa *search domains*), `proxy_pass` sem URI e `Host` repassado com a porta (`$http_host`). A tela de login não envia mais `Authorization` na própria chamada de login.
- **Ordem de subida**: um initContainer aguarda o Postgres antes de iniciar o backend (no Compose isso era o `depends_on` com healthcheck).
- **Segredos**: as senhas dos manifestos são só para a demonstração local; em produção ficariam fora do repositório.

### Evidências

Saídas reais dos comandos da demonstração (Pods, Services, Deployments, balanceamento, Pod recriado e persistência) estão em [`docs/evidencias/evidencia-demonstracao.txt`](docs/evidencias/evidencia-demonstracao.txt). Os prints de tela ficam na mesma pasta.

### Limpeza

```bash
kubectl delete namespace mecanica
minikube stop
```
