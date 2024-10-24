import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { selectToken } from '../../generalSlices/userSlice'
import EnvironmentVars from '../../EnvironmentVars'
import apiHelper from '../../apis/api-helper'
import { updateTableData as updateIntersectionTableData } from '../adminIntersectionTab/adminIntersectionTabSlice'
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
  apiData: {} as adminEditIntersectionData,
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
 * @param {RootState['adminAddIntersection']} state - The current state of the adminEditIntersection slice.
 * @returns {boolean} - Returns true if the form is valid, otherwise false.
 */
export const validateFormContents = (state: RootState['adminEditIntersection']) => {
  if (state.value.selectedOrganizations.length === 0) {
    return false
  } else {
    return true
  }
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
  let json = data

  if (!json.bbox || !json.bbox.latitude1 || !json.bbox.longitude1 || !json.bbox.latitude2 || !json.bbox.longitude2) {
    delete json.bbox
  }
  if (!json.intersection_name) {
    delete json.intersection_name
  }
  if (!json.origin_ip) {
    delete json.origin_ip
  }

  let organizationsToAdd = []
  let organizationsToRemove = []
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

  let rsusToAdd = []
  let rsusToRemove = []
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

/**
 * Fetches intersection data from the API
 * - Fetches intersection data for a given intersection_id
 *
 * @param {string} intersection_id - The intersection_id of the intersection to fetch.
 * @returns {Promise<{ success: boolean, message: string, data?: adminEditIntersectionData }>} - The success status, message, and intersection data.
 */
export const getIntersectionInfo = createAsyncThunk(
  'adminEditIntersection/getIntersectionInfo',
  async (intersection_id: string, { getState, dispatch }) => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    const data = await apiHelper._getDataWithCodes({
      url: EnvironmentVars.adminIntersection,
      token,
      query_params: { intersection_id },
      additional_headers: { 'Content-Type': 'application/json' },
    })

    switch (data.status) {
      case 200:
        dispatch(adminEditIntersectionSlice.actions.updateStates(data.body))
        return { success: true, message: '', data: data.body }
      default:
        return { success: false, message: data.message }
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

/**
 * Edits an intersection
 * - Edits an intersection with the given intersection_id
 *
 * @param {AdminEditIntersectionBody} json - The intersection data to apply to the edit.
 * @returns {Promise<{ success: boolean, message: string }>} - The success status and message.
 */
export const editIntersection = createAsyncThunk(
  'adminEditIntersection/editIntersection',
  async (json: AdminEditIntersectionBody, { getState, dispatch }) => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    const data = await apiHelper._patchData({
      url: EnvironmentVars.adminIntersection,
      token,
      query_params: { intersection_id: json.orig_intersection_id },
      body: JSON.stringify(json),
    })

    switch (data.status) {
      case 200:
        dispatch(updateIntersectionTableData())
        return { success: true, message: 'Changes were successfully applied!' }
      default:
        return { success: false, message: data.message }
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

/**
 * Submits the intersection form, first validating the form contents and then calling the editIntersection thunk
 *
 * @param {AdminEditIntersectionFormType} data - The intersection form data to submit.
 * @returns {Promise<{ submitAttempt: boolean, success: boolean, message: string }>} - The submit attempt status, success status, and message. The submitAttempt is used to display validation error messages on the form.
 */
export const submitForm = createAsyncThunk(
  'adminEditIntersection/submitForm',
  async (data: AdminEditIntersectionFormType, { getState, dispatch }) => {
    const currentState = getState() as RootState
    if (validateFormContents(currentState.adminEditIntersection)) {
      let json = mapFormToRequestJson(data, currentState.adminEditIntersection)
      let res = await dispatch(editIntersection(json))
      if ((res.payload as any).success) {
        return { submitAttempt: false, success: true, message: 'Intersection Updated Successfully' }
      } else {
        return { submitAttempt: false, success: false, message: (res.payload as any).message }
      }
    } else {
      return { submitAttempt: true, success: false, message: 'Please fill out all required fields' }
    }
  }
)

export const adminEditIntersectionSlice = createSlice({
  name: 'adminEditIntersection',
  initialState: {
    loading: false,
    value: initialState,
  },
  reducers: {
    setSelectedOrganizations: (state, action) => {
      state.value.selectedOrganizations = action.payload
    },
    setSelectedRsus: (state, action) => {
      state.value.selectedRsus = action.payload
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
  extraReducers: (builder) => {
    builder
      .addCase(getIntersectionInfo.pending, (state) => {
        state.loading = true
      })
      .addCase(getIntersectionInfo.fulfilled, (state, action) => {
        state.loading = false
        if (action.payload.success) {
          state.value.apiData = action.payload.data
        }
      })
      .addCase(getIntersectionInfo.rejected, (state) => {
        state.loading = false
      })
      .addCase(editIntersection.pending, (state) => {
        state.loading = true
      })
      .addCase(editIntersection.fulfilled, (state, action) => {
        state.loading = false
      })
      .addCase(editIntersection.rejected, (state) => {
        state.loading = false
      })
      .addCase(submitForm.fulfilled, (state, action) => {
        state.value.submitAttempt = action.payload.submitAttempt
      })
  },
})

export const { setSelectedOrganizations, setSelectedRsus, updateStates } = adminEditIntersectionSlice.actions

export const selectLoading = (state: RootState) => state.adminEditIntersection.loading
export const selectApiData = (state: RootState) => state.adminEditIntersection.value.apiData
export const selectOrganizations = (state: RootState) => state.adminEditIntersection.value.organizations
export const selectSelectedOrganizations = (state: RootState) => state.adminEditIntersection.value.selectedOrganizations
export const selectRsus = (state: RootState) => state.adminEditIntersection.value.rsus
export const selectSelectedRsus = (state: RootState) => state.adminEditIntersection.value.selectedRsus
export const selectSubmitAttempt = (state: RootState) => state.adminEditIntersection.value.submitAttempt

export default adminEditIntersectionSlice.reducer
