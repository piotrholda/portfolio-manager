# Portfolio Manager

## Open API JSON
http://localhost:8080/v3/api-docs

## Swagger UI
http://localhost:8080/swagger-ui/index.html

## Strategy and simulation response formats

Both endpoints accept the existing JSON request body:

- `POST /v1/strategy/dualEquityMomentum`
- `POST /v1/simulation/dualEquityMomentum`

Send `Accept: application/json` to get structured JSON. CSV remains the default
when `Accept` is absent or `*/*`, and is explicitly available with `Accept: text/csv`.
For multiple accepted formats, quality weights (`q`) determine the response;
CSV wins ties for backward compatibility. Unsupported formats return HTTP 406.
CSV retains its existing columns, rounding and attachment filename.

JSON contains:

- `quotations`: an array of `{ ticker, points }` series. Each ticker includes
  `code`, `exchangeCode` and `currencyCode`; each point contains `date` and `value`.
  Strategy values are adjusted closing prices in the ticker's currency.
  Simulation values are percentage changes from simulation start.
- `transactions`: an array of `{ date, transactionType, ticker }`.
- `results` (simulation only): portfolio percentage changes as `{ date, value }` points.
  A value of `0` means unchanged, `10` means +10%, and `-5` means -5%.

Dates use `YYYY-MM-DD`. JSON numbers retain backend calculation precision
without CSV's two-decimal rounding. Series are ordered by ticker code, exchange
and currency; points and transactions are ordered by date. Missing observations
are omitted rather than filled with zero. JSON responses are not file attachments.
The request bodies and both response schemas are documented in Swagger UI.

## To Do List
1. Add last Transaction on the 1-st day of each month when last quotation is last day of current month.
2. Implement skip last n month algorithm.

## Python API Swagger UI
http://localhost:5000/docs

## Python API Open API JSON
http://localhost:5000/openapi.json

## Python API ReDoc
http://localhost:5000/redoc

## H2 Console
http://localhost:8080/h2-console

## Build process
1. Build and run Docker Compose
```bash
docker compose up --build -d
```
2. Build Maven
```bash
mvn clean install
```
