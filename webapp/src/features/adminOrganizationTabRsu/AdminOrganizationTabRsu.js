import React, { useState, useEffect } from 'react'
import AdminTable from '../../components/AdminTable'
import EnvironmentVars from '../../EnvironmentVars'
import { AiOutlinePlusCircle } from 'react-icons/ai'
import { ThemeProvider, createTheme } from '@mui/material'
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
import { useSelector, useDispatch } from 'react-redux'

import '../adminRsuTab/Admin.css'

const AdminOrganizationTabRsu = (props) => {
  const { selectedOrg, orgPatchJson, fetchPatchOrganization, updateTableData } = props
  const dispatch = useDispatch()

  const availableRsuList = useSelector(selectAvailableRsuList)
  const selectedRsuList = useSelector(selectSelectedRsuList)
  const loadingGlobal = useSelector(selectLoadingGlobal)
  const [rsuColumns] = useState([
    { title: 'IP Address', field: 'ip', id: 0, width: '31%' },
    { title: 'Primary Route', field: 'primary_route', id: 1, width: '31%' },
    { title: 'Milepost', field: 'milepost', id: 2, width: '31%' },
  ])

  let rsuActions = [
    {
      icon: 'delete',
      tooltip: 'Remove From Organization',
      position: 'row',
      onClick: (event, rowData) => {
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
      onClick: (event, rowData) => {
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
  }, [, selectedOrg])

  const rsuOnDelete = async (rsu) => {
    dispatch(rsuDeleteSingle({ rsu, orgPatchJson, selectedOrg, fetchPatchOrganization, updateTableData }))
  }

  const rsuMultiDelete = async (rows) => {
    dispatch(rsuDeleteMultiple({ rows, orgPatchJson, selectedOrg, fetchPatchOrganization, updateTableData }))
  }

  const rsuMultiAdd = async (rsuList) => {
    dispatch(rsuAddMultiple({ rsuList, orgPatchJson, selectedOrg, fetchPatchOrganization, updateTableData }))
  }

  const accordionTheme = createTheme({
    palette: {
      text: {
        primary: '#ffffff',
        secondary: '#ffffff',
        disabled: '#ffffff',
        hint: '#ffffff',
      },
      divider: '#333',
      background: {
        paper: '#0e2052',
      },
    },
  })

  const innerAccordionTheme = createTheme({
    palette: {
      text: {
        primary: '#fff',
        secondary: '#fff',
        disabled: '#fff',
        hint: '#fff',
      },
      divider: '#333',
      background: {
        paper: '#333',
      },
    },
  })

  return (
    <div className="accordion">
      <ThemeProvider theme={accordionTheme}>
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
                <ThemeProvider theme={innerAccordionTheme}>
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
    </div>
  )
}

export default AdminOrganizationTabRsu
