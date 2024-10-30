import { PayloadAction, createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { selectToken } from '../../generalSlices/userSlice'
import EnvironmentVars from '../../EnvironmentVars'
import apiHelper from '../../apis/api-helper'
import { updateTableData as updateIntersectionTableData } from '../adminIntersectionTab/adminIntersectionTabSlice'
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
  apiData: {} as AdminIntersectionKeyedCreationInfo,
  selectedOrganizations: [] as AdminIntersectionKeyedCreationInfo['organizations'],
  selectedRsus: [] as AdminIntersectionKeyedCreationInfo['rsus'],
  submitAttempt: false,
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
    let keyedApiJson = {} as AdminIntersectionKeyedCreationInfo

    let data = []
    for (let i = 0; i < apiJson['organizations'].length; i++) {
      let value = apiJson['organizations'][i]
      let temp = { id: i, name: value }
      data.push(temp)
    }
    keyedApiJson.organizations = data

    data = []
    for (let i = 0; i < apiJson['rsus'].length; i++) {
      let value = apiJson['rsus'][i]
      let temp = { id: i, name: value?.replace('/32', '') }
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
  if (state.value.selectedOrganizations.length === 0) {
    return false
  } else {
    return true
  }
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

  let tempOrganizations = []
  for (var i = 0; i < state.value.selectedOrganizations.length; i++) {
    tempOrganizations.push(state.value.selectedOrganizations[i].name)
  }

  json.organizations = tempOrganizations

  let tempRsus = []
  for (var i = 0; i < state.value.selectedRsus.length; i++) {
    tempRsus.push(state.value.selectedRsus[i].name)
  }

  json.rsus = tempRsus

  return json
}

/**
 * Fetches the intersection creation data from the API, then sets the response to 'apiData' in the state.
 * - This data includes the organizations and rsus available for selection in the intersection creation form.
 *
 * @returns {Promise<AdminIntersectionKeyedCreationInfo>} - The intersection creation data in keyed format.
 */
export const getIntersectionCreationData = createAsyncThunk(
  'adminAddIntersection/getIntersectionCreationData',
  async (_, { getState }) => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    const data = (await apiHelper._getData({
      url: EnvironmentVars.adminAddIntersection,
      token,
      additional_headers: { 'Content-Type': 'application/json' },
    })) as AdminIntersectionCreationInfo
    return convertApiJsonToKeyedFormat(data)
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

/**
 * Creates an intersection using the API, then resets the form and updates the intersection table data.
 * If the intersection creation is successful, the form is reset and the intersection table data is updated.
 * If the intersection creation fails, the error message is returned.
 *
 * @param {AdminIntersectionCreationBody} payload.json - The intersection creation request body.
 * @param {() => void} payload.reset - The function to reset the react-hook-form.
 * @returns {Promise<{ success?: boolean, message?: string }>} - The success status and message of the intersection creation.
 */
export const createIntersection = createAsyncThunk(
  'adminAddIntersection/createIntersection',
  async (payload: { json: AdminIntersectionCreationBody; reset: () => void }, { getState, dispatch }) => {
    const { json, reset } = payload
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    if (!json?.bbox) {
      delete json.bbox
    }
    if (!json?.intersection_name) {
      delete json.intersection_name
    }
    if (!json?.origin_ip) {
      delete json.origin_ip
    }

    const data = await apiHelper._postData({
      url: EnvironmentVars.adminAddIntersection,
      body: JSON.stringify(json),
      token,
    })
    switch (data.status) {
      case 200:
        dispatch(adminAddIntersectionSlice.actions.resetForm()) // clear state data for form
        dispatch(updateIntersectionTableData())
        reset() // clear persistent form data from react-hook-form
        return { success: true, message: '' }
      default:
        return { success: false, message: data.message }
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

/**
 * Submits the intersection creation form, then returns the success status and message.
 *
 * If the form is valid, the intersection is created and the success status and message are returned.
 * If the form is invalid, the error message is returned.
 *
 * @param {AdminAddIntersectionForm} payload.data - The form data for adding an intersection.
 * @param {() => void} payload.reset - The function to reset the react-hook-form.
 * @returns {Promise<{ submitAttempt: boolean, success: boolean, message: string }>} - The success status and message of the intersection creation. submitAttempt is used to display form invalid message
 *
 */
export const submitForm = createAsyncThunk(
  'adminAddIntersection/submitForm',
  async (payload: { data: AdminAddIntersectionForm; reset: () => void }, { getState, dispatch }) => {
    const { data, reset } = payload

    const currentState = getState() as RootState
    if (validateFormContents(currentState.adminAddIntersection)) {
      let json = mapFormToRequestJson(data, currentState.adminAddIntersection)
      let res = await dispatch(createIntersection({ json, reset }))
      if ((res.payload as any).success) {
        return { submitAttempt: false, success: true, message: 'Intersection Created Successfully' }
      } else {
        return { submitAttempt: false, success: false, message: (res.payload as any).message }
      }
    } else {
      return { submitAttempt: true, success: false, message: 'Please fill out all required fields' }
    }
  }
)

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
  extraReducers: (builder) => {
    builder
      .addCase(getIntersectionCreationData.pending, (state) => {
        state.loading = true
      })
      .addCase(getIntersectionCreationData.fulfilled, (state, action) => {
        state.loading = false
        state.value.apiData = action.payload
      })
      .addCase(getIntersectionCreationData.rejected, (state) => {
        state.loading = false
      })
      .addCase(createIntersection.pending, (state) => {
        state.loading = true
      })
      .addCase(createIntersection.fulfilled, (state, action) => {
        state.loading = false
      })
      .addCase(createIntersection.rejected, (state) => {
        state.loading = false
      })
      .addCase(submitForm.fulfilled, (state, action) => {
        state.value.submitAttempt = action.payload.submitAttempt
      })
  },
})

export const { resetForm, updateSelectedOrganizations, updateSelectedRsus } = adminAddIntersectionSlice.actions

export const selectApiData = (state: RootState) => state.adminAddIntersection.value.apiData
export const selectOrganizations = (state: RootState) => state.adminAddIntersection.value.apiData?.organizations ?? []
export const selectRsus = (state: RootState) => state.adminAddIntersection.value.apiData?.rsus ?? []

export const selectSelectedOrganizations = (state: RootState) => state.adminAddIntersection.value.selectedOrganizations
export const selectSelectedRsus = (state: RootState) => state.adminAddIntersection.value.selectedRsus
export const selectSubmitAttempt = (state: RootState) => state.adminAddIntersection.value.submitAttempt
export const selectLoading = (state: RootState) => state.adminAddIntersection.loading

export default adminAddIntersectionSlice.reducer
