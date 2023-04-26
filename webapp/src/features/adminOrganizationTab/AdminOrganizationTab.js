import React, { useEffect } from 'react'
import AdminAddOrganization from '../adminAddOrganization/AdminAddOrganization'
import AdminOrganizationTabRsu from '../adminOrganizationTabRsu/AdminOrganizationTabRsu'
import AdminOrganizationTabUser from '../adminOrganizationTabUser/AdminOrganizationTabUser'
import AdminEditOrganization from '../adminEditOrganization/AdminEditOrganization'
import AdminOrganizationDeleteMenu from '../../components/AdminOrganizationDeleteMenu'
import { IoChevronBackCircleOutline, IoRefresh } from 'react-icons/io5'
import { AiOutlinePlusCircle } from 'react-icons/ai'
import Grid from '@mui/material/Grid'
import EditIcon from '@mui/icons-material/Edit'
import { DropdownList } from 'react-widgets'
import {
  selectActiveDiv,
  selectTitle,
  selectOrgData,
  selectSelectedOrg,
  selectRsuTableData,
  selectUserTableData,
  selectErrorState,
  selectErrorMsg,

  // actions
  editOrg,
  deleteOrg,
  getOrgData,
  updateTitle,
  setActiveDiv,
  setSelectedOrg,
} from './adminOrganizationTabSlice'
import { useSelector, useDispatch } from 'react-redux'

import '../adminRsuTab/Admin.css'

const AdminOrganizationTab = (props) => {
  const dispatch = useDispatch()
  const activeDiv = useSelector(selectActiveDiv)
  const title = useSelector(selectTitle)
  const orgData = useSelector(selectOrgData)
  const selectedOrg = useSelector(selectSelectedOrg)
  const rsuTableData = useSelector(selectRsuTableData)
  const userTableData = useSelector(selectUserTableData)
  const errorState = useSelector(selectErrorState)
  const errorMsg = useSelector(selectErrorMsg)

  let orgPatchJson = {
    orig_name: selectedOrg.name,
    name: selectedOrg.name,
    users_to_add: [],
    users_to_modify: [],
    users_to_remove: [],
    rsus_to_add: [],
    rsus_to_remove: [],
  }

  const updateOrgData = async (specifiedOrg) => {
    dispatch(getOrgData({ orgName: 'all', all: true, specifiedOrg }))
  }

  useEffect(() => {
    updateOrgData()
  }, [])

  const updateTableData = (orgName) => {
    dispatch(getOrgData({ orgName }))
  }

  useEffect(() => {
    updateTableData(selectedOrg.name)
  }, [selectedOrg])

  useEffect(() => {
    dispatch(updateTitle())
  }, [activeDiv])

  const editOrganization = (json) => dispatch(editOrg(json))

  const refresh = () => {
    updateTableData(selectedOrg.name)
  }

  return (
    <div>
      <div>
        <h3 className="panel-header">
          {activeDiv !== 'organization_table' && (
            <button
              key="org_table"
              className="admin_table_button"
              onClick={() => {
                dispatch(setActiveDiv('organization_table'))
              }}
            >
              <IoChevronBackCircleOutline size={20} />
            </button>
          )}
          {title}
          {activeDiv === 'organization_table' && [
            <button
              key="plus_button"
              className="plus_button"
              onClick={() => {
                dispatch(setActiveDiv('add_organization'))
              }}
              title="Add Organization"
            >
              <AiOutlinePlusCircle size={20} />
            </button>,
            <button
              key="refresh_button"
              className="plus_button"
              onClick={() => {
                refresh()
              }}
              title="Refresh Organizations"
            >
              <IoRefresh size={20} />
            </button>,
          ]}
        </h3>
      </div>

      {errorState && <p className="error-msg">Failed to obtain data due to error: {errorMsg}</p>}

      {activeDiv === 'organization_table' && (
        <div>
          <Grid container>
            <Grid item xs={0}>
              <DropdownList
                style={{ width: '250px' }}
                className="form-dropdown"
                dataKey="name"
                textField="name"
                data={orgData}
                value={selectedOrg}
                onChange={(value) => dispatch(setSelectedOrg(value))}
              />
            </Grid>
            <Grid item xs={0}>
              <button
                className="delete_button"
                onClick={(value) => dispatch(setActiveDiv('edit_organization'))}
                title="Edit Organization"
              >
                <EditIcon size={20} />
              </button>
            </Grid>
            <Grid item xs={0}>
              <AdminOrganizationDeleteMenu
                deleteOrganization={() => dispatch(deleteOrg(selectedOrg.name))}
                selectedOrganization={selectedOrg.name}
              />
            </Grid>
          </Grid>

          <div className="scroll-div-org-tab">
            {activeDiv === 'organization_table' && [
              <AdminOrganizationTabRsu
                selectedOrg={selectedOrg.name}
                orgPatchJson={orgPatchJson}
                fetchPatchOrganization={editOrganization}
                updateTableData={updateTableData}
                tableData={rsuTableData}
                key="rsu"
              />,
              <AdminOrganizationTabUser
                selectedOrg={selectedOrg.name}
                orgPatchJson={orgPatchJson}
                fetchPatchOrganization={editOrganization}
                updateTableData={updateTableData}
                tableData={userTableData}
                key="user"
              />,
            ]}
          </div>
        </div>
      )}

      {activeDiv === 'add_organization' && (
        <div className="scoll-div">
          <AdminAddOrganization updateOrganizationData={updateOrgData} />
        </div>
      )}

      {activeDiv === 'edit_organization' && (
        <div className="scoll-div">
          <AdminEditOrganization selectedOrg={selectedOrg.name} updateOrganizationData={updateOrgData} />
        </div>
      )}
    </div>
  )
}

export default AdminOrganizationTab
