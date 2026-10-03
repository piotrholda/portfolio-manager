# Warstwa webowa — stan prac

Aktualizacja: 2026-10-03.

## Cel i etap

Realizacja `.codex/prompts/ui.md`: UI w React w tym samym repozytorium,
wywoływanie istniejącego API, tabele i wykresy, trwały zapis stanu prac.
Po dyskusji użytkownik zlecił najpierw dodanie JSON do strategii i symulacji.
UI jest kolejnym etapem. Aplikacja pozostaje jednoużytkownikowa, bez autentykacji
i autoryzacji. Rozszerzenie backendu ma zachować dotychczasową funkcjonalność.

## Ustalenia z kodu

- Backend: Java 11, Spring Boot 2.5.5, Maven, OpenAPI.
- API `/v1/security`, `/v1/quotation`, `/v1/corporateaction`,
  `/v1/strategy/dualEquityMomentum`, `/v1/simulation/dualEquityMomentum`.
- Strategie i symulacje obsługują CSV i JSON. Strategia pokazuje wybór
  instrumentów, symulacja dodatkowo znormalizowane wyniki portfela.
- Import notowań obejmuje dostawcę danych i plik CSV.
- Osobny serwis Python istnieje w `python-stock-api/`.
- Maven kopiuje obecnie tylko zasoby YAML; osadzenie UI w JAR wymaga zmiany konfiguracji.
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

Backend zakończony i zweryfikowany. Wrócić do UI zgodnie z dalszymi instrukcjami
użytkownika. Propozycja technologii powyżej pozostaje punktem wyjścia.
