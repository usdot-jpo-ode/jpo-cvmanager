import { Box, CircularProgress, Container, LinearProgress, useTheme } from '@mui/material'
import {
  useGetEmailSubscriptionsQuery,
  useUpdateEmailSubscriptionsMutation,
} from '../features/api/subscriptionManagementApiSlice'
import { EmailSubscription } from '../models/email-subscriptions'
import { headerTabHeight } from '../styles/index'
import SubscriptionForm from './SubscriptionForm'

const SubscriptionManagement = () => {
  const theme = useTheme()

  // Fetch email subscriptions with RTK Query
  const { data, isLoading, isFetching } = useGetEmailSubscriptionsQuery()
  const [updateEmailSubscriptions] = useUpdateEmailSubscriptionsMutation()

  const handleSave = async (subscriptions: EmailSubscription[]) => updateEmailSubscriptions(subscriptions).unwrap()

  // Show loading while fetching data OR while subscriptions state is being initialized
  if (isLoading) {
    return (
      <Container maxWidth="md">
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="100vh">
          <CircularProgress />
        </Box>
      </Container>
    )
  }

  return (
    <Container
      maxWidth={false}
      sx={{
        backgroundColor: theme.palette.background.default,
        height: `calc(100vh - ${headerTabHeight}px)`,
        overflowY: 'auto',
      }}
    >
      <Container maxWidth="md">
        <Box sx={{ py: 4, position: 'relative' }}>
          {/* Show progress bar during refetch, but keep form mounted */}
          {isFetching && (
            <LinearProgress
              sx={{
                position: 'absolute',
                top: 0,
                left: 0,
                right: 0,
                zIndex: 1000,
              }}
            />
          )}
          <SubscriptionForm
            subscriptions={data?.subscriptions ?? []}
            onSave={handleSave}
            title="Manage Your Email Subscriptions"
            showUnsubscribeAll={true}
            showHomepageLink={false}
          />
        </Box>
      </Container>
    </Container>
  )
}

export default SubscriptionManagement
