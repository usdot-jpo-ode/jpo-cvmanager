import { Box } from '@mui/material'
import { NotificationsTable } from '../../features/intersections/notifications/notifications-table'
import React from 'react'

const Page = () => {
  return (
    <>
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          py: 8,
        }}
      >
        <NotificationsTable simple={false} />
      </Box>
    </>
  )
}

export default Page
