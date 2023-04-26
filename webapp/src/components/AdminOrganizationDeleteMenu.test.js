import React from 'react'
import { render } from '@testing-library/react'
import AdminOrganizationDeleteMenu from './AdminOrganizationDeleteMenu'
import { replaceChaoticIds } from '../utils/test-utils'

it('should take a snapshot', () => {
  const { container } = render(<AdminOrganizationDeleteMenu />)

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
