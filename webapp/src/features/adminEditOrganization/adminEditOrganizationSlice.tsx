import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { selectToken } from '../../generalSlices/userSlice'
import EnvironmentVars from '../../EnvironmentVars'
import apiHelper from '../../apis/api-helper'
import { RootState } from '../../store'
import { adminOrgPatch, editOrg, getOrgData } from '../adminOrganizationTab/adminOrganizationTabSlice'

const initialState = {
  successMsg: '',
  errorState: false,
  errorMsg: '',
}

export const updateStates = (setValue: (key: string, value: any) => void, selectedOrgName: string) => {
  setValue('orig_name', selectedOrgName)
  setValue('name', selectedOrgName)
}

export const editOrganization = createAsyncThunk(
  'adminEditOrganization/editOrganization',
  async (
    payload: {
      json: adminOrgPatch
      selectedOrg: string
      setValue: (key: string, value: any) => void
    },
    { dispatch }
  ) => {
    const { json, selectedOrg, setValue } = payload

    const patchJson: adminOrgPatch = {
      orig_name: selectedOrg,
      name: json.name,
      users_to_modify: [],
    }

    const data = (await dispatch(editOrg(patchJson))).payload as { success: boolean; message: string }

    if (data.success) {
      dispatch(getOrgData({ orgName: 'all', all: true, specifiedOrg: json.name }))
      setTimeout(() => dispatch(adminEditOrganizationSlice.actions.setSuccessMsg('')), 5000)
      updateStates(setValue, json.name)
      return { success: true, message: data.message }
    } else {
      setTimeout(() => dispatch(adminEditOrganizationSlice.actions.setSuccessMsg('')), 5000)
      return { success: false, message: data.message }
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
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

export const selectLoading = (state: RootState) => state.adminEditOrganization.loading
export const selectSuccessMsg = (state: RootState) => state.adminEditOrganization.value.successMsg
export const selectErrorState = (state: RootState) => state.adminEditOrganization.value.errorState
export const selectErrorMsg = (state: RootState) => state.adminEditOrganization.value.errorMsg

export default adminEditOrganizationSlice.reducer
