import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { selectToken } from '../../generalSlices/userSlice'
import EnvironmentVars from '../../EnvironmentVars'
import apiHelper from '../../apis/api-helper'
import { updateTableData as updateRsuTableData } from '../adminRsuTab/adminRsuTabSlice'
import { RootState } from '../../store'
import { AdminEditRsuFormType } from './AdminEditRsu'

export type adminEditRsuData = {
  rsu_data: AdminEditRsuFormType
  allowed_selections: {
    primary_routes: string[]
    rsu_models: string[]
    ssh_credential_groups: string[]
    snmp_credential_groups: string[]
    snmp_version_groups: string[]
    organizations: string[]
  }
}

const initialState = {
  apiData: undefined as adminEditRsuData | undefined,
  primaryRoutes: [] as { name: string }[],
  selectedRoute: '',
  otherRouteDisabled: true,
  rsuModels: [] as { name: string }[],
  selectedModel: '',
  sshCredentialGroups: [] as { name: string }[],
  selectedSshGroup: '',
  snmpCredentialGroups: [] as { name: string }[],
  selectedSnmpGroup: '',
  snmpVersions: [] as { name: string }[],
  selectedSnmpVersion: '',
  organizations: [] as { name: string }[],
  selectedOrganizations: [] as { name: string }[],
  submitAttempt: false,
}

export const checkForm = (state: RootState['adminEditRsu']) => {
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

export const updateJson = (data: AdminEditRsuFormType, state: RootState['adminEditRsu']) => {
  const json = data

  if (state.value.selectedRoute !== 'Other') {
    json.primary_route = state.value.selectedRoute
  }
  json.milepost = Number(json.milepost)
  json.model = state.value.selectedModel
  json.ssh_credential_group = state.value.selectedSshGroup
  json.snmp_credential_group = state.value.selectedSnmpGroup
  json.snmp_version_group = state.value.selectedSnmpVersion

  const organizationsToAdd = []
  const organizationsToRemove = []
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
  async (rsu_ip: string, { getState, dispatch }) => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    const data = await apiHelper._getDataWithCodes({
      url: EnvironmentVars.adminRsu,
      token,
      query_params: { rsu_ip },
      additional_headers: { 'Content-Type': 'application/json' },
      tag: 'rsu',
    })

    switch (data.status) {
      case 200:
        dispatch(adminEditRsuSlice.actions.updateStates(data.body))
        return { success: true, message: '', data: data.body }
      default:
        return { success: false, message: data.message }
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const editRsu = createAsyncThunk(
  'adminEditRsu/editRsu',
  async (json: { orig_ip: string }, { getState, dispatch }) => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    const data = await apiHelper._patchData({
      url: EnvironmentVars.adminRsu,
      token,
      query_params: { rsu_ip: json.orig_ip },
      body: JSON.stringify(json),
      tag: 'rsu',
    })

    switch (data.status) {
      case 200:
        dispatch(updateRsuTableData())
        return { success: true, message: 'Changes were successfully applied!' }
      default:
        return { success: false, message: data.message }
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const submitForm = createAsyncThunk(
  'adminEditRsu/submitForm',
  async (data: AdminEditRsuFormType, { getState, dispatch }) => {
    const currentState = getState() as RootState
    if (checkForm(currentState.adminEditRsu)) {
      const json = updateJson(data, currentState.adminEditRsu)
      const res = await dispatch(editRsu(json))
      if ((res.payload as any).success) {
        return { submitAttempt: false, success: true, message: 'RSU Updated Successfully' }
      } else {
        return { submitAttempt: false, success: false, message: (res.payload as any).message }
      }
    } else {
      return { submitAttempt: true, success: false, message: 'Please fill out all required fields' }
    }
  }
)

export const adminEditRsuSlice = createSlice({
  name: 'adminEditRsu',
  initialState: {
    loading: false,
    value: initialState,
  },
  reducers: {
    clear: (state) => {
      state.value = initialState
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
    updateStates: (state, action: { payload: adminEditRsuData }) => {
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
          state.value.apiData = action.payload.data
        }
      })
      .addCase(getRsuInfo.rejected, (state) => {
        state.loading = false
      })
      .addCase(editRsu.pending, (state) => {
        state.loading = true
      })
      .addCase(editRsu.fulfilled, (state) => {
        state.loading = false
      })
      .addCase(editRsu.rejected, (state) => {
        state.loading = false
      })
      .addCase(submitForm.fulfilled, (state, action) => {
        state.value.submitAttempt = action.payload.submitAttempt
      })
  },
})

export const {
  clear,
  updateSelectedRoute,
  setSelectedRoute,
  setSelectedModel,
  setSelectedSshGroup,
  setSelectedSnmpGroup,
  setSelectedSnmpVersion,
  setSelectedOrganizations,
  updateStates,
} = adminEditRsuSlice.actions

export const selectLoading = (state: RootState) => state.adminEditRsu.loading
export const selectApiData = (state: RootState) => state.adminEditRsu.value.apiData
export const selectPrimaryRoutes = (state: RootState) => state.adminEditRsu.value.primaryRoutes
export const selectSelectedRoute = (state: RootState) => state.adminEditRsu.value.selectedRoute
export const selectOtherRouteDisabled = (state: RootState) => state.adminEditRsu.value.otherRouteDisabled
export const selectRsuModels = (state: RootState) => state.adminEditRsu.value.rsuModels
export const selectSelectedModel = (state: RootState) => state.adminEditRsu.value.selectedModel
export const selectSshCredentialGroups = (state: RootState) => state.adminEditRsu.value.sshCredentialGroups
export const selectSelectedSshGroup = (state: RootState) => state.adminEditRsu.value.selectedSshGroup
export const selectSnmpCredentialGroups = (state: RootState) => state.adminEditRsu.value.snmpCredentialGroups
export const selectSelectedSnmpGroup = (state: RootState) => state.adminEditRsu.value.selectedSnmpGroup
export const selectSnmpVersions = (state: RootState) => state.adminEditRsu.value.snmpVersions
export const selectSelectedSnmpVersion = (state: RootState) => state.adminEditRsu.value.selectedSnmpVersion
export const selectOrganizations = (state: RootState) => state.adminEditRsu.value.organizations
export const selectSelectedOrganizations = (state: RootState) => state.adminEditRsu.value.selectedOrganizations
export const selectSubmitAttempt = (state: RootState) => state.adminEditRsu.value.submitAttempt

export default adminEditRsuSlice.reducer
