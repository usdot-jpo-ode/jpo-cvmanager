import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { selectToken } from '../../generalSlices/userSlice'
import EnvironmentVars from '../../EnvironmentVars'
import apiHelper from '../../apis/api-helper'
import { updateTableData as updateRsuTableData } from '../adminRsuTab/adminRsuTabSlice'
import { RootState } from '../../store'
import { AdminOrgRsu } from '../adminOrganizationTab/adminOrganizationTabSlice'
import { AdminRsu } from '../../types/Rsu'
import { AdminAddRsuForm } from './AdminAddRsu'

export type AdminRsuCreationInfo = {
  primary_routes: string[]
  rsu_models: string[]
  ssh_credential_groups: string[]
  snmp_credential_groups: string[]
  snmp_version_groups: string[]
  organizations: string[]
}

export type AdminRsuKeyedCreationInfo = {
  primary_routes: { id: number; name: string }[]
  rsu_models: { id: number; name: string }[]
  ssh_credential_groups: { id: number; name: string }[]
  snmp_credential_groups: { id: number; name: string }[]
  snmp_version_groups: { id: number; name: string }[]
  organizations: { id: number; name: string }[]
}

export type AdminRsuCreationBody = {
  ip: string
  milepost: number
  serial_number: string
  scms_id: string
  geo_position: {
    latitude: number
    longitude: number
  }
  primary_route: string
  model: string
  ssh_credential_group: string
  snmp_credential_group: string
  snmp_version_group: string
  organizations: string[]
}

const initialState = {
  successMsg: '',
  apiData: {} as AdminRsuKeyedCreationInfo,
  errorState: false,
  errorMsg: '',
  selectedRoute: 'Select Route (Required)',
  otherRouteDisabled: true,
  selectedModel: 'Select RSU Model (Required)',
  selectedSshGroup: 'Select SSH Group (Required)',
  selectedSnmpGroup: 'Select SNMP Group (Required)',
  selectedSnmpVersion: 'Select SNMP Version (Required)',
  selectedOrganizations: [] as AdminRsuKeyedCreationInfo['organizations'],
  submitAttempt: false,
}

export const updateApiJson = (apiJson: AdminRsuCreationInfo): AdminRsuKeyedCreationInfo => {
  if (Object.keys(apiJson).length !== 0) {
    let keyedApiJson = {} as AdminRsuKeyedCreationInfo

    let data = []
    for (let i = 0; i < apiJson['primary_routes'].length; i++) {
      let value = apiJson['primary_routes'][i]
      let temp = { id: i, name: value }
      data.push(temp)
    }
    keyedApiJson.primary_routes = data

    data = []
    for (let i = 0; i < apiJson['rsu_models'].length; i++) {
      let value = apiJson['rsu_models'][i]
      let temp = { id: i, name: value }
      data.push(temp)
    }
    keyedApiJson.rsu_models = data

    data = []
    for (let i = 0; i < apiJson['ssh_credential_groups'].length; i++) {
      let value = apiJson['ssh_credential_groups'][i]
      let temp = { id: i, name: value }
      data.push(temp)
    }
    keyedApiJson.ssh_credential_groups = data

    data = []
    for (let i = 0; i < apiJson['snmp_credential_groups'].length; i++) {
      let value = apiJson['snmp_credential_groups'][i]
      let temp = { id: i, name: value }
      data.push(temp)
    }
    keyedApiJson.snmp_credential_groups = data

    data = []
    for (let i = 0; i < apiJson['snmp_version_groups'].length; i++) {
      let value = apiJson['snmp_version_groups'][i]
      let temp = { id: i, name: value }
      data.push(temp)
    }
    keyedApiJson.snmp_version_groups = data

    data = []
    for (let i = 0; i < apiJson['organizations'].length; i++) {
      let value = apiJson['organizations'][i]
      let temp = { id: i, name: value }
      data.push(temp)
    }
    keyedApiJson.organizations = data

    return keyedApiJson
  }
}

export const checkForm = (state: RootState['adminAddRsu']) => {
  if (state.value.selectedRoute === 'Select Route (Required)') {
    return false
  } else if (state.value.selectedModel === 'Select RSU Model (Required)') {
    return false
  } else if (state.value.selectedSshGroup === 'Select SSH Group (Required)') {
    return false
  } else if (state.value.selectedSnmpGroup === 'Select SNMP Group (Required)') {
    return false
  } else if (state.value.selectedSnmpVersion === 'Select SNMP Version (Required)') {
    return false
  } else if (state.value.selectedOrganizations.length === 0) {
    return false
  } else {
    return true
  }
}

export const updateJson = (data: AdminAddRsuForm, state: RootState['adminAddRsu']): AdminRsuCreationBody => {
  const json: any = data
  // creating geo_position object from latitudes and longitude
  json.geo_position = {
    latitude: Number(data.latitude),
    longitude: Number(data.longitude),
  }
  delete json.latitude
  delete json.longitude
  if (state.value.selectedRoute !== 'Other') {
    json.primary_route = state.value.selectedRoute
  }
  json.milepost = Number(data.milepost)
  json.model = state.value.selectedModel
  json.ssh_credential_group = state.value.selectedSshGroup
  json.snmp_credential_group = state.value.selectedSnmpGroup
  json.snmp_version_group = state.value.selectedSnmpVersion

  let tempOrganizations = []
  for (var i = 0; i < state.value.selectedOrganizations.length; i++) {
    tempOrganizations.push(state.value.selectedOrganizations[i].name)
  }

  json.organizations = tempOrganizations

  return json
}

export const getRsuCreationData = createAsyncThunk(
  'adminAddRsu/getRsuCreationData',
  async (_, { getState }) => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    const data = (await apiHelper._getData({
      url: EnvironmentVars.adminAddRsu,
      token,
      additional_headers: { 'Content-Type': 'application/json' },
    })) as AdminRsuCreationInfo
    return updateApiJson(data)
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const createRsu = createAsyncThunk(
  'adminAddRsu/createRsu',
  async (payload: { json: AdminRsuCreationBody; reset: () => void }, { getState, dispatch }) => {
    const { json, reset } = payload
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    const data = await apiHelper._postData({
      url: EnvironmentVars.adminAddRsu,
      body: JSON.stringify(json),
      token,
    })
    switch (data.status) {
      case 200:
        dispatch(adminAddRsuSlice.actions.resetForm())
        setTimeout(() => dispatch(setSuccessMsg('')), 5000)
        dispatch(updateRsuTableData())
        reset()
        return { success: true, message: '' }
      default:
        return { success: false, message: data.message }
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const submitForm = createAsyncThunk(
  'adminAddRsu/submitForm',
  async (payload: { data: AdminAddRsuForm; reset: () => void }, { getState, dispatch }) => {
    const { data, reset } = payload

    const currentState = getState() as RootState
    if (checkForm(currentState.adminAddRsu)) {
      let json = updateJson(data, currentState.adminAddRsu)
      dispatch(createRsu({ json, reset }))
      return false
    } else {
      return true
    }
  }
)

export const adminAddRsuSlice = createSlice({
  name: 'adminAddRsu',
  initialState: {
    loading: false,
    value: initialState,
  },
  reducers: {
    setSuccessMsg: (state, action) => {
      state.value.successMsg = action.payload
    },
    updateSelectedRoute: (state, action) => {
      state.value.selectedRoute = action.payload
      state.value.otherRouteDisabled = action.payload === 'Other'
    },
    updateSelectedModel: (state, action) => {
      state.value.selectedModel = action.payload
    },
    updateSelectedSshGroup: (state, action) => {
      state.value.selectedSshGroup = action.payload
    },
    updateSelectedSnmpGroup: (state, action) => {
      state.value.selectedSnmpGroup = action.payload
    },
    updateSelectedSnmpVersion: (state, action) => {
      state.value.selectedSnmpVersion = action.payload
    },
    updateSelectedOrganizations: (state, action) => {
      state.value.selectedOrganizations = action.payload
    },
    resetForm: (state) => {
      state.value.selectedRoute = 'Select Route (Required)'
      state.value.otherRouteDisabled = false
      state.value.selectedModel = 'Select RSU Model (Required)'
      state.value.selectedSshGroup = 'Select SSH Group (Required)'
      state.value.selectedSnmpGroup = 'Select SNMP Group (Required)'
      state.value.selectedSnmpVersion = 'Select SNMP Version (Required)'
      state.value.selectedOrganizations = []
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(getRsuCreationData.pending, (state) => {
        state.loading = true
        state.value.errorState = false
      })
      .addCase(getRsuCreationData.fulfilled, (state, action) => {
        state.loading = false
        state.value.apiData = action.payload
        state.value.errorState = false
      })
      .addCase(getRsuCreationData.rejected, (state) => {
        state.loading = false
        state.value.errorState = true
      })
      .addCase(createRsu.pending, (state) => {
        state.loading = true
        state.value.errorState = false
      })
      .addCase(createRsu.fulfilled, (state, action) => {
        state.loading = false
        if (action.payload.success) {
          state.value.successMsg = 'RSU Creation is successful.'
          state.value.errorMsg = ''
          state.value.errorState = false
        } else {
          state.value.successMsg = ''
          state.value.errorMsg = action.payload.message
          state.value.errorState = true
        }
      })
      .addCase(createRsu.rejected, (state) => {
        state.loading = false
        state.value.errorState = true
        state.value.errorMsg = 'unknown error'
      })
      .addCase(submitForm.fulfilled, (state, action) => {
        state.value.submitAttempt = action.payload
      })
  },
})

export const {
  setSuccessMsg,
  resetForm,
  updateSelectedRoute,
  updateSelectedModel,
  updateSelectedSshGroup,
  updateSelectedSnmpGroup,
  updateSelectedSnmpVersion,
  updateSelectedOrganizations,
} = adminAddRsuSlice.actions

export const selectApiData = (state: RootState) => state.adminAddRsu.value.apiData
export const selectPrimaryRoutes = (state: RootState) => state.adminAddRsu.value.apiData?.primary_routes ?? []
export const selectRsuModels = (state: RootState) => state.adminAddRsu.value.apiData?.rsu_models ?? []
export const selectSshCredentialGroups = (state: RootState) =>
  state.adminAddRsu.value.apiData?.ssh_credential_groups ?? []
export const selectSnmpCredentialGroups = (state: RootState) =>
  state.adminAddRsu.value.apiData?.snmp_credential_groups ?? []
export const selectSnmpVersions = (state: RootState) => state.adminAddRsu.value.apiData?.snmp_version_groups ?? []
export const selectOrganizations = (state: RootState) => state.adminAddRsu.value.apiData?.organizations ?? []

export const selectSuccessMsg = (state: RootState) => state.adminAddRsu.value.successMsg
export const selectErrorState = (state: RootState) => state.adminAddRsu.value.errorState
export const selectErrorMsg = (state: RootState) => state.adminAddRsu.value.errorMsg
export const selectSelectedRoute = (state: RootState) => state.adminAddRsu.value.selectedRoute
export const selectOtherRouteDisabled = (state: RootState) => state.adminAddRsu.value.otherRouteDisabled
export const selectSelectedModel = (state: RootState) => state.adminAddRsu.value.selectedModel
export const selectSelectedSshGroup = (state: RootState) => state.adminAddRsu.value.selectedSshGroup
export const selectSelectedSnmpGroup = (state: RootState) => state.adminAddRsu.value.selectedSnmpGroup
export const selectSelectedSnmpVersion = (state: RootState) => state.adminAddRsu.value.selectedSnmpVersion
export const selectSelectedOrganizations = (state: RootState) => state.adminAddRsu.value.selectedOrganizations
export const selectSubmitAttempt = (state: RootState) => state.adminAddRsu.value.submitAttempt
export const selectLoading = (state: RootState) => state.adminAddRsu.loading

export default adminAddRsuSlice.reducer
