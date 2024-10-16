import React, { useEffect } from 'react'
import AdminAddOrganization from '../adminAddOrganization/AdminAddOrganization'
import AdminOrganizationTabRsu from '../adminOrganizationTabRsu/AdminOrganizationTabRsu'
import AdminOrganizationTabIntersection from '../adminOrganizationTabIntersection/AdminOrganizationTabIntersection'
import AdminOrganizationTabUser from '../adminOrganizationTabUser/AdminOrganizationTabUser'
import AdminEditOrganization from '../adminEditOrganization/AdminEditOrganization'
import AdminOrganizationDeleteMenu from '../../components/AdminOrganizationDeleteMenu'
import { IoChevronBackCircleOutline, IoRefresh } from 'react-icons/io5'
import { AiOutlinePlusCircle } from 'react-icons/ai'
import Grid from '@mui/material/Grid'
import EditIcon from '@mui/icons-material/Edit'
import { DropdownList } from 'react-widgets'
import {
  selectOrgData,
  selectSelectedOrg,
  selectSelectedOrgName,
  selectSelectedOrgEmail,
  selectRsuTableData,
  selectIntersectionTableData,
  selectUserTableData,

  // actions
  deleteOrg,
  getOrgData,
  updateTitle,
  setSelectedOrg,
  AdminOrgSummary,
} from './adminOrganizationTabSlice'
import { useSelector, useDispatch } from 'react-redux'

import '../adminRsuTab/Admin.css'
import { RootState } from '../../store'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { Route, Routes, useLocation, useNavigate } from 'react-router-dom'
import { NotFound } from '../../pages/404'
import toast from 'react-hot-toast'
import { changeOrganization, selectOrganizationName, setOrganizationList } from '../../generalSlices/userSlice'

const getTitle = (activeTab: string) => {
  if (activeTab === undefined) {
    return 'CV Manager Organizations'
  } else if (activeTab === 'editOrganization') {
    return ''
  } else if (activeTab === 'addOrganization') {
    return ''
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
  const selectedOrgEmail = useSelector(selectSelectedOrgEmail)
  const rsuTableData = useSelector(selectRsuTableData)
  const intersectionTableData = useSelector(selectIntersectionTableData)
  const userTableData = useSelector(selectUserTableData)

  const notifySuccess = (message: string) => toast.success(message)
  const notifyError = (message: string) => toast.error(message)
  const defaultOrgName = useSelector(selectOrganizationName)
  var defaultOrgData = orgData.find((org) => org.name === defaultOrgName)

  useEffect(() => {
    dispatch(getOrgData({ orgName: 'all', all: true, specifiedOrg: undefined })).then(() => {
      // on first render set the default organization in the admin
      // organization tab to the currently selected organization
      if (defaultOrgData) {
        const selectedOrg = (orgData ?? []).find(
          (organization: AdminOrgSummary) => organization?.name === defaultOrgName
        )
        dispatch(setSelectedOrg(selectedOrg))
        defaultOrgData = null
      }
    })
  }, [dispatch])

  const getAllOrgData = () => {
    dispatch(getOrgData({ orgName: 'all', all: true, specifiedOrg: undefined })).then((data: any | undefined) => {
      if (data !== undefined && !data.payload?.success) {
        notifyError('Failed to obtain organizations due to error: ' + data.payload?.message)
      }
    })
  }

  const getSelectedOrgData = () => {
    dispatch(getOrgData({ orgName: selectedOrgName })).then((data: any) => {
      if (data !== undefined && !data.payload?.success) {
        notifyError('Failed to obtain data due to error: ' + data.payload?.message)
      }
    })
  }

  useEffect(() => {
    getAllOrgData()
  }, [dispatch])

  const updateTableData = (orgName: string) => {
    dispatch(getOrgData({ orgName })).then((data: any) => {
      if (!data.payload.success) {
        notifyError('Failed to obtain data due to error: ' + data.payload.message)
      }
    })
  }

  useEffect(() => {
    getSelectedOrgData()
  }, [selectedOrgName, dispatch])

  useEffect(() => {
    dispatch(updateTitle())
  }, [activeTab, dispatch])

  const refresh = () => {
    updateTableData(selectedOrgName)
  }

  const handleOrgDelete = (orgName) => {
    dispatch(deleteOrg(orgName)).then((data: any) => {
      data.payload.success
        ? notifySuccess(data.payload.message)
        : notifyError('Failed to delete organization due to error: ' + data.payload.message)
    })
    dispatch(setOrganizationList({ value: { name: orgName }, type: 'delete' }))
    dispatch(changeOrganization(orgData[0].name))
  }

  return (
    <div>
      <div>
        <h3 className="panel-header">
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
                    onClick={(_) => navigate('editOrganization/' + selectedOrg?.name)}
                    title="Edit Organization"
                  >
                    <EditIcon size={20} component={undefined} style={{ color: 'white' }} />
                  </button>
                </Grid>
                <Grid item xs={0}>
                  <AdminOrganizationDeleteMenu
                    deleteOrganization={() => handleOrgDelete(selectedOrgName)}
                    selectedOrganization={selectedOrgName}
                  />
                </Grid>
              </Grid>

              <div className="scroll-div-org-tab">
                <>
                  <AdminOrganizationTabRsu
                    selectedOrg={selectedOrgName}
                    selectedOrgEmail={selectedOrgEmail}
                    updateTableData={updateTableData}
                    tableData={rsuTableData}
                    key="rsu"
                  />
                  <AdminOrganizationTabIntersection
                    selectedOrg={selectedOrgName}
                    selectedOrgEmail={selectedOrgEmail}
                    updateTableData={updateTableData}
                    tableData={intersectionTableData}
                    key="intersection"
                  />
                  <AdminOrganizationTabUser
                    selectedOrg={selectedOrgName}
                    selectedOrgEmail={selectedOrgEmail}
                    updateTableData={updateTableData}
                    tableData={userTableData}
                    key="user"
                  />
                </>
              </div>
            </div>
          }
        />
        <Route path="addOrganization" element={<AdminAddOrganization />} />
        <Route path="editOrganization/:orgName" element={<AdminEditOrganization />} />
        <Route
          path="*"
          element={
            <NotFound
              redirectRoute="/dashboard/admin/organization"
              redirectRouteName="Admin Organization Page"
              offsetHeight={319}
              description="This page does not exist. Please return to the admin organization page."
            />
          }
        />
      </Routes>
    </div>
  )
}

export default AdminOrganizationTab
