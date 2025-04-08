import React from 'react'
import { render } from '@testing-library/react'
import { ReportListTable } from './report-list-table'
import { Provider } from 'react-redux'
import { ThemeProvider } from '@mui/material'
import { testTheme } from '../../../styles'
import { setupStore } from '../../../store'
import { replaceChaoticIds } from '../../../utils/test-utils'
import { sampleReports } from './testing-data/sample-reports'
import { BrowserRouter } from 'react-router-dom'

// Mock date-fns format to use a specific timezone
jest.mock('date-fns', () => {
  const originalDateFns = jest.requireActual('date-fns')
  const { toZonedTime, fromZonedTime, format } = jest.requireActual('date-fns-tz')

  function formatDateToCustomString(date: Date): string {
    const options: Intl.DateTimeFormatOptions = {
      month: 'short', // Abbreviated month (e.g., "Mar")
      day: '2-digit', // Day of the month (e.g., "08")
      hour: 'numeric', // Hour (e.g., "1", "12")
      minute: '2-digit', // Minutes (e.g., "05")
      second: '2-digit', // Seconds (e.g., "30")
      hour12: true, // Use 12-hour format with AM/PM
    }

    return new Intl.DateTimeFormat('en-US', options).format(date)
  }

  return {
    ...originalDateFns,
    format: (date: Date, formatString) => formatDateToCustomString(date),
  }
})

it('should take a snapshot', () => {
  const { container } = render(
    <ThemeProvider theme={testTheme}>
      <Provider store={setupStore({})}>
        <BrowserRouter>
          <ReportListTable
            reports={sampleReports}
            reportsCount={sampleReports.length}
            onPageChange={() => {}}
            onRowsPerPageChange={() => {}}
            page={0}
            rowsPerPage={10}
            onViewReport={() => {}}
          />
        </BrowserRouter>
      </Provider>
    </ThemeProvider>
  )

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
