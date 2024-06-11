import { Box } from '@mui/material'
import { NotificationsTable } from './notifications/notifications-table'
import { DashboardLayout } from './dashboard-layout'
import React from 'react'

const Page = () => {
  return (
    <Box
      component="main"
      sx={{
        flexGrow: 1,
        py: 8,
      }}
    >
      <NotificationsTable simple={false} />
    </Box>
  )
}

Page.getLayout = (page) => <DashboardLayout>{page}</DashboardLayout>

export default Page
