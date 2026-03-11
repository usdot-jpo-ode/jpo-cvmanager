import { resolve } from 'node:path'
import { readFileSync, existsSync } from 'node:fs'
import { defineConfig, loadEnv, Plugin } from 'vite'
import { defineConfig as defineTestConfig, mergeConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'
import tsconfigPaths from 'vite-tsconfig-paths'

export default defineConfig(({ mode }) => {
  setEnv(mode)
  const config = {
    plugins: [
      react(),
      tsconfigPaths(),
      envPlugin(),
      devServerPlugin(),
      sourcemapPlugin(),
      buildPathPlugin(),
      importPrefixPlugin(),
      htmlPlugin(mode),
    ],
    base: '/',
  }

  return mergeConfig(
    config,
    defineTestConfig({
      test: {
        globals: true,
        environment: 'jsdom',
        setupFiles: './src/setupTests.ts',
        deps: {
          inline: ['keycloak-js'],
        },
        resolveSnapshotPath: (testPath, snapExtension) => {
          const path = testPath.split('/')
          const filename = path.pop()
          return [...path, '__snapshots__', filename].join('/') + snapExtension
        },
        alias: {
          '\\.(css|less|scss|sass)$': 'identity-obj-proxy',
          'worker-loader!mapbox-gl/dist/mapbox-gl-csp-worker': resolve(__dirname, '__mocks__/worker-loader.js'),
        },
        coverage: {
          reporter: ['text', 'json', 'html'],
        },
      },
    })
  )
})

function setEnv(mode: string) {
  Object.assign(
    process.env,
    loadEnv(mode, resolve(__dirname, '..'), ['VITE_', 'MAPBOX_TOKEN']),
    loadEnv(mode, '.', ['VITE_', 'NODE_ENV', 'PUBLIC_URL'])
  )
  process.env.NODE_ENV ||= mode
  const { homepage } = JSON.parse(readFileSync('package.json', 'utf-8'))
  process.env.PUBLIC_URL ||= homepage
    ? `${homepage.startsWith('http') || homepage.startsWith('/') ? homepage : `/${homepage}`}`.replace(/\/$/, '')
    : ''
}

// Expose `process.env` environment variables to your client code
// Migration guide: Follow the guide below to replace process.env with import.meta.env in your app
// https://vitejs.dev/guide/env-and-mode.html#env-variables
function envPlugin(): Plugin {
  return {
    name: 'env-plugin',
    config(_, { mode }) {
      const rootEnv = loadEnv(mode, resolve(__dirname, '..'), ['VITE_', 'MAPBOX_TOKEN'])
      const env = loadEnv(mode, '.', ['VITE_', 'NODE_ENV', 'PUBLIC_URL'])
      const combinedEnv = { ...rootEnv, ...env }
      if (rootEnv.MAPBOX_TOKEN && !combinedEnv.VITE_MAPBOX_TOKEN) {
        combinedEnv.VITE_MAPBOX_TOKEN = rootEnv.MAPBOX_TOKEN
      }
      return {
        define: Object.fromEntries(
          Object.entries(combinedEnv).map(([key, value]) => [`process.env.${key}`, JSON.stringify(value)])
        ),
      }
    },
  }
}

// Setup HOST, SSL, PORT
// Migration guide: Follow the guides below
// https://vitejs.dev/config/server-options.html#server-host
// https://vitejs.dev/config/server-options.html#server-https
// https://vitejs.dev/config/server-options.html#server-port
function devServerPlugin(): Plugin {
  return {
    name: 'dev-server-plugin',
    config(_, { mode }) {
      const { HOST, PORT, HTTPS, SSL_CRT_FILE, SSL_KEY_FILE } = loadEnv(mode, '.', [
        'HOST',
        'PORT',
        'HTTPS',
        'SSL_CRT_FILE',
        'SSL_KEY_FILE',
      ])
      const https = HTTPS === 'true'
      return {
        server: {
          host: HOST || '0.0.0.0',
          port: parseInt(PORT || '3000', 10),
          open: true,
          ...(https &&
            SSL_CRT_FILE &&
            SSL_KEY_FILE && {
              https: {
                cert: readFileSync(resolve(SSL_CRT_FILE)),
                key: readFileSync(resolve(SSL_KEY_FILE)),
              },
            }),
        },
      }
    },
  }
}

// Migration guide: Follow the guide below
// https://vitejs.dev/config/build-options.html#build-sourcemap
function sourcemapPlugin(): Plugin {
  return {
    name: 'sourcemap-plugin',
    config(_, { mode }) {
      const { GENERATE_SOURCEMAP } = loadEnv(mode, '.', ['GENERATE_SOURCEMAP'])
      return {
        build: {
          sourcemap: GENERATE_SOURCEMAP === 'true',
        },
      }
    },
  }
}

// Migration guide: Follow the guide below
// https://vitejs.dev/config/build-options.html#build-outdir
function buildPathPlugin(): Plugin {
  return {
    name: 'build-path-plugin',
    config(_, { mode }) {
      const { BUILD_PATH } = loadEnv(mode, '.', ['BUILD_PATH'])
      return {
        build: {
          outDir: BUILD_PATH || 'build',
        },
      }
    },
  }
}

// To resolve modules from node_modules, you can prefix paths with ~
// https://create-react-app.dev/docs/adding-a-sass-stylesheet
// Migration guide: Follow the guide below
// https://vitejs.dev/config/shared-options.html#resolve-alias
function importPrefixPlugin(): Plugin {
  return {
    name: 'import-prefix-plugin',
    config() {
      return {
        resolve: {
          alias: [{ find: /^~([^/])/, replacement: '$1' }],
        },
      }
    },
  }
}

// Replace %ENV_VARIABLES% in index.html
// https://vitejs.dev/guide/api-plugin.html#transformindexhtml
// Migration guide: Follow the guide below
// https://vitejs.dev/guide/env-and-mode.html#html-env-replacement
function htmlPlugin(mode: string): Plugin {
  const rootEnv = loadEnv(mode, resolve(__dirname, '..'), ['VITE_', 'MAPBOX_TOKEN'])
  const env = loadEnv(mode, '.', ['VITE_', 'NODE_ENV', 'PUBLIC_URL'])
  const combinedEnv = { ...rootEnv, ...env }
  if (rootEnv.MAPBOX_TOKEN && !combinedEnv.VITE_MAPBOX_TOKEN) {
    combinedEnv.VITE_MAPBOX_TOKEN = rootEnv.MAPBOX_TOKEN
  }
  return {
    name: 'html-plugin',
    transformIndexHtml: {
      order: 'pre',
      handler(html) {
        return html.replace(/%(.*?)%/g, (match, p1) => combinedEnv[p1] ?? match)
      },
    },
  }
}
