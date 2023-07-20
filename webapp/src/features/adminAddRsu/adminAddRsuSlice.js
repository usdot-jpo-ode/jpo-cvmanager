import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { selectToken } from '../../generalSlices/userSlice'
import EnvironmentVars from '../../EnvironmentVars'
import apiHelper from '../../apis/api-helper'
import { updateTableData as updateRsuTableData } from '../adminRsuTab/adminRsuTabSlice'

const initialState = {
  successMsg: '',
  apiData: {},
  errorState: false,
  errorMsg: '',
  primaryRoutes: [],
  selectedRoute: 'Select Route',
  otherRouteDisabled: true,
  rsuModels: [],
  selectedModel: 'Select RSU Model',
  sshCredentialGroups: [],
  selectedSshGroup: 'Select SSH Group',
  snmpCredentialGroups: [],
  selectedSnmpGroup: 'Select SNMP Group',
  snmpVersions: [],
  selectedSnmpVersion: 'Select SNMP Version',
  organizations: [],
  selectedOrganizations: [],
  submitAttempt: false,
}

export const updateApiJson = (apiJson) => {
  if (Object.keys(apiJson).length !== 0) {
    let keyedApiJson = {}

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

export const checkForm = (state) => {
  if (state.value.selectedRoute === 'Select Route') {
    return false
  } else if (state.value.selectedModel === 'Select RSU Model') {
    return false
  } else if (state.value.selectedSshGroup === 'Select SSH Group') {
    return false
  } else if (state.value.selectedSnmpGroup === 'Select SNMP Group') {
    return false
  } else if (state.value.selectedSnmpVersion === 'Select SNMP Version') {
    return false
  } else if (state.value.selectedOrganizations.length === 0) {
    return false
  } else {
    return true
  }
}

export const updateJson = (data, state) => {
  let json = data
  // creating geo_position object from latitudes and longitude
  json.geo_position = {
    latitude: Number(json.latitude),
    longitude: Number(json.longitude),
  }
  delete json.latitude
  delete json.longitude
  if (state.value.selectedRoute !== 'Other') {
    json.primary_route = state.value.selectedRoute
  }
  json.milepost = Number(json.milepost)
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
    const currentState = getState()
    const token = selectToken(currentState)

    const data = await apiHelper._getData({
      url: EnvironmentVars.adminAddRsu,
      token,
      additional_headers: { 'Content-Type': 'application/json' },
    })
    return updateApiJson(data)
  },
  { condition: (_, { getState }) => selectToken(getState()) }
)

export const createRsu = createAsyncThunk(
  'adminAddRsu/createRsu',
  async (payload, { getState, dispatch }) => {
    const { json, reset } = payload
    const currentState = getState()
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
  { condition: (_, { getState }) => selectToken(getState()) }
)

export const submitForm = createAsyncThunk('adminAddRsu/submitForm', async (payload, { getState, dispatch }) => {
  const { data, reset } = payload

  const currentState = getState()
  if (checkForm(currentState.adminAddRsu)) {
    let json = updateJson(data, currentState.adminAddRsu)
    dispatch(createRsu({ json, reset }))
    return false
  } else {
    return true
  }
})

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
      state.value.selectedRoute = 'Select Route'
      state.value.otherRouteDisabled = false
      state.value.selectedModel = 'Select RSU Model'
      state.value.selectedSshGroup = 'Select SSH Group'
      state.value.selectedSnmpGroup = 'Select SNMP Group'
      state.value.selectedSnmpVersion = 'Select SNMP Version'
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
  setOtherRouteDisabled,
  resetForm,
  updateSelectedRoute,
  updateSelectedModel,
  updateSelectedSshGroup,
  updateSelectedSnmpGroup,
  updateSelectedSnmpVersion,
  updateSelectedOrganizations,
} = adminAddRsuSlice.actions

export const selectApiData = (state) => state.adminAddRsu.value.apiData
export const selectPrimaryRoutes = (state) => state.adminAddRsu.value.apiData?.primary_routes ?? []
export const selectRsuModels = (state) => state.adminAddRsu.value.apiData?.rsu_models ?? []
export const selectSshCredentialGroups = (state) => state.adminAddRsu.value.apiData?.ssh_credential_groups ?? []
export const selectSnmpCredentialGroups = (state) => state.adminAddRsu.value.apiData?.snmp_credential_groups ?? []
export const selectSnmpVersions = (state) => state.adminAddRsu.value.apiData?.snmp_version_groups ?? []
export const selectOrganizations = (state) => state.adminAddRsu.value.apiData?.organizations ?? []

export const selectSuccessMsg = (state) => state.adminAddRsu.value.successMsg
export const selectErrorState = (state) => state.adminAddRsu.value.errorState
export const selectErrorMsg = (state) => state.adminAddRsu.value.errorMsg
export const selectSelectedRoute = (state) => state.adminAddRsu.value.selectedRoute
export const selectOtherRouteDisabled = (state) => state.adminAddRsu.value.otherRouteDisabled
export const selectSelectedModel = (state) => state.adminAddRsu.value.selectedModel
export const selectSelectedSshGroup = (state) => state.adminAddRsu.value.selectedSshGroup
export const selectSelectedSnmpGroup = (state) => state.adminAddRsu.value.selectedSnmpGroup
export const selectSelectedSnmpVersion = (state) => state.adminAddRsu.value.selectedSnmpVersion
export const selectSelectedOrganizations = (state) => state.adminAddRsu.value.selectedOrganizations
export const selectSubmitAttempt = (state) => state.adminAddRsu.value.submitAttempt
export const selectLoading = (state) => state.adminAddRsu.loading

export default adminAddRsuSlice.reducer
