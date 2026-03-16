import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    dedupe: ['react', 'react-dom', 'react-redux'],
  },
  server: {
    port: 3000,
    strictPort: true,
    proxy: {
      "/api": {
        target: "http://localhost:9191",
        changeOrigin: true,
      },
      "/open-api": {
        target: "http://localhost:9191",
        changeOrigin: true,
      },
    },
  },
})
