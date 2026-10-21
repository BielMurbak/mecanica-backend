param([switch]$SemPausa)

# Roteiro da demonstracao do Mensal 2 (Kubernetes no Minikube).
# Cada passo limpa a tela, mostra o comando, executa e espera voce tirar o print.
# Uso (na raiz do repo mecanica-backend):
#   powershell -ExecutionPolicy Bypass -File docs\evidencias\rodar-demo.ps1

$ErrorActionPreference = "Continue"

function Passo([string]$n, [string]$titulo, [string]$mostrar, [scriptblock]$acao) {
    Clear-Host
    Write-Host "=== Passo $n : $titulo ===" -ForegroundColor Yellow
    Write-Host "> $mostrar" -ForegroundColor Cyan
    Write-Host ""
    & $acao
    Write-Host ""
    if (-not $SemPausa) {
        Read-Host "Tire o print (Win+Shift+S) e aperte Enter para o proximo passo" | Out-Null
    }
}

Passo "1" "Pods (com IP e no)" "kubectl get pods -n mecanica -o wide" {
    kubectl get pods -n mecanica -o wide
}

Passo "2" "Services" "kubectl get services -n mecanica" {
    kubectl get services -n mecanica
}

Passo "3" "Deployments" "kubectl get deployments -n mecanica" {
    kubectl get deployments -n mecanica
}

Passo "4" "ConfigMap, Secret e disco (PVC)" "kubectl get configmap,secret,pvc -n mecanica" {
    kubectl get configmap,secret,pvc -n mecanica
}

Passo "5" "Balanceamento de carga (20 chamadas ao Service frontend)" "kubectl run lb-teste ... curl http://frontend/ (20x) e kubectl logs" {
    kubectl delete pod lb-teste -n mecanica --ignore-not-found | Out-Null
    kubectl run lb-teste --restart=Never --image=curlimages/curl -n mecanica -- sh -c 'i=0; while [ $i -lt 20 ]; do curl -s -I http://frontend/ | grep -i x-served-by; i=$((i+1)); done' | Out-Null
    do {
        Start-Sleep -Seconds 3
        $fase = kubectl get pod lb-teste -n mecanica -o jsonpath='{.status.phase}'
    } until ($fase -eq "Succeeded" -or $fase -eq "Failed")
    $linhas = kubectl logs lb-teste -n mecanica
    $linhas
    Write-Host ""
    Write-Host "--- Quantas respostas por Pod do frontend:" -ForegroundColor Green
    $linhas | Group-Object | Select-Object Count, Name | Format-Table -AutoSize
    kubectl delete pod lb-teste -n mecanica | Out-Null
}

$script:podApagado = ""

Passo "6" "Auto-recuperacao: apagar um Pod do backend (ANTES e logo DEPOIS)" "kubectl delete pod <um backend> -n mecanica  e  kubectl get pods -l app=backend" {
    $script:podApagado = kubectl get pods -n mecanica -l app=backend -o jsonpath='{.items[0].metadata.name}'
    Write-Host "ANTES:" -ForegroundColor Green
    kubectl get pods -n mecanica -l app=backend
    Write-Host ""
    Write-Host "Apagando o Pod $($script:podApagado) ..." -ForegroundColor Green
    kubectl delete pod $script:podApagado -n mecanica --wait=false
    Start-Sleep -Seconds 5
    Write-Host ""
    Write-Host "5 SEGUNDOS DEPOIS (o antigo termina e um novo nasce):" -ForegroundColor Green
    kubectl get pods -n mecanica -l app=backend
}

Passo "7" "Auto-recuperacao: o Deployment voltou a ter 2 de 2" "kubectl rollout status deploy/backend  e  kubectl get pods" {
    kubectl rollout status deploy/backend -n mecanica --timeout=300s
    Write-Host ""
    kubectl get pods -n mecanica -l app=backend
    Write-Host ""
    kubectl get deployments -n mecanica
}

Passo "8" "Persistencia: dados ANTES e DEPOIS de apagar o Pod do Postgres" "kubectl delete pod -l app=postgres  e  SELECT na tabela mecanicos" {
    Write-Host "ANTES (dados no banco):" -ForegroundColor Green
    kubectl exec deployment/postgres -n mecanica -- psql -U mecanica -d mecanica -c "SELECT id, nome, especialidade FROM mecanicos;"
    Write-Host "Apagando o Pod do Postgres ..." -ForegroundColor Green
    kubectl delete pod -l app=postgres -n mecanica
    kubectl rollout status deploy/postgres -n mecanica --timeout=180s
    Start-Sleep -Seconds 5
    Write-Host ""
    Write-Host "Pod novo:" -ForegroundColor Green
    kubectl get pods -n mecanica -l app=postgres
    Write-Host ""
    Write-Host "DEPOIS (os dados continuam la):" -ForegroundColor Green
    kubectl exec deployment/postgres -n mecanica -- psql -U mecanica -d mecanica -c "SELECT id, nome, especialidade FROM mecanicos;"
}

Clear-Host
Write-Host "Pronto! Faltam os prints da APLICACAO no navegador:" -ForegroundColor Yellow
Write-Host "  1) Em outro terminal:  kubectl port-forward svc/frontend 8080:80 -n mecanica"
Write-Host "  2) Abra http://localhost:8080  (login admin / admin123)"
Write-Host "  3) Print da tela de Mecanicos e da tela de Servicos"
