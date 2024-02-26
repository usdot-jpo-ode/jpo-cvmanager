import React from 'react'
import { render } from '@testing-library/react'
import RsuRebootMenu from './RsuRebootMenu'
import { Provider } from 'react-redux'
import { setupStore } from '../store'
import { replaceChaoticIds } from '../utils/test-utils'

it('should take a snapshot', () => {
  const { container } = render(
    <Provider store={setupStore({})}>
      <RsuRebootMenu />
    </Provider>
  )

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
