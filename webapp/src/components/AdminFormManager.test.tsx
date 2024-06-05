import React from 'react'
import { render } from '@testing-library/react'
import AdminFormManager from './AdminFormManager'
import { replaceChaoticIds } from '../utils/test-utils'
import { setupStore } from '../store'
import { Provider } from 'react-redux'
import { BrowserRouter } from 'react-router-dom'

it('snapshot rsu', () => {
  const { container } = render(
    <Provider store={setupStore({})}>
      <BrowserRouter>
        <AdminFormManager activeForm={'add_rsu'} />
      </BrowserRouter>
    </Provider>
  )

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})

it('snapshot user', () => {
  const { container } = render(
    <Provider store={setupStore({})}>
      <BrowserRouter>
        <AdminFormManager activeForm={'add_user'} />
      </BrowserRouter>
    </Provider>
  )

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})

it('snapshot organization', () => {
  const { container } = render(
    <Provider store={setupStore({})}>
      <BrowserRouter>
        <AdminFormManager activeForm={'add_organization'} />
      </BrowserRouter>
    </Provider>
  )

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
