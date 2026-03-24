import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit'
import { selectToken, setOrganizationList } from '../../generalSlices/userSlice'
import EnvironmentVars from '../../EnvironmentVars'
import apiHelper from '../../apis/api-helper'
import { RootState } from '../../store'
import {
  AdminOrgTabUserAddMultiple,
  AdminOrgTabUserBulkEdit,
  AdminOrgUserDeleteMultiple,
} from './AdminOrganizationTabUserTypes'

import { adminOrgPatch, AdminOrgUser, editOrg } from '../adminOrganizationTab/adminOrganizationTabSlice'
import {
  ORGANIZATION_API_AVAILABLE_USER_LIST_TAG,
  ORGANIZATION_API_USER_LIST_TAG,
  ORGANIZATION_API_USER_TAG,
  organizationApiSlice,
} from '../api/organizationApiSlice'

const initialState = {
  selectedUserList: [] as AdminOrgUser[],
  availableRoles: [] as { role: string }[],
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

export const userDeleteSingle = createAsyncThunk(
  'adminOrganizationTabUser/userDeleteSingle',
  async (
    payload: {
      user: { email: string; role: string }
      selectedOrg: string
      selectedOrgEmail: string
      updateTableData: (org: string) => void
    },
    { getState, dispatch }
  ) => {
    const { user, selectedOrg, selectedOrgEmail, updateTableData } = payload

    const promises = []
    const userData =
      (await dispatch(organizationApiSlice.endpoints.getUserOrganizations.initiate(user.email)))?.data ?? []

    if (userData?.length > 1) {
      const userRole = { email: user.email, role: user.role }
      const patchJson: adminOrgPatch = {
        name: selectedOrg,
        email: selectedOrgEmail,
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
    // Invalidate RTK Query cache
    dispatch(
      organizationApiSlice.util.invalidateTags([
        ORGANIZATION_API_USER_LIST_TAG,
        ORGANIZATION_API_AVAILABLE_USER_LIST_TAG,
        { type: ORGANIZATION_API_USER_TAG, id: user.email },
      ])
    )

    const res = await Promise.all(promises)
    dispatch(refresh({ selectedOrg, updateTableData }))

    if ((res[0].payload as any).success) {
      return { success: true, message: 'User deleted successfully' }
    } else {
      return { success: false, message: 'Failed to delete user' }
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const userDeleteMultiple = createAsyncThunk(
  'adminOrganizationTabUser/userDeleteMultiple',
  async (payload: AdminOrgUserDeleteMultiple, { getState, dispatch }) => {
    const { users, selectedOrg, selectedOrgEmail, updateTableData } = payload

    const invalidUsers = []
    const patchJson: adminOrgPatch = {
      name: selectedOrg,
      email: selectedOrgEmail,
      users_to_remove: [],
    }
    for (const user of users) {
      const userData =
        (await dispatch(organizationApiSlice.endpoints.getUserOrganizations.initiate(user.email)))?.data ?? []
      if (userData?.length > 1) {
        const userRole = { email: user.email, role: user.role }
        patchJson.users_to_remove.push(userRole)
      } else {
        invalidUsers.push(user.email)
      }
    }
    if (invalidUsers.length === 0) {
      const res = await dispatch(editOrg(patchJson))
      dispatch(refresh({ selectedOrg, updateTableData }))
      if ((res.payload as any).success) {
        const userTags = users.map((user) => ({ type: ORGANIZATION_API_USER_TAG, id: user.email }))
        dispatch(
          organizationApiSlice.util.invalidateTags([
            ORGANIZATION_API_USER_LIST_TAG,
            ORGANIZATION_API_AVAILABLE_USER_LIST_TAG,
            ...userTags,
          ])
        )
        return { success: true, message: 'User(s) deleted successfully' }
      } else {
        return { success: false, message: 'Failed to delete user(s)' }
      }
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
    const { userList, selectedOrg, selectedOrgEmail, updateTableData } = payload

    const patchJson: adminOrgPatch = {
      name: selectedOrg,
      email: selectedOrgEmail,
      users_to_add: [],
    }
    for (const user of userList) {
      const userRole = { email: user?.email, role: user?.role }
      patchJson.users_to_add.push(userRole)
    }
    const res = await dispatch(editOrg(patchJson))
    dispatch(refresh({ selectedOrg, updateTableData }))
    if ((res.payload as any).success) {
      const userTags = userList.map((user) => ({ type: ORGANIZATION_API_USER_TAG, id: user.email }))
      dispatch(
        organizationApiSlice.util.invalidateTags([
          ORGANIZATION_API_USER_LIST_TAG,
          ORGANIZATION_API_AVAILABLE_USER_LIST_TAG,
          ...userTags,
        ])
      )
      return { success: true, message: 'User(s) added successfully' }
    } else {
      return { success: false, message: 'Failed to add user(s)' }
    }
  },
  {
    condition: (payload: AdminOrgTabUserAddMultiple, { getState }) =>
      selectToken(getState() as RootState) != undefined && payload.userList?.length != 0,
  }
)

export const userBulkEdit = createAsyncThunk(
  'adminOrganizationTabUser/userBulkEdit',
  async (payload: AdminOrgTabUserBulkEdit, { dispatch }) => {
    const { json, selectedOrg, selectedUser, selectedOrgEmail, updateTableData } = payload

    const patchJson: adminOrgPatch = {
      name: selectedOrg,
      email: selectedOrgEmail,
      users_to_modify: [],
    }
    const rows = Object.values(json)
    let orgUpdateVal = {}
    for (const row of rows) {
      if (row.newData.email === selectedUser) {
        orgUpdateVal = { name: selectedOrg, role: row.newData.role }
      }
      const userRole = { email: row.newData.email, role: row.newData.role }
      patchJson.users_to_modify.push(userRole)
    }
    const res = await dispatch(editOrg(patchJson))
    dispatch(refresh({ selectedOrg, updateTableData }))

    if (Object.keys(orgUpdateVal).length > 0) {
      dispatch(setOrganizationList({ value: orgUpdateVal, orgName: selectedOrg, type: 'update' }))
    }

    if ((res.payload as any).success) {
      return { success: true, message: 'User(s) updated successfully' }
    } else {
      return { success: false, message: 'Failed to update user(s)' }
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
    setSelectedUserList: (state, action: PayloadAction<AdminUser[]>) => {
      state.value.selectedUserList = action.payload.map((user) => ({
        ...user,
        role: undefined,
        organizations: user.organizations.map((org) => ({ name: org.organization, role: org.role })),
      }))
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
  },
})

export const { setSelectedUserList, setSelectedUserRole } = adminOrganizationTabUserSlice.actions

export const selectLoading = (state: RootState) => state.adminOrganizationTabUser.loading
export const selectSelectedUserList = (state: RootState) => state.adminOrganizationTabUser.value.selectedUserList
export const selectAvailableRoles = (state: RootState) => state.adminOrganizationTabUser.value.availableRoles

export default adminOrganizationTabUserSlice.reducer
