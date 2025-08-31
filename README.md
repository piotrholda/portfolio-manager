# Portfolio Manager

## Open API JSON
http://localhost:8080/v3/api-docs

## Swagger UI
http://localhost:8080/swagger-ui/index.html

## To Do List
1. Add last Transaction on the 1-st day of each month when last quotation is last day of current month.
2. Implement skip last n month algorithm.

## Python API Swagger UI
http://localhost:5000/docs

## Python API Open API JSON
http://localhost:5000/openapi.json

## Python API ReDoc
http://localhost:5000/redoc

## Build process
1. Build and run Docker Compose
```bash
docker compose up --build -d
```
2. Build Maven
```bash
mvn clean install
```
