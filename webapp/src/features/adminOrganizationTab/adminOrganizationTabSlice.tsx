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
}

const initialState = {
  activeDiv: 'organization_table',
  title: 'Organizations',
  orgData: [] as AdminOrgSummary[],
  selectedOrg: {} as AdminOrgSummary,
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
  async (json: adminOrgPatch, { getState }) => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    const jsonComplete: adminOrgPatch = {
      orig_name: json.orig_name ?? json.name,
      users_to_add: [],
      users_to_modify: [],
      users_to_remove: [],
      rsus_to_add: [],
      rsus_to_remove: [],
      intersections_to_add: [],
      intersections_to_remove: [],
      ...json,
    }

    const data = await apiHelper._patchData({
      url: EnvironmentVars.adminOrg,
      token,
      body: JSON.stringify(jsonComplete),
    })

    switch (data.status) {
      case 200:
        console.debug('PATCH successful ', json)
        return { success: true, message: '' }
      default:
        return { success: false, message: data.message }
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
            let tempData = []
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
            } else {
              state.value.selectedOrg = tempData[0]
            }
          } else {
            const org_data = data?.org_data as AdminOrgSingle
            state.value.rsuTableData = org_data?.org_rsus
            state.value.intersectionTableData = org_data?.org_intersections
            state.value.userTableData = org_data?.org_users
          }
        } else {
        }
        state.loading = false
      })
      .addCase(getOrgData.rejected, (state) => {
        state.loading = false
      })
      .addCase(editOrg.pending, (state) => {
        state.loading = true
      })
      .addCase(editOrg.fulfilled, (state) => {
        state.loading = false
      })
      .addCase(editOrg.rejected, (state) => {
        state.loading = false
      })
      .addCase(deleteOrg.fulfilled, (state, action) => {
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

export default adminOrganizationTabSlice.reducer
