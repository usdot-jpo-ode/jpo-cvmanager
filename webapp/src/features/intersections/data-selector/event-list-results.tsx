import PerfectScrollbar from 'react-perfect-scrollbar'
import PropTypes from 'prop-types'
import { format } from 'date-fns'
import {
  Box,
  Card,
  IconButton,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TablePagination,
  TableRow,
  Typography,
} from '@mui/material'
import React from 'react'
import MapRoundedIcon from '@mui/icons-material/MapRounded'
import { useNavigate } from 'react-router-dom'

export const EventListResults = ({ events, eventsCount, onPageChange, onRowsPerPageChange, page, rowsPerPage }) => {
  const navigate = useNavigate()

  const getEventDescription = (event: MessageMonitor.Event) => {
    return JSON.stringify(event).replace(/,/g, ', ')
  }

  return (
    <Card>
      <PerfectScrollbar>
        <Box sx={{ minWidth: 1050, overflowX: 'auto' }}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Event Type</TableCell>
                <TableCell>Date</TableCell>
                <TableCell>View On Map</TableCell>
                <TableCell>Message</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {events.map((event: MessageMonitor.Event, i: number) => {
                return (
                  <TableRow hover key={i}>
                    <TableCell>
                      <Box
                        sx={{
                          alignItems: 'center',
                          display: 'flex',
                        }}
                      >
                        <Typography color="textPrimary" variant="body1">
                          {event.eventType}
                        </Typography>
                      </Box>
                    </TableCell>
                    <TableCell>{format(event.eventGeneratedAt, 'MM/dd/yyyy HH:mm:ss')}</TableCell>
                    <TableCell align="right">
                      <IconButton
                        component="a"
                        onClick={() =>
                          navigate(
                            `/dashboard/intersectionMap/timestamp/${event.intersectionID}/${event.eventGeneratedAt}`
                          )
                        }
                      >
                        <MapRoundedIcon fontSize="medium" />
                      </IconButton>
                    </TableCell>
                    <TableCell>{getEventDescription(event)}</TableCell>
                  </TableRow>
                )
              })}
            </TableBody>
          </Table>
        </Box>
      </PerfectScrollbar>
      <TablePagination
        component="div"
        count={eventsCount}
        onPageChange={onPageChange}
        onRowsPerPageChange={onRowsPerPageChange}
        page={page}
        rowsPerPage={rowsPerPage}
        rowsPerPageOptions={[5, 10, 25]}
      />
    </Card>
  )
}

EventListResults.propTypes = {
  events: PropTypes.array.isRequired,
  onSelectedItemsChanged: PropTypes.func,
}
