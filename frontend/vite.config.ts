import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, '.', '');
  const target = env.API_PROXY_TARGET || 'http://localhost:8080';
  return {
    plugins: [react()],
    server: {
      host: '127.0.0.1',
      port: 5173,
      strictPort: true,
      proxy: Object.fromEntries(
        ['/v1', '/v3/api-docs', '/swagger-ui', '/swagger-ui.html'].map((path) => [path, { target }]),
      ),
    },
    preview: { host: '127.0.0.1', port: 4173, strictPort: true },
  };
});
