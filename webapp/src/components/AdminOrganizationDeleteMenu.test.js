import React from 'react'
import { render, screen, fireEvent } from '@testing-library/react'
import AdminOrganizationDeleteMenu from './AdminOrganizationDeleteMenu'
import { replaceChaoticIds } from '../utils/test-utils'
import { confirmAlert } from 'react-confirm-alert'
jest.mock('react-confirm-alert') // SoundPlayer is now a mock constructor

it('should take a snapshot', () => {
  const { container } = render(<AdminOrganizationDeleteMenu />)

  expect(replaceChaoticIds(container)).toMatchSnapshot()

  fireEvent.click(screen.getByTitle('Delete Organization'))
  expect(confirmAlert).toHaveBeenCalledTimes(1)
})
