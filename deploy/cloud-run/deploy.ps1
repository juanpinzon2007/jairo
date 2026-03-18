$ErrorActionPreference = "Stop"

param(
    [string]$ProjectId,
    [string]$Region = "europe-west1",
    [string]$Repository = "inventory-microservices",
    [string]$ImageTag = "latest"
)

if (-not $ProjectId) { throw "ProjectId es obligatorio" }

$baseImage = "$Region-docker.pkg.dev/$ProjectId/$Repository"

gcloud run deploy user-service `
  --project $ProjectId `
  --region $Region `
  --image "$baseImage/user-service:$ImageTag" `
  --allow-unauthenticated `
  --env-vars-file "deploy/cloud-run/user-service.env.yaml"

gcloud run deploy catalog-service `
  --project $ProjectId `
  --region $Region `
  --image "$baseImage/catalog-service:$ImageTag" `
  --allow-unauthenticated `
  --env-vars-file "deploy/cloud-run/catalog-service.env.yaml"

gcloud run deploy inventory-service `
  --project $ProjectId `
  --region $Region `
  --image "$baseImage/inventory-service:$ImageTag" `
  --allow-unauthenticated `
  --env-vars-file "deploy/cloud-run/inventory-service.env.yaml"

gcloud run deploy report-service `
  --project $ProjectId `
  --region $Region `
  --image "$baseImage/report-service:$ImageTag" `
  --allow-unauthenticated `
  --env-vars-file "deploy/cloud-run/report-service.env.yaml"

gcloud run deploy api-gateway `
  --project $ProjectId `
  --region $Region `
  --image "$baseImage/api-gateway:$ImageTag" `
  --allow-unauthenticated `
  --env-vars-file "deploy/cloud-run/api-gateway.env.yaml"
