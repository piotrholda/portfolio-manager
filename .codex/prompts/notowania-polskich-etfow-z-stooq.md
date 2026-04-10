Pobieranie notowań polskich ETF-ów z serwisu Stooq można zrealizować za pomocą API:

Przykład dla ETF-u Beta na GPW:

https://stooq.pl/q/d/l/?s=etfbm40tr.pl&d1=20250101&d2=20260329&i=d

Gdzie:

s=etfbm40tr.pl — ticker,
d1=20250101 — data od,
d2=20260329 — data do,
i=d — dane dzienne.

Jeśli chcesz tylko wszystkie dzienne dane bez podawania zakresu, często działa też krótsza wersja:

https://stooq.pl/q/d/l/?s=etfbm40tr.pl&i=d
