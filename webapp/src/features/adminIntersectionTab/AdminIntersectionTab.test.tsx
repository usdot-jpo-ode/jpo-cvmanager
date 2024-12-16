import React from 'react'
import { render, fireEvent, screen } from '@testing-library/react'
import AdminIntersectionTab from './AdminIntersectionTab'
import { Provider } from 'react-redux'
import { ThemeProvider } from '@mui/material'
import { testTheme } from '../../styles'
import { setupStore } from '../../store'
import { replaceChaoticIds } from '../../utils/test-utils'
import { BrowserRouter } from 'react-router-dom'

it('snapshot add', () => {
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

  fireEvent.click(screen.queryByTitle('Add Intersection'))

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})

it('snapshot refresh', () => {
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

  fireEvent.click(screen.queryByTitle('Refresh Intersections'))

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
