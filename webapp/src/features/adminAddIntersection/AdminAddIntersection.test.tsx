import React from 'react'
import { render } from '@testing-library/react'
import AdminAddIntersection from './AdminAddIntersection'
import { Provider } from 'react-redux'
import { ThemeProvider } from '@mui/material'
import { testTheme } from '../../styles'
import { setupStore } from '../../store'
import { replaceChaoticIds } from '../../utils/test-utils'
import { BrowserRouter, Routes, Route } from 'react-router-dom'

it('should take a snapshot', () => {
  const { container } = render(
    <ThemeProvider theme={testTheme}>
      <Provider store={setupStore({})}>
        <BrowserRouter>
          <Routes>
            <Route path="*" element={<AdminAddIntersection />} />
          </Routes>
        </BrowserRouter>
      </Provider>
    </ThemeProvider>
  )

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
