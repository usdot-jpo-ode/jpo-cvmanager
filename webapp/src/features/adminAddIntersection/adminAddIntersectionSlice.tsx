import { PayloadAction, createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { selectToken } from '../../generalSlices/userSlice'
import EnvironmentVars from '../../EnvironmentVars'
import apiHelper from '../../apis/api-helper'
import { updateTableData as updateIntersectionTableData } from '../adminIntersectionTab/adminIntersectionTabSlice'
import { RootState } from '../../store'
import { AdminAddIntersectionForm } from './AdminAddIntersection'

export type AdminIntersectionCreationInfo = {
  organizations: string[]
  rsus: string[]
}

export type AdminIntersectionKeyedCreationInfo = {
  organizations: { id: number; name: string }[]
  rsus: { id: number; name: string }[]
}

export type AdminIntersectionCreationBody = {
  intersection_id: string
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
  organizations: string[]
  rsus: string[]
}

const initialState = {
  apiData: {} as AdminIntersectionKeyedCreationInfo,
  selectedOrganizations: [] as AdminIntersectionKeyedCreationInfo['organizations'],
  selectedRsus: [] as AdminIntersectionKeyedCreationInfo['rsus'],
  submitAttempt: false,
}

export const updateApiJson = (apiJson: AdminIntersectionCreationInfo): AdminIntersectionKeyedCreationInfo => {
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

export const checkForm = (state: RootState['adminAddIntersection']) => {
  if (state.value.selectedOrganizations.length === 0) {
    return false
  } else {
    return true
  }
}

export const updateJson = (
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
    return updateApiJson(data)
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

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
        dispatch(adminAddIntersectionSlice.actions.resetForm())
        dispatch(updateIntersectionTableData())
        reset()
        return { success: true, message: '' }
      default:
        return { success: false, message: data.message }
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const submitForm = createAsyncThunk(
  'adminAddIntersection/submitForm',
  async (payload: { data: AdminAddIntersectionForm; reset: () => void }, { getState, dispatch }) => {
    const { data, reset } = payload

    const currentState = getState() as RootState
    if (checkForm(currentState.adminAddIntersection)) {
      let json = updateJson(data, currentState.adminAddIntersection)
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
