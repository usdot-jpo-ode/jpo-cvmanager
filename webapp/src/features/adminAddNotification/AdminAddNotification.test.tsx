import React from 'react'
import { render } from '@testing-library/react'
import AdminAddNotification from '../adminAddNotification/AdminAddNotification'
import { Provider } from 'react-redux'
import { setupStore } from '../../store'
import { replaceChaoticIds } from '../../utils/test-utils'

it('should take a snapshot', () => {
  const { container } = render(
    <Provider store={setupStore({})}>
      <AdminAddNotification />
    </Provider>
  )

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
