import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { selectToken } from '../../generalSlices/userSlice'
import EnvironmentVars from '../../EnvironmentVars'
import apiHelper from '../../apis/api-helper'
import { RootState } from '../../store'

export type AdminOrgSummary = {
  name: string
  email: string
  user_count: number
  rsu_count: number
  intersection_count: number
}

export type AdminOrgSingle = {
  org_users: AdminOrgUser[]
  org_rsus: AdminOrgRsu[]
  org_intersections: AdminOrgIntersection[]
}

export type AdminOrgUser = {
  email: string
  first_name: string
  last_name: string
  role: string
  id?: number
  organizations?: { name: string; role: string }[]
}

export type AdminOrgRsu = {
  ip: string
  primary_route: string
  milepost: number
  tim_deposit: boolean
  snmp_monitoring: boolean
}

export type AdminOrgIntersection = {
  intersection_id: string
  intersection_name: string
  ref_pt: {
    latitude: string
    longitude: string
  }
}

export type adminOrgPatch = {
  orig_name?: string
  name: string
  email: string
  users_to_add?: { email: string; role: string }[]
  users_to_modify?: { email: string; role: string }[]
  users_to_remove?: { email: string; role: string }[]
  rsus_to_add?: string[]
  rsus_to_remove?: string[]
  intersections_to_add?: string[]
  intersections_to_remove?: string[]
  tim_deposit?: boolean
  snmp_monitoring?: boolean
}

const initialState = {
  activeDiv: 'organization_table',
  title: 'Organizations',
  orgData: [] as AdminOrgSummary[],
  selectedOrg: undefined as AdminOrgSummary | undefined,
  rsuTableData: [] as AdminOrgRsu[],
  intersectionTableData: [] as AdminOrgIntersection[],
  userTableData: [] as AdminOrgUser[],
}

export const getOrgData = createAsyncThunk(
  'adminOrganizationTab/getOrgData',
  async (
    payload: {
      orgName: string
      all?: boolean
      specifiedOrg?: string
    },
    { getState }
  ): Promise<{
    success: boolean
    message: string
    data?: { org_data: AdminOrgSummary[] | AdminOrgSingle }
    all?: boolean
    specifiedOrg?: string
  }> => {
    const { orgName, all, specifiedOrg } = payload
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    const data = await apiHelper._getDataWithCodes({
      url: EnvironmentVars.adminOrg,
      token,
      query_params: { org_name: orgName },
    })

    switch (data.status) {
      case 200:
        return { success: true, message: '', data: data.body, all: all ?? false, specifiedOrg }
      default:
        return { success: false, message: data.message }
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const deleteOrg = createAsyncThunk(
  'adminOrganizationTab/deleteOrg',
  async (org: string, { getState, dispatch }) => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    const data = await apiHelper._deleteData({
      url: EnvironmentVars.adminOrg,
      token,
      query_params: { org_name: org },
    })

    switch (data.status) {
      case 200:
        console.debug('Successfully deleted Organization: ' + org)
        dispatch(getOrgData({ orgName: 'all', all: true }))
        return { success: true, message: 'Successfully deleted Organization: ' + org }
      default:
        console.error(data)
        return { success: false, message: data.message }
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const editOrg = createAsyncThunk(
  'adminOrganizationTab/editOrg',
  async (json: adminOrgPatch & { url?: string }, { getState }): Promise<{
    success: boolean
    message: string
    data?: { org_data: AdminOrgSingle }
  }> => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    const { url, ...jsonWithoutUrl } = json
    const jsonComplete: adminOrgPatch = {
      orig_name: jsonWithoutUrl.orig_name ?? jsonWithoutUrl.name,
      users_to_add: [],
      users_to_modify: [],
      users_to_remove: [],
      rsus_to_add: [],
      rsus_to_remove: [],
      intersections_to_add: [],
      intersections_to_remove: [],
      ...jsonWithoutUrl,
    }

    const data = await apiHelper._patchData({
      url: url ?? EnvironmentVars.adminOrg,
      token,
      body: JSON.stringify(jsonComplete),
    })

    switch (data.status) {
      case 200:
        console.debug('Successfully edited organization')
        return { success: true, message: '', data: data.body }
      default:
        return { success: false, message: data.message }
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const updateOrgTimDeposit = createAsyncThunk(
  'adminOrganizationTab/updateOrgTimDeposit',
  async (
    payload: { orgName: string; email: string; timDeposit: boolean },
    { getState, dispatch }
  ): Promise<{
    success: boolean
    message: string
    data?: { org_data: AdminOrgSingle }
  }> => {
    const { orgName, email, timDeposit } = payload

    const patchJson: adminOrgPatch = {
      name: orgName,
      email: email,
      tim_deposit: timDeposit,
    }

    const res = await dispatch(editOrg({ ...patchJson, url: EnvironmentVars.adminOrgTimDeposit }))
    if ((res.payload as any).success) {
      return {
        success: true,
        message: 'Successfully updated TIM deposit for all RSUs in ' + orgName,
        data: (res.payload as any).data,
      }
    } else {
      return { success: false, message: (res.payload as any).message }
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const updateOrgSnmpMonitoring = createAsyncThunk(
  'adminOrganizationTab/updateOrgSnmpMonitoring',
  async (
    payload: { orgName: string; email: string; snmpMonitoring: boolean },
    { getState, dispatch }
  ): Promise<{
    success: boolean
    message: string
    data?: { org_data: AdminOrgSingle }
  }> => {
    const { orgName, email, snmpMonitoring } = payload

    const patchJson: adminOrgPatch = {
      name: orgName,
      email: email,
      snmp_monitoring: snmpMonitoring,
    }

    const res = await dispatch(editOrg({ ...patchJson, url: EnvironmentVars.adminOrgSnmpMonitoring }))
    if ((res.payload as any).success) {
      return {
        success: true,
        message: 'Successfully updated SNMP monitoring for all RSUs in ' + orgName,
        data: (res.payload as any).data,
      }
    } else {
      return { success: false, message: (res.payload as any).message }
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const adminOrganizationTabSlice = createSlice({
  name: 'adminOrganizationTab',
  initialState: {
    loading: false,
    value: initialState,
  },
  reducers: {
    updateTitle: (state) => {
      if (state.value.activeDiv === 'organization_table') {
        state.value.title = 'CV Manager Organizations'
      } else if (state.value.activeDiv === 'edit_organization') {
        state.value.title = 'Edit Organization'
      } else if (state.value.activeDiv === 'add_organization') {
        state.value.title = 'Add Organization'
      }
    },
    setActiveDiv: (state, action) => {
      state.value.activeDiv = action.payload
    },
    setSelectedOrg: (state, action) => {
      state.value.selectedOrg = action.payload
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(getOrgData.pending, (state) => {
        state.loading = true
      })
      .addCase(getOrgData.fulfilled, (state, action) => {
        state.loading = false
        if (action.payload.success) {
          const data = action.payload.data
          if (action.payload.all) {
            const tempData = []
            let i = 0
            const org_data = data?.org_data as AdminOrgSummary[]
            for (const x in org_data) {
              const temp = {
                ...org_data[x],
                id: i,
              }
              tempData.push(temp)
              i += 1
            }
            state.value.orgData = tempData
            if (action.payload.specifiedOrg) {
              for (let i = 0; i < tempData.length; i++) {
                if (tempData[i].name === action.payload.specifiedOrg) {
                  state.value.selectedOrg = tempData[i]
                  break
                }
              }
            } else if (state.value.selectedOrg) {
              const currentOrg = tempData.find((org) => org.name === state.value.selectedOrg.name)
              state.value.selectedOrg = currentOrg ?? tempData[0]
            } else {
              state.value.selectedOrg = tempData[0]
            }
          } else {
            const org_data = data?.org_data as AdminOrgSingle
            state.value.rsuTableData = org_data?.org_rsus
            state.value.intersectionTableData = org_data?.org_intersections
            state.value.userTableData = org_data?.org_users
          }
        }
        state.loading = false
      })
      .addCase(getOrgData.rejected, (state) => {
        state.loading = false
      })
      .addCase(editOrg.pending, (state) => {
        state.loading = true
      })
      .addCase(editOrg.fulfilled, (state, action) => {
        state.loading = false
        if (action.payload.success && action.payload.data) {
          const data = action.payload.data
          const org_data = data?.org_data as AdminOrgSingle
          state.value.rsuTableData = org_data?.org_rsus
          state.value.intersectionTableData = org_data?.org_intersections
          state.value.userTableData = org_data?.org_users
        }
      })
      .addCase(updateOrgTimDeposit.fulfilled, (state, action) => {
        state.loading = false
        if (action.payload.success && action.payload.data) {
          const data = action.payload.data
          const org_data = data?.org_data as AdminOrgSingle
          state.value.rsuTableData = org_data?.org_rsus
          state.value.intersectionTableData = org_data?.org_intersections
          state.value.userTableData = org_data?.org_users
        }
      })
      .addCase(updateOrgSnmpMonitoring.fulfilled, (state, action) => {
        state.loading = false
        if (action.payload.success && action.payload.data) {
          const data = action.payload.data
          const org_data = data?.org_data as AdminOrgSingle
          state.value.rsuTableData = org_data?.org_rsus
          state.value.intersectionTableData = org_data?.org_intersections
          state.value.userTableData = org_data?.org_users
        }
      })
      .addCase(editOrg.rejected, (state) => {
        state.loading = false
      })
      .addCase(deleteOrg.fulfilled, (state) => {
        state.loading = false
      })
  },
})

export const { updateTitle, setActiveDiv, setSelectedOrg } = adminOrganizationTabSlice.actions

export const selectLoading = (state: RootState) => state.adminOrganizationTab.loading
export const selectActiveDiv = (state: RootState) => state.adminOrganizationTab.value.activeDiv
export const selectTitle = (state: RootState) => state.adminOrganizationTab.value.title
export const selectOrgData = (state: RootState) => state.adminOrganizationTab.value.orgData
export const selectSelectedOrg = (state: RootState) => state.adminOrganizationTab.value.selectedOrg
export const selectSelectedOrgName = (state: RootState) => state.adminOrganizationTab.value.selectedOrg?.name
export const selectSelectedOrgEmail = (state: RootState) => state.adminOrganizationTab.value.selectedOrg?.email
export const selectRsuTableData = (state: RootState) => state.adminOrganizationTab.value.rsuTableData
export const selectIntersectionTableData = (state: RootState) => state.adminOrganizationTab.value.intersectionTableData
export const selectUserTableData = (state: RootState) => state.adminOrganizationTab.value.userTableData
export const selectTimDeposit = (state: RootState) => {
  const rsus = state.adminOrganizationTab.value.rsuTableData
  if (Array.isArray(rsus) && rsus.length > 0) {
    const allTimEnabled = rsus.every((rsu) => rsu.tim_deposit === true)
    const allTimDisabled = rsus.every((rsu) => rsu.tim_deposit === false)
    if (allTimEnabled) {
      return 'Enabled'
    } else if (allTimDisabled) {
      return 'Disabled'
    } else {
      return 'Mixed'
    }
  }
  return 'Disabled'
}
export const selectSnmpMonitoring = (state: RootState) => {
  const rsus = state.adminOrganizationTab.value.rsuTableData
  if (Array.isArray(rsus) && rsus.length > 0) {
    const allSnmpEnabled = rsus.every((rsu) => rsu.snmp_monitoring === true)
    const allSnmpDisabled = rsus.every((rsu) => rsu.snmp_monitoring === false)
    if (allSnmpEnabled) {
      return 'Enabled'
    } else if (allSnmpDisabled) {
      return 'Disabled'
    } else {
      return 'Mixed'
    }
  }
  return 'Disabled'
}

export default adminOrganizationTabSlice.reducer
