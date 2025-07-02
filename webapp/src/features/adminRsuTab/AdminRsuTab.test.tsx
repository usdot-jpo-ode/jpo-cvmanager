import React from 'react'
import { render, fireEvent, screen } from '@testing-library/react'
import AdminRsuTab from './AdminRsuTab'
import { Provider } from 'react-redux'
import { ThemeProvider } from '@mui/material'
import { testTheme } from '../../styles'
import { setupStore } from '../../store'
import { replaceChaoticIds } from '../../utils/test-utils'
import { BrowserRouter } from 'react-router-dom'

it('should take a snapshot', () => {
  const { container } = render(
    <ThemeProvider theme={testTheme}>
      <Provider store={setupStore({ adminRsuTab: { loading: false, value: { activeDiv: 'rsu_table' } } })}>
        <BrowserRouter>
          <AdminRsuTab />
        </BrowserRouter>
      </Provider>
    </ThemeProvider>
  )

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
