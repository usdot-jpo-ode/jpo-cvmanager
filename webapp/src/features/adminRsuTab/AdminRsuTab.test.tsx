import React from 'react'
import { render, fireEvent, screen } from '@testing-library/react'
import AdminRsuTab from './AdminRsuTab'
import { Provider } from 'react-redux'
import { ThemeProvider } from '@mui/material'
import { testTheme } from '../../styles'
import { setupStore } from '../../store'
import { replaceChaoticIds } from '../../utils/test-utils'
import { BrowserRouter } from 'react-router-dom'

it('snapshot add', () => {
  const { container } = render(
    <ThemeProvider theme={testTheme}>
      <Provider store={setupStore({ adminRsuTab: { loading: false, value: { activeDiv: 'rsu_table' } } })}>
        <BrowserRouter>
          <AdminRsuTab />
        </BrowserRouter>
      </Provider>
    </ThemeProvider>
  )

  fireEvent.click(screen.queryByTitle('Add RSU'))

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})

it('snapshot refresh', () => {
  const { container } = render(
    <ThemeProvider theme={testTheme}>
      <Provider store={setupStore({ adminRsuTab: { loading: false, value: { activeDiv: 'rsu_table' } } })}>
        <BrowserRouter>
          <AdminRsuTab />
        </BrowserRouter>
      </Provider>
    </ThemeProvider>
  )

  fireEvent.click(screen.queryByTitle('Refresh RSUs'))

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
