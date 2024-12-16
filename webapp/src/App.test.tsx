import React from 'react'
import { render } from '@testing-library/react'
import App from './App'
import { Provider } from 'react-redux'
import { ThemeProvider } from '@mui/material'
import { testTheme } from './styles'
import { setupStore } from './store'
import { replaceChaoticIds } from './utils/test-utils'

Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: jest.fn().mockImplementation((query) => ({
    matches: query === '(prefers-color-scheme: dark)',
    media: query,
    onchange: null,
    addEventListener: jest.fn(),
    removeEventListener: jest.fn(),
    dispatchEvent: jest.fn(),
  })),
})

jest.mock('./EnvironmentVars', () => ({
  WEBAPP_THEME_LIGHT: 'light',
  WEBAPP_THEME_DARK: 'dark',
  getMessageTypes: jest.fn(() => ['BSM']),
  getMapboxInitViewState: jest.fn(() => ({
    latitude: 39.7392,
    longitude: -104.9903,
    zoom: 10,
  })),
}))

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
