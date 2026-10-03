import React from 'react';
import ReactDOM from 'react-dom/client';
import { MantineProvider, createTheme } from '@mantine/core';
import { BrowserRouter } from 'react-router-dom';
import '@mantine/core/styles.css';
import './styles.css';
import { App } from './App';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

const queryClient = new QueryClient();

const theme = createTheme({
  primaryColor: 'teal',
  defaultRadius: 'md',
  fontFamily: 'Inter, "Segoe UI", sans-serif',
  headings: { fontFamily: 'Inter, "Segoe UI", sans-serif' },
});

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <MantineProvider theme={theme} forceColorScheme="light">
      <QueryClientProvider client={queryClient}>
        <BrowserRouter><App /></BrowserRouter>
      </QueryClientProvider>
    </MantineProvider>
  </React.StrictMode>,
);
