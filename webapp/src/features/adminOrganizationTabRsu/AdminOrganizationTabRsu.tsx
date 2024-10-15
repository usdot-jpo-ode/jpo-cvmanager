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
  selectAvailableRsuList,
  selectSelectedRsuList,

  // actions
  setSelectedRsuList,
  getRsuData,
  rsuDeleteSingle,
  rsuDeleteMultiple,
  rsuAddMultiple,
} from './adminOrganizationTabRsuSlice'
import { selectLoadingGlobal } from '../../generalSlices/userSlice'

import '../adminRsuTab/Admin.css'
import { Action, Column } from '@material-table/core'
import { AdminOrgRsu } from '../adminOrganizationTab/adminOrganizationTabSlice'
import toast from 'react-hot-toast'

import { accordionTheme, outerAccordionTheme } from '../../styles'
import { useAppDispatch, useAppSelector } from '../../hooks'

interface AdminOrganizationTabRsuProps {
  selectedOrg: string
  selectedOrgEmail: string
  tableData: AdminOrgRsu[]
  updateTableData: (orgname: string) => void
}

const AdminOrganizationTabRsu = (props: AdminOrganizationTabRsuProps) => {
  const { selectedOrg, selectedOrgEmail, updateTableData } = props
  const dispatch = useAppDispatch()

  const availableRsuList = useAppSelector(selectAvailableRsuList)
  const selectedRsuList = useAppSelector(selectSelectedRsuList)
  const loadingGlobal = useAppSelector(selectLoadingGlobal)
  const [rsuColumns] = useState<Column<any>[]>([
    { title: 'IP Address', field: 'ip', id: 0, width: '31%' },
    { title: 'Primary Route', field: 'primary_route', id: 1, width: '31%' },
    { title: 'Milepost', field: 'milepost', id: 2, width: '31%' },
  ])

  let rsuActions: Action<AdminOrgRsu>[] = [
    {
      icon: 'delete',
      tooltip: 'Remove From Organization',
      position: 'row',
      onClick: (event, rowData: AdminOrgRsu) => {
        const buttons = [
          { label: 'Yes', onClick: () => rsuOnDelete(rowData) },
          { label: 'No', onClick: () => {} },
        ]
        const alertOptions = Options(
          'Delete RSU',
          'Are you sure you want to delete "' + rowData.ip + '" from ' + selectedOrg + ' organization?',
          buttons
        )
        confirmAlert(alertOptions)
      },
    },
    {
      tooltip: 'Remove All Selected From Organization',
      icon: 'delete',
      onClick: (event, rowData: AdminOrgRsu[]) => {
        const buttons = [
          { label: 'Yes', onClick: () => rsuMultiDelete(rowData) },
          { label: 'No', onClick: () => {} },
        ]
        const alertOptions = Options(
          'Delete Selected RSUs',
          'Are you sure you want to delete ' + rowData.length + ' RSUs from ' + selectedOrg + ' organization?',
          buttons
        )
        confirmAlert(alertOptions)
      },
    },
  ]

  useEffect(() => {
    dispatch(setSelectedRsuList([]))
    dispatch(getRsuData(selectedOrg))
  }, [selectedOrg, dispatch])

  const rsuOnDelete = async (rsu: AdminOrgRsu) => {
    dispatch(rsuDeleteSingle({ rsu, selectedOrg, selectedOrgEmail, updateTableData })).then((data) => {
      if (!(data.payload as any).success) {
        toast.error((data.payload as any).message)
      } else {
        toast.success((data.payload as any).message)
      }
    })
  }

  const rsuMultiDelete = async (rows: AdminOrgRsu[]) => {
    dispatch(rsuDeleteMultiple({ rows, selectedOrg, selectedOrgEmail, updateTableData })).then((data) => {
      if (!(data.payload as any).success) {
        toast.error((data.payload as any).message)
      } else {
        toast.success((data.payload as any).message)
      }
    })
  }

  const rsuMultiAdd = async (rsuList: AdminOrgRsu[]) => {
    dispatch(rsuAddMultiple({ rsuList, selectedOrg, selectedOrgEmail, updateTableData })).then((data) => {
      if (!(data.payload as any).success) {
        toast.error((data.payload as any).message)
      } else {
        toast.success((data.payload as any).message)
      }
    })
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
              <Typography style={{ fontSize: '18px' }}>{selectedOrg} RSUs</Typography>
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
                          <Typography>Add RSUs to {selectedOrg}</Typography>
                        </AccordionSummary>
                        <AccordionDetails>
                          <div className="spacer-large-rsu">
                            <Multiselect
                              className="org-multiselect"
                              dataKey="id"
                              textField="ip"
                              placeholder="Click to add RSUs"
                              data={availableRsuList}
                              value={selectedRsuList}
                              onChange={(value) => {
                                dispatch(setSelectedRsuList(value))
                              }}
                            />

                            <button
                              key="rsu_plus_button"
                              className="admin-button"
                              onClick={() => rsuMultiAdd(selectedRsuList)}
                              title="Add RSUs To Organization"
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
                    title={'Modify RSU-Organization Assignment'}
                    data={props.tableData}
                    columns={rsuColumns}
                    actions={rsuActions}
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

export default AdminOrganizationTabRsu
