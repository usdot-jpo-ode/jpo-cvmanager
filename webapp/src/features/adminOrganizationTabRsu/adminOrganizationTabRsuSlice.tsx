import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { selectToken } from '../../generalSlices/userSlice'
import EnvironmentVars from '../../EnvironmentVars'
import apiHelper from '../../apis/api-helper'

const initialState = {
  availableRsuList: [],
  selectedRsuList: [],
}

export const getRsuDataByIp = async (rsu_ip, token) => {
  const data = await apiHelper._getDataWithCodes({
    url: EnvironmentVars.adminRsu,
    token,
    query_params: { rsu_ip },
  })

  return data
}

export const getRsuData = createAsyncThunk(
  'adminOrganizationTabRsu/getRsuData',
  async (orgName, { getState, dispatch }) => {
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
  { condition: (_, { getState }) => selectToken(getState()) }
)

export const rsuDeleteSingle = createAsyncThunk(
  'adminOrganizationTabRsu/rsuDeleteSingle',
  async (payload, { getState, dispatch }) => {
    const { rsu, orgPatchJson, selectedOrg, fetchPatchOrganization, updateTableData } = payload
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    let promises = []
    const rsuData = (await getRsuDataByIp(rsu.ip, token)).body
    if (rsuData?.rsu_data?.organizations?.length > 1) {
      let patchJson = orgPatchJson
      patchJson.rsus_to_remove = [rsu.ip]
      promises.push(fetchPatchOrganization(patchJson))
    } else {
      alert(
        'Cannot remove RSU ' + rsu.ip + ' from ' + selectedOrg + ' because it must belong to at least one organization.'
      )
    }
    Promise.all(promises).then(() => {
      dispatch(refresh({ selectedOrg, updateTableData }))
    })
  },
  { condition: (_, { getState }) => selectToken(getState()) }
)

export const rsuDeleteMultiple = createAsyncThunk(
  'adminOrganizationTabRsu/rsuDeleteMultiple',
  async (payload, { getState, dispatch }) => {
    const { rows, orgPatchJson, selectedOrg, fetchPatchOrganization, updateTableData } = payload
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    const invalidRsus = []
    const patchJson = { ...orgPatchJson }
    for (const row of rows) {
      const rsuData = (await getRsuDataByIp(row.ip, token)).body
      if (rsuData?.rsu_data?.organizations?.length > 1) {
        patchJson.rsus_to_remove.push(row.ip)
      } else {
        invalidRsus.push(row.ip)
      }
    }
    if (invalidRsus.length === 0) {
      await fetchPatchOrganization(patchJson)
      dispatch(refresh({ selectedOrg, updateTableData }))
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
  { condition: (_, { getState }) => selectToken(getState()) }
)

export const rsuAddMultiple = createAsyncThunk(
  'adminOrganizationTabRsu/rsuAddMultiple',
  async (payload, { getState, dispatch }) => {
    const { rsuList, orgPatchJson, selectedOrg, fetchPatchOrganization, updateTableData } = payload

    const patchJson = { ...orgPatchJson }
    for (const row of rsuList) {
      let patchJson = orgPatchJson
      patchJson.rsus_to_add.push(row.ip)
    }
    await fetchPatchOrganization(patchJson)
    dispatch(refresh({ selectedOrg, updateTableData }))
  },
  { condition: (_, { getState }) => selectToken(getState()) }
)

export const refresh = createAsyncThunk(
  'adminOrganizationTabRsu/refresh',
  async (payload, { dispatch }) => {
    const { selectedOrg, updateTableData } = payload
    updateTableData(selectedOrg)
    dispatch(getRsuData(selectedOrg))
  },
  { condition: (_, { getState }) => selectToken(getState()) }
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
          let availableRsuList = []
          let counter = 0
          if (rsuData?.rsu_data) {
            for (const rsu of rsuData.rsu_data) {
              const rsuOrgs = rsu?.organizations
              if (!rsuOrgs.includes(action.payload.orgName)) {
                let tempValue = {}
                tempValue.id = counter
                tempValue.ip = rsu.ip
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
