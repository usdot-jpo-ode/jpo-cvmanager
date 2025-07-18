import {
  Box,
  Button,
  Card,
  Container,
  Grid2,
  InputAdornment,
  Tab,
  Tabs,
  TextField,
  TextFieldProps,
  Typography,
  useTheme,
} from '@mui/material'
import { NotificationsTableResults } from './notifications-table-results'
import RefreshIcon from '@mui/icons-material/Refresh'
import SearchIcon from '@mui/icons-material/Search'
import NotificationApi from '../../../apis/intersections/notification-api'
import React, { useEffect, useState, useRef } from 'react'
import { selectToken } from '../../../generalSlices/userSlice'
import { selectSelectedIntersectionId } from '../../../generalSlices/intersectionSlice'
import { useSelector } from 'react-redux'
import { Close } from '@mui/icons-material'

const tabs = [
  {
    label: 'All',
    value: 'all',
    description: '',
  },
  {
    label: 'Cease Broadcast',
    value: 'CeaseBroadcast',
    description: 'Notification Requests to Cease Broadcast of Associated Messages',
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

export const NotificationsTable = (props: { simple: boolean }) => {
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
  const token = useSelector(selectToken)
  const dbIntersectionId = useSelector(selectSelectedIntersectionId)
  const theme = useTheme()

  const updateNotifications = () => {
    if (dbIntersectionId) {
      NotificationApi.getActiveNotifications({
        token: token,
        intersectionId: dbIntersectionId,
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
    // wait 1 second, then re-request notifications
    setTimeout(() => {
      updateNotifications()
    }, 1000)
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
      <Container
        maxWidth={false}
        sx={{
          backgroundColor: theme.palette.background.paper,
          marginTop: theme.spacing(3),
          borderRadius: '4px',
        }}
        disableGutters
      >
        <Card sx={{ overflowY: 'auto' }}>
          {!simple && (
            <Box>
              <Tabs
                onChange={handleTabsChange}
                value={currentTab}
                centered
                sx={{
                  px: 3,
                  mt: 1,
                  '& .MuiButtonBase-root': { textTransform: 'capitalize' },
                  '& .MuiTabs-indicator': { backgroundColor: theme.palette.custom.rowActionIcon },
                  '& .Mui-selected': { color: `${theme.palette.custom.rowActionIcon} !important` },
                }}
              >
                {tabs.map((tab) => (
                  <Tab key={tab.value} label={tab.label} value={tab.value} />
                ))}
              </Tabs>
              <Box
                sx={{
                  alignItems: 'center',
                  display: 'flex',
                  alignContent: 'space-between',
                  justifyContent: 'flex-start',
                  flexWrap: 'wrap',
                  m: -1.5,
                  p: 2,
                }}
              >
                <Box
                  sx={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    mb: 1,
                    width: '100%',
                  }}
                >
                  <Typography color={theme.palette.text.secondary}>{currentDescription}</Typography>
                </Box>
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
                    variant="standard"
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
                    placeholder="Search..."
                    sx={{
                      '& .Mui-focused::after': {
                        borderBottom: `2px solid ${theme.palette.custom.rowActionIcon}`,
                      },
                    }}
                  />
                </Box>
                <Button
                  color="info"
                  variant="outlined"
                  onClick={updateNotifications}
                  startIcon={<RefreshIcon fontSize="small" />}
                  sx={{ m: 1 }}
                  className="museo-slab capital-case"
                >
                  Refresh
                </Button>
              </Box>
            </Box>
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
              pb: 1,
            }}
          >
            <Grid2 container justifyContent="right" spacing={3}>
              <Grid2>
                <Button
                  sx={{
                    m: 1,
                    mr: 3,
                  }}
                  variant="outlined"
                  color="info"
                  startIcon={<Close fontSize="small" />}
                  onClick={() => {
                    dismissNotifications(acceptedNotifications)
                  }}
                  disabled={acceptedNotifications.length <= 0 ? true : false}
                  className="museo-slab capital-case"
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
