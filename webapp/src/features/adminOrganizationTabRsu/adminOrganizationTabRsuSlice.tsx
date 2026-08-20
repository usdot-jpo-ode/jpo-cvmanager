import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { selectToken } from '../../generalSlices/userSlice'
import { RootState } from '../../store'
import {
  AdminOrgRsuDeleteMultiple,
  AdminOrgRsuDeleteSingle,
  AdminOrgRsuWithId,
  AdminOrgTabRsuAddMultiple,
} from './AdminOrganizationTabRsuTypes'
import { adminOrgPatch, editOrg } from '../adminOrganizationTab/adminOrganizationTabSlice'
import {
  ORGANIZATION_API_AVAILABLE_RSU_LIST_TAG,
  ORGANIZATION_API_RSU_LIST_TAG,
  ORGANIZATION_API_RSU_TAG,
  organizationApiSlice,
} from '../api/organizationApiSlice'

const initialState = {
  selectedRsuList: [] as AdminOrgRsuWithId[],
}

export const rsuDeleteSingle = createAsyncThunk(
  'adminOrganizationTabRsu/rsuDeleteSingle',
  async (payload: AdminOrgRsuDeleteSingle, { dispatch }) => {
    const { rsu, selectedOrg, selectedOrgEmail, updateTableData } = payload

    const promises = []
    const rsuData = (await dispatch(organizationApiSlice.endpoints.getRsuOrganizations.initiate(rsu.ip)))?.data ?? []

    if (rsuData.length > 1) {
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
    // Invalidate RTK Query cache
    dispatch(
      organizationApiSlice.util.invalidateTags([
        ORGANIZATION_API_RSU_LIST_TAG,
        ORGANIZATION_API_AVAILABLE_RSU_LIST_TAG,
        { type: ORGANIZATION_API_RSU_TAG, id: rsu.ip },
      ])
    )

    const res = await Promise.all(promises)
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
  async (payload: AdminOrgRsuDeleteMultiple, { dispatch }) => {
    const { rows, selectedOrg, selectedOrgEmail, updateTableData } = payload

    const invalidRsus = []
    const patchJson: adminOrgPatch = {
      name: selectedOrg,
      email: selectedOrgEmail,
      rsus_to_remove: [],
    }
    for (const row of rows) {
      const rsuData = (await dispatch(organizationApiSlice.endpoints.getRsuOrganizations.initiate(row.ip)))?.data ?? []
      console.error('RSU Data for', row.ip, rsuData)
      if (rsuData.length > 1) {
        patchJson.rsus_to_remove.push(row.ip)
      } else {
        invalidRsus.push(row.ip)
      }
    }
    if (invalidRsus.length === 0) {
      const res = await dispatch(editOrg(patchJson))
      dispatch(refresh({ selectedOrg, updateTableData }))
      if ((res.payload as any).success) {
        const rsuTags = rows.map((row) => ({ type: ORGANIZATION_API_RSU_TAG, id: row.ip }))
        dispatch(
          organizationApiSlice.util.invalidateTags([
            ORGANIZATION_API_RSU_LIST_TAG,
            ORGANIZATION_API_AVAILABLE_RSU_LIST_TAG,
            ...rsuTags,
          ])
        )
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
  async (payload: AdminOrgTabRsuAddMultiple, { dispatch }) => {
    const { rsuList, selectedOrg, selectedOrgEmail, updateTableData } = payload

    const patchJson: adminOrgPatch = {
      name: selectedOrg,
      email: selectedOrgEmail,
      rsus_to_add: [],
    }
    for (const row of rsuList) {
      patchJson.rsus_to_add.push(row.ip)
    }
    const res = await dispatch(editOrg(patchJson))
    dispatch(refresh({ selectedOrg, updateTableData }))
    if ((res.payload as any).success) {
      const rsuTags = rsuList.map((rsu) => ({ type: ORGANIZATION_API_RSU_TAG, id: rsu.ip }))
      dispatch(
        organizationApiSlice.util.invalidateTags([
          ORGANIZATION_API_RSU_LIST_TAG,
          ORGANIZATION_API_AVAILABLE_RSU_LIST_TAG,
          ...rsuTags,
        ])
      )
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
    builder.addCase(refresh.fulfilled, (state) => {
      state.value.selectedRsuList = []
    })
  },
})

export const { setSelectedRsuList } = adminOrganizationTabRsuSlice.actions

export const selectLoading = (state: RootState) => state.adminOrganizationTabRsu.loading
export const selectSelectedRsuList = (state: RootState) => state.adminOrganizationTabRsu.value.selectedRsuList

export default adminOrganizationTabRsuSlice.reducer
