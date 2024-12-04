import React from 'react'
import { render } from '@testing-library/react'
import DisplayCounts from './DisplayCounts'
import { Provider } from 'react-redux'
import { ThemeProvider } from '@mui/material'
import { testTheme } from '../../styles'
import { setupStore } from '../../store'
import { MockLocalizationProvider, replaceChaoticIds } from '../../utils/test-utils'

// // Mock the @mui/x-date-pickers module
jest.mock('@mui/x-date-pickers', () => {
  const actual = jest.requireActual('@mui/x-date-pickers')
  return {
    ...actual,
    LocalizationProvider: MockLocalizationProvider,
  }
})

// Mock the dayjs library
jest.mock('dayjs', () => {
  const actualDayjs = jest.requireActual('dayjs')
  const mockDayjs = (date) => actualDayjs(date).tz('America/Denver')
  mockDayjs.extend = actualDayjs.extend
  mockDayjs.Ls = actualDayjs.Ls
  return mockDayjs
})

it('should take a snapshot', () => {
  const { container } = render(
    <ThemeProvider theme={testTheme}>
      <Provider store={setupStore({})}>
        <DisplayCounts />
      </Provider>
    </ThemeProvider>
  )

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
