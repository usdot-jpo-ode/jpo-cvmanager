import React from 'react'
import { render } from '@testing-library/react'
import AdminAddOrganization from './AdminAddOrganization'
import { Provider } from 'react-redux'
import { setupStore } from '../../store'
import { replaceChaoticIds } from '../../utils/test-utils'
import { BrowserRouter, Route, Routes } from 'react-router-dom'

it('should take a snapshot', () => {
  const { container } = render(
    <Provider store={setupStore({})}>
      <BrowserRouter>
        <Routes>
          <Route path="*" element={<AdminAddOrganization />} />
        </Routes>
      </BrowserRouter>
    </Provider>
  )

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
