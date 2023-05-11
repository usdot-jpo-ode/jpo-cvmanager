import React from 'react'
import { render } from '@testing-library/react'
import RsuUpdateItem from './RsuUpdateItem'
import { replaceChaoticIds } from '../utils/test-utils'

it('should take a snapshot', () => {
  const { container } = render(<RsuUpdateItem osUpdateAvailable={[]} fwUpdateAvailable={[]} />)

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
