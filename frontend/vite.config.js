import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      //  API_TARGET 으로 다른 포트의 백엔드에 붙일 수 있다 (기본 8080). 로컬에서 서버 두 벌을 띄울 때 쓴다
      '/api': { target: process.env.API_TARGET || 'http://localhost:8080', changeOrigin: false },
    },
  },
})
