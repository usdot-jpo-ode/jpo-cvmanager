import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { selectToken } from '../../generalSlices/userSlice'
import EnvironmentVars from '../../EnvironmentVars'
import apiHelper from '../../apis/api-helper'
import { RootState } from '../../store'
import {
  adminOrgPatch,
  editOrg,
  getOrgData,
  selectSelectedOrg,
  setSelectedOrg,
} from '../adminOrganizationTab/adminOrganizationTabSlice'

const initialState = {
  successMsg: '',
}

export const updateStates = (
  setValue: (key: string, value: any) => void,
  selectedOrgName: string,
  selectedOrgEmail: string
) => {
  setValue('orig_name', selectedOrgName)
  setValue('name', selectedOrgName)
  setValue('email', selectedOrgEmail)
}

export const editOrganization = createAsyncThunk(
  'adminEditOrganization/editOrganization',
  async (
    payload: {
      json: adminOrgPatch
      selectedOrg: string
      setValue: (key: string, value: any) => void
    },
    { dispatch, getState }
  ) => {
    const { json, selectedOrg, setValue } = payload
    const prevSelectedOrg = selectSelectedOrg(getState() as RootState)

    const patchJson: adminOrgPatch = {
      orig_name: selectedOrg,
      name: json.name,
      email: json.email,
      users_to_modify: [],
    }

    const data = (await dispatch(editOrg(patchJson))).payload as { success: boolean; message: string }

    if (data.success) {
      dispatch(getOrgData({ orgName: 'all', all: true, specifiedOrg: json.name }))
      setTimeout(() => dispatch(adminEditOrganizationSlice.actions.setSuccessMsg('')), 5000)
      dispatch(setSelectedOrg({ ...prevSelectedOrg, name: json.name }))
      updateStates(setValue, json.name, json.email)
      return { success: true, message: data.message == '' ? 'Organization updated successfully' : data.message }
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
        } else {
          state.value.successMsg = ''
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

export default adminEditOrganizationSlice.reducer
