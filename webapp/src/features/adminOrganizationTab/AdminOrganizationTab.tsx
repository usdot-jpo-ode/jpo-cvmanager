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
import { Route, Routes, useLocation, useNavigate } from 'react-router-dom'
import AdminTable from '../../components/AdminTable'
import { NotFoundRedirect } from '../../pages/404'
import AdminAddRsu from '../adminAddRsu/AdminAddRsu'
import AdminEditRsu from '../adminEditRsu/AdminEditRsu'
import { setRouteNotFound } from '../../generalSlices/userSlice'

const getTitle = (activeTab: string) => {
  if (activeTab === undefined) {
    return 'CV Manager Organizations'
  } else if (activeTab === 'editOrganization') {
    return 'Edit Organization'
  } else if (activeTab === 'addOrganization') {
    return 'Add Organization'
  }
  return 'Unknown'
}

const AdminOrganizationTab = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const navigate = useNavigate()
  const location = useLocation()

  const activeTab = location.pathname.split('/')[4]
  const title = getTitle(activeTab)

  const orgData = useSelector(selectOrgData)
  const selectedOrg = useSelector(selectSelectedOrg)
  const selectedOrgName = useSelector(selectSelectedOrgName)
  const rsuTableData = useSelector(selectRsuTableData)
  const userTableData = useSelector(selectUserTableData)
  const errorState = useSelector(selectErrorState)
  const errorMsg = useSelector(selectErrorMsg)

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
  }, [activeTab, dispatch])

  const refresh = () => {
    updateTableData(selectedOrgName)
  }

  return (
    <div>
      <div>
        <h3 className="panel-header">
          {activeTab !== undefined && (
            <button key="org_table" className="admin_table_button" onClick={() => navigate('.')}>
              <IoChevronBackCircleOutline size={20} />
            </button>
          )}
          {title}
          {activeTab === undefined && [
            <button
              key="plus_button"
              className="plus_button"
              onClick={() => {
                navigate('addOrganization')
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

      {errorState && (
        <p className="error-msg" role="alert">
          Failed to obtain data due to error: {errorMsg}
        </p>
      )}

      <Routes>
        <Route
          path="/"
          element={
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
                    onClick={(_) => navigate('editOrganization/' + selectedOrg.name)}
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
                <>
                  <AdminOrganizationTabRsu
                    selectedOrg={selectedOrgName}
                    updateTableData={updateTableData}
                    tableData={rsuTableData}
                    key="rsu"
                  />
                  <AdminOrganizationTabUser
                    selectedOrg={selectedOrgName}
                    updateTableData={updateTableData}
                    tableData={userTableData}
                    key="user"
                  />
                </>
              </div>
            </div>
          }
        />
        <Route
          path="addOrganization"
          element={
            <div className="scroll-div-tab">
              <AdminAddOrganization />
            </div>
          }
        />
        <Route
          path="editOrganization/:orgName"
          element={
            <div className="scroll-div-tab">
              <AdminEditOrganization />
            </div>
          }
        />
        <Route path="*" element={<NotFoundRedirect />} />
      </Routes>
    </div>
  )
}

export default AdminOrganizationTab
