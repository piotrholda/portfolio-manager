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

Java 11 is required. The `ui` Maven profile builds React + TypeScript + Vite,
runs backend tests, and packages the frontend in the Spring Boot JAR. It downloads
its own Node 24.19.0 into `target/frontend-tools`; a global Node installation is
not needed for this build. Dependencies are pinned in `frontend/package-lock.json`.
The Windows wrapper uses `JAVA_HOME` if set; make sure it points to the intended
JDK (check with `.\mvnw.cmd -v`).

1. Start the Python API (required by the existing Java client generator):

```bash
docker compose up --build -d python-api
```

Wait until http://localhost:5000/openapi.json responds before building.

2. Build backend and UI from the repository root:

```powershell
.\mvnw.cmd -Pui clean package
```

On Linux/macOS use `./mvnw -Pui clean package` (or `mvn -Pui clean package`).
The existing backend-only workflow remains available without `-Pui`.

3. Run the combined application:

```powershell
java -jar target/portfolio-manager-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

Open http://localhost:8080. The `dev` profile uses the existing local H2 database
under `db/dev`. The UI has a responsive layout. The Instruments page lists and
creates securities through the existing `/v1/security` API. Other modules have
placeholder pages. API and Swagger remain at their existing URLs.
The Dockerfile also serves the UI when its input JAR was built with `-Pui`.
Docker Compose currently starts only the Python service, not the Java application.

## Frontend development

For automatic browser updates while editing UI, install Node 24 LTS and run:

```powershell
cd frontend
npm.cmd ci
npm.cmd run dev
```

Open http://127.0.0.1:5173. On Linux/macOS use `npm` instead of `npm.cmd`.
The current layout works without a backend. Vite proxies `/v1`, `/v3/api-docs`
and Swagger paths to `http://localhost:8080` when the backend is running.
Set `API_PROXY_TARGET` in `frontend/.env.local` to use another backend port, e.g.
`API_PROXY_TARGET=http://localhost:8081`.

Standalone frontend build and static preview:

```bash
npm run build
npm run preview
```

Preview is available at http://127.0.0.1:4173 and does not proxy API requests.
The source lives in `frontend/src`; build output goes to `frontend/dist`.
The Maven profile copies that output into `BOOT-INF/classes/static` inside the JAR.
Only the known UI routes are forwarded to `index.html`, so missing API endpoints
and static assets continue to return 404.

## Instruments UI

Open the Instruments page (`/instruments`) to list securities or add a new one.
The table shows name, type, symbol, exchange and currency. The form supports
shares, ETFs and currencies. Name, symbol and a three-letter currency code are
required; exchange is optional. Symbol, exchange and currency codes are normalized
to uppercase. After a successful save the list refreshes automatically.
Failed saves preserve entered values, and failed list requests can be retried.

Frontend interaction tests use Vitest, Testing Library and jsdom:

```powershell
cd frontend
npm.cmd test
```

These tests also run automatically with the Maven `ui` profile during the `test`
phase, including the `package` build.
