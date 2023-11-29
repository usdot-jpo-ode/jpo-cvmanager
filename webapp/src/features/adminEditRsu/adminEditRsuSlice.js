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
  selectedRoute: '',
  otherRouteDisabled: true,
  rsuModels: [],
  selectedModel: '',
  sshCredentialGroups: [],
  selectedSshGroup: '',
  snmpCredentialGroups: [],
  selectedSnmpGroup: '',
  snmpVersions: [],
  selectedSnmpVersion: '',
  organizations: [],
  selectedOrganizations: [],
  submitAttempt: false,
}

export const checkForm = (state) => {
  if (state.value.selectedRoute === '') {
    return false
  } else if (state.value.selectedModel === '') {
    return false
  } else if (state.value.selectedSshGroup === '') {
    return false
  } else if (state.value.selectedSnmpGroup === '') {
    return false
  } else if (state.value.selectedSnmpVersion === '') {
    return false
  } else if (state.value.selectedOrganizations.length === 0) {
    return false
  } else {
    return true
  }
}

export const updateJson = (data, state) => {
  let json = data

  if (state.value.selectedRoute !== 'Other') {
    json.primary_route = state.value.selectedRoute
  }
  json.milepost = Number(json.milepost)
  json.model = state.value.selectedModel
  json.ssh_credential_group = state.value.selectedSshGroup
  json.snmp_credential_group = state.value.selectedSnmpGroup
  json.snmp_version_group = state.value.selectedSnmpVersion

  let organizationsToAdd = []
  let organizationsToRemove = []
  for (const org of state.value.apiData.allowed_selections.organizations) {
    if (
      state.value.selectedOrganizations.some((e) => e.name === org) &&
      !state.value.apiData.rsu_data.organizations.includes(org)
    ) {
      organizationsToAdd.push(org)
    }
    if (
      state.value.apiData.rsu_data.organizations.includes(org) &&
      state.value.selectedOrganizations.some((e) => e.name === org) === false
    ) {
      organizationsToRemove.push(org)
    }
  }

  json.organizations_to_add = organizationsToAdd
  json.organizations_to_remove = organizationsToRemove

  return json
}

export const getRsuInfo = createAsyncThunk(
  'adminEditRsu/getRsuInfo',
  async (rsu_ip, { getState, dispatch }) => {
    const currentState = getState()
    const token = selectToken(currentState)

    const data = await apiHelper._getDataWithCodes({
      url: EnvironmentVars.adminRsu,
      token,
      query_params: { rsu_ip },
      additional_headers: { 'Content-Type': 'application/json' },
    })

    switch (data.status) {
      case 200:
        dispatch(adminEditRsuSlice.actions.updateStates(data.body))
        return { success: true, message: '', data: data.body }
      default:
        return { success: false, message: data.message }
    }
  },
  { condition: (_, { getState }) => selectToken(getState()) }
)

export const editRsu = createAsyncThunk(
  'adminEditRsu/editRsu',
  async (json, { getState, dispatch }) => {
    const currentState = getState()
    const token = selectToken(currentState)

    const data = await apiHelper._patchData({
      url: EnvironmentVars.adminRsu,
      token,
      query_params: { rsu_ip: json.orig_ip },
      body: JSON.stringify(json),
    })

    switch (data.status) {
      case 200:
        setTimeout(() => dispatch(adminEditRsuSlice.actions.setSuccessMsg('')), 3000)
        dispatch(updateRsuTableData())
        return { success: true, message: 'Changes were successfully applied!' }
      default:
        return { success: false, message: data.message }
    }
  },
  { condition: (_, { getState }) => selectToken(getState()) }
)

export const submitForm = createAsyncThunk('adminEditRsu/submitForm', async (data, { getState, dispatch }) => {
  const currentState = getState()
  if (checkForm(currentState.adminEditRsu)) {
    let json = updateJson(data, currentState.adminEditRsu)
    dispatch(editRsu(json))
    return false
  } else {
    return true
  }
})

export const adminEditRsuSlice = createSlice({
  name: 'adminEditRsu',
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
    setSelectedRoute: (state, action) => {
      state.value.selectedRoute = action.payload
    },
    setSelectedModel: (state, action) => {
      state.value.selectedModel = action.payload
    },
    setSelectedSshGroup: (state, action) => {
      state.value.selectedSshGroup = action.payload
    },
    setSelectedSnmpGroup: (state, action) => {
      state.value.selectedSnmpGroup = action.payload
    },
    setSelectedSnmpVersion: (state, action) => {
      state.value.selectedSnmpVersion = action.payload
    },
    setSelectedOrganizations: (state, action) => {
      state.value.selectedOrganizations = action.payload
    },
    updateStates: (state, action) => {
      const apiData = action.payload

      const allowedSelections = apiData.allowed_selections
      state.value.primaryRoutes = allowedSelections.primary_routes.map((val) => {
        return { name: val }
      })
      state.value.rsuModels = allowedSelections.rsu_models.map((val) => {
        return { name: val }
      })
      state.value.sshCredentialGroups = allowedSelections.ssh_credential_groups.map((val) => {
        return { name: val }
      })
      state.value.snmpCredentialGroups = allowedSelections.snmp_credential_groups.map((val) => {
        return { name: val }
      })
      state.value.snmpVersions = allowedSelections.snmp_version_groups.map((val) => {
        return { name: val }
      })
      state.value.organizations = allowedSelections.organizations.map((val) => {
        return { name: val }
      })

      state.value.selectedRoute = apiData.rsu_data.primary_route
      state.value.selectedModel = apiData.rsu_data.model
      state.value.selectedSshGroup = apiData.rsu_data.ssh_credential_group
      state.value.selectedSnmpGroup = apiData.rsu_data.snmp_credential_group
      state.value.selectedSnmpVersion = apiData.rsu_data.snmp_version_group

      state.value.selectedOrganizations = apiData.rsu_data.organizations.map((val) => {
        return { name: val }
      })

      state.value.apiData = apiData
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(getRsuInfo.pending, (state) => {
        state.loading = true
      })
      .addCase(getRsuInfo.fulfilled, (state, action) => {
        state.loading = false
        if (action.payload.success) {
          state.value.errorMsg = ''
          state.value.errorState = false
          state.value.apiData = action.payload.data
        } else {
          state.value.errorMsg = action.payload.message
          state.value.errorState = true
        }
      })
      .addCase(getRsuInfo.rejected, (state) => {
        state.loading = false
      })
      .addCase(editRsu.pending, (state) => {
        state.loading = true
      })
      .addCase(editRsu.fulfilled, (state, action) => {
        state.loading = false
        if (action.payload.success) {
          state.value.successMsg = action.payload.message
          state.value.errorMsg = ''
          state.value.errorState = false
        } else {
          state.value.successMsg = ''
          state.value.errorMsg = action.payload.message
          state.value.errorState = true
        }
      })
      .addCase(editRsu.rejected, (state) => {
        state.loading = false
      })
      .addCase(submitForm.fulfilled, (state, action) => {
        state.value.submitAttempt = action.payload
      })
  },
})

export const {
  setSuccessMsg,
  updateSelectedRoute,
  setSelectedRoute,
  setSelectedModel,
  setSelectedSshGroup,
  setSelectedSnmpGroup,
  setSelectedSnmpVersion,
  setSelectedOrganizations,
  updateStates,
} = adminEditRsuSlice.actions

export const selectLoading = (state) => state.adminEditRsu.loading
export const selectSuccessMsg = (state) => state.adminEditRsu.value.successMsg
export const selectApiData = (state) => state.adminEditRsu.value.apiData
export const selectErrorState = (state) => state.adminEditRsu.value.errorState
export const selectErrorMsg = (state) => state.adminEditRsu.value.errorMsg
export const selectPrimaryRoutes = (state) => state.adminEditRsu.value.primaryRoutes
export const selectSelectedRoute = (state) => state.adminEditRsu.value.selectedRoute
export const selectOtherRouteDisabled = (state) => state.adminEditRsu.value.otherRouteDisabled
export const selectRsuModels = (state) => state.adminEditRsu.value.rsuModels
export const selectSelectedModel = (state) => state.adminEditRsu.value.selectedModel
export const selectSshCredentialGroups = (state) => state.adminEditRsu.value.sshCredentialGroups
export const selectSelectedSshGroup = (state) => state.adminEditRsu.value.selectedSshGroup
export const selectSnmpCredentialGroups = (state) => state.adminEditRsu.value.snmpCredentialGroups
export const selectSelectedSnmpGroup = (state) => state.adminEditRsu.value.selectedSnmpGroup
export const selectSnmpVersions = (state) => state.adminEditRsu.value.snmpVersions
export const selectSelectedSnmpVersion = (state) => state.adminEditRsu.value.selectedSnmpVersion
export const selectOrganizations = (state) => state.adminEditRsu.value.organizations
export const selectSelectedOrganizations = (state) => state.adminEditRsu.value.selectedOrganizations
export const selectSubmitAttempt = (state) => state.adminEditRsu.value.submitAttempt

export default adminEditRsuSlice.reducer
