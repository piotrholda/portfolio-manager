import { afterEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MantineProvider } from '@mantine/core';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter } from 'react-router-dom';
import { App } from '../../App';
import type { Security } from './api';

const existing: Security = {
  securityId: 'existing-id', name: 'Verizon Communications', type: 'SHARE',
  googleTicker: { code: 'VZ', exchangeCode: 'NYSE', currencyCode: 'USD' },
};
const created: Security = {
  securityId: 'created-id', name: 'Vanguard S&P 500 ETF', type: 'ETF',
  googleTicker: { code: 'VOO', exchangeCode: null, currencyCode: 'USD' },
};
const clients: QueryClient[] = [];
const json = (body: unknown, status = 200) => new Response(JSON.stringify(body), {
  status, headers: { 'Content-Type': 'application/json' },
});

function renderApp() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } });
  clients.push(client);
  return render(
    <MantineProvider env="test">
      <QueryClientProvider client={client}>
        <MemoryRouter initialEntries={['/instruments']}><App /></MemoryRouter>
      </QueryClientProvider>
    </MantineProvider>,
  );
}

async function openAndFillForm() {
  const user = userEvent.setup();
  await user.click(screen.getByRole('button', { name: 'Dodaj instrument' }));
  await user.type(screen.getByRole('textbox', { name: /Nazwa instrumentu/ }), '  Vanguard S&P 500 ETF  ');
  await user.type(screen.getByRole('textbox', { name: /Symbol instrumentu/ }), ' voo ');
  await user.type(screen.getByRole('textbox', { name: /Waluta/ }), 'usd');
  return user;
}

afterEach(() => { clients.splice(0).forEach((client) => client.clear()); });

describe('Instrumenty', () => {
  it('loads the real API contract and tolerates incomplete existing instruments', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(json([
      existing, { securityId: 'legacy', name: 'Bez tickera', type: null, googleTicker: null },
    ]));
    renderApp();
    const row = await screen.findByRole('row', { name: /Verizon Communications/ });
    expect(within(row).getByText('Akcja')).toBeTruthy();
    expect(within(row).getByText('VZ')).toBeTruthy();
    expect(within(row).getByText('NYSE')).toBeTruthy();
    expect(within(row).getByText('USD')).toBeTruthy();
    expect(screen.getByRole('row', { name: /Bez tickera/ })).toBeTruthy();
    expect(fetchMock.mock.calls[0][0]).toBe('/v1/security');
    expect(fetchMock.mock.calls[0][1]?.headers).toEqual({ Accept: 'application/json' });
  });

  it('shows loading and then the empty state', async () => {
    let resolve!: (response: Response) => void;
    vi.spyOn(globalThis, 'fetch').mockImplementation(() => new Promise((done) => { resolve = done; }));
    renderApp();
    expect(screen.getByText('Ładowanie instrumentów…')).toBeTruthy();
    resolve(json([]));
    expect(await screen.findByText('Dodaj swój pierwszy instrument')).toBeTruthy();
  });

  it('can recover from a non-JSON server error by retrying the list', async () => {
    vi.spyOn(globalThis, 'fetch')
      .mockResolvedValueOnce(new Response('<html>Proxy error</html>', { status: 502 }))
      .mockResolvedValueOnce(json([existing]));
    renderApp();
    expect(await screen.findByText(/HTTP 502/)).toBeTruthy();
    await userEvent.click(screen.getByRole('button', { name: 'Spróbuj ponownie' }));
    expect(await screen.findByRole('row', { name: /Verizon Communications/ })).toBeTruthy();
    expect(screen.queryByRole('alert')).toBeNull();
  });

  it('creates an instrument, trims fields, normalizes codes and refreshes the list', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch')
      .mockResolvedValueOnce(json([existing]))
      .mockResolvedValueOnce(json(created))
      .mockResolvedValueOnce(json([existing, created]));
    renderApp();
    await screen.findByText(existing.name!);
    const user = await openAndFillForm();
    await user.click(screen.getByRole('button', { name: 'Zapisz instrument' }));
    expect(await screen.findByRole('row', { name: /Vanguard S&P 500 ETF/ })).toBeTruthy();
    expect(screen.getByRole('status').textContent).toContain('Dodano instrument');
    expect(screen.queryByRole('dialog')).toBeNull();
    const postCall = fetchMock.mock.calls.find(([, options]) => options?.method === 'POST');
    expect(postCall?.[0]).toBe('/v1/security');
    expect(JSON.parse(postCall![1]!.body as string)).toEqual({
      name: created.name, type: 'ETF',
      googleTicker: { code: 'VOO', exchangeCode: null, currencyCode: 'USD' },
    });
    await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(3));
  });

  it('validates whitespace-only fields before sending a POST', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(json([]));
    renderApp();
    await screen.findByText('Dodaj swój pierwszy instrument');
    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: 'Dodaj instrument' }));
    await user.type(screen.getByRole('textbox', { name: /Nazwa instrumentu/ }), '   ');
    await user.click(screen.getByRole('button', { name: 'Zapisz instrument' }));
    expect(screen.getByText('Podaj nazwę instrumentu.')).toBeTruthy();
    expect(screen.getByText('Podaj symbol instrumentu.')).toBeTruthy();
    expect(screen.getByText(/Podaj trzyliterowy kod waluty/)).toBeTruthy();
    expect(fetchMock).toHaveBeenCalledTimes(1);
  });

  it('keeps entered values when saving fails and allows a corrected retry', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch')
      .mockResolvedValueOnce(json([]))
      .mockResolvedValueOnce(json({ message: 'Nie można zapisać instrumentu.' }, 500))
      .mockResolvedValueOnce(json(created))
      .mockResolvedValueOnce(json([created]));
    renderApp();
    await screen.findByText('Dodaj swój pierwszy instrument');
    const user = await openAndFillForm();
    await user.click(screen.getByRole('button', { name: 'Zapisz instrument' }));
    expect(await screen.findByText('Nie można zapisać instrumentu.')).toBeTruthy();
    expect((screen.getByRole('textbox', { name: /Symbol instrumentu/ }) as HTMLInputElement).value).toBe(' voo ');
    await user.click(screen.getByRole('button', { name: 'Zapisz instrument' }));
    expect(await screen.findByRole('row', { name: /Vanguard S&P 500 ETF/ })).toBeTruthy();
    expect(fetchMock.mock.calls.filter(([, options]) => options?.method === 'POST')).toHaveLength(2);
  });

  it('does not duplicate a pending save or dismiss its form', async () => {
    let resolve!: (response: Response) => void;
    const fetchMock = vi.spyOn(globalThis, 'fetch')
      .mockResolvedValueOnce(json([]))
      .mockImplementationOnce(() => new Promise((done) => { resolve = done; }))
      .mockResolvedValueOnce(json([created]));
    renderApp();
    await screen.findByText('Dodaj swój pierwszy instrument');
    const user = await openAndFillForm();
    const save = screen.getByRole('button', { name: 'Zapisz instrument' });
    await user.dblClick(save);
    expect((save as HTMLButtonElement).disabled).toBe(true);
    expect((screen.getByRole('button', { name: 'Anuluj' }) as HTMLButtonElement).disabled).toBe(true);
    await user.keyboard('{Escape}');
    expect(screen.getByRole('dialog')).toBeTruthy();
    expect(fetchMock.mock.calls.filter(([, options]) => options?.method === 'POST')).toHaveLength(1);
    resolve(json(created));
    expect(await screen.findByRole('row', { name: /Vanguard S&P 500 ETF/ })).toBeTruthy();
  });

  it('reports an unreachable backend and can cancel a form without saving', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockRejectedValue(new TypeError('Failed to fetch'));
    renderApp();
    expect(await screen.findByText(/Sprawdź, czy backend jest uruchomiony/)).toBeTruthy();
    const user = await openAndFillForm();
    await user.click(screen.getByRole('button', { name: 'Anuluj' }));
    expect(screen.queryByRole('dialog')).toBeNull();
    expect(fetchMock.mock.calls.filter(([, options]) => options?.method === 'POST')).toHaveLength(0);
  });
});
