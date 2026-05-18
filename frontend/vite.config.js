import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      dts: false,
      resolvers: [
        ElementPlusResolver({
          importStyle: 'css'
        })
      ]
    }),
    Components({
      dts: false,
      resolvers: [
        ElementPlusResolver({
          importStyle: 'css',
          directives: true
        })
      ]
    })
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          'vue-vendor': ['vue', 'vue-router', 'pinia', 'axios']
        }
      }
    }
  },
  server: {
    port: 5173
  }
})
