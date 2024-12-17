import { Box, Button, Card, Container, Divider, Grid2, TextFieldProps, CardHeader } from '@mui/material'
import { AssessmentListResults } from './assessment-list-results'
import React, { useEffect, useState, useRef } from 'react'

const applyPagination = (parameters, page, rowsPerPage) =>
  parameters.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)

export const AssessmentDataTable = (props: {
  assessments: Assessment[]
  onDownload: () => void
  onDownloadJson: () => void
}) => {
  const { assessments, onDownload, onDownloadJson } = props
  const queryRef = useRef<TextFieldProps>(null)
  const [page, setPage] = useState(0)
  const [rowsPerPage, setRowsPerPage] = useState(10)
  const [currentDescription, setCurrentDescription] = useState('')

  useEffect(() => {
    setPage(0)
  }, [assessments])

  const handlePageChange = (event, newPage) => {
    setPage(newPage)
  }

  const handleRowsPerPageChange = (event) => {
    setRowsPerPage(parseInt(event.target.value, 10))
  }

  // Usually query is done on backend with indexing solutions
  const paginatedNotifications = applyPagination(assessments, page, rowsPerPage)

  return (
    <>
      <Container maxWidth={false}>
        <Card>
          <>
            <CardHeader title="Data" />
            <Divider />
          </>

          <AssessmentListResults
            assessments={paginatedNotifications}
            assessmentsCount={assessments.length}
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
                  disabled={assessments.length <= 0 ? true : false}
                >
                  Download
                </Button>
                <Button
                  sx={{ m: 1 }}
                  variant="contained"
                  onClick={onDownloadJson}
                  disabled={assessments.length <= 0 ? true : false}
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
