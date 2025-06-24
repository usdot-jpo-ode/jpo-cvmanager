import React from 'react'
import { render } from '@testing-library/react'
import AdminOrganizationTabRsu from './AdminOrganizationTabRsu'
import { Provider } from 'react-redux'
import { ThemeProvider } from '@mui/material'
import { testTheme } from '../../styles'
import { setupStore } from '../../store'
import { replaceChaoticIds } from '../../utils/test-utils'

it('should take a snapshot', () => {
  const { container } = render(
    <ThemeProvider theme={testTheme}>
      <Provider store={setupStore({})}>
        <AdminOrganizationTabRsu
          selectedOrg={''}
          selectedOrgEmail={''}
          tableData={[]}
          // eslint-disable-next-line @typescript-eslint/no-unused-vars
          updateTableData={(orgname: string) => {}}
        />
      </Provider>
    </ThemeProvider>
  )

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
