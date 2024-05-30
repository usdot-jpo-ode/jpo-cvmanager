import React from 'react'
import { render } from '@testing-library/react'
import AdminOrganizationTab from './AdminOrganizationTab'
import { Provider } from 'react-redux'
import { setupStore } from '../../store'
import { replaceChaoticIds } from '../../utils/test-utils'
import { BrowserRouter } from 'react-router-dom'

it('should take a snapshot', () => {
  const { container } = render(
    <Provider store={setupStore({})}>
      <BrowserRouter>
        <AdminOrganizationTab />
      </BrowserRouter>
    </Provider>
  )

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
