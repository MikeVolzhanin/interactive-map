import { defineConfig } from 'vite'
import path from 'path'
import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'


function figmaAssetResolver() {
  return {
    name: 'figma-asset-resolver',
    resolveId(id) {
      if (id.startsWith('figma:asset/')) {
        const filename = id.replace('figma:asset/', '')
        return path.resolve(__dirname, 'src/assets', filename)
      }
    },
  }
}

export default defineConfig({
  plugins: [
    figmaAssetResolver(),
    // The React and Tailwind plugins are both required for Make, even if
    // Tailwind is not being actively used – do not remove them
    react(),
    tailwindcss(),
  ],
  resolve: {
    alias: {
      // Alias @ to the src directory
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 3000,
    proxy: {
      // Без Java-бэкенда: тот же путь, что и у HseNnovHtmlProxyController — прямо на nnov.hse.ru
      '/api/public/hse-nnov-html': {
        target: 'https://nnov.hse.ru',
        changeOrigin: true,
        secure: true,
        rewrite: (reqPath) => {
          const prefix = '/api/public/hse-nnov-html/';
          if (!reqPath.startsWith(prefix)) {
            return reqPath;
          }
          const rest = reqPath.slice(prefix.length);
          if (rest === 'news' || rest === '') {
            return '/news/';
          }
          return `/${rest}`;
        },
      },
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },

  // File types to support raw imports. Never add .css, .tsx, or .ts files to this.
  assetsInclude: ['**/*.svg', '**/*.csv'],
})
