import React from 'react'
import { render, screen, fireEvent } from '@testing-library/react'
import AdminOrganizationDeleteMenu from './AdminOrganizationDeleteMenu'
import { replaceChaoticIds } from '../utils/test-utils'
import { confirmAlert } from 'react-confirm-alert'
import { ThemeProvider } from '@mui/material'
import { testTheme } from '../styles'
jest.mock('react-confirm-alert')

it('should take a snapshot', () => {
  const { container } = render(
    <ThemeProvider theme={testTheme}>
      <AdminOrganizationDeleteMenu selectedOrganization={''} deleteOrganization={() => {}} />
    </ThemeProvider>
  )

  expect(replaceChaoticIds(container)).toMatchSnapshot()

  fireEvent.click(screen.getByTitle('Delete Organization'))
  expect(confirmAlert).toHaveBeenCalledTimes(1)
})
