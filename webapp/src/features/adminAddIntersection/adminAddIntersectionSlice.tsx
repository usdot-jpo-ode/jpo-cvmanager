import { PayloadAction, createSlice } from '@reduxjs/toolkit'
import { RootState } from '../../store'
import { AdminAddIntersectionForm } from './AdminAddIntersection'
import { AdminIntersection } from '../../models/Intersection'

export type AdminIntersectionCreationInfo = {
  organizations: string[]
  rsus: string[]
}

export type AdminIntersectionKeyedCreationInfo = {
  organizations: { id: number; name: string }[]
  rsus: { id: number; name: string }[]
}

// No changes required currently - just an admin intersection object
export type AdminIntersectionCreationBody = AdminIntersection

const initialState = {
  selectedOrganizations: [] as AdminIntersectionKeyedCreationInfo['organizations'],
  selectedRsus: [] as AdminIntersectionKeyedCreationInfo['rsus'],
}

/**
 * Convert intersection creation info api returned JSON to a keyed format, for use in the intersection creation form
 *
 * @param {AdminIntersectionCreationInfo} apiJson - The intersection creation api response body.
 * @returns {AdminIntersectionKeyedCreationInfo} - Keyed and prepared intersection creation info object.
 */
export const convertApiJsonToKeyedFormat = (
  apiJson: AdminIntersectionCreationInfo
): AdminIntersectionKeyedCreationInfo => {
  if (Object.keys(apiJson).length !== 0) {
    const keyedApiJson = {} as AdminIntersectionKeyedCreationInfo

    let data = []
    for (let i = 0; i < apiJson['organizations'].length; i++) {
      const value = apiJson['organizations'][i]
      const temp = { id: i, name: value }
      data.push(temp)
    }
    keyedApiJson.organizations = data

    data = []
    for (let i = 0; i < apiJson['rsus'].length; i++) {
      const value = apiJson['rsus'][i]
      const temp = { id: i, name: value?.replace('/32', '') }
      data.push(temp)
    }
    keyedApiJson.rsus = data

    return keyedApiJson
  }
}

/**
 * Checks if the intersection creation form is valid
 * - At least one organization is selected
 *
 * No other checks are required, all other data is validated by the form input fields
 *
 * @param {RootState['adminAddIntersection']} state - The current state of the adminAddIntersection slice.
 * @returns {boolean} - Returns true if the form is valid, otherwise false.
 */
export const validateFormContents = (state: RootState['adminAddIntersection']) => {
  return state.value.selectedOrganizations.length !== 0;
}

/**
 * Map JSON form entry data to intersection creation request body
 *
 * @param {AdminAddIntersectionForm} data - The form data for adding an intersection.
 * @param {RootState['adminAddIntersection']} state - The current state of the adminAddIntersection slice.
 * @returns {AdminIntersectionCreationBody} - The updated JSON object for intersection creation.
 */
export const mapFormToRequestJson = (
  data: AdminAddIntersectionForm,
  state: RootState['adminAddIntersection']
): AdminIntersectionCreationBody => {
  const json: any = data
  // creating geo_position object from latitudes and longitude
  json.intersection_id = Number(data.intersection_id)
  json.ref_pt = {
    latitude: Number(data.ref_pt.latitude),
    longitude: Number(data.ref_pt.longitude),
  }
  if (data.bbox?.latitude1 && data.bbox?.longitude1 && data.bbox?.latitude2 && data.bbox?.longitude2) {
    json.bbox = {
      latitude1: Number(data.bbox.latitude1),
      longitude1: Number(data.bbox.longitude1),
      latitude2: Number(data.bbox.latitude2),
      longitude2: Number(data.bbox.longitude2),
    }
  }

  const tempOrganizations = []
  for (let i = 0; i < state.value.selectedOrganizations.length; i++) {
    tempOrganizations.push(state.value.selectedOrganizations[i].name)
  }

  json.organizations = tempOrganizations

  const tempRsus = []
  for (let i = 0; i < state.value.selectedRsus.length; i++) {
    tempRsus.push(state.value.selectedRsus[i].name)
  }

  json.rsus = tempRsus

  return json
}

export const adminAddIntersectionSlice = createSlice({
  name: 'adminAddIntersection',
  initialState: {
    loading: false,
    value: initialState,
  },
  reducers: {
    updateSelectedOrganizations: (
      state,
      action: PayloadAction<
        {
          id: number
          name: string
        }[]
      >
    ) => {
      state.value.selectedOrganizations = action.payload
    },
    updateSelectedRsus: (
      state,
      action: PayloadAction<
        {
          id: number
          name: string
        }[]
      >
    ) => {
      state.value.selectedRsus = action.payload
    },
    resetForm: (state) => {
      state.value.selectedOrganizations = []
      state.value.selectedRsus = []
    },
  },
})

export const { resetForm, updateSelectedOrganizations, updateSelectedRsus } = adminAddIntersectionSlice.actions

export const selectSelectedOrganizations = (state: RootState) => state.adminAddIntersection.value.selectedOrganizations
export const selectSelectedRsus = (state: RootState) => state.adminAddIntersection.value.selectedRsus
export const selectLoading = (state: RootState) => state.adminAddIntersection.loading

export default adminAddIntersectionSlice.reducer
