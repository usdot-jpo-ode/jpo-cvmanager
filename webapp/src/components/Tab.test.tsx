import React from 'react'
import { render } from '@testing-library/react'
import Tab from './Tab'
import { replaceChaoticIds } from '../utils/test-utils'
import { BrowserRouter } from 'react-router-dom'

it('should take a snapshot', () => {
  const { container } = render(
    <BrowserRouter>
      <Tab onClick={() => {}} activeTab={''} path={''} label={''} />
    </BrowserRouter>
  )

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
