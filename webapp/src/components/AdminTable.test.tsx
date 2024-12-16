import React from 'react'
import { render } from '@testing-library/react'
import AdminTable from './AdminTable'
import { replaceChaoticIds } from '../utils/test-utils'
import { ThemeProvider } from '@mui/material'
import { testTheme } from '../styles'

it('should take a snapshot', () => {
  const { container } = render(
    <ThemeProvider theme={testTheme}>
      <AdminTable actions={[]} columns={[]} data={[]} title={''} />
    </ThemeProvider>
  )

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
