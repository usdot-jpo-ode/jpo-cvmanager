import { useEffect, useMemo, useState } from 'react'
import {
  Box,
  Paper,
  Typography,
  FormControlLabel,
  Checkbox,
  Button,
  Alert,
  CircularProgress,
  Divider,
  FormGroup,
} from '@mui/material'
import HomeIcon from '@mui/icons-material/Home'
import { EmailSubscription } from '../models/email-subscriptions'
import { useNavigate } from 'react-router-dom'

interface SubscriptionFormProps {
  subscriptions: EmailSubscription[]
  onSave: (subscriptions: EmailSubscription[]) => Promise<void>
  title?: string
  showUnsubscribeAll?: boolean
  showHomepageLink?: boolean
}

const isRequiredRoleUser = (role: string): boolean => {
  const normalizedRole = role.toLowerCase()
  return normalizedRole === 'user'
}

const SubscriptionForm = ({
  subscriptions: initialSubscriptions,
  onSave,
  title = 'Email Subscription Preferences',
  showUnsubscribeAll = true,
  showHomepageLink = true,
}: SubscriptionFormProps) => {
  const navigate = useNavigate()
  const [subscriptions, setSubscriptions] = useState<Record<string, EmailSubscription>>(() => {
    const initial: Record<string, EmailSubscription> = {}
    initialSubscriptions.forEach((sub) => {
      initial[sub.category] = { ...sub }
    })
    return initial
  })
  const [prevSubscriptions, setPrevSubscriptions] = useState<Record<string, EmailSubscription>>(() => {
    const initial: Record<string, EmailSubscription> = {}
    initialSubscriptions.forEach((sub) => {
      initial[sub.category] = { ...sub }
    })
    return initial
  })
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState(false)

  const isModified = useMemo(() => {
    return Object.values(prevSubscriptions).some((sub) => {
      const current = subscriptions[sub.category]
      return (
        current?.immediate !== sub.immediate ||
        current?.hourly !== sub.hourly ||
        current?.daily !== sub.daily ||
        current?.weekly !== sub.weekly ||
        current?.monthly !== sub.monthly
      )
    })
  }, [prevSubscriptions, subscriptions])

  useEffect(() => {
    // If the user has made changes, we don't want to overwrite their selections when initialSubscriptions updates
    if (isModified) return

    const initial: Record<string, EmailSubscription> = {}
    initialSubscriptions.forEach((sub) => {
      initial[sub.category] = { ...sub }
    })
    setSubscriptions(initial)
    setPrevSubscriptions(initial)
  }, [initialSubscriptions])

  const handleFrequencyToggle = (
    categoryId: string,
    frequency: 'immediate' | 'hourly' | 'daily' | 'weekly' | 'monthly'
  ) => {
    setSubscriptions((prev) => ({
      ...prev,
      [categoryId]: { ...prev[categoryId], [frequency]: !prev[categoryId][frequency] },
    }))
  }

  const handleSave = async (e?: React.FormEvent) => {
    // Prevent form submission if triggered by form submit event
    e?.preventDefault()

    setSaving(true)
    setError(null)
    setSuccess(false)

    try {
      await onSave(Object.values(subscriptions))
      setPrevSubscriptions({ ...subscriptions })
      setSuccess(true)

      // Clear success message after 3 seconds
      setTimeout(() => {
        setSuccess(false)
      }, 3000)
    } catch (err) {
      setError('Failed to save subscription preferences. Please try again.')
    } finally {
      setSaving(false)
    }
  }

  const handleUnsubscribeAll = () => {
    setSubscriptions((prev) => {
      const updated: Record<string, EmailSubscription> = {}
      Object.keys(prev).forEach((category) => {
        updated[category] = {
          ...prev[category],
          immediate: false,
          hourly: false,
          daily: false,
          weekly: false,
          monthly: false,
        }
      })
      return updated
    })
  }

  return (
    <Paper elevation={3} sx={{ p: 2 }}>
      <Typography variant="h5" component="h3" gutterBottom>
        {title}
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {success && (
        <>
          <Alert severity="success" sx={{ mb: 2 }}>
            Subscription preferences saved successfully!
          </Alert>
        </>
      )}

      <Divider sx={{ my: 1.5 }} />

      <FormGroup>
        {initialSubscriptions.map((cat) => {
          return (
            <Box
              key={cat.category}
              sx={{
                p: 0.75,
                mb: 0.75,
                border: 1,
                borderColor: 'divider',
                borderRadius: 0.5,
                backgroundColor: 'transparent',
              }}
            >
              <Box>
                <Typography variant="body1" fontWeight="medium">
                  {cat.category}
                  {!isRequiredRoleUser(cat.required_role) && (
                    <Typography
                      component="span"
                      variant="caption"
                      sx={{
                        ml: 1,
                        px: 1,
                        py: 0.25,
                        backgroundColor: 'primary.main',
                        color: 'primary.contrastText',
                        borderRadius: 1,
                      }}
                    >
                      {cat.required_role}
                    </Typography>
                  )}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {cat.description}
                </Typography>
              </Box>

              {/* Frequency Options */}
              <Box sx={{ ml: 1.5, mt: 0, display: 'flex', flexWrap: 'wrap', gap: 0 }}>
                {cat.supports_immediate && (
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={subscriptions[cat.category]?.immediate || false}
                        onChange={() => handleFrequencyToggle(cat.category, 'immediate')}
                        color="secondary"
                        size="small"
                      />
                    }
                    label={<Typography variant="body2">Immediate</Typography>}
                    sx={{ my: 0, mr: 1, minHeight: 24 }}
                  />
                )}

                {cat.supports_hourly && (
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={subscriptions[cat.category]?.hourly || false}
                        onChange={() => handleFrequencyToggle(cat.category, 'hourly')}
                        color="secondary"
                        size="small"
                      />
                    }
                    label={<Typography variant="body2">Hourly</Typography>}
                    sx={{ my: 0, mr: 1, minHeight: 24 }}
                  />
                )}

                {cat.supports_daily && (
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={subscriptions[cat.category]?.daily || false}
                        onChange={() => handleFrequencyToggle(cat.category, 'daily')}
                        color="secondary"
                        size="small"
                      />
                    }
                    label={<Typography variant="body2">Daily</Typography>}
                    sx={{ my: 0, mr: 1, minHeight: 24 }}
                  />
                )}

                {cat.supports_weekly && (
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={subscriptions[cat.category]?.weekly || false}
                        onChange={() => handleFrequencyToggle(cat.category, 'weekly')}
                        color="secondary"
                        size="small"
                      />
                    }
                    label={<Typography variant="body2">Weekly</Typography>}
                    sx={{ my: 0, mr: 1, minHeight: 24 }}
                  />
                )}

                {cat.supports_monthly && (
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={subscriptions[cat.category]?.monthly || false}
                        onChange={() => handleFrequencyToggle(cat.category, 'monthly')}
                        color="secondary"
                        size="small"
                      />
                    }
                    label={<Typography variant="body2">Monthly</Typography>}
                    sx={{ my: 0, mr: 1, minHeight: 24 }}
                  />
                )}
              </Box>
            </Box>
          )
        })}
      </FormGroup>

      {initialSubscriptions.length === 0 && (
        <Alert severity="warning" sx={{ mt: 2 }}>
          Unable to retrieve subscription details - this unsubscribe link may be invalid
        </Alert>
      )}

      <Divider sx={{ my: 1.5 }} />

      <Box sx={{ display: 'flex', gap: 1, justifyContent: 'space-between' }}>
        <Box>
          {showHomepageLink && (
            <Button
              variant="outlined"
              startIcon={<HomeIcon />}
              onClick={() => navigate('/')}
              disabled={saving}
              size="small"
              sx={{ mr: 0.5, minHeight: 32, px: 1 }}
            >
              Home
            </Button>
          )}
          {showUnsubscribeAll && (
            <Button
              variant="outlined"
              color="warning"
              onClick={handleUnsubscribeAll}
              disabled={saving || initialSubscriptions.length === 0}
              size="small"
              sx={{ minHeight: 32, px: 1 }}
            >
              Unsubscribe from All
            </Button>
          )}
        </Box>
        <Button
          variant="contained"
          color="primary"
          onClick={handleSave}
          disabled={saving || initialSubscriptions.length === 0 || !isModified}
          startIcon={saving && <CircularProgress size={20} />}
          size="small"
          sx={{ ml: showUnsubscribeAll ? 0 : 'auto', minHeight: 32, px: 1.25 }}
        >
          {saving ? 'Saving...' : 'Save Preferences'}
        </Button>
      </Box>

      <Box sx={{ mt: 1.5 }}>
        <Typography variant="caption" color="text.secondary">
          Your subscription preferences will be applied immediately. You can update these settings at any time.
        </Typography>
      </Box>
    </Paper>
  )
}

export default SubscriptionForm
