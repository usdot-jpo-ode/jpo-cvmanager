import React from 'react'
import { render } from '@testing-library/react'
import AdminIntersectionTab from './AdminIntersectionTab'
import { Provider } from 'react-redux'
import { ThemeProvider } from '@mui/material'
import { testTheme } from '../../styles'
import { setupStore } from '../../store'
import { replaceChaoticIds } from '../../utils/test-utils'
import { BrowserRouter } from 'react-router-dom'

test('should take a snapshot', () => {
  const { container } = render(
    <ThemeProvider theme={testTheme}>
      <Provider
        store={setupStore({ adminIntersectionTab: { loading: false, value: { activeDiv: 'intersection_table' } } })}
      >
        <BrowserRouter>
          <AdminIntersectionTab />
        </BrowserRouter>
      </Provider>
    </ThemeProvider>
  )
  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
