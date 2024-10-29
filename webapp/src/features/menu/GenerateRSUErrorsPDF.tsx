import React, { useRef, useState } from 'react'
import { useReactToPrint } from 'react-to-print'
import Button from '@mui/material/Button'
import { StyledEngineProvider, ThemeProvider } from '@mui/material'
import { theme } from '../../styles'
import AdminTable from '../../components/AdminTable'
import MaterialTable from '@material-table/core'

const GenerateRSUErrorsPDF = (props) => {
  const contentRef = useRef(null)
  const errorRef = useRef(null)
  const handlePrint = useReactToPrint({ contentRef })
  const handleErrorPrint = useReactToPrint({ contentRef: errorRef })

  return (
    <StyledEngineProvider injectFirst>
      <ThemeProvider theme={theme}>
        <div className={'containerDiv'}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1rem' }}>
            <Button
              style={{ color: 'white' }}
              onClick={() => {
                handlePrint()
              }}
            >
              Print Full Report
            </Button>
            <Button
              style={{ color: 'white' }}
              onClick={() => {
                handleErrorPrint()
              }}
            >
              Print Error Report
            </Button>
          </div>
          <div style={{ display: 'none' }}>
            <div
              ref={contentRef}
              style={{
                margin: '50px',
                fontFamily: 'Arial Helvetica Sans-Serif',
                height: '100vh',
              }}
            >
              <h1 style={{ textAlign: 'center', marginBottom: '10px' }}>RSU Summary</h1>
              <br />
              <p>Below is the generated RSU summary report for all RSUs at {new Date().toISOString()} UTC:</p>
              <div style={{ marginTop: '25px' }}>
                <MaterialTable
                  columns={[
                    {
                      title: 'RSU',
                      field: 'rsu',
                      render: (rowData) => <p style={{ color: 'black', fontWeight: 'bold' }}>{rowData.rsu}</p>,
                    },
                    {
                      title: 'Road',
                      field: 'road',
                      render: (rowData) => <p style={{ color: 'black', fontWeight: 'bold' }}>{rowData.road}</p>,
                    },
                    {
                      title: 'Online Status',
                      field: 'online_status',
                      render: (rowData) => (
                        <p
                          style={
                            rowData.online_status.includes('RSU Offline')
                              ? {
                                  color: '#B60202',
                                  fontWeight: 'bold',
                                }
                              : {
                                  color: '#2B6510',
                                  fontWeight: 'bold',
                                }
                          }
                        >
                          {rowData.online_status}
                        </p>
                      ),
                    },
                    {
                      title: 'SCMS Status',
                      field: 'scms_status',
                      render: (rowData) => (
                        <p
                          style={
                            rowData.scms_status.includes('SCMS Healthy')
                              ? {
                                  color: '#2B6510',
                                  fontWeight: 'bold',
                                }
                              : {
                                  color: '#B60202',
                                  fontWeight: 'bold',
                                }
                          }
                        >
                          {rowData.scms_status}
                        </p>
                      ),
                    },
                  ]}
                  actions={[]}
                  data={props.rows}
                  title=""
                  options={{
                    toolbar: false,
                    search: false,
                    paging: false,
                  }}
                  style={{
                    backgroundColor: 'white',
                    color: 'black',
                    fontFamily: 'Arial Helvetica Sans-Serif',
                    border: 'none',
                  }}
                />
              </div>
            </div>
            <div
              ref={errorRef}
              style={{
                margin: '50px',
                fontFamily: 'Arial Helvetica Sans-Serif',
                height: '100vh',
              }}
            >
              <h1 style={{ textAlign: 'center', marginBottom: '10px' }}>RSU Error Summary</h1>
              <br />
              <p>Below is the generated RSU Error summary report for all RSUs at {new Date().toISOString()} UTC:</p>
              <div style={{ marginTop: '25px' }}>
                <MaterialTable
                  columns={[
                    {
                      title: 'RSU',
                      field: 'rsu',
                      render: (rowData) => <p style={{ color: 'black', fontWeight: 'bold' }}>{rowData.rsu}</p>,
                    },
                    {
                      title: 'Road',
                      field: 'road',
                      render: (rowData) => <p style={{ color: 'black', fontWeight: 'bold' }}>{rowData.road}</p>,
                    },
                    {
                      title: 'Online Status',
                      field: 'online_status',
                      render: (rowData) => (
                        <p
                          style={
                            rowData.online_status.includes('RSU Offline')
                              ? {
                                  color: '#B60202',
                                  fontWeight: 'bold',
                                }
                              : {
                                  color: '#2B6510',
                                  fontWeight: 'bold',
                                }
                          }
                        >
                          {rowData.online_status}
                        </p>
                      ),
                    },
                    {
                      title: 'SCMS Status',
                      field: 'scms_status',
                      render: (rowData) => (
                        <p
                          style={
                            rowData.scms_status.includes('SCMS Healthy')
                              ? {
                                  color: '#2B6510',
                                  fontWeight: 'bold',
                                }
                              : {
                                  color: '#B60202',
                                  fontWeight: 'bold',
                                }
                          }
                        >
                          {rowData.scms_status}
                        </p>
                      ),
                    },
                  ]}
                  actions={[]}
                  data={
                    props.rows !== undefined
                      ? props.rows.filter(
                          (row) =>
                            row.online_status.includes('RSU Offline') || row.scms_status.includes('SCMS Unhealthy')
                        )
                      : []
                  }
                  title=""
                  options={{
                    toolbar: false,
                    search: false,
                    paging: false,
                  }}
                  style={{
                    backgroundColor: 'white',
                    color: 'black',
                    fontFamily: 'Arial Helvetica Sans-Serif',
                    border: 'none',
                  }}
                />
              </div>
            </div>
          </div>
        </div>
      </ThemeProvider>
    </StyledEngineProvider>
  )
}

export default GenerateRSUErrorsPDF
