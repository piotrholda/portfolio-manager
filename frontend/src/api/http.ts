export async function requestJson<T>(url: string, options: RequestInit = {}): Promise<T> {
  let response: Response;
  try {
    response = await fetch(url, {
      ...options,
      headers: { Accept: 'application/json', ...options.headers },
    });
  } catch (error) {
    if (error instanceof Error && error.name === 'AbortError') throw error;
    throw new Error('Nie udało się połączyć z serwerem. Sprawdź, czy backend jest uruchomiony.');
  }

  if (!response.ok) {
    let message = `Serwer zwrócił błąd HTTP ${response.status}. Spróbuj ponownie.`;
    try {
      const body = await response.json();
      if (typeof body.message === 'string' && body.message.trim()) message = body.message;
    } catch {
      // Proxy and server errors may return HTML or an empty body.
    }
    throw new Error(message);
  }

  try {
    return await response.json() as T;
  } catch {
    throw new Error('Serwer zwrócił nieprawidłową odpowiedź. Odśwież listę instrumentów.');
  }
}
