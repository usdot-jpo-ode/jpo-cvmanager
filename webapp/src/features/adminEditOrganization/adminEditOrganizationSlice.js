import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { selectToken } from '../../generalSlices/userSlice'
import EnvironmentVars from '../../EnvironmentVars'
import apiHelper from '../../apis/api-helper'

const initialState = {
  successMsg: '',
  errorState: false,
  errorMsg: '',
}

export const updateStates = (setValue, selectedOrgName) => {
  setValue('orig_name', selectedOrgName)
  setValue('name', selectedOrgName)
}

export const createJsonBody = (data, selectedOrg) => {
  const json = {
    orig_name: selectedOrg,
    name: data.name,
    users_to_add: [],
    users_to_modify: [],
    users_to_remove: [],
    rsus_to_add: [],
    rsus_to_remove: [],
  }
  return json
}

export const editOrganization = createAsyncThunk(
  'adminEditOrganization/editOrganization',
  async (payload, { getState, dispatch }) => {
    const { json, selectedOrg, setValue, updateOrganizationData } = payload
    const currentState = getState()
    const token = selectToken(currentState)

    const data = await apiHelper._patchData({
      url: EnvironmentVars.adminOrg,
      token,
      body: JSON.stringify(createJsonBody(json, selectedOrg)),
    })

    switch (data.status) {
      case 200:
        updateOrganizationData(json.name)
        setTimeout(() => dispatch(adminEditOrganizationSlice.actions.setSuccessMsg('')), 5000)
        updateStates(setValue, json.name)
        return { success: true, message: 'Changes were successfully applied!' }
      default:
        setTimeout(() => dispatch(adminEditOrganizationSlice.actions.setSuccessMsg('')), 5000)
        return { success: false, message: data.message }
    }
  },
  { condition: (_, { getState }) => selectToken(getState()) }
)

export const adminEditOrganizationSlice = createSlice({
  name: 'adminEditOrganization',
  initialState: {
    loading: false,
    value: initialState,
  },
  reducers: {
    setSuccessMsg: (state, action) => {
      state.value.successMsg = action.payload
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(editOrganization.pending, (state) => {
        state.loading = true
      })
      .addCase(editOrganization.fulfilled, (state, action) => {
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
      .addCase(editOrganization.rejected, (state) => {
        state.loading = false
      })
  },
})

export const { setSuccessMsg } = adminEditOrganizationSlice.actions

export const selectLoading = (state) => state.adminEditOrganization.loading
export const selectSuccessMsg = (state) => state.adminEditOrganization.value.successMsg
export const selectErrorState = (state) => state.adminEditOrganization.value.errorState
export const selectErrorMsg = (state) => state.adminEditOrganization.value.errorMsg

export default adminEditOrganizationSlice.reducer
