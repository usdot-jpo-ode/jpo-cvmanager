import PerfectScrollbar from 'react-perfect-scrollbar'
import PropTypes from 'prop-types'
import { format } from 'date-fns'
import {
  Box,
  Card,
  Checkbox,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TablePagination,
  TableRow,
  Typography,
  IconButton,
} from '@mui/material'
import React from 'react'
import { v4 as uuid } from 'uuid'
import MapRoundedIcon from '@mui/icons-material/MapRounded'
import { useNavigate } from 'react-router-dom'

export const AssessmentListResults = ({
  customers,
  allTabNotifications,
  notificationsCount,
  selectedNotifications,
  onSelectedItemsChanged,
  onPageChange,
  onRowsPerPageChange,
  page,
  rowsPerPage,
}) => {
  const navigate = useNavigate()

  const handleSelectAll = (event) => {
    let newSelectedCustomerIds: uuid[] = []

    if (event.target.checked) {
      newSelectedCustomerIds = allTabNotifications.map((customer) => customer.id)
    } else {
      newSelectedCustomerIds = []
    }

    onSelectedItemsChanged(newSelectedCustomerIds)
  }

  const handleSelectOne = (event, notificationId) => {
    if (!selectedNotifications.includes(notificationId)) {
      onSelectedItemsChanged((prevSelected) => [...prevSelected, notificationId])
    } else {
      onSelectedItemsChanged((prevSelected) => prevSelected.filter((id) => id !== notificationId))
    }
  }

  return (
    <Card>
      <PerfectScrollbar>
        <Box sx={{ minWidth: 1050 }}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell padding="checkbox">
                  <Checkbox
                    checked={selectedNotifications.length === notificationsCount}
                    color="primary"
                    indeterminate={
                      selectedNotifications.length > 0 && selectedNotifications.length < notificationsCount
                    }
                    onChange={handleSelectAll}
                  />
                </TableCell>
                <TableCell>Notification Type</TableCell>
                <TableCell>Date</TableCell>
                <TableCell>Message</TableCell>
                <TableCell>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {customers.map((customer) => {
                const isNotificationSelected = [...selectedNotifications].indexOf(customer.id) !== -1

                return (
                  <TableRow
                    hover
                    key={customer.id}
                    selected={[...selectedNotifications].indexOf(customer.message) !== -1}
                  >
                    <TableCell padding="checkbox">
                      <Checkbox
                        checked={isNotificationSelected}
                        onChange={(event) => handleSelectOne(event, customer.id)}
                        value="true"
                      />
                    </TableCell>
                    <TableCell>
                      <Box
                        sx={{
                          alignItems: 'center',
                          display: 'flex',
                        }}
                      >
                        <Typography color="textPrimary" variant="body1">
                          {customer.notificationType}
                        </Typography>
                      </Box>
                    </TableCell>
                    <TableCell>{format(customer.notificationGeneratedAt, 'MM/dd/yyyy')}</TableCell>
                    <TableCell>{customer.notificationText}</TableCell>
                    <TableCell align="right">
                      <IconButton onClick={() => navigate(`/map/notification/${customer.id}`)}>
                        <MapRoundedIcon fontSize="medium" />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                )
              })}
            </TableBody>
          </Table>
        </Box>
      </PerfectScrollbar>
      <TablePagination
        component="div"
        count={notificationsCount}
        onPageChange={onPageChange}
        onRowsPerPageChange={onRowsPerPageChange}
        page={page}
        rowsPerPage={rowsPerPage}
        rowsPerPageOptions={[5, 10, 25]}
      />
    </Card>
  )
}

AssessmentListResults.propTypes = {
  customers: PropTypes.array.isRequired,
  onSelectedItemsChanged: PropTypes.func,
}
