import React from 'react'
import { render } from '@testing-library/react'
import Tab from './Tab'
import { replaceChaoticIds } from '../utils/test-utils'

it('should take a snapshot', () => {
  const { container } = render(<Tab onClick={() => {}} activeTab={''} path={''} label={''} />)

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
