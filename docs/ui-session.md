# Warstwa webowa — stan prac

Aktualizacja: 2026-10-03.

## Zamknięcie sesji — 2026-10-03

- Użytkownik zakończył pracę na dziś. Brak rozpoczętych zadań wymagających dokończenia.
- Moduł Instrumenty jest ukończony. W chwili zamykania sesji drzewo robocze było
  czyste, a HEAD wskazywał `edc4dbc` (`Implement Instrument UI.`).
  Niniejsza aktualizacja dokumentu jest jedyną zmianą wykonaną przy zamknięciu sesji.
- Ostatnia weryfikacja: pełny build `-Pui package`, 42 testy backendu i 8 testów UI
  zakończone sukcesem. Nie trzeba powtarzać testów dla samej aktualizacji tego pliku.
- Agent nie pozostawił uruchomionych serwerów. Użytkownik uruchamiał backend
  samodzielnie w IntelliJ; stan procesów uruchomionych przez użytkownika nie był sprawdzany.
- Kolejny moduł nie został jeszcze wybrany. Przy wznowieniu ustalić zakres z użytkownikiem;
  notowania/import, strategie i symulacje nadal mają puste ekrany.

## Uruchomienie przy wznowieniu

- Backend: istniejąca konfiguracja IntelliJ, zwykle port 8080.
- Frontend: konfiguracja npm dla `frontend/package.json`, command `run`, script `dev`;
  adres http://127.0.0.1:5173. Można połączyć konfiguracje przez Compound.
- Po pobraniu nowych zależności wykonać `npm.cmd ci` w katalogu `frontend`.
- Do Vite wymagany jest Node >=22.12.0; rekomendowano Node 24 LTS.
  Użytkownik zgłaszał systemowy Node 21 i otrzymał instrukcję aktualizacji przez MSI.
  Nie potwierdził jeszcze wykonania aktualizacji. W IntelliJ należy wskazać właściwy
  interpreter Node; npm w PowerShell uruchamiać jako `npm.cmd`.
- Build całości: `mvnw -Pui clean package`. Profil Maven instaluje własny Node 24.19.0,
  niezależnie od wersji systemowej. Python API musi udostępniać OpenAPI na porcie 5000
  dla istniejącego generatora klienta Java.
- Testy UI: `npm.cmd test` w `frontend`; szczegóły obsługi aplikacji w README.

## Cel i etap

Realizacja `.codex/prompts/ui.md`: UI w React w tym samym repozytorium,
wywoływanie istniejącego API, tabele i wykresy, trwały zapis stanu prac.
Backend JSON został ukończony i wypchnięty przez użytkownika. Następnie użytkownik
zlecił szkielet UI z ogólnym layoutem, bez funkcji biznesowych, aby sprawdzić build
i uruchamianie. Aplikacja pozostaje jednoużytkownikowa, bez autentykacji
i autoryzacji. Rozszerzenie backendu ma zachować dotychczasową funkcjonalność.

## Ustalenia z kodu

- Backend: Java 11, Spring Boot 2.5.5, Maven, OpenAPI.
- API `/v1/security`, `/v1/quotation`, `/v1/corporateaction`,
  `/v1/strategy/dualEquityMomentum`, `/v1/simulation/dualEquityMomentum`.
- Strategie i symulacje obsługują CSV i JSON. Strategia pokazuje wybór
  instrumentów, symulacja dodatkowo znormalizowane wyniki portfela.
- Import notowań obejmuje dostawcę danych i plik CSV.
- Osobny serwis Python istnieje w `python-stock-api/`.
- Profil Maven `ui` buduje frontend i kopiuje go do zasobów statycznych JAR-a.
- `.codex/plan.md` wskazuje symulacje i wykresy jako cel, a model FIRE jako kolejny feature.

## Propozycja do dyskusji

- React + TypeScript + Vite jako SPA w `frontend/`.
- Moduły funkcjonalne: instruments, quotations, corporate-actions, strategies, simulations.
- React Router, TanStack Query, Mantine i Mantine Charts (Recharts).
- Logika inwestycyjna i obliczenia wyników pozostają w Javie.
- Przeglądarka komunikuje się ze Springiem; integracja z Pythonem pozostaje po stronie backendu.
- Rozwój: Vite z proxy `/v1` do Springa. Wdrożenie początkowe:
  statyczny build UI w JAR, fallback SPA z wyłączeniem API.
- Zaimplementowano reprezentację JSON wyników przez `Accept: application/json`,
  zachowując dotychczasowy CSV jako format domyślny.
- Typy API docelowo generowane z kontrolowanego snapshotu OpenAPI.
- Stan serwera w TanStack Query, formularze lokalnie, filtry widoków w URL.
- Pierwszy pionowy zakres: parametry symulacji → API → wykres + tabela + eksport CSV.

## Wykonane zmiany backendu

- Te same endpointy i requesty; format odpowiedzi wybierany nagłówkiem Accept.
- CSV domyślnie także przy wildcard i remisie wag q; nieakceptowany format daje 406.
- Jawne DTO: StrategyResponse, SimulationResponse i wspólne ResultData.
- JSON zawiera quotations (serie ticker + points), transactions oraz results
  dla symulacji. Punkt to date + value. Pełna tożsamość tickera zachowuje giełdę i walutę.
- Strategia zwraca skorygowane ceny; symulacja procentowe zmiany od początku symulacji,
  zarówno dla instrumentów, jak i wyników portfela. JSON nie dodaje zaokrągleń CSV.
- Schematy obu formatów w OpenAPI, opis kontraktu w README.
- Logika obliczeń i kod generowania CSV pozostają bez zmian.
- Nie zmieniano docker-compose.yml; zmiana nie wymaga nowego serwisu.
- Rozszerzono testy integracyjne o zgodność JSON/CSV, nagłówki, brak Accept,
  wildcard, wagi q, HTTP 406 i OpenAPI. Dodano testy jednostkowe negocjacji formatu.

## Weryfikacja

- Pełne `mvn test` przez wrapper: 33 testy, 0 błędów, 0 niepowodzeń,
  0 pominiętych (2026-10-03).
- Log: `target/json-api-full-tests.log`.
- Maven uruchomiono poza sandboxem po zgodzie użytkownika, ponieważ sandbox
  blokuje połączenia sieciowe wrappera.

## Następny krok

Użytkownik przetestował i wypchnął szkielet UI. Zlecił teraz listę i dodawanie
instrumentów na podstawie istniejących endpointów Security. Moduł ten jest
zaimplementowany; pozostałe ekrany czekają na dalsze instrukcje.

## Szkielet UI — wykonane

- `frontend/`: React, TypeScript, Vite, Mantine, React Router, ikony Lucide;
  dokładne wersje w package.json i package-lock.json.
- Polski layout: boczne menu, nagłówek, przegląd modułów, stopka, responsywne menu.
- Puste ekrany: symulacje, strategie, instrumenty, notowania, zdarzenia korporacyjne.
- Brak pobierania danych, przykładowych wyników, formularzy i obliczeń.
- `mvnw -Pui clean package`: lokalny Node 24.19.0, npm ci, kontrola TypeScript,
  Vite build, testy Java, frontend w `BOOT-INF/classes/static`.
- Build backendu bez profilu ui nadal dostępny. Dockerfile korzysta z tego samego JAR-a.
- Vite dev na 127.0.0.1:5173, proxy API do localhost:8080 (API_PROXY_TARGET umożliwia zmianę).
- UiController przekazuje tylko jawnie wskazane trasy do index.html; brak ogólnego
  fallbacku przechwytującego API lub pliki statyczne.
- CI buduje profil ui i uruchamia Python API potrzebne istniejącemu generatorowi klienta.
  Zmiany CI nie były uruchamiane na GitHub Actions w tej sesji.
- Naprawiono mvnw.cmd: normalizacja BASE_DIR usuwa problem końcowego ukośnika
  przed cudzysłowem. Wrapper -v działa; lokalne JAVA_HOME wskazuje JDK 21,
  natomiast wykonany pełny build korzystał bezpośrednio z Javy 11.
- README zawiera instrukcje budowania, uruchamiania i pracy z Vite.

## Weryfikacja UI i pakowania

- Pełny clean package z profilem ui: BUILD SUCCESS, 42 testy, bez błędów i pominięć.
- Log pełnego builda: `ui-build.log` (ignorowany przez Git).
- Uruchomiono wygenerowany JAR na porcie 8081 z tymczasową bazą H2 w pamięci;
  sprawdzono HTTP 200 dla wszystkich 6 tras UI, JS/CSS, OpenAPI, Swagger i GET /v1/security.
- Brakujący endpoint API i brakujący plik JS poprawnie zwracają HTTP 404.
- Vite dev uruchomiony i sprawdzony: HTML, głęboka trasa, transformowane TSX i CSS
  zwracają HTTP 200. Po sprawdzeniu serwer Vite zatrzymano.
- Brak dostępnej przeglądarki w narzędziu CUA: wygląd i interakcje w przeglądarce
  wymagają jeszcze sprawdzenia przez użytkownika; nie deklarowano testu wizualnego.
- Testowy JAR pozostawiono na http://127.0.0.1:8081 do obejrzenia layoutu;
  dostępność zależy od utrzymania procesu sesji. Nie używa bazy użytkownika.
  Następnie zatrzymano go na prośbę użytkownika i potwierdzono brak odpowiedzi portu.

## Instrumenty — wykonane

- `/instruments` podłączono do GET i POST `/v1/security` bez zmiany backendu.
- Tabela: nazwa, polski opis typu, symbol, giełda, waluta; toleruje brakujące
  wartości starszych rekordów.
- Modal dodawania: nazwa, typ (SHARE, ETF, CURRENCY), symbol, giełda i waluta.
  Nazwa/symbol wymagane, waluta 3 litery, giełda opcjonalna. Przycinanie spacji,
  normalizacja kodów do wielkich liter, długości zgodne z polami encji.
- Po zapisie komunikat sukcesu, uzupełnienie cache i automatyczne odświeżenie listy.
- Obsługa ładowania, pustej listy, błędów HTTP/sieci oraz ręczne ponowienie GET.
- Nieudany POST zachowuje formularz. Podczas zapisu zablokowano ponowne wysłanie
  i zamykanie modalu.
- Dodano TanStack Query do obsługi danych serwera i wspólny klient JSON.
- Zaktualizowano przegląd modułów, aby Instrumenty nie były oznaczone jako placeholder.
- Dodano Vitest, Testing Library i jsdom; testy działają także w profilu Maven ui.
- Testy interakcji obejmują kontrakt API, brakujące dane, ładowanie, pustą listę,
  błąd HTML/proxy i ponowienie, dodawanie/odświeżenie, walidację, błąd zapisu
  i ponowienie, blokadę podwójnego zapisu, błąd sieci oraz anulowanie formularza.
- Samodzielne `npm test`: 8 testów przeszło; `npm run build` zakończone poprawnie.
- Pełny `-Pui package`: BUILD SUCCESS, 42 testy backendu i 8 testów frontendu,
  bez błędów (2026-10-03). Log: `ui-build.log`.
- Nie uruchamiano dodatkowych serwerów ani nie dopisywano testowych instrumentów
  do bazy użytkownika. Testy UI używają kontrolowanych odpowiedzi HTTP w jsdom.
