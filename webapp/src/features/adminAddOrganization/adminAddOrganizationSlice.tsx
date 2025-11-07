import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { selectToken } from '../../generalSlices/userSlice'
import EnvironmentVars from '../../EnvironmentVars'
import apiHelper from '../../apis/api-helper'
import { RootState } from '../../store'
import { getOrgData } from '../adminOrganizationTab/adminOrganizationTabSlice'
import { AdminAddOrgForm } from './AdminAddOrganization'

export const addOrg = createAsyncThunk(
  'adminAddOrganization/addOrg',
  async (
    payload: {
      json: AdminAddOrgForm
    },
    { getState, dispatch }
  ) => {
    const { json } = payload
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    const data = await apiHelper._postData({
      url: EnvironmentVars.adminAddOrg,
      token,
      body: JSON.stringify(json),
    })
    switch (data.status) {
      case 200:
        dispatch(getOrgData({ orgName: 'all', all: true, specifiedOrg: json.name }))
        return { success: true, message: 'Organization Creation is successful.' }
      default:
        return { success: false, message: data.message }
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const adminAddOrganizationSlice = createSlice({
  name: 'adminAddOrganization',
  initialState: {
    loading: false,
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(addOrg.pending, (state) => {
        state.loading = true
      })
      .addCase(addOrg.fulfilled, (state) => {
        state.loading = false
      })
      .addCase(addOrg.rejected, (state) => {
        state.loading = false
      })
  },
})

export const selectLoading = (state: RootState) => state.adminAddOrganization.loading

export default adminAddOrganizationSlice.reducer
