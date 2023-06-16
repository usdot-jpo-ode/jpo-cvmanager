import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { selectToken } from '../../generalSlices/userSlice'
import EnvironmentVars from '../../EnvironmentVars'
import apiHelper from '../../apis/api-helper'

const initialState = {
  successMsg: '',
  selectedOrganizationNames: [],
  selectedOrganizations: [],
  organizationNames: [],
  availableRoles: [],
  apiData: {},
  errorState: false,
  errorMsg: '',
  submitAttempt: false,
}

export const organizationParser = (data, submitOrgs, apiData) => {
  let orgsToAdd = []
  let orgsToModify = []
  let orgsToRemove = []

  for (const org of apiData.user_data.organizations) {
    if (submitOrgs.some((e) => e.name === org.name)) {
      var index = submitOrgs.findIndex(function (item, i) {
        return item.name === org.name
      })
      if (submitOrgs[index].role !== org.role) {
        const changedOrg = { name: submitOrgs[index].name, role: submitOrgs[index].role }
        orgsToModify.push(changedOrg)
      }
    } else {
      const removedOrg = { name: org.name, role: org.role }
      orgsToRemove.push(removedOrg)
    }
  }

  for (const org of submitOrgs) {
    if (!apiData.user_data.organizations.some((e) => e.name === org.name)) {
      const newOrg = { name: org.name, role: org.role }
      orgsToAdd.push(newOrg)
    }
  }

  data.organizations_to_add = orgsToAdd
  data.organizations_to_modify = orgsToModify
  data.organizations_to_remove = orgsToRemove
  return data
}

export const getUserData = createAsyncThunk(
  'adminEditUser/getUserData',
  async (email, { getState, dispatch }) => {
    const currentState = getState()
    const token = selectToken(currentState)

    const data = await apiHelper._getDataWithCodes({
      url: EnvironmentVars.adminUser,
      token,
      query_params: { user_email: email },
      additional_headers: { 'Content-Type': 'application/json' },
    })

    switch (data.status) {
      case 200:
        dispatch(adminEditUserSlice.actions.updateStates(data.body))
        return { success: true, message: '', data: data.body }
      default:
        return { success: false, message: data.message }
    }
  },
  { condition: (_, { getState }) => selectToken(getState()) }
)

export const editUser = createAsyncThunk(
  'adminEditUser/editUser',
  async (payload, { getState, dispatch }) => {
    const { json, updateUserData } = payload
    const currentState = getState()
    const token = selectToken(currentState)

    const data = await apiHelper._patchData({
      url: EnvironmentVars.adminUser,
      token,
      body: JSON.stringify(json),
    })

    switch (data.status) {
      case 200:
        updateUserData()
        setTimeout(() => dispatch(adminEditUserSlice.actions.setSuccessMsg('')), 5000)
        return { success: true, message: 'Changes were successfully applied!' }
      default:
        return { success: false, message: data.message }
    }
  },
  { condition: (_, { getState }) => selectToken(getState()) }
)

export const submitForm = createAsyncThunk('adminEditUser/submitForm', async (payload, { getState, dispatch }) => {
  const { data, updateUserData } = payload
  const currentState = getState()
  const selectedOrganizations = selectSelectedOrganizations(currentState)
  const apiData = selectApiData(currentState)

  if (selectedOrganizations.length !== 0) {
    let submitOrgs = [...selectedOrganizations].map((org) => ({ ...org }))
    submitOrgs.forEach((elm) => delete elm.id)
    const tempData = organizationParser(data, submitOrgs, apiData)
    dispatch(editUser({ json: tempData, updateUserData }))
    return false
  } else {
    return true
  }
})

export const adminEditUserSlice = createSlice({
  name: 'adminEditUser',
  initialState: {
    loading: false,
    value: initialState,
  },
  reducers: {
    updateOrganizations: (state, action) => {
      let newOrganizations = []
      for (const name of action.payload) {
        if (state.value.selectedOrganizations.some((e) => e.name === name.name)) {
          newOrganizations.push(state.value.selectedOrganizations.find((e) => e.name === name.name))
        } else {
          const tempName = { ...name }
          tempName.role = state.value.availableRoles[0].role
          newOrganizations.push(tempName)
        }
      }
      state.value.selectedOrganizations = newOrganizations
      state.value.selectedOrganizationNames = action.payload
    },
    setSuccessMsg: (state, action) => {
      state.value.successMsg = action.payload
    },
    setSelectedRole: (state, action) => {
      const selectedOrganizations = [...state.value.selectedOrganizations]
      const { name, role } = action.payload
      const index = selectedOrganizations.findIndex((org) => org.name === name)
      selectedOrganizations[index].role = role
      state.value.selectedOrganizations = selectedOrganizations
    },
    updateStates: (state, action) => {
      const data = action.payload
      if (Object.keys(data).length !== 0) {
        let orgData = []
        data.allowed_selections.organizations.forEach((org, index) => orgData.push({ id: index, name: org }))
        state.value.organizationNames = orgData

        let roleData = []
        data.allowed_selections.roles.forEach((role) => roleData.push({ role }))
        state.value.availableRoles = roleData

        let tempOrganizations = []
        let tempOrganizationNames = []

        data.user_data.organizations.forEach((org, index) => {
          tempOrganizations.push({ id: index, name: org.name, role: org.role })
          tempOrganizationNames.push({ id: index, name: org.name })
        })

        state.value.selectedOrganizations = tempOrganizations
        state.value.selectedOrganizationNames = tempOrganizationNames
      }
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
          state.value.successMsg = action.payload.message
          state.value.errorMsg = ''
          state.value.errorState = false
          state.value.apiData = action.payload.data
        } else {
          state.value.successMsg = ''
          state.value.errorMsg = action.payload.message
          state.value.errorState = true
        }
      })
      .addCase(getUserData.rejected, (state) => {
        state.loading = false
      })
      .addCase(editUser.pending, (state) => {
        state.loading = true
      })
      .addCase(editUser.fulfilled, (state, action) => {
        state.loading = false
        if (action.payload.success) {
          state.value.successMsg = action.payload.message
          state.value.errorMsg = ''
          state.value.errorState = false
        } else {
          state.value.successMsg = ''
          state.value.errorMsg = action.payload.message
          state.value.errorState = true
        }
      })
      .addCase(editUser.rejected, (state) => {
        state.loading = false
      })
      .addCase(submitForm.fulfilled, (state, action) => {
        state.value.submitAttempt = action.payload
      })
  },
})

export const { updateOrganizations, setSuccessMsg, updateStates, setSelectedRole } = adminEditUserSlice.actions

export const selectLoading = (state) => state.adminEditUser.loading
export const selectSuccessMsg = (state) => state.adminEditUser.value.successMsg
export const selectSelectedOrganizationNames = (state) => state.adminEditUser.value.selectedOrganizationNames
export const selectSelectedOrganizations = (state) => state.adminEditUser.value.selectedOrganizations
export const selectOrganizationNames = (state) => state.adminEditUser.value.organizationNames
export const selectAvailableRoles = (state) => state.adminEditUser.value.availableRoles
export const selectApiData = (state) => state.adminEditUser.value.apiData
export const selectErrorState = (state) => state.adminEditUser.value.errorState
export const selectErrorMsg = (state) => state.adminEditUser.value.errorMsg
export const selectSubmitAttempt = (state) => state.adminEditUser.value.submitAttempt

export default adminEditUserSlice.reducer
