import React from 'react'
import { render } from '@testing-library/react'
import Admin from './Admin'
import { Provider } from 'react-redux'
import { ThemeProvider } from '@mui/material'
import { testTheme } from '../styles'
import { setupStore } from '../store'
import { replaceChaoticIds } from '../utils/test-utils'
import { BrowserRouter } from 'react-router-dom'

it('should take a snapshot', () => {
  const { container } = render(
    <ThemeProvider theme={testTheme}>
      <ThemeProvider theme={testTheme}>
        <Provider store={setupStore({})}>
          <BrowserRouter>
            <Admin />
          </BrowserRouter>
        </Provider>
      </ThemeProvider>
    </ThemeProvider>
  )
  replaceChaoticIds(container)

  expect(container).toMatchSnapshot()
})
