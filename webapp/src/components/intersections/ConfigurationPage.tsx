import React, { useState, useEffect } from 'react'
import { Box, Button, Card, Container, InputAdornment, Tab, Tabs, TextField, Typography, useTheme } from '@mui/material'
import { ConfigParamListTable } from '../../features/intersections/configuration/configuration-list-table'
import RefreshIcon from '@mui/icons-material/Refresh'
import SearchIcon from '@mui/icons-material/Search'
import { selectSelectedIntersectionId } from '../../generalSlices/intersectionSlice'
import { useSelector } from 'react-redux'
import { useGetIntersectionParametersQuery } from '../../features/api/intersectionApiSlice'
import { Route, Routes } from 'react-router-dom'
import ConfigParamEdit from './configuration/edit'
import ConfigParamCreate from './configuration/create'
import ConfigParamRemove from './configuration/remove'
import { NotFound } from '../../pages/404'

const tabs = [
  {
    label: 'General',
    value: 'GENERAL',
    description: 'General configuration parameters',
  },
  {
    label: 'Default',
    value: 'DEFAULT',
    description: 'Broad non-intersection specific configuration parameters',
  },
  {
    label: 'Intersection',
    value: 'INTERSECTION',
    description: 'Intersection specific configurable configuration parameters',
  },
  {
    label: 'Debug',
    value: 'DEBUG',
    description: 'Signal state configuration parameters',
  },
  {
    label: 'Read Only',
    value: 'READ_ONLY',
    description: 'Signal state configuration parameters',
  },
  {
    label: 'All',
    value: 'all',
    description: 'All Configuration Parameters',
  },
]

const applyFilters = (
  parameters: Config[],
  filter: {
    query: string
    tab: string
  }
) =>
  parameters.filter((parameter) => {
    if (filter.query) {
      let queryMatched = false
      const properties = ['key', 'category', 'description']
      properties.forEach((property) => {
        if (parameter[property]?.toLowerCase().includes(filter.query.toLowerCase())) {
          queryMatched = true
        }
      })

      if (!queryMatched) {
        return false
      }
    }

    if (filter.tab === 'all') {
      return true
    } else if (filter.tab === 'GENERAL') {
      return parameter['updateType'] !== 'READ_ONLY' && !parameter['key'].includes('debug')
    } else if (filter.tab === 'DEBUG') {
      return parameter['key'].includes('debug')
    } else {
      return parameter['updateType'] == filter.tab && !parameter['key'].includes('debug')
    }
  })

const applyPagination = (parameters, page, rowsPerPage) =>
  parameters.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)

const Page = () => {
  const [currentTab, setCurrentTab] = useState('GENERAL')
  const [page, setPage] = useState(0)
  const [rowsPerPage, setRowsPerPage] = useState(10)
  const [currentDescription, setCurrentDescription] = useState('')
  const intersectionId = useSelector(selectSelectedIntersectionId)
  const [filter, setFilter] = useState({
    query: '',
    tab: currentTab,
  })

  const theme = useTheme()

  const { data: parameters, refetch } = useGetIntersectionParametersQuery(intersectionId)

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

  const handleQueryChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setFilter((prevState) => ({
      ...prevState,
      query: event.target.value,
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
  const filteredParameters = applyFilters(parameters ?? [], filter)
  const paginatedParameters = applyPagination(filteredParameters, page, rowsPerPage)

  return (
    <Routes>
      <Route
        path="/"
        element={
          <>
            <Box
              component="main"
              sx={{
                flexGrow: 1,
              }}
            >
              <Container
                maxWidth={false}
                sx={{
                  backgroundColor: theme.palette.background.paper,
                  marginTop: theme.spacing(3),
                  borderRadius: '4px',
                }}
                disableGutters
              >
                <Card>
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
                        onSubmit={(e) => {
                          e.preventDefault()
                          // Handle form submission if needed
                        }}
                        sx={{
                          flexGrow: 1,
                          m: 1.5,
                        }}
                      >
                        <TextField
                          defaultValue=""
                          variant="standard"
                          onChange={handleQueryChange}
                          slotProps={{
                            input: {
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
                        onClick={refetch}
                        startIcon={<RefreshIcon fontSize="small" />}
                        sx={{ m: 1 }}
                        className="museo-slab capital-case"
                      >
                        Refresh
                      </Button>
                    </Box>
                  </Box>

                  <ConfigParamListTable
                    intersectionId={intersectionId}
                    parameters={paginatedParameters}
                    parametersCount={filteredParameters.length}
                    onPageChange={handlePageChange}
                    onRowsPerPageChange={handleRowsPerPageChange}
                    rowsPerPage={rowsPerPage}
                    page={page}
                  />
                </Card>
              </Container>
            </Box>
          </>
        }
      />
      <Route path=":key/edit" element={<ConfigParamEdit />} />
      <Route path=":key/create" element={<ConfigParamCreate />} />
      <Route path=":key/remove" element={<ConfigParamRemove />} />
      <Route
        path="*"
        element={
          <NotFound
            redirectRoute="/dashboard/intersectionDashboard/configuration"
            redirectRouteName="Configuration Page"
            offsetHeight={319}
            description="This page does not exist. Please return to the admin RSU page."
          />
        }
      />
    </Routes>
  )
}

export default Page
