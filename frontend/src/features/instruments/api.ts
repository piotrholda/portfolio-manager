import { requestJson } from '../../api/http';

export type SecurityType = 'CURRENCY' | 'SHARE' | 'ETF';

export interface Ticker {
  code: string | null;
  exchangeCode: string | null;
  currencyCode: string | null;
}

export interface Security {
  securityId: string;
  name: string | null;
  type: SecurityType | null;
  googleTicker: Ticker | null;
}

export interface CreateSecurityRequest {
  name: string;
  type: SecurityType;
  googleTicker: Ticker;
}

export const securitiesKey = ['securities'] as const;

export const securityTypeLabels: Record<SecurityType, string> = {
  CURRENCY: 'Waluta',
  SHARE: 'Akcja',
  ETF: 'ETF',
};

export function getSecurities(signal?: AbortSignal) {
  return requestJson<Security[]>('/v1/security', { signal });
}

export function createSecurity(request: CreateSecurityRequest) {
  return requestJson<Security>('/v1/security', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  });
}
