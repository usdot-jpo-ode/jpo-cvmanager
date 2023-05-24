import React from 'react'
import { render } from '@testing-library/react'
import RsuMarker from './RsuMarker'
import { replaceChaoticIds } from '../utils/test-utils'

it('should take a snapshot', () => {
  const { container } = render(<RsuMarker />)

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
