import { useState } from 'react';
import { Alert, Badge, Button, Group, Loader, Modal, Paper, Table, Text } from '@mantine/core';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { AlertCircle, Check, Layers3, Plus, RefreshCw } from 'lucide-react';
import { getSecurities, securitiesKey, securityTypeLabels } from './api';
import type { Security } from './api';
import { InstrumentForm } from './InstrumentForm';

export function InstrumentsPage() {
  const queryClient = useQueryClient();
  const [formOpened, setFormOpened] = useState(false);
  const [saving, setSaving] = useState(false);
  const [success, setSuccess] = useState<string | null>(null);
  const query = useQuery({
    queryKey: securitiesKey,
    queryFn: ({ signal }) => getSecurities(signal),
    retry: false,
  });
  const instruments = query.data;

  function created(security: Security) {
    // Cancel any older list response before inserting the successfully saved instrument.
    void queryClient.cancelQueries({ queryKey: securitiesKey });
    queryClient.setQueryData<Security[]>(securitiesKey, (current) => [
      ...(current ?? []).filter((item) => item.securityId !== security.securityId),
      security,
    ]);
    void queryClient.invalidateQueries({ queryKey: securitiesKey });
    setFormOpened(false);
    setSuccess(`Dodano instrument „${security.name ?? 'Bez nazwy'}”.`);
  }

  return (
    <>
      <div className="instruments-heading">
        <div className="page-heading">
          <span className="eyebrow">ANALIZA I DANE</span>
          <h1>Instrumenty</h1>
          <p>Instrumenty, giełdy i waluty wykorzystywane w Twoich analizach.</p>
        </div>
        <Button leftSection={<Plus size={18} />} onClick={() => { setSuccess(null); setFormOpened(true); }}>Dodaj instrument</Button>
      </div>

      {success && <Alert color="teal" icon={<Check size={18} />} role="status" mb="md" withCloseButton onClose={() => setSuccess(null)} closeButtonLabel="Zamknij komunikat">{success}</Alert>}
      {query.isError && (
        <Alert color="red" title="Nie udało się pobrać listy instrumentów" icon={<AlertCircle size={18} />} role="alert" mb="md">
          <Text size="sm">{query.error.message}</Text>
          <Button mt="sm" size="xs" variant="light" color="red" onClick={() => void query.refetch()} loading={query.isFetching}>Spróbuj ponownie</Button>
        </Alert>
      )}

      <Paper withBorder className="instruments-panel">
        <Group justify="space-between" p="lg" className="instruments-toolbar">
          <Group gap="sm"><h2>Lista instrumentów</h2>{instruments && <Badge color="teal" variant="light">{instruments.length}</Badge>}</Group>
          <Button size="xs" variant="subtle" color="gray" leftSection={<RefreshCw size={15} />} loading={query.isFetching} onClick={() => void query.refetch()}>Odśwież</Button>
        </Group>

        {query.isPending && <div className="instruments-state" role="status"><Loader size="sm" /><Text size="sm" c="dimmed">Ładowanie instrumentów…</Text></div>}
        {instruments?.length === 0 && <div className="instruments-state"><Layers3 size={32} strokeWidth={1.5} /><h3>Dodaj swój pierwszy instrument</h3><p>Lista jest pusta. Dodaj akcję, ETF lub walutę, aby rozpocząć pracę.</p></div>}
        {!!instruments?.length && (
          <Table.ScrollContainer minWidth={650}>
            <Table verticalSpacing="md" horizontalSpacing="lg" highlightOnHover>
              <Table.Thead><Table.Tr><Table.Th>Nazwa</Table.Th><Table.Th>Typ</Table.Th><Table.Th>Symbol</Table.Th><Table.Th>Giełda</Table.Th><Table.Th>Waluta</Table.Th></Table.Tr></Table.Thead>
              <Table.Tbody>
                {instruments.map((instrument) => (
                  <Table.Tr key={instrument.securityId}>
                    <Table.Td className="instrument-name">{instrument.name || '—'}</Table.Td>
                    <Table.Td><Badge variant="light" color="gray">{instrument.type ? securityTypeLabels[instrument.type] ?? instrument.type : '—'}</Badge></Table.Td>
                    <Table.Td>{instrument.googleTicker?.code || '—'}</Table.Td>
                    <Table.Td>{instrument.googleTicker?.exchangeCode || '—'}</Table.Td>
                    <Table.Td>{instrument.googleTicker?.currencyCode || '—'}</Table.Td>
                  </Table.Tr>
                ))}
              </Table.Tbody>
            </Table>
          </Table.ScrollContainer>
        )}
      </Paper>

      <Modal opened={formOpened} onClose={() => { if (!saving) setFormOpened(false); }} title="Dodaj instrument" centered size="lg" closeOnEscape={!saving} closeOnClickOutside={!saving} withCloseButton={!saving}>
        {formOpened && <InstrumentForm onCreated={created} onCancel={() => setFormOpened(false)} onSavingChange={setSaving} />}
      </Modal>
    </>
  );
}
