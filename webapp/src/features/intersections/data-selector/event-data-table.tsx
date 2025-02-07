import { Box, Button, Card, Container, Divider, Grid2, TextFieldProps, CardHeader } from '@mui/material'
import { EventListResults } from './event-list-results'
import React, { useEffect, useState, useRef } from 'react'

const applyPagination = (parameters, page, rowsPerPage) =>
  parameters.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)

export const EventDataTable = (props: {
  events: MessageMonitor.Event[]
  onDownload: () => void
  onDownloadJson: () => void
}) => {
  const { events, onDownload, onDownloadJson } = props
  const [page, setPage] = useState(0)
  const [rowsPerPage, setRowsPerPage] = useState(10)

  useEffect(() => {
    setPage(0)
  }, events)

  const handlePageChange = (event, newPage) => {
    setPage(newPage)
  }

  const handleRowsPerPageChange = (event) => {
    setRowsPerPage(parseInt(event.target.value, 10))
  }

  // Usually query is done on backend with indexing solutions
  const paginatedNotifications = [...applyPagination(events, page, rowsPerPage)]

  return (
    <>
      <Container maxWidth={false}>
        <Card>
          <>
            <CardHeader title="Data" />
            <Divider />
          </>
          <EventListResults
            events={paginatedNotifications}
            eventsCount={events.length}
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
                  sx={{ m: 1 }}
                  variant="contained"
                  onClick={onDownload}
                  disabled={events.length <= 0 ? true : false}
                >
                  Download
                </Button>
                <Button
                  sx={{ m: 1 }}
                  variant="contained"
                  onClick={onDownloadJson}
                  disabled={events.length <= 0 ? true : false}
                >
                  Download JSON
                </Button>
              </Grid2>
            </Grid2>
          </Box>
        </Box>
      </Container>
    </>
  )
}
