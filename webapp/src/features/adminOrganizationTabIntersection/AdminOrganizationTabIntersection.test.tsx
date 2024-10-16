import React from 'react'
import { render } from '@testing-library/react'
import AdminOrganizationTabIntersection from './AdminOrganizationTabIntersection'
import { Provider } from 'react-redux'
import { setupStore } from '../../store'
import { replaceChaoticIds } from '../../utils/test-utils'

it('should take a snapshot', () => {
  const { container } = render(
    <Provider store={setupStore({})}>
      <AdminOrganizationTabIntersection
        selectedOrg={''}
        selectedOrgEmail={''}
        tableData={[]}
        updateTableData={(orgname: string) => {}}
      />
    </Provider>
  )

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
