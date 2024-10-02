import React from 'react'
import { render } from '@testing-library/react'
import GenerateRSUErrorsPDF from './GenerateRSUErrorsPDF'
import { Provider } from 'react-redux'
import { setupStore } from '../../store'
import { replaceChaoticIds } from '../../utils/test-utils'

jest.useFakeTimers().setSystemTime(new Date('2024-10-01'))

it('should take a snapshot', () => {
  const { container } = render(
    <Provider store={setupStore({})}>
      <GenerateRSUErrorsPDF />
    </Provider>
  )

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
