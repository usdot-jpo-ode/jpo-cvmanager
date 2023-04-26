import React from 'react'
import { render } from '@testing-library/react'
import BsmMap from './BsmMap'
import { Provider } from 'react-redux'
import { setupStore } from '../store'
import { replaceChaoticIds } from '../utils/test-utils'

it('should take a snapshot', () => {
  const initialState = {
    rsu: { value: { bsmStart: new Date(2023, 2, 1, 0), bsmEnd: new Date(2023, 2, 1, 1), bsmCoordinates: [] } },
  }
  const { container } = render(
    <Provider store={setupStore(initialState)}>
      <BsmMap />
    </Provider>
  )

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
