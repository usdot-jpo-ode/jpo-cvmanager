import React from 'react'
import { render } from '@testing-library/react'
import HeatMap from './HeatMap'
import { Provider } from 'react-redux'
import { setupStore } from '../store'
import { replaceChaoticIds } from '../utils/test-utils'

it('should take a snapshot', () => {
  const { container } = render(
    <Provider store={setupStore({})}>
      <HeatMap />
    </Provider>
  )

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
