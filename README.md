# Portfolio Manager

## Open API JSON
http://localhost:8080/v3/api-docs

## Swagger UI
http://localhost:8080/swagger-ui/index.html

## To Do List
1. Find dividend data source and adjust quotations.
2. Add splits and normalize historical data. Add manually. (https://www.digrin.com/stocks/detail/CWI/stock_split)

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
