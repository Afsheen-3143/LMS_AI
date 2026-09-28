import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Dev server for the React showcase frontend. Runs on 5174 so it doesn't
// collide with the plain-JS test console in tools/ai-assistant-ui (5173).
// The backend (SecurityConfig CORS) already allows any origin, so no proxy
// is required - the API base URL is configured in-app instead.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5174,
  },
});
