# Inventario con microservicios reactivos

Backend de inventario con arquitectura distribuida sobre Spring Boot, WebFlux, PostgreSQL, Kafka, Docker y Google Cloud Run.

## Microservicios

- `api-gateway`: punto de entrada unico.
- `discovery-server`: Eureka para entorno local y Docker.
- `user-service`: autenticacion JWT, usuarios y roles.
- `catalog-service`: categorias y productos.
- `inventory-service`: entradas, salidas, stock y publicacion de eventos Kafka.
- `report-service`: reportes agregados con proyecciones locales alimentadas por Kafka.
- `shared-lib`: seguridad JWT y contratos compartidos.

## Reglas funcionales implementadas

- Todo producto nace con stock `0`.
- El stock solo se alimenta por movimientos `ENTRY` y `EXIT`.
- El rol `USER` puede registrar entradas y salidas.
- El rol `USER` no puede crear categorias ni productos.
- El rol `ADMIN` puede crear usuarios, categorias y productos.
- Las salidas se registran sin importar a donde vaya el producto; esa referencia queda en `reference` y `notes`.

## Estructura tecnica

```text
.
|-- api-gateway
|-- catalog-service
|-- discovery-server
|-- inventory-service
|-- report-service
|-- shared-lib
|-- user-service
|-- docker-compose.yml
`-- deploy/cloud-run
```

Cada microservicio tiene:

- `application.yml`
- `application-cloud.yml`
- `Dockerfile`
- base de datos PostgreSQL independiente
- migraciones Flyway
- endpoints reactivos con WebFlux

## Endpoints principales

### Autenticacion y usuarios

- `POST /api/auth/login`
- `GET /api/users` solo `ADMIN`
- `POST /api/users` solo `ADMIN`

### Catalogo

- `GET /api/categories`
- `POST /api/categories` solo `ADMIN`
- `GET /api/products`
- `GET /api/products/{productId}`
- `POST /api/products` solo `ADMIN`

### Inventario

- `POST /api/movements`
- `GET /api/stocks/{productId}`
- `GET /api/movements/product/{productId}`

### Reportes

- `GET /api/reports/stock-summary`
- `GET /api/reports/movements/{productId}`

## Credenciales iniciales

- `admin@inventory.local` / `Admin123*`
- `user@inventory.local` / `User123*`

## Ejecucion local

### Requisitos

- Java 17
- Maven o `mvnw`
- Docker y Docker Compose

### Opcion 1: Maven

```bash
./mvnw clean verify
```

### Opcion 2: Docker Compose

```bash
docker compose up --build
```

Puertos locales:

- Gateway: `8080`
- User Service: `8081`
- Catalog Service: `8082`
- Inventory Service: `8083`
- Report Service: `8084`
- Eureka: `8761`
- Kafka externo: `9094`
- PostgreSQL: `5433`, `5434`, `5435`, `5436`

## Flujo sugerido

1. Login con `admin@inventory.local`.
2. Crear categorias.
3. Crear productos.
4. Login con `user@inventory.local`.
5. Registrar entradas y salidas.
6. Consultar stock y reportes.

## Kafka

- `inventory-service` publica cada movimiento en el topic `inventory.movements`.
- `report-service` consume ese topic y mantiene tablas de proyeccion propias.
- En local, `docker-compose.yml` levanta Kafka en modo KRaft.
- En Google Cloud Run, la configuracion queda lista para un cluster de Google Cloud Managed Service for Apache Kafka o cualquier broker Kafka compatible.

## Google Cloud Run

La configuracion de nube queda preparada para desplegar sin Eureka, usando URLs directas entre servicios, PostgreSQL en Cloud SQL y Kafka administrado.

Si despliegas desde la UI de Cloud Run conectando este repositorio directamente, la raiz del repo ahora construye `api-gateway` por defecto mediante el `Dockerfile` raiz. Eso evita que Cloud Run intente compilar el `pom.xml` agregador como si fuera una aplicacion ejecutable.

### Archivos incluidos

- `cloudbuild.yaml`: build de imagenes hacia Artifact Registry.
- `deploy/cloud-run/*.env.yaml`: variables por microservicio.
- `deploy/cloud-run/deploy.ps1`: despliegue de los 5 servicios en Cloud Run.
- `deploy/cloud-run/create-kafka-topic.ps1`: creacion del topic `inventory.movements`.

### Recomendacion de infraestructura

- 1 Artifact Registry para imagenes.
- 1 instancia Cloud SQL PostgreSQL.
- 4 bases logicas separadas en Cloud SQL:
  - `user_db`
  - `catalog_db`
  - `inventory_db`
  - `report_db`
- 1 cluster de Google Cloud Managed Service for Apache Kafka.
- 5 servicios en Cloud Run:
  - `api-gateway`
  - `user-service`
  - `catalog-service`
  - `inventory-service`
  - `report-service`

### Build de imagenes

```bash
gcloud builds submit --config cloudbuild.yaml
```

### Despliegue rapido desde repositorio

Si solo quieres publicar la API de entrada desde la UI de Cloud Run:

1. Conecta este repositorio.
2. Usa la raiz del repo.
3. Deja que Cloud Run use el `Dockerfile` raiz.

Ese flujo construye `api-gateway` en el puerto `8080`.

### Despliegue

1. Editar los archivos `deploy/cloud-run/*.env.yaml`.
2. Reemplazar placeholders de Cloud SQL, URLs, secreto JWT y credenciales Kafka.
3. Crear el topic:

```powershell
./deploy/cloud-run/create-kafka-topic.ps1 -ProjectId TU_PROYECTO -ClusterId TU_CLUSTER
```

4. Desplegar:

```powershell
./deploy/cloud-run/deploy.ps1 -ProjectId TU_PROYECTO -ImageTag TU_TAG
```

## Notas de despliegue

- En `cloud`, Eureka queda deshabilitado automaticamente.
- La comunicacion entre servicios se resuelve con variables `CLIENTS_*_URL` y `SERVICES_*_URI`.
- La conexion a Cloud SQL esta preparada con `com.google.cloud.sql.postgres.SocketFactory`.
- Kafka usa `SPRING_KAFKA_BOOTSTRAP_SERVERS` y propiedades SASL/SSL si el cluster lo requiere.

## Estado de validacion

No pude ejecutar `mvn verify` en este entorno porque la sesion no tiene `java` ni `JAVA_HOME` disponibles. La estructura, codigo y despliegue quedaron preparados, pero la compilacion real debes correrla en una maquina con JDK 17 instalado.
