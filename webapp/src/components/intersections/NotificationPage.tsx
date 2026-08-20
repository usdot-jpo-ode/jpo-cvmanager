import { Box } from '@mui/material'
import { NotificationsTable } from '../../features/intersections/notifications/notifications-table'

const Page = () => {
  return (
    <>
      <Box
        component="main"
        sx={{
          flexGrow: 1,
        }}
      >
        <NotificationsTable simple={false} />
      </Box>
    </>
  )
}

export default Page
