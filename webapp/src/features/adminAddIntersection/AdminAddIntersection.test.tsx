import React from 'react'
import { render } from '@testing-library/react'
import AdminAddIntersection from './AdminAddIntersection'
import { Provider } from 'react-redux'
import { setupStore } from '../../store'
import { replaceChaoticIds } from '../../utils/test-utils'
import { BrowserRouter, Routes, Route } from 'react-router-dom'

it('should take a snapshot', () => {
  const { container } = render(
    <Provider store={setupStore({})}>
      <BrowserRouter>
        <Routes>
          <Route path="*" element={<AdminAddIntersection />} />
        </Routes>
      </BrowserRouter>
    </Provider>
  )

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
