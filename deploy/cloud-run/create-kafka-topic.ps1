$ErrorActionPreference = "Stop"

param(
    [string]$Topic = "inventory.movements",
    [string]$ClusterId,
    [string]$Location = "us-central1",
    [int]$Partitions = 3,
    [int]$ReplicationFactor = 3,
    [string]$ProjectId
)

if (-not $ClusterId) { throw "ClusterId es obligatorio" }
if (-not $ProjectId) { throw "ProjectId es obligatorio" }

gcloud config set project $ProjectId | Out-Null

gcloud managed-kafka topics create $Topic `
  --cluster $ClusterId `
  --location $Location `
  --partitions $Partitions `
  --replication-factor $ReplicationFactor
