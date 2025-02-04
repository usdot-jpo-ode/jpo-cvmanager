import React from 'react'
import { render } from '@testing-library/react'
import SnmpwalkItem from './SnmpwalkItem'
import { replaceChaoticIds } from '../utils/test-utils'

it('should take a snapshot', () => {
  const { container } = render(<SnmpwalkItem content={{}} index={''} handleDelete={() => ({})} />)

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
