import { render } from '@testing-library/react'
import Help from './Help'
import { replaceChaoticIds } from '../utils/test-utils'
import { MemoryRouter } from 'react-router-dom'
import { Provider } from 'react-redux'
import { ThemeProvider } from '@mui/material'
import { testTheme } from '../styles'
import { setupStore } from '../store'

it('should take a snapshot', () => {
  jest.mock('../EnvironmentVars', () => ({
    EnvironmentVars: {
      ENABLE_RSU_FEATURES: true,
      ENABLE_WZDX_FEATURES: true,
      ENABLE_HAAS_FEATURES: true,
    },
  }))

  const { container } = render(
    <ThemeProvider theme={testTheme}>
      <Provider
        store={setupStore({ adminIntersectionTab: { loading: false, value: { activeDiv: 'intersection_table' } } })}
      >
        <MemoryRouter>
          <Help />
        </MemoryRouter>
      </Provider>
    </ThemeProvider>
  )

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
