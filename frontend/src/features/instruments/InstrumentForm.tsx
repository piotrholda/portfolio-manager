import { useState } from 'react';
import type { FormEvent } from 'react';
import { Alert, Button, Group, NativeSelect, Stack, TextInput } from '@mantine/core';
import { AlertCircle } from 'lucide-react';
import { createSecurity, securityTypeLabels } from './api';
import type { Security, SecurityType } from './api';

interface Props {
  onCreated: (security: Security) => void;
  onCancel: () => void;
  onSavingChange: (saving: boolean) => void;
}

export function InstrumentForm({ onCreated, onCancel, onSavingChange }: Props) {
  const [name, setName] = useState('');
  const [type, setType] = useState<SecurityType>('ETF');
  const [code, setCode] = useState('');
  const [exchangeCode, setExchangeCode] = useState('');
  const [currencyCode, setCurrencyCode] = useState('');
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (saving) return;
    const validation: Record<string, string> = {};
    if (!name.trim()) validation.name = 'Podaj nazwę instrumentu.';
    if (!code.trim()) validation.code = 'Podaj symbol instrumentu.';
    if (!/^[a-zA-Z]{3}$/.test(currencyCode.trim())) validation.currencyCode = 'Podaj trzyliterowy kod waluty, np. USD lub PLN.';
    setErrors(validation);
    if (Object.keys(validation).length) return;

    setError(null);
    setSaving(true);
    onSavingChange(true);
    try {
      const security = await createSecurity({
        name: name.trim(),
        type,
        googleTicker: {
          code: code.trim().toUpperCase(),
          exchangeCode: exchangeCode.trim().toUpperCase() || null,
          currencyCode: currencyCode.trim().toUpperCase(),
        },
      });
      onCreated(security);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Nie udało się dodać instrumentu.');
    } finally {
      setSaving(false);
      onSavingChange(false);
    }
  }

  return (
    <form onSubmit={submit} noValidate>
      <Stack gap="md">
        {error && <Alert color="red" title="Nie udało się zapisać instrumentu" icon={<AlertCircle size={18} />} role="alert">{error}</Alert>}
        <TextInput label="Nazwa instrumentu" placeholder="np. Vanguard S&P 500 ETF" required maxLength={255} value={name} onChange={(event) => setName(event.currentTarget.value)} error={errors.name} disabled={saving} data-autofocus />
        <NativeSelect label="Typ instrumentu" required data={Object.entries(securityTypeLabels).map(([value, label]) => ({ value, label }))} value={type} onChange={(event) => setType(event.currentTarget.value as SecurityType)} disabled={saving} />
        <TextInput label="Symbol instrumentu" placeholder="np. VOO" required maxLength={255} value={code} onChange={(event) => setCode(event.currentTarget.value)} error={errors.code} disabled={saving} />
        <div className="instrument-form-grid">
          <TextInput label="Giełda" placeholder="np. NYSEARCA" description="Opcjonalnie, np. dla walut można pominąć." maxLength={255} value={exchangeCode} onChange={(event) => setExchangeCode(event.currentTarget.value)} disabled={saving} />
          <TextInput label="Waluta" placeholder="np. USD" required maxLength={3} value={currencyCode} onChange={(event) => setCurrencyCode(event.currentTarget.value)} error={errors.currencyCode} disabled={saving} />
        </div>
        <Group justify="flex-end" mt="sm">
          <Button variant="default" type="button" onClick={onCancel} disabled={saving}>Anuluj</Button>
          <Button type="submit" loading={saving}>Zapisz instrument</Button>
        </Group>
      </Stack>
    </form>
  );
}
