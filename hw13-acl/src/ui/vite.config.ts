import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
    plugins: [react()],
    server: {
        port: 5173,
        proxy: {
            '/api': 'http://localhost:8080',
            '/login': {
                target: 'http://localhost:8080',
                bypass(req) {
                    if (req.method === 'GET') return req.url;
                    return null;
                },
            },
            '/logout': 'http://localhost:8080',
        },
    },
    build: {
        outDir: '../../src/main/resources/static',
        emptyOutDir: true,
    },
});
