import React from 'react'
import { render } from '@testing-library/react'
import App from './App'
import { Provider } from 'react-redux'
import { ThemeProvider } from '@mui/material'
import { testTheme } from './styles'
import { setupStore } from './store'
import { replaceChaoticIds } from './utils/test-utils'

import { vi } from 'vitest'

vi.mock('./EnvironmentVars', () => ({
  default: {
    WEBAPP_THEME_LIGHT: 'light',
    WEBAPP_THEME_DARK: 'dark',
    getMapboxInitViewState: vi.fn(() => ({
      latitude: 39.7392,
      longitude: -104.9903,
      zoom: 10,
    })),
    KEYCLOAK_HOST_URL: 'https://keycloak.example.com',
    KEYCLOAK_CLIENT_ID: 'keycloak-client-id',
    KEYCLOAK_REALM: 'keycloak-realm',
  },
}))

vi.mock('@react-keycloak/web', () => ({
  useKeycloak: () => ({
    keycloak: {
      authenticated: false,
      login: vi.fn(),
      logout: vi.fn(),
      register: vi.fn(),
      accountManagement: vi.fn(),
      loadUserProfile: vi.fn(),
    },
    initialized: true,
  }),
  ReactKeycloakProvider: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}))

beforeAll(() => {
  Object.defineProperty(window, 'matchMedia', {
    writable: true,
    value: vi.fn().mockImplementation((query) => ({
      matches: query === '(prefers-color-scheme: dark)',
      media: query,
      onchange: null,
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
      dispatchEvent: vi.fn(),
    })),
  })
})

it('should take a snapshot', () => {
  const { container } = render(
    <ThemeProvider theme={testTheme}>
      <Provider
        store={setupStore({
          user: {
            value: {
              authLoginData: { data: 'data' },
            },
          },
        })}
      >
        <App />
      </Provider>
    </ThemeProvider>
  )

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
