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
