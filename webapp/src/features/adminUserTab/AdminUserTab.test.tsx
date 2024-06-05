import React from 'react'
import { render } from '@testing-library/react'
import AdminUserTab from './AdminUserTab'
import { Provider } from 'react-redux'
import { setupStore } from '../../store'
import { replaceChaoticIds } from '../../utils/test-utils'
import { BrowserRouter } from 'react-router-dom'

it('should take a snapshot', () => {
  const { container } = render(
    <Provider store={setupStore({})}>
      <BrowserRouter>
        <AdminUserTab />
      </BrowserRouter>
    </Provider>
  )

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
