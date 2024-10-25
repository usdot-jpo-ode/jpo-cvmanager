import React, { useState, useEffect } from 'react'
import AdminTable from '../../components/AdminTable'
import { AiOutlinePlusCircle } from 'react-icons/ai'
import { ThemeProvider, StyledEngineProvider } from '@mui/material'
import Accordion from '@mui/material/Accordion'
import AccordionSummary from '@mui/material/AccordionSummary'
import AccordionDetails from '@mui/material/AccordionDetails'
import Typography from '@mui/material/Typography'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import { Multiselect } from 'react-widgets'
import { confirmAlert } from 'react-confirm-alert'
import { Options } from '../../components/AdminDeletionOptions'
import {
  selectAvailableIntersectionList,
  selectSelectedIntersectionList,

  // actions
  setSelectedIntersectionList,
  getIntersectionData,
  intersectionDeleteSingle,
  intersectionDeleteMultiple,
  intersectionAddMultiple,
} from './adminOrganizationTabIntersectionSlice'
import { selectLoadingGlobal } from '../../generalSlices/userSlice'
import { useSelector, useDispatch } from 'react-redux'

import '../adminIntersectionTab/Admin.css'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../store'
import { Action, Column } from '@material-table/core'
import { AdminOrgIntersection } from '../adminOrganizationTab/adminOrganizationTabSlice'
import toast from 'react-hot-toast'

import { accordionTheme, outerAccordionTheme } from '../../styles'

interface AdminOrganizationTabIntersectionProps {
  selectedOrg: string
  selectedOrgEmail: string
  tableData: AdminOrgIntersection[]
  updateTableData: (orgname: string) => void
}

const AdminOrganizationTabIntersection = (props: AdminOrganizationTabIntersectionProps) => {
  const { selectedOrg, selectedOrgEmail, updateTableData } = props
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()

  const availableIntersectionList = useSelector(selectAvailableIntersectionList)
  const selectedIntersectionList = useSelector(selectSelectedIntersectionList)
  const loadingGlobal = useSelector(selectLoadingGlobal)
  const [intersectionColumns] = useState<Column<any>[]>([
    { title: 'ID', field: 'intersection_id', id: 0, width: '31%' },
    { title: 'Name', field: 'intersection_name', id: 1, width: '31%' },
  ])

  let intersectionActions: Action<AdminOrgIntersection>[] = [
    {
      icon: 'delete',
      tooltip: 'Remove From Organization',
      position: 'row',
      onClick: (event, rowData: AdminOrgIntersection) => {
        const buttons = [
          { label: 'Yes', onClick: () => intersectionOnDelete(rowData) },
          { label: 'No', onClick: () => {} },
        ]
        const alertOptions = Options(
          'Delete Intersection',
          'Are you sure you want to delete "' + rowData.intersection_id + '" from ' + selectedOrg + ' organization?',
          buttons
        )
        confirmAlert(alertOptions)
      },
    },
    {
      tooltip: 'Remove All Selected From Organization',
      icon: 'delete',
      onClick: (event, rowData: AdminOrgIntersection[]) => {
        const buttons = [
          { label: 'Yes', onClick: () => intersectionMultiDelete(rowData) },
          { label: 'No', onClick: () => {} },
        ]
        const alertOptions = Options(
          'Delete Selected Intersections',
          'Are you sure you want to delete ' + rowData.length + ' Intersections from ' + selectedOrg + ' organization?',
          buttons
        )
        confirmAlert(alertOptions)
      },
    },
  ]

  useEffect(() => {
    dispatch(setSelectedIntersectionList([]))
    dispatch(getIntersectionData(selectedOrg))
  }, [selectedOrg, dispatch])

  const intersectionOnDelete = async (intersection: AdminOrgIntersection) => {
    dispatch(intersectionDeleteSingle({ intersection, selectedOrg, selectedOrgEmail, updateTableData })).then(
      (data) => {
        if (!(data.payload as any).success) {
          toast.error((data.payload as any).message)
        } else {
          toast.success((data.payload as any).message)
        }
      }
    )
  }

  const intersectionMultiDelete = async (rows: AdminOrgIntersection[]) => {
    dispatch(intersectionDeleteMultiple({ rows, selectedOrg, selectedOrgEmail, updateTableData })).then((data) => {
      if (!(data.payload as any).success) {
        toast.error((data.payload as any).message)
      } else {
        toast.success((data.payload as any).message)
      }
    })
  }

  const intersectionMultiAdd = async (intersectionList: AdminOrgIntersection[]) => {
    dispatch(intersectionAddMultiple({ intersectionList, selectedOrg, selectedOrgEmail, updateTableData })).then(
      (data) => {
        if (!(data.payload as any).success) {
          toast.error((data.payload as any).message)
        } else {
          toast.success((data.payload as any).message)
        }
      }
    )
  }

  return (
    <div className="accordion">
      <StyledEngineProvider injectFirst>
        <ThemeProvider theme={outerAccordionTheme}>
          <Accordion className="accordion-content">
            <AccordionSummary
              expandIcon={<ExpandMoreIcon className="expand" />}
              aria-controls="panel1a-content"
              id="panel1a-header"
            >
              <Typography style={{ fontSize: '18px' }}>{selectedOrg} Intersections</Typography>
            </AccordionSummary>
            <AccordionDetails>
              {loadingGlobal === false && [
                <div className="accordion" key="accordion">
                  <StyledEngineProvider injectFirst>
                    <ThemeProvider theme={accordionTheme}>
                      <Accordion>
                        <AccordionSummary
                          expandIcon={<ExpandMoreIcon className="expand" />}
                          aria-controls="panel1a-content"
                          id="panel1a-header"
                        >
                          <Typography>Add Intersections to {selectedOrg}</Typography>
                        </AccordionSummary>
                        <AccordionDetails>
                          <div className="spacer-large-intersection">
                            <Multiselect
                              className="org-multiselect"
                              dataKey="id"
                              textField="intersection_id"
                              placeholder="Click to add Intersections"
                              data={availableIntersectionList}
                              value={selectedIntersectionList}
                              onChange={(value) => {
                                dispatch(setSelectedIntersectionList(value))
                              }}
                            />

                            <button
                              key="intersection_plus_button"
                              className="admin-button"
                              onClick={() => intersectionMultiAdd(selectedIntersectionList)}
                              title="Add Intersections To Organization"
                            >
                              <AiOutlinePlusCircle size={20} />
                            </button>
                          </div>
                        </AccordionDetails>
                      </Accordion>
                    </ThemeProvider>
                  </StyledEngineProvider>
                </div>,
                <div key="adminTable">
                  <AdminTable
                    title={'Modify Intersection-Organization Assignment'}
                    data={props.tableData}
                    columns={intersectionColumns}
                    actions={intersectionActions}
                  />
                </div>,
              ]}
            </AccordionDetails>
          </Accordion>
        </ThemeProvider>
      </StyledEngineProvider>
    </div>
  )
}

export default AdminOrganizationTabIntersection
