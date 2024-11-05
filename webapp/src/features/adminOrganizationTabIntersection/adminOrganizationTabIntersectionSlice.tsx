import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { selectToken } from '../../generalSlices/userSlice'
import EnvironmentVars from '../../EnvironmentVars'
import apiHelper from '../../apis/api-helper'
import { RootState } from '../../store'
import {
  AdminOrgIntersectionDeleteMultiple,
  AdminOrgIntersectionDeleteSingle,
  AdminOrgIntersectionWithId,
  AdminOrgTabIntersectionAddMultiple,
} from './AdminOrganizationTabIntersectionTypes'
import { adminOrgPatch, editOrg } from '../adminOrganizationTab/adminOrganizationTabSlice'

const initialState = {
  availableIntersectionList: [] as AdminOrgIntersectionWithId[],
  selectedIntersectionList: [] as AdminOrgIntersectionWithId[],
}

export const getIntersectionDataById = async (intersection_id: string, token: string) => {
  const data = await apiHelper._getDataWithCodes({
    url: EnvironmentVars.adminIntersection,
    token,
    query_params: { intersection_id },
  })

  return data
}

export const getIntersectionData = createAsyncThunk(
  'adminOrganizationTabIntersection/getIntersectionData',
  async (orgName: string, { getState, dispatch }) => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    const data = await getIntersectionDataById('all', token)

    switch (data.status) {
      case 200:
        return { success: true, message: '', data: data.body, orgName }
      default:
        return { success: false, message: data.message }
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const intersectionDeleteSingle = createAsyncThunk(
  'adminOrganizationTabIntersection/intersectionDeleteSingle',
  async (payload: AdminOrgIntersectionDeleteSingle, { getState, dispatch }) => {
    const { intersection, selectedOrg, selectedOrgEmail, updateTableData } = payload
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    let promises = []
    const intersectionData = (await getIntersectionDataById(intersection.intersection_id, token)).body
    if (intersectionData?.intersection_data?.organizations?.length > 1) {
      const patchJson: adminOrgPatch = {
        name: selectedOrg,
        email: selectedOrgEmail,
        intersections_to_remove: [intersection.intersection_id],
      }
      promises.push(dispatch(editOrg(patchJson)))
    } else {
      alert(
        'Cannot remove Intersection ' +
          intersection.intersection_id +
          ' from ' +
          selectedOrg +
          ' because it must belong to at least one organization.'
      )
    }
    var res = await Promise.all(promises)
    dispatch(refresh({ selectedOrg, updateTableData }))

    if ((res[0].payload as any).success) {
      return { success: true, message: 'Intersection deleted successfully' }
    } else {
      return { success: false, message: 'Failed to delete Intersection' }
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const intersectionDeleteMultiple = createAsyncThunk(
  'adminOrganizationTabIntersection/intersectionDeleteMultiple',
  async (payload: AdminOrgIntersectionDeleteMultiple, { getState, dispatch }) => {
    const { rows, selectedOrg, selectedOrgEmail, updateTableData } = payload
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    const invalidIntersections = []
    const patchJson: adminOrgPatch = {
      name: selectedOrg,
      email: selectedOrgEmail,
      intersections_to_remove: [],
    }
    for (const row of rows) {
      const intersectionData = (await getIntersectionDataById(row.intersection_id, token)).body
      if (intersectionData?.intersection_data?.organizations?.length > 1) {
        patchJson.intersections_to_remove.push(row.intersection_id)
      } else {
        invalidIntersections.push(row.intersection_id)
      }
    }
    if (invalidIntersections.length === 0) {
      var res = await dispatch(editOrg(patchJson))
      dispatch(refresh({ selectedOrg, updateTableData }))
      if ((res.payload as any).success) {
        return { success: true, message: 'Intersection(s) deleted successfully' }
      } else {
        return { success: false, message: 'Failed to delete Intersection(s)' }
      }
    } else {
      alert(
        'Cannot remove Intersection(s) ' +
          invalidIntersections.map((ip) => ip.toString()).join(', ') +
          ' from ' +
          selectedOrg +
          ' because they must belong to at least one organization.'
      )
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const intersectionAddMultiple = createAsyncThunk(
  'adminOrganizationTabIntersection/intersectionAddMultiple',
  async (payload: AdminOrgTabIntersectionAddMultiple, { getState, dispatch }) => {
    const { intersectionList, selectedOrg, selectedOrgEmail, updateTableData } = payload

    const patchJson: adminOrgPatch = {
      name: selectedOrg,
      email: selectedOrgEmail,
      intersections_to_add: [],
    }
    for (const row of intersectionList) {
      patchJson.intersections_to_add.push(row.intersection_id)
    }
    var res = await dispatch(editOrg(patchJson))
    dispatch(refresh({ selectedOrg, updateTableData }))
    if ((res.payload as any).success) {
      return { success: true, message: 'Intersection(s) added successfully' }
    } else {
      return { success: false, message: 'Failed to add Intersection(s)' }
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const refresh = createAsyncThunk(
  'adminOrganizationTabIntersection/refresh',
  async (
    payload: {
      selectedOrg: string
      updateTableData: (selectedOrg: string) => void
    },
    { dispatch }
  ) => {
    const { selectedOrg, updateTableData } = payload
    updateTableData(selectedOrg)
    dispatch(getIntersectionData(selectedOrg))
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const adminOrganizationTabIntersectionSlice = createSlice({
  name: 'adminOrganizationTabIntersection',
  initialState: {
    loading: false,
    value: initialState,
  },
  reducers: {
    setSelectedIntersectionList: (state, action) => {
      state.value.selectedIntersectionList = action.payload
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(getIntersectionData.pending, (state) => {
        state.loading = true
      })
      .addCase(getIntersectionData.fulfilled, (state, action) => {
        state.loading = false
        if (action.payload.success) {
          const intersectionData = action.payload.data
          let availableIntersectionList = [] as AdminOrgIntersectionWithId[]
          let counter = 0
          if (intersectionData?.intersection_data) {
            for (const intersection of intersectionData.intersection_data) {
              const intersectionOrgs = intersection?.organizations
              if (!intersectionOrgs.includes(action.payload.orgName)) {
                let tempValue = {
                  id: counter,
                  intersection_id: intersection.intersection_id,
                } as AdminOrgIntersectionWithId
                availableIntersectionList.push(tempValue)
                counter += 1
              }
            }
          }
          state.value.availableIntersectionList = availableIntersectionList
        }
      })
      .addCase(getIntersectionData.rejected, (state) => {
        state.loading = false
      })
      .addCase(refresh.fulfilled, (state) => {
        state.value.selectedIntersectionList = []
      })
  },
})

export const { setSelectedIntersectionList } = adminOrganizationTabIntersectionSlice.actions

export const selectLoading = (state: RootState) => state.adminOrganizationTabIntersection.loading
export const selectAvailableIntersectionList = (state: RootState) =>
  state.adminOrganizationTabIntersection.value.availableIntersectionList
export const selectSelectedIntersectionList = (state: RootState) =>
  state.adminOrganizationTabIntersection.value.selectedIntersectionList

export default adminOrganizationTabIntersectionSlice.reducer
