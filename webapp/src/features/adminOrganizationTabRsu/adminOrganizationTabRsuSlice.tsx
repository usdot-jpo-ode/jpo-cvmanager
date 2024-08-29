import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { selectToken } from '../../generalSlices/userSlice'
import EnvironmentVars from '../../EnvironmentVars'
import apiHelper from '../../apis/api-helper'
import { RootState } from '../../store'
import { AdminRsu } from '../../types/Rsu'
import {
  AdminOrgRsuDeleteMultiple,
  AdminOrgRsuDeleteSingle,
  AdminOrgRsuWithId,
  AdminOrgTabRsuAddMultiple,
} from './AdminOrganizationTabRsuTypes'
import { adminOrgPatch, editOrg, selectSelectedOrgName } from '../adminOrganizationTab/adminOrganizationTabSlice'

const initialState = {
  availableRsuList: [] as AdminOrgRsuWithId[],
  selectedRsuList: [] as AdminOrgRsuWithId[],
}

export const getRsuDataByIp = async (rsu_ip: string, token: string) => {
  const data = await apiHelper._getDataWithCodes({
    url: EnvironmentVars.adminRsu,
    token,
    query_params: { rsu_ip },
  })

  return data
}

export const getRsuData = createAsyncThunk(
  'adminOrganizationTabRsu/getRsuData',
  async (orgName: string, { getState, dispatch }) => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    const data = await getRsuDataByIp('all', token)

    switch (data.status) {
      case 200:
        return { success: true, message: '', data: data.body, orgName }
      default:
        return { success: false, message: data.message }
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const rsuDeleteSingle = createAsyncThunk(
  'adminOrganizationTabRsu/rsuDeleteSingle',
  async (payload: AdminOrgRsuDeleteSingle, { getState, dispatch }) => {
    const { rsu, selectedOrg, selectedOrgEmail, updateTableData } = payload
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    let promises = []
    const rsuData = (await getRsuDataByIp(rsu.ip, token)).body
    if (rsuData?.rsu_data?.organizations?.length > 1) {
      const patchJson: adminOrgPatch = {
        name: selectedOrg,
        email: selectedOrgEmail,
        rsus_to_remove: [rsu.ip],
      }
      promises.push(dispatch(editOrg(patchJson)))
    } else {
      alert(
        'Cannot remove RSU ' + rsu.ip + ' from ' + selectedOrg + ' because it must belong to at least one organization.'
      )
    }
    var res = await Promise.all(promises)
    dispatch(refresh({ selectedOrg, updateTableData }))

    if ((res[0].payload as any).success) {
      return { success: true, message: 'RSU deleted successfully' }
    } else {
      return { success: false, message: 'Failed to delete RSU' }
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const rsuDeleteMultiple = createAsyncThunk(
  'adminOrganizationTabRsu/rsuDeleteMultiple',
  async (payload: AdminOrgRsuDeleteMultiple, { getState, dispatch }) => {
    const { rows, selectedOrg, selectedOrgEmail, updateTableData } = payload
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    const invalidRsus = []
    const patchJson: adminOrgPatch = {
      name: selectedOrg,
      email: selectedOrgEmail,
      rsus_to_remove: [],
    }
    for (const row of rows) {
      const rsuData = (await getRsuDataByIp(row.ip, token)).body
      if (rsuData?.rsu_data?.organizations?.length > 1) {
        patchJson.rsus_to_remove.push(row.ip)
      } else {
        invalidRsus.push(row.ip)
      }
    }
    if (invalidRsus.length === 0) {
      var res = await dispatch(editOrg(patchJson))
      dispatch(refresh({ selectedOrg, updateTableData }))
      if ((res.payload as any).success) {
        return { success: true, message: 'RSU(s) deleted successfully' }
      } else {
        return { success: false, message: 'Failed to delete RSU(s)' }
      }
    } else {
      alert(
        'Cannot remove RSU(s) ' +
          invalidRsus.map((ip) => ip.toString()).join(', ') +
          ' from ' +
          selectedOrg +
          ' because they must belong to at least one organization.'
      )
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const rsuAddMultiple = createAsyncThunk(
  'adminOrganizationTabRsu/rsuAddMultiple',
  async (payload: AdminOrgTabRsuAddMultiple, { getState, dispatch }) => {
    const { rsuList, selectedOrg, selectedOrgEmail, updateTableData } = payload

    const patchJson: adminOrgPatch = {
      name: selectedOrg,
      email: selectedOrgEmail,
      rsus_to_add: [],
    }
    for (const row of rsuList) {
      patchJson.rsus_to_add.push(row.ip)
    }
    var res = await dispatch(editOrg(patchJson))
    dispatch(refresh({ selectedOrg, updateTableData }))
    if ((res.payload as any).success) {
      return { success: true, message: 'RSU(s) added successfully' }
    } else {
      return { success: false, message: 'Failed to add RSU(s)' }
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const refresh = createAsyncThunk(
  'adminOrganizationTabRsu/refresh',
  async (
    payload: {
      selectedOrg: string
      updateTableData: (selectedOrg: string) => void
    },
    { dispatch }
  ) => {
    const { selectedOrg, updateTableData } = payload
    updateTableData(selectedOrg)
    dispatch(getRsuData(selectedOrg))
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const adminOrganizationTabRsuSlice = createSlice({
  name: 'adminOrganizationTabRsu',
  initialState: {
    loading: false,
    value: initialState,
  },
  reducers: {
    setSelectedRsuList: (state, action) => {
      state.value.selectedRsuList = action.payload
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(getRsuData.pending, (state) => {
        state.loading = true
      })
      .addCase(getRsuData.fulfilled, (state, action) => {
        state.loading = false
        if (action.payload.success) {
          const rsuData = action.payload.data
          let availableRsuList = [] as AdminOrgRsuWithId[]
          let counter = 0
          if (rsuData?.rsu_data) {
            for (const rsu of rsuData.rsu_data) {
              const rsuOrgs = rsu?.organizations
              if (!rsuOrgs.includes(action.payload.orgName)) {
                let tempValue = {
                  id: counter,
                  ip: rsu.ip,
                } as AdminOrgRsuWithId
                availableRsuList.push(tempValue)
                counter += 1
              }
            }
          }
          state.value.availableRsuList = availableRsuList
        }
      })
      .addCase(getRsuData.rejected, (state) => {
        state.loading = false
      })
      .addCase(refresh.fulfilled, (state) => {
        state.value.selectedRsuList = []
      })
  },
})

export const { setSelectedRsuList } = adminOrganizationTabRsuSlice.actions

export const selectLoading = (state: RootState) => state.adminOrganizationTabRsu.loading
export const selectAvailableRsuList = (state: RootState) => state.adminOrganizationTabRsu.value.availableRsuList
export const selectSelectedRsuList = (state: RootState) => state.adminOrganizationTabRsu.value.selectedRsuList

export default adminOrganizationTabRsuSlice.reducer
