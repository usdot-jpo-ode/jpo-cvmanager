import React from 'react'
import { render } from '@testing-library/react'
import Help from './Help'
import { replaceChaoticIds } from '../utils/test-utils'

it('should take a snapshot', () => {
  const { container } = render(<Help />)

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
