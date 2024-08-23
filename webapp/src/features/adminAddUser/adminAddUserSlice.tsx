import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { selectToken } from '../../generalSlices/userSlice'
import EnvironmentVars from '../../EnvironmentVars'
import apiHelper from '../../apis/api-helper'
import { getAvailableUsers } from '../adminUserTab/adminUserTabSlice'
import { RootState } from '../../store'
import { AdminOrgUser } from '../adminOrganizationTab/adminOrganizationTabSlice'

export type AdminUserForm = {
  email: string
  first_name: string
  last_name: string
  super_user: boolean
  organizations: string[]
  roles: string[]
  receive_error_emails: boolean
}

const initialState = {
  selectedOrganizationNames: [] as { id: number; name: string; role: string }[],
  selectedOrganizations: [] as { id: number; name: string; role: string }[],
  organizationNames: [] as { id: number; name: string }[],
  availableRoles: [] as { role: string }[],
  apiData: {} as AdminUserForm,
  submitAttempt: false,
}

export const getUserData = createAsyncThunk(
  'adminAddUser/getUserData',
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
        return { success: true, message: '', data: data.body }
      default:
        return { success: false, message: data.message }
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const createUser = createAsyncThunk(
  'adminAddUser/createUser',
  async (payload: { json: AdminUser; reset: () => void }, { getState, dispatch }) => {
    const { json, reset } = payload
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    const data = await apiHelper._postData({
      url: EnvironmentVars.adminAddUser,
      token,
      body: JSON.stringify(json),
    })

    switch (data.status) {
      case 200:
        dispatch(getAvailableUsers())
        dispatch(resetForm(reset))
        return { success: true, message: 'User Creation is successful.' }
      default:
        return { success: false, message: data.message }
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const resetForm = createAsyncThunk('adminAddUser/resetForm', async (reset: () => void, { dispatch }) => {
  reset()
})

export const submitForm = createAsyncThunk(
  'adminAddUser/submitForm',
  async (payload: { data: AdminUserForm; reset: () => void }, { getState, dispatch }) => {
    const { data, reset } = payload
    const currentState = getState() as RootState
    const selectedOrganizations = selectSelectedOrganizations(currentState)
    if (selectedOrganizations.length !== 0) {
      const submitOrgs = [...selectedOrganizations].map((org) => ({ ...org }))
      submitOrgs.forEach((elm) => delete elm.id)
      const tempData: AdminUser = {
        ...data,
        organizations: submitOrgs,
      }
      let res = await dispatch(createUser({ json: tempData, reset }))
      if ((res.payload as any).success) {
        return { submitAttempt: false, success: true, message: 'User Created Successfully' }
      } else {
        return { submitAttempt: false, success: false, message: (res.payload as any).message }
      }
    } else {
      return { submitAttempt: true, success: false, message: 'Please fill out all required fields' }
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const adminAddUserSlice = createSlice({
  name: 'adminAddUser',
  initialState: {
    loading: false,
    value: initialState,
  },
  reducers: {
    updateOrganizationNamesApiData: (state) => {
      if (Object.keys(state.value.apiData).length !== 0) {
        let orgData = [] as { id: number; name: string }[]
        state.value.apiData.organizations.forEach((organization, index) =>
          orgData.push({ id: index, name: organization })
        )
        state.value.organizationNames = orgData
      }
    },
    updateAvailableRolesApiData: (state) => {
      if (Object.keys(state.value.apiData).length !== 0) {
        let roleData = [] as { role: string }[]
        state.value.apiData.roles.forEach((role) => roleData.push({ role }))
        state.value.availableRoles = roleData
      }
    },
    updateOrganizations: (state, action) => {
      let newOrganizations = []
      for (const name of action.payload) {
        if (state.value.selectedOrganizations.some((e) => e.name === name.name)) {
          var index = state.value.selectedOrganizations.findIndex(function (item, i) {
            return item.name === name.name
          })
          newOrganizations.push(state.value.selectedOrganizations[index])
        } else if (!state.value.selectedOrganizations.some((e) => e.name === name.name)) {
          newOrganizations.push({ ...name, role: state.value.availableRoles[0].role })
        }
      }
      state.value.selectedOrganizations = newOrganizations
      state.value.selectedOrganizationNames = action.payload
    },
    setSelectedRole: (
      state,
      action: {
        payload: {
          id: number
          name: string
          role: string
        }
      }
    ) => {
      const selectedOrganizations = [...state.value.selectedOrganizations]
      const { name, role } = action.payload
      const index = selectedOrganizations.findIndex((org) => org.name === name)
      selectedOrganizations[index].role = role
      state.value.selectedOrganizations = selectedOrganizations
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(getUserData.pending, (state) => {
        state.loading = true
      })
      .addCase(getUserData.fulfilled, (state, action) => {
        state.loading = false
        if (action.payload.success) {
          state.value.apiData = action.payload.data
        }
      })
      .addCase(getUserData.rejected, (state) => {
        state.loading = false
      })
      .addCase(createUser.pending, (state) => {
        state.loading = true
      })
      .addCase(createUser.fulfilled, (state, action) => {
        state.loading = false
      })
      .addCase(createUser.rejected, (state) => {
        state.loading = false
      })
      .addCase(resetForm.fulfilled, (state) => {
        state.value.selectedOrganizations = []
        state.value.selectedOrganizationNames = []
      })
      .addCase(submitForm.fulfilled, (state, action) => {
        state.value.submitAttempt = action.payload.submitAttempt
      })
  },
})

export const { updateOrganizationNamesApiData, updateAvailableRolesApiData, updateOrganizations, setSelectedRole } =
  adminAddUserSlice.actions

export const selectLoading = (state: RootState) => state.adminAddUser.loading
export const selectSelectedOrganizationNames = (state: RootState) => state.adminAddUser.value.selectedOrganizationNames
export const selectSelectedOrganizations = (state: RootState) => state.adminAddUser.value.selectedOrganizations
export const selectOrganizationNames = (state: RootState) => state.adminAddUser.value.organizationNames
export const selectAvailableRoles = (state: RootState) => state.adminAddUser.value.availableRoles
export const selectApiData = (state: RootState) => state.adminAddUser.value.apiData
export const selectSubmitAttempt = (state: RootState) => state.adminAddUser.value.submitAttempt

export default adminAddUserSlice.reducer
