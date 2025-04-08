import React from 'react'
import { render } from '@testing-library/react'
import { ReportListFilters } from './report-list-filters'
import { Provider } from 'react-redux'
import { ThemeProvider } from '@mui/material'
import { testTheme } from '../../../styles'
import { setupStore } from '../../../store'
import { MockLocalizationProvider, replaceChaoticIds } from '../../../utils/test-utils'

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
        <ReportListFilters
          open={true}
          onClose={() => {}}
          filters={{ startDate: new Date('2025-04-07'), endDate: new Date('2025-04-08') }}
          onChange={() => {}}
          loading={false}
          containerRef={() => {}}
          setOpenReportGenerationDialog={() => {}}
        />
      </Provider>
    </ThemeProvider>
  )

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
