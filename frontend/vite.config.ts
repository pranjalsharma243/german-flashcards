import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { VitePWA } from 'vite-plugin-pwa';

export default defineConfig({
  plugins: [
    react(),
    VitePWA({
      registerType: 'autoUpdate',
      includeAssets: ['favicon.svg'],
      manifest: {
        name: 'Deutsch Meister',
        short_name: 'DeutschMeister',
        description: 'German B1 vocabulary flashcards with spaced repetition',
        theme_color: '#4f46e5',
        background_color: '#f5f5ff',
        display: 'standalone',
        orientation: 'portrait',
        start_url: '/',
        icons: [
          { src: 'pwa-192.png', sizes: '192x192', type: 'image/png' },
          { src: 'pwa-512.png', sizes: '512x512', type: 'image/png' },
          { src: 'pwa-512.png', sizes: '512x512', type: 'image/png', purpose: 'maskable' },
        ],
        categories: ['education'],
        lang: 'en',
        shortcuts: [
          { name: 'FSRS Review', short_name: 'Review', url: '/?mode=fsrs', description: 'Start your daily review session' },
          { name: 'AI Chat Tutor', short_name: 'Chat', url: '/?mode=chat', description: 'Chat with the AI tutor' },
        ],
      },
      workbox: {
        // Cache API responses for chapters and cards (offline reading)
        runtimeCaching: [
          {
            urlPattern: /^\/api\/chapters/,
            handler: 'StaleWhileRevalidate',
            options: {
              cacheName: 'api-chapters',
              expiration: { maxEntries: 20, maxAgeSeconds: 86400 },
            },
          },
          {
            urlPattern: /^\/api\/tts\//,
            handler: 'CacheFirst',
            options: {
              cacheName: 'tts-audio',
              expiration: { maxEntries: 500, maxAgeSeconds: 2592000 }, // 30 days
            },
          },
        ],
        globPatterns: ['**/*.{js,css,html,woff2}'],
        cleanupOutdatedCaches: true,
      },
      devOptions: {
        enabled: false, // Don't run SW in dev mode
      },
    }),
  ],
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080',
      '/oauth2': { target: 'http://localhost:8080', changeOrigin: true },
      '/login/oauth2': { target: 'http://localhost:8080', changeOrigin: true },
    },
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['react', 'react-dom'],
          charts: ['recharts'],
          icons: ['lucide-react'],
        },
      },
    },
  },
});
