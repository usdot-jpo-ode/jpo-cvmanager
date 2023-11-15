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
  selectSelectedOrgName,
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
import { RootState } from '../../store'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'

const AdminOrganizationTab = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const activeDiv = useSelector(selectActiveDiv)
  const title = useSelector(selectTitle)
  const orgData = useSelector(selectOrgData)
  const selectedOrg = useSelector(selectSelectedOrg)
  const selectedOrgName = useSelector(selectSelectedOrgName)
  const rsuTableData = useSelector(selectRsuTableData)
  const userTableData = useSelector(selectUserTableData)
  const errorState = useSelector(selectErrorState)
  const errorMsg = useSelector(selectErrorMsg)

  const updateOrgData = async (specifiedOrg: string) => {
    dispatch(getOrgData({ orgName: 'all', all: true, specifiedOrg }))
  }

  useEffect(() => {
    dispatch(getOrgData({ orgName: 'all', all: true, specifiedOrg: undefined }))
  }, [dispatch])

  const updateTableData = (orgName: string) => {
    dispatch(getOrgData({ orgName }))
  }

  useEffect(() => {
    dispatch(getOrgData({ orgName: selectedOrgName }))
  }, [selectedOrgName, dispatch])

  useEffect(() => {
    dispatch(updateTitle())
  }, [activeDiv, dispatch])

  const refresh = () => {
    updateTableData(selectedOrgName)
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
                onClick={(_) => dispatch(setActiveDiv('edit_organization'))}
                title="Edit Organization"
              >
                <EditIcon size={20} component={undefined} />
              </button>
            </Grid>
            <Grid item xs={0}>
              <AdminOrganizationDeleteMenu
                deleteOrganization={() => dispatch(deleteOrg(selectedOrgName))}
                selectedOrganization={selectedOrgName}
              />
            </Grid>
          </Grid>

          <div className="scroll-div-org-tab">
            {activeDiv === 'organization_table' && [
              <AdminOrganizationTabRsu
                selectedOrg={selectedOrgName}
                updateTableData={updateTableData}
                tableData={rsuTableData}
                key="rsu"
              />,
              <AdminOrganizationTabUser
                selectedOrg={selectedOrgName}
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
          <AdminAddOrganization />
        </div>
      )}

      {activeDiv === 'edit_organization' && (
        <div className="scoll-div">
          <AdminEditOrganization />
        </div>
      )}
    </div>
  )
}

export default AdminOrganizationTab
