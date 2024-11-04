import React from 'react'
import { render } from '@testing-library/react'
import Menu from './Menu'
import { Provider } from 'react-redux'
import { ThemeProvider } from '@mui/material'
import { testTheme } from '../../styles'
import { setupStore } from '../../store'
import { replaceChaoticIds } from '../../utils/test-utils'
import ReactDOM from 'react-dom'

it('should take a snapshot', () => {
  const { container } = render(
    <ThemeProvider theme={testTheme}>
      <Provider store={setupStore({})}>
        <Menu />
      </Provider>
    </ThemeProvider>
  )

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
