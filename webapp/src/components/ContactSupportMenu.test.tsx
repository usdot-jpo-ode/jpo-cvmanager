import { render } from '@testing-library/react'
import ContactSupportMenu from './ContactSupportMenu'
import { replaceChaoticIds } from '../utils/test-utils'
import { setupStore } from '../store'
import { Provider } from 'react-redux'
import { ThemeProvider } from '@mui/material'
import { testTheme } from '../styles'
import { BrowserRouter } from 'react-router-dom'

it('should take a snapshot', () => {
  const { container } = render(
    <ThemeProvider theme={testTheme}>
      <Provider store={setupStore({})}>
        <BrowserRouter>
          <ContactSupportMenu />
        </BrowserRouter>
      </Provider>
    </ThemeProvider>
  )

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
