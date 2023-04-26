import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { selectToken } from '../../generalSlices/userSlice'
import EnvironmentVars from '../../EnvironmentVars'
import apiHelper from '../../apis/api-helper'

const initialState = {
  availableUserList: [],
  selectedUserList: [],
  availableRoles: [],
}

const getUserData = async (email, token) => {
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
    const currentState = getState()
    const token = selectToken(currentState)

    const data = await apiHelper._getDataWithCodes({
      url: EnvironmentVars.adminAddUser,
      token,
      additional_headers: { 'Content-Type': 'application/json' },
    })

    switch (data.status) {
      case 200:
        return { success: true, message: '', data: data.body }
      case 400:
      case 500:
        return { success: false, message: data.message }
    }
    return data
  },
  { condition: (_, { getState }) => selectToken(getState()) }
)

export const getAvailableUsers = createAsyncThunk(
  'adminOrganizationTabUser/getAvailableUsers',
  async (orgName, { getState }) => {
    const currentState = getState()
    const token = selectToken(currentState)

    const data = await getUserData('all', token)

    switch (data.status) {
      case 200:
        return { success: true, message: '', data: data.body, orgName }
      case 400:
      case 500:
        return { success: false, message: data.message }
    }
    return data
  },
  { condition: (_, { getState }) => selectToken(getState()) }
)

export const userDeleteSingle = createAsyncThunk(
  'adminOrganizationTabUser/userDeleteSingle',
  async (payload, { getState, dispatch }) => {
    const { user, orgPatchJson, selectedOrg, fetchPatchOrganization, updateTableData } = payload
    const currentState = getState()
    const token = selectToken(currentState)

    let promises = []
    const userData = (await getUserData(user.email, token)).body
    if (userData?.user_data?.organizations?.length > 1) {
      const userRole = { email: user.email, role: user.role }
      let patchJson = { ...orgPatchJson }

      patchJson.users_to_remove = [userRole]
      promises.push(fetchPatchOrganization(patchJson))
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
  { condition: (_, { getState }) => selectToken(getState()) }
)

export const userDeleteMultiple = createAsyncThunk(
  'adminOrganizationTabUser/userDeleteMultiple',
  async (payload, { getState, dispatch }) => {
    const { users, orgPatchJson, selectedOrg, fetchPatchOrganization, updateTableData } = payload
    const currentState = getState()
    const token = selectToken(currentState)

    let promises = []
    for (const user of users) {
      const userData = (await getUserData(user.email, token)).body
      if (userData?.user_data?.organizations?.length > 1) {
        const userRole = { email: user.email, role: user.role }
        let patchJson = { ...orgPatchJson }
        patchJson.rsus_to_remove = [userRole]
        promises.push(fetchPatchOrganization(patchJson))
      } else {
        alert(
          'Cannot remove User ' +
            user.email +
            ' from ' +
            selectedOrg +
            ' because they must belong to at least one organization.'
        )
      }
    }
    Promise.all(promises).then(() => {
      dispatch(refresh({ selectedOrg, updateTableData }))
    })
  },
  { condition: (_, { getState }) => selectToken(getState()) }
)

export const userAddMultiple = createAsyncThunk(
  'adminOrganizationTabUser/userAddMultiple',
  async (payload, { dispatch }) => {
    const { userList, orgPatchJson, selectedOrg, fetchPatchOrganization, updateTableData } = payload
    let promises = []
    for (const user of userList) {
      const patchJson = { ...orgPatchJson }
      const userRole = { email: user?.email, role: user?.role }
      patchJson.users_to_add = [userRole]
      promises.push(fetchPatchOrganization(patchJson))
    }
    Promise.all(promises).then(() => {
      dispatch(refresh({ selectedOrg, updateTableData }))
    })
  },
  { condition: (payload, { getState }) => selectToken(getState()) && payload.userList != [] }
)

export const userbulkEdit = createAsyncThunk(
  'adminOrganizationTabUser/userAddMultiple',
  async (payload, { dispatch }) => {
    const { json, orgPatchJson, selectedOrg, fetchPatchOrganization, updateTableData } = payload
    let promises = []
    const rows = Object.values(json)
    for (var row of rows) {
      let patchJson = { ...orgPatchJson }
      const userRole = { email: row.newData.email, role: row.newData.role }
      patchJson.users_to_modify = [userRole]
      promises.push(fetchPatchOrganization(patchJson))
    }
    Promise.all(promises).then(() => {
      dispatch(refresh({ selectedOrg, updateTableData }))
    })
  },
  { condition: (_, { getState }) => selectToken(getState()) }
)

export const refresh = createAsyncThunk(
  'adminOrganizationTabUser/refresh',
  async (payload, { dispatch }) => {
    const { selectedOrg, updateTableData } = payload
    updateTableData(selectedOrg)
    dispatch(getAvailableUsers(selectedOrg))
    dispatch(setSelectedUserList([]))
  },
  { condition: (_, { getState }) => selectToken(getState()) }
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
            let role = {}
            role.role = apiData.roles[i]
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
                let tempValue = {}
                tempValue.id = counter
                tempValue.email = user.email
                tempValue.role = 'user'
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

export const selectLoading = (state) => state.adminOrganizationTabUser.loading
export const selectAvailableUserList = (state) => state.adminOrganizationTabUser.value.availableUserList
export const selectSelectedUserList = (state) => state.adminOrganizationTabUser.value.selectedUserList
export const selectAvailableRoles = (state) => state.adminOrganizationTabUser.value.availableRoles

export default adminOrganizationTabUserSlice.reducer
