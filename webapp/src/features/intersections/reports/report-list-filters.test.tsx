import React from 'react'
import { render } from '@testing-library/react'
import { ReportListFilters } from './report-list-filters'
import { Provider } from 'react-redux'
import { ThemeProvider } from '@mui/material'
import { testTheme } from '../../../styles'
import { setupStore } from '../../../store'
import { replaceChaoticIds } from '../../../utils/test-utils'

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
