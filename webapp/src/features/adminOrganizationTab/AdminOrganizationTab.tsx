import React, { useEffect } from 'react'
import AdminAddOrganization from '../adminAddOrganization/AdminAddOrganization'
import AdminOrganizationTabRsu from '../adminOrganizationTabRsu/AdminOrganizationTabRsu'
import AdminOrganizationTabIntersection from '../adminOrganizationTabIntersection/AdminOrganizationTabIntersection'
import AdminOrganizationTabUser from '../adminOrganizationTabUser/AdminOrganizationTabUser'
import AdminEditOrganization from '../adminEditOrganization/AdminEditOrganization'
import AdminOrganizationDeleteMenu from '../../components/AdminOrganizationDeleteMenu'
import Grid2 from '@mui/material/Grid2'
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
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../store'
import { Route, Routes, useLocation, useNavigate } from 'react-router-dom'
import { NotFound } from '../../pages/404'
import toast from 'react-hot-toast'
import { changeOrganization, selectOrganizationName, setOrganizationList } from '../../generalSlices/userSlice'
import { ConditionalRenderIntersection, ConditionalRenderRsu } from '../../feature-flags'
import { ContainedIconButton } from '../../styles/components/ContainedIconButton'
import { alpha, Button, useTheme } from '@mui/material'
import { AddCircleOutline, EditOutlined, Refresh } from '@mui/icons-material'

const AdminOrganizationTab = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const navigate = useNavigate()
  const theme = useTheme()
  const location = useLocation()

  const activeTab = location.pathname.split('/')[4]

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
  let defaultOrgData = orgData.find((org) => org.name === defaultOrgName)

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
      if (data.payload.success) {
        notifySuccess(data.payload.message)
      } else {
        notifyError('Failed to delete organization due to error: ' + data.payload.message)
      }
    })
    dispatch(setOrganizationList({ value: { name: orgName }, type: 'delete' }))
    dispatch(changeOrganization(orgData[0].name))
  }

  return (
    <Routes>
      <Route
        path="/"
        element={
          <div style={{ backgroundColor: theme.palette.background.paper, height: 'fit-content', padding: '10px 0px' }}>
            <div style={{ display: 'flex', flexDirection: 'row', margin: '10px' }}>
              <Grid2
                sx={{
                  display: 'flex',
                  flexDirection: 'row',
                  width: '70%',
                  justifyContent: 'flex-start',
                  alignItems: 'center',
                }}
              >
                <Grid2 size={{ xs: 0 }} sx={{ marginLeft: '10px' }}>
                  <DropdownList
                    style={{ width: '250px' }}
                    dataKey="name"
                    textField="name"
                    data={orgData}
                    value={selectedOrg}
                    onChange={(value) => dispatch(setSelectedOrg(value))}
                  />
                </Grid2>
                <Grid2 size={{ xs: 0 }} sx={{ marginLeft: '10px' }}>
                  <ContainedIconButton
                    key="delete_button"
                    title="Edit Organization"
                    onClick={() => navigate('editOrganization/' + selectedOrg?.name)}
                    sx={{
                      backgroundColor: 'transparent',
                      borderRadius: '2px',
                      '&:hover': {
                        backgroundColor: alpha(theme.palette.text.primary, 0.1),
                      },
                    }}
                  >
                    <EditOutlined size={20} sx={{ color: theme.palette.custom.rowActionIcon }} component={undefined} />
                  </ContainedIconButton>
                </Grid2>
                <Grid2 size={{ xs: 0 }} sx={{ marginLeft: '10px' }}>
                  <AdminOrganizationDeleteMenu
                    deleteOrganization={() => handleOrgDelete(selectedOrgName)}
                    selectedOrganization={selectedOrgName}
                  />
                </Grid2>
              </Grid2>
              <Grid2
                sx={{
                  display: 'flex',
                  flexDirection: 'row',
                  width: '30%',
                  justifyContent: 'flex-end',
                  alignItems: 'center',
                }}
              >
                {activeTab === undefined && [
                  <Grid2 size={{ xs: 0 }} sx={{ marginRight: '10px' }}>
                    <Button
                      key="refresh_button"
                      title="Refresh Organizations"
                      onClick={() => refresh()}
                      variant="outlined"
                      color="info"
                      size="small"
                      className="museo-slab capital-case"
                      startIcon={<Refresh />}
                    >
                      Refresh
                    </Button>
                  </Grid2>,
                  <Grid2 size={{ xs: 0 }} sx={{ marginRight: '10px' }}>
                    <Button
                      key="plus_button"
                      title="Add Organization"
                      onClick={() => navigate('addOrganization')}
                      startIcon={<AddCircleOutline />}
                      variant="contained"
                      size="small"
                      className="museo-slab capital-case"
                    >
                      New
                    </Button>
                  </Grid2>,
                ]}
              </Grid2>
            </div>

            <div className="scroll-div-org-tab">
              <>
                <ConditionalRenderRsu>
                  <AdminOrganizationTabRsu
                    selectedOrg={selectedOrgName}
                    selectedOrgEmail={selectedOrgEmail}
                    updateTableData={updateTableData}
                    tableData={rsuTableData}
                    key="rsu"
                  />
                </ConditionalRenderRsu>
                <ConditionalRenderIntersection>
                  <AdminOrganizationTabIntersection
                    selectedOrg={selectedOrgName}
                    selectedOrgEmail={selectedOrgEmail}
                    updateTableData={updateTableData}
                    tableData={intersectionTableData}
                    key="intersection"
                  />
                </ConditionalRenderIntersection>
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
  )
}

export default AdminOrganizationTab
