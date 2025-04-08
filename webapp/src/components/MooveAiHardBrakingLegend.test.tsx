import React from 'react'
import { render } from '@testing-library/react'
import MooveAiHardBrakingLegend from './MooveAiHardBrakingLegend'
import { replaceChaoticIds } from '../utils/test-utils'

it('should take a snapshot', () => {
  const { container } = render(<MooveAiHardBrakingLegend />)

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
