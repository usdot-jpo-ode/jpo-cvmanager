import {
  Box,
  Button,
  Card,
  Container,
  Divider,
  Grid2,
  InputAdornment,
  Stack,
  Tab,
  Tabs,
  TextField,
  TextFieldProps,
  Typography,
  CardHeader,
} from '@mui/material'
import { NotificationsTableResults } from './notifications-table-results'
import RefreshIcon from '@mui/icons-material/Refresh'
import SearchIcon from '@mui/icons-material/Search'
import NotificationApi from '../../../apis/intersections/notification-api'
import React, { useEffect, useState, useRef } from 'react'
import { selectToken } from '../../../generalSlices/userSlice'
import { selectSelectedIntersectionId, selectSelectedRoadRegulatorId } from '../../../generalSlices/intersectionSlice'
import { useAppSelector } from '../../../hooks'

const tabs = [
  {
    label: 'All',
    value: 'all',
    description: 'All Notifications',
  },
  {
    label: 'Cease Broadcast',
    value: 'CeaseBaroadcast',
    description: 'Notification Requests to Cease Broadcast of associated messages',
  },
]

const applyFilters = (parameters, filter) =>
  parameters.filter((parameter) => {
    if (filter.query) {
      let queryMatched = false
      const properties = ['notificationType', 'notificationText']
      properties.forEach((property) => {
        if (parameter[property].toLowerCase().includes(filter.query.toLowerCase())) {
          queryMatched = true
        }
      })

      if (!queryMatched) {
        return false
      }
    }

    if (filter.tab === 'all') {
      return true
    }

    return parameter['notificationType'] == filter.tab
  })

const applyPagination = (parameters, page, rowsPerPage) =>
  parameters.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)

export const NotificationsTable = (props: { simple: Boolean }) => {
  const { simple } = props
  const queryRef = useRef<TextFieldProps>(null)
  const [notifications, setNotifications] = useState<MessageMonitor.Notification[]>([])
  const [acceptedNotifications, setAcceptedNotifications] = useState<string[]>([])
  const [expandedNotifications, setExpandedNotifications] = useState<string[]>([])
  const [currentTab, setCurrentTab] = useState('all')
  const [page, setPage] = useState(0)
  const [rowsPerPage, setRowsPerPage] = useState(10)
  const [currentDescription, setCurrentDescription] = useState('')
  const [filter, setFilter] = useState({
    query: '',
    tab: currentTab,
  })
  const token = useAppSelector(selectToken)
  const dbIntersectionId = useAppSelector(selectSelectedIntersectionId)
  const roadRegulatorId = useAppSelector(selectSelectedRoadRegulatorId)

  const updateNotifications = () => {
    if (dbIntersectionId) {
      NotificationApi.getActiveNotifications({
        token: token,
        intersectionId: dbIntersectionId,
        roadRegulatorId: roadRegulatorId,
      }).then((notifs) => setNotifications(notifs))
    } else {
      console.error('Did not attempt to update notifications. Intersection ID:', dbIntersectionId)
    }
  }

  const dismissNotifications = (ids: string[]) => {
    if (dbIntersectionId) {
      NotificationApi.dismissNotifications({ token: token, ids })
    } else {
      console.error('Did not attempt to dismiss notifications. Intersection ID:', dbIntersectionId)
    }
  }

  useEffect(() => {
    updateNotifications()
    setAcceptedNotifications([])
  }, [dbIntersectionId])

  useEffect(() => {
    updateDescription()
  }, [currentTab])

  const handleTabsChange = (event, value) => {
    const updatedFilter = { ...filter, tab: value }
    setCurrentTab(value)
    setFilter(updatedFilter)
    setPage(0)
    setCurrentTab(value)
  }

  const handleQueryChange = (event) => {
    event.preventDefault()
    setFilter((prevState) => ({
      ...prevState,
      query: queryRef.current?.value as string,
    }))
  }

  const handlePageChange = (event, newPage) => {
    setPage(newPage)
  }

  const handleRowsPerPageChange = (event) => {
    setRowsPerPage(parseInt(event.target.value, 10))
  }

  const updateDescription = () => {
    for (let i = 0; i < tabs.length; i++) {
      if (tabs[i].value === currentTab) {
        setCurrentDescription(tabs[i].description)
      }
    }
  }

  // Usually query is done on backend with indexing solutions
  const filteredNotifications = applyFilters(notifications, filter)
  const paginatedNotifications = applyPagination(filteredNotifications, page, rowsPerPage)

  return (
    <>
      <Container maxWidth={false}>
        {!simple && (
          <>
            <Box
              sx={{
                alignItems: 'center',
                display: 'flex',
                justifyContent: 'space-between',
                flexWrap: 'wrap',
                m: -1,
              }}
            >
              <Grid2 container justifyContent="space-between" spacing={3}>
                <Grid2>
                  <Typography sx={{ m: 1 }} variant="h4" color="text.secondary">
                    Notifications
                  </Typography>
                </Grid2>
              </Grid2>
              <Box
                sx={{
                  m: -1,
                  mt: 3,
                }}
              ></Box>
            </Box>
            <Box
              sx={{
                m: -1,
                mt: 3,
                mb: 3,
              }}
            >
              <Button
                color="primary"
                variant="contained"
                onClick={updateNotifications}
                startIcon={<RefreshIcon fontSize="small" sx={{ color: 'white' }} />}
                sx={{ m: 1 }}
              >
                Refresh
              </Button>
            </Box>
          </>
        )}
        <Card sx={{ overflowY: 'scroll' }}>
          {!simple && (
            <>
              <CardHeader title="Notifications" />
              <Tabs
                indicatorColor="primary"
                onChange={handleTabsChange}
                scrollButtons="auto"
                sx={{ px: 3 }}
                textColor="primary"
                value={currentTab}
                variant="scrollable"
              >
                {tabs.map((tab) => (
                  <Tab key={tab.value} label={tab.label} value={tab.value} />
                ))}
              </Tabs>
              <Divider />
              <Box
                sx={{
                  alignItems: 'center',
                  display: 'flex',
                  flexWrap: 'wrap',
                  m: -1.5,
                  p: 3,
                }}
              >
                <Stack>
                  <Box
                    component="form"
                    onSubmit={handleQueryChange}
                    sx={{
                      flexGrow: 1,
                      m: 1.5,
                    }}
                  >
                    <TextField
                      defaultValue=""
                      fullWidth
                      slotProps={{
                        input: {
                          ref: queryRef,
                          startAdornment: (
                            <InputAdornment position="start">
                              <SearchIcon fontSize="small" />
                            </InputAdornment>
                          ),
                        },
                      }}
                      placeholder="Search parameters"
                    />
                  </Box>
                  <Typography variant="body1">{currentDescription}</Typography>
                </Stack>
              </Box>
            </>
          )}

          <NotificationsTableResults
            customers={paginatedNotifications}
            allTabNotifications={notifications}
            notificationsCount={filteredNotifications.length}
            selectedNotifications={acceptedNotifications}
            onSelectedItemsChanged={setAcceptedNotifications}
            expandedNotifications={expandedNotifications}
            onExpandedItemsChanged={setExpandedNotifications}
            onPageChange={handlePageChange}
            onRowsPerPageChange={handleRowsPerPageChange}
            rowsPerPage={rowsPerPage}
            page={page}
          />
        </Card>
        <Box sx={{ mb: 4 }}>
          <Box
            sx={{
              m: -1,
              mt: 3,
            }}
          >
            <Grid2 container justifyContent="left" spacing={3}>
              <Grid2>
                <Button
                  sx={{
                    m: 1,
                    color: 'white', // Normal state text color
                    '&.Mui-disabled': {
                      color: 'grey', // Disabled state text color
                    },
                  }}
                  variant="contained"
                  onClick={() => {
                    dismissNotifications(acceptedNotifications)
                  }}
                  disabled={acceptedNotifications.length <= 0 ? true : false}
                >
                  Dismiss Notifications
                </Button>
              </Grid2>
            </Grid2>
          </Box>
        </Box>
      </Container>
    </>
  )
}
