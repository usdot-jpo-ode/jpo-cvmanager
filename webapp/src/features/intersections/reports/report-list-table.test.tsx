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
vi.mock('date-fns', async () => {
  const originalDateFns: any = await vi.importActual('date-fns')
  const { formatInTimeZone } = await import('date-fns-tz')
  return {
    ...originalDateFns,
    format: (date: Date, formatStr: string) => formatInTimeZone(date, 'America/Denver', formatStr),
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
