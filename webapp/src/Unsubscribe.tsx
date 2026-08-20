import { useSearchParams } from 'react-router-dom'
import { Box, CircularProgress, Container, LinearProgress } from '@mui/material'
import { useGetEmailSubscriptionsQuery, useUpdateEmailSubscriptionsMutation } from './features/api/unsubscribeApiSlice'
import SubscriptionForm from './components/SubscriptionForm'
import { EmailSubscription } from './models/email-subscriptions'

const Unsubscribe = () => {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')
  const { data, isLoading, isFetching, isError, error } = useGetEmailSubscriptionsQuery(token ?? '', { skip: !token })
  const [updateEmailSubscriptions] = useUpdateEmailSubscriptionsMutation()

  const getErrorMessage = (queryError: unknown): string => {
    if (!queryError || typeof queryError !== 'object') {
      return 'Failed to load subscriptions. Please try again later.'
    }

    let status: String = ''

    if ('originalStatus' in queryError) {
      status = String((queryError as { originalStatus: unknown }).originalStatus)
    } else if ('status' in queryError) {
      status = String((queryError as { status: unknown }).status)
    }

    if (status === '403') {
      return 'This unsubscribe link is invalid or has expired.'
    } else if (status === '500') {
      return 'A server error occurred while loading subscriptions. Please try again later.'
    }

    return 'Failed to load subscriptions. Please try again later.'
  }

  if (!token) {
    return (
      <Container maxWidth="md">
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="100vh">
          <p>Invalid or missing token. Please check your email link.</p>
        </Box>
      </Container>
    )
  }

  const handleSave = async (subscriptions: EmailSubscription[]) => {
    await updateEmailSubscriptions({ token, subscriptions }).unwrap()
  }

  if (isLoading) {
    return (
      <Container maxWidth="md">
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="100vh">
          <CircularProgress />
        </Box>
      </Container>
    )
  }

  if (isError) {
    return (
      <Container maxWidth="md">
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="100vh">
          <p>{getErrorMessage(error)}</p>
        </Box>
      </Container>
    )
  }

  return (
    <Container maxWidth="md" sx={{ height: '100vh', overflowY: 'auto' }}>
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
          showHomepageLink={true}
        />
      </Box>
    </Container>
  )
}

export default Unsubscribe
