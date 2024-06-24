import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { selectToken, setOrganizationList } from '../../generalSlices/userSlice'
import EnvironmentVars from '../../EnvironmentVars'
import apiHelper from '../../apis/api-helper'
import { RootState } from '../../store'
import {
  AdminOrgTabUserAddMultiple,
  AdminOrgTabUserBulkEdit,
  AdminOrgUserDeleteMultiple,
} from './AdminOrganizationTabUserTypes'
import { ApiMsgRespWithCodes } from '../../apis/rsu-api-types'
import { adminOrgPatch, AdminOrgUser, editOrg } from '../adminOrganizationTab/adminOrganizationTabSlice'

const initialState = {
  availableUserList: [] as {
    id: number
    email: string
    role: string
  }[],
  selectedUserList: [] as AdminOrgUser[],
  availableRoles: [] as { role: string }[],
}

export const getUserData = async (
  email: string,
  token: string
): Promise<ApiMsgRespWithCodes<{ user_data: AdminOrgUser | AdminOrgUser[] }>> => {
  return await apiHelper._getDataWithCodes({
    url: EnvironmentVars.adminUser,
    token,
    query_params: { user_email: email },
    additional_headers: { 'Content-Type': 'application/json' },
  })
}

export const getAvailableRoles = createAsyncThunk(
  'adminOrganizationTabUser/getAvailableRoles',
  async (_, { getState }) => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    const data = await apiHelper._getDataWithCodes({
      url: EnvironmentVars.adminAddUser,
      token,
      additional_headers: { 'Content-Type': 'application/json' },
    })

    switch (data.status) {
      case 200:
        return { success: true, message: '', data: data.body as AvailableRoles }
      default:
        return { success: false, message: data.message }
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const getAvailableUsers = createAsyncThunk(
  'adminOrganizationTabUser/getAvailableUsers',
  async (orgName: string, { getState }) => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    const data = await getUserData('all', token)

    switch (data.status) {
      case 200:
        return { success: true, message: '', data: data.body as { user_data: AdminOrgUser[] }, orgName }
      default:
        return { success: false, message: data.message }
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const userDeleteSingle = createAsyncThunk(
  'adminOrganizationTabUser/userDeleteSingle',
  async (
    payload: {
      user: { email: string; role: string }
      selectedOrg: string
      updateTableData: (org: string) => void
    },
    { getState, dispatch }
  ) => {
    const { user, selectedOrg, updateTableData } = payload
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    let promises = []
    const userData = (await getUserData(user.email, token)).body as { user_data: AdminOrgUser }
    if (userData?.user_data?.organizations?.length > 1) {
      const userRole = { email: user.email, role: user.role }
      const patchJson: adminOrgPatch = {
        name: selectedOrg,
        users_to_remove: [userRole],
      }
      promises.push(dispatch(editOrg(patchJson)))
    } else {
      alert(
        'Cannot remove User ' +
          user.email +
          ' from ' +
          selectedOrg +
          ' because they must belong to at least one organization.'
      )
    }
    Promise.all(promises).then(() => {
      dispatch(refresh({ selectedOrg, updateTableData }))
    })
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const userDeleteMultiple = createAsyncThunk(
  'adminOrganizationTabUser/userDeleteMultiple',
  async (payload: AdminOrgUserDeleteMultiple, { getState, dispatch }) => {
    const { users, selectedOrg, updateTableData } = payload
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    const invalidUsers = []
    const patchJson: adminOrgPatch = {
      name: selectedOrg,
      users_to_remove: [],
    }
    for (const user of users) {
      const userData = (await getUserData(user.email, token)).body as { user_data: AdminOrgUser }
      if (userData?.user_data?.organizations?.length > 1) {
        const userRole = { email: user.email, role: user.role }
        patchJson.users_to_remove.push(userRole)
      } else {
        invalidUsers.push(user.email)
      }
    }
    if (invalidUsers.length === 0) {
      await dispatch(editOrg(patchJson))
      dispatch(refresh({ selectedOrg, updateTableData }))
    } else {
      alert(
        'Cannot remove User(s) ' +
          invalidUsers.map((email) => email.toString()).join(', ') +
          ' from ' +
          selectedOrg +
          ' because they must belong to at least one organization.'
      )
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const userAddMultiple = createAsyncThunk(
  'adminOrganizationTabUser/userAddMultiple',
  async (payload: AdminOrgTabUserAddMultiple, { dispatch }) => {
    const { userList, selectedOrg, updateTableData } = payload

    const patchJson: adminOrgPatch = {
      name: selectedOrg,
      users_to_add: [],
    }
    for (const user of userList) {
      const userRole = { email: user?.email, role: user?.role }
      patchJson.users_to_add.push(userRole)
    }
    await dispatch(editOrg(patchJson))
    dispatch(refresh({ selectedOrg, updateTableData }))
  },
  {
    condition: (payload: AdminOrgTabUserAddMultiple, { getState }) =>
      selectToken(getState() as RootState) != undefined && payload.userList?.length != 0,
  }
)

export const userBulkEdit = createAsyncThunk(
  'adminOrganizationTabUser/userBulkEdit',
  async (payload: AdminOrgTabUserBulkEdit, { dispatch }) => {
    const { json, selectedOrg, selectedUser, updateTableData } = payload

    const patchJson: adminOrgPatch = {
      name: selectedOrg,
      users_to_modify: [],
    }
    const rows = Object.values(json)
    var orgUpdateVal = {}
    for (var row of rows) {
      if (row.newData.email === selectedUser) {
        orgUpdateVal = { name: selectedOrg, role: row.newData.role }
      }
      const userRole = { email: row.newData.email, role: row.newData.role }
      patchJson.users_to_modify.push(userRole)
    }
    await dispatch(editOrg(patchJson))
    dispatch(refresh({ selectedOrg, updateTableData }))
    if (Object.keys(orgUpdateVal).length > 0) {
      dispatch(setOrganizationList({ value: orgUpdateVal, orgName: selectedOrg, type: 'update' }))
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const refresh = createAsyncThunk(
  'adminOrganizationTabUser/refresh',
  async (
    payload: {
      selectedOrg: string
      updateTableData: any
    },
    { dispatch }
  ) => {
    const { selectedOrg, updateTableData } = payload
    updateTableData(selectedOrg)
    dispatch(getAvailableUsers(selectedOrg))
    dispatch(setSelectedUserList([]))
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const adminOrganizationTabUserSlice = createSlice({
  name: 'adminOrganizationTabUser',
  initialState: {
    loading: false,
    value: initialState,
  },
  reducers: {
    setSelectedUserList: (state, action) => {
      state.value.selectedUserList = action.payload
    },
    setSelectedUserRole: (state, action) => {
      const { email, role } = action.payload
      const selectedUsers = [...state.value.selectedUserList]
      const userIndex = selectedUsers.findIndex((user) => user.email === email)
      const user = { ...selectedUsers[userIndex] }
      if (user) {
        user.role = role
      }
      selectedUsers[userIndex] = user
      state.value.selectedUserList = selectedUsers
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(getAvailableRoles.pending, (state) => {
        state.loading = true
      })
      .addCase(getAvailableRoles.fulfilled, (state, action) => {
        state.loading = false
        if (action.payload.success) {
          const roleData = []
          const apiData = action.payload.data
          for (let i = 0; i < apiData.roles.length; i++) {
            const role = {
              role: apiData.roles[i],
            }
            roleData.push(role)
          }
          state.value.availableRoles = roleData
        }
      })
      .addCase(getAvailableRoles.rejected, (state) => {
        state.loading = false
      })
      .addCase(getAvailableUsers.pending, (state) => {
        state.loading = true
      })
      .addCase(getAvailableUsers.fulfilled, (state, action) => {
        state.loading = false
        if (action.payload.success) {
          const userData = action.payload.data
          let availableUserList = []
          let counter = 0
          if (userData?.user_data) {
            for (const user of userData.user_data) {
              const userOrgs = user?.organizations
              if (!userOrgs.some((e) => e.name === action.payload.orgName)) {
                let tempValue = {
                  id: counter,
                  email: user.email,
                  role: 'user',
                }
                availableUserList.push(tempValue)
                counter += 1
              }
            }
          }
          state.value.availableUserList = availableUserList
        }
      })
      .addCase(getAvailableUsers.rejected, (state) => {
        state.loading = false
      })
  },
})

export const { setSelectedUserList, setSelectedUserRole } = adminOrganizationTabUserSlice.actions

export const selectLoading = (state: RootState) => state.adminOrganizationTabUser.loading
export const selectAvailableUserList = (state: RootState) => state.adminOrganizationTabUser.value.availableUserList
export const selectSelectedUserList = (state: RootState) => state.adminOrganizationTabUser.value.selectedUserList
export const selectAvailableRoles = (state: RootState) => state.adminOrganizationTabUser.value.availableRoles

export default adminOrganizationTabUserSlice.reducer
