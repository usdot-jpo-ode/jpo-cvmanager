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

const initialState = {
  apiData: {} as adminEditIntersectionData,
  organizations: [] as { name: string }[],
  selectedOrganizations: [] as { name: string }[],
  rsus: [] as { name: string }[],
  selectedRsus: [] as { name: string }[],
  submitAttempt: false,
}

function isEmptyObject(obj: any): boolean {
  return obj && Object.keys(obj).length === 0
}

export const checkForm = (state: RootState['adminEditIntersection']) => {
  if (state.value.selectedOrganizations.length === 0) {
    return false
  } else {
    return true
  }
}

export const updateJson = (data: AdminEditIntersectionFormType, state: RootState['adminEditIntersection']) => {
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
    const formattedRsu = rsu?.replace('/32', '')
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

export const editIntersection = createAsyncThunk(
  'adminEditIntersection/editIntersection',
  async (json: { orig_intersection_id: string }, { getState, dispatch }) => {
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

export const submitForm = createAsyncThunk(
  'adminEditIntersection/submitForm',
  async (data: AdminEditIntersectionFormType, { getState, dispatch }) => {
    const currentState = getState() as RootState
    if (checkForm(currentState.adminEditIntersection)) {
      let json = updateJson(data, currentState.adminEditIntersection)
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
        return { name: val?.replace('/32', '') }
      })

      state.value.selectedOrganizations = apiData.intersection_data.organizations.map((val) => {
        return { name: val }
      })
      state.value.selectedRsus = apiData.intersection_data.rsus.map((val) => {
        return { name: val?.replace('/32', '') }
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
