import { createSlice } from '@reduxjs/toolkit'
import { RootState } from '../../store'
import { AdminEditIntersectionFormType } from './AdminEditIntersection'

export type adminEditIntersectionData = {
  intersection_data: AdminEditIntersectionFormType
  allowed_selections: {
    organizations: string[]
    rsus: string[]
  }
}

export type AdminEditIntersectionBody = {
  intersection_id: string
  orig_intersection_id: string
  ref_pt: {
    latitude: string
    longitude: string
  }
  bbox?: {
    latitude1: string
    longitude1: string
    latitude2: string
    longitude2: string
  }
  intersection_name?: string
  origin_ip?: string
  organizations_to_add: string[]
  organizations_to_remove: string[]
  rsus_to_add: string[]
  rsus_to_remove: string[]
}

const initialState = {
  apiData: undefined as adminEditIntersectionData | undefined,
  organizations: [] as { name: string }[],
  selectedOrganizations: [] as { name: string }[],
  rsus: [] as { name: string }[],
  selectedRsus: [] as { name: string }[],
  submitAttempt: false,
}

/**
 * Checks if the intersection modification form is valid
 * - At least one organization is selected
 *
 * No other checks are required, all other data is validated by the form input fields
 *
 * @param {RootState['adminEditIntersection']} state - The current state of the adminEditIntersection slice.
 * @returns {boolean} - Returns true if the form is valid, otherwise false.
 */
export const validateFormContents = (state: RootState['adminEditIntersection']) => {
  return state.value.selectedOrganizations.length !== 0;
}

/**
 * Map JSON form entry data to intersection edit request body
 * - Remove any optional empty fields
 * - Add lists of organizations and rsus to add/remove
 * - Ensure RSU ips have removed /32 from the end for api compatibility
 *
 * @param {AdminEditIntersectionFormType} data - The form data for editing an intersection.
 * @param {RootState['adminEditIntersection']} state - The current state of the adminEditIntersection slice.
 * @returns {AdminEditIntersectionBody} - The updated JSON object for intersection editing.
 */
export const mapFormToRequestJson = (
  data: AdminEditIntersectionFormType,
  state: RootState['adminEditIntersection']
): AdminEditIntersectionBody => {
  const json = { ...data }

  if (!json.bbox || !json.bbox.latitude1 || !json.bbox.longitude1 || !json.bbox.latitude2 || !json.bbox.longitude2) {
    delete json.bbox
  }
  if (!json.intersection_name) {
    delete json.intersection_name
  }
  if (!json.origin_ip) {
    delete json.origin_ip
  }

  const organizationsToAdd = []
  const organizationsToRemove = []
  for (const org of state.value.apiData.allowed_selections.organizations) {
    if (
      state.value.selectedOrganizations.some((e) => e.name === org) &&
      !state.value.apiData.intersection_data.organizations.includes(org)
    ) {
      organizationsToAdd.push(org)
    }
    if (
      state.value.apiData.intersection_data.organizations.includes(org) &&
      state.value.selectedOrganizations.some((e) => e.name === org) === false
    ) {
      organizationsToRemove.push(org)
    }
  }

  json.organizations_to_add = organizationsToAdd
  json.organizations_to_remove = organizationsToRemove

  const rsusToAdd = []
  const rsusToRemove = []
  for (const rsu of state.value.apiData.allowed_selections.rsus) {
    const formattedRsu = rsu?.replace('/32', '') // Remove /32 from the end of the RSU name for comparison
    if (
      state.value.selectedRsus.some((e) => e.name === formattedRsu) &&
      !state.value.apiData.intersection_data.rsus.includes(formattedRsu)
    ) {
      rsusToAdd.push(formattedRsu)
    }
    if (
      state.value.apiData.intersection_data.rsus.includes(formattedRsu) &&
      state.value.selectedRsus.some((e) => e.name === formattedRsu) === false
    ) {
      rsusToRemove.push(formattedRsu)
    }
  }

  json.rsus_to_add = rsusToAdd
  json.rsus_to_remove = rsusToRemove

  return json
}

export const adminEditIntersectionSlice = createSlice({
  name: 'adminEditIntersection',
  initialState: {
    value: initialState,
  },
  reducers: {
    clear: (state) => {
      state.value = initialState
    },
    setSelectedOrganizations: (state, action) => {
      state.value.selectedOrganizations = action.payload
    },
    setSelectedRsus: (state, action) => {
      state.value.selectedRsus = action.payload
    },
    setSubmitAttempt: (state, action: { payload: boolean }) => {
      state.value.submitAttempt = action.payload
    },
    updateStates: (state, action: { payload: adminEditIntersectionData }) => {
      const apiData = action.payload

      const allowedSelections = apiData.allowed_selections
      state.value.organizations = allowedSelections.organizations.map((val) => {
        return { name: val }
      })
      state.value.rsus = allowedSelections.rsus.map((val) => {
        return { name: val?.replace('/32', '') } // Remove /32 from the end of the RSU name for human readability
      })

      state.value.selectedOrganizations = apiData.intersection_data.organizations.map((val) => {
        return { name: val }
      })
      state.value.selectedRsus = apiData.intersection_data.rsus.map((val) => {
        return { name: val?.replace('/32', '') } // Remove /32 from the end of the RSU name for human readability
      })

      state.value.apiData = apiData
    },
  },
})

export const { clear, setSelectedOrganizations, setSelectedRsus, setSubmitAttempt, updateStates } =
  adminEditIntersectionSlice.actions

export const selectApiData = (state: RootState) => state.adminEditIntersection.value.apiData
export const selectOrganizations = (state: RootState) => state.adminEditIntersection.value.organizations
export const selectSelectedOrganizations = (state: RootState) => state.adminEditIntersection.value.selectedOrganizations
export const selectRsus = (state: RootState) => state.adminEditIntersection.value.rsus
export const selectSelectedRsus = (state: RootState) => state.adminEditIntersection.value.selectedRsus
export const selectSubmitAttempt = (state: RootState) => state.adminEditIntersection.value.submitAttempt

export default adminEditIntersectionSlice.reducer
