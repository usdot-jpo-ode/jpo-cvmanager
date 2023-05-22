import React from 'react'
import { render } from '@testing-library/react'
import ConfigureItem from './ConfigureItem'
import { replaceChaoticIds } from '../utils/test-utils'

it('should take a snapshot', () => {
  const { container } = render(<ConfigureItem indexList={[]} />)

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
