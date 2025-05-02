import React from 'react'
import { render } from '@testing-library/react'
import { ReportGenerationDialog } from './report-generation-dialog'
import { Provider } from 'react-redux'
import { ThemeProvider } from '@mui/material'
import { testTheme } from '../../../styles'
import { setupStore } from '../../../store'
import { replaceChaoticIds } from '../../../utils/test-utils'

it('should take a snapshot', () => {
  const { container } = render(
    <ThemeProvider theme={testTheme}>
      <Provider
        store={setupStore({
          user: { value: { authLoginData: { token: '' } } },
        })}
      >
        <ReportGenerationDialog open={true} onClose={() => {}} onReportGenerated={() => {}} />
      </Provider>
    </ThemeProvider>
  )

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
