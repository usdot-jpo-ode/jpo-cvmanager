import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { selectToken } from '../../generalSlices/userSlice'
import EnvironmentVars from '../../EnvironmentVars'
import apiHelper from '../../apis/api-helper'
import { RootState } from '../../store'
import { getAvailableUsers } from '../adminUserTab/adminUserTabSlice'

type UserDataResp = { success: boolean; message: string; data?: UserApiData }
export type UserApiData = {
  user_data: AdminUser
}
export type UserApiDataOrgs = {
  orig_email: string
  email: string
  first_name: string
  last_name: string
  super_user: boolean
  organizations_to_add: { name: string; role: string }[]
  organizations_to_modify: { name: string; role: string }[]
  organizations_to_remove: { name: string; role: string }[]
}

const initialState = {
  selectedOrganizationNames: [] as { name: string; id: number }[],
  selectedOrganizations: [] as { name: string; role: string; id: number }[],
  organizationNames: [] as { name: string; id: number }[],
  availableRoles: [] as { role: string }[],
  apiData: undefined as UserApiData | undefined,
  submitAttempt: false,
}

export const organizationParser = (
  data: UserApiDataOrgs,
  submitOrgs: Array<{ name: string; role: string }>,
  apiData: UserApiData
) => {
  let orgsToAdd = []
  let orgsToModify = []
  let orgsToRemove = []

  for (const org of apiData.user_data.organizations) {
    if (submitOrgs.some((e) => e.name === org.name)) {
      var index = submitOrgs.findIndex(function (item, i) {
        return item.name === org.name
      })
      if (submitOrgs[index].role !== org.role) {
        const changedOrg = { name: submitOrgs[index].name, role: submitOrgs[index].role }
        orgsToModify.push(changedOrg)
      }
    } else {
      const removedOrg = { name: org.name, role: org.role }
      orgsToRemove.push(removedOrg)
    }
  }

  for (const org of submitOrgs) {
    if (!apiData.user_data.organizations.some((e) => e.name === org.name)) {
      const newOrg = { name: org.name, role: org.role }
      orgsToAdd.push(newOrg)
    }
  }

  data.organizations_to_add = orgsToAdd
  data.organizations_to_modify = orgsToModify
  data.organizations_to_remove = orgsToRemove
  return data
}

export const getUserData = createAsyncThunk(
  'adminEditUser/getUserData',
  async (email: string, { getState, dispatch }): Promise<UserDataResp> => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    const data = await apiHelper._getDataWithCodes({
      url: EnvironmentVars.adminUser,
      token,
      query_params: { user_email: email },
      additional_headers: { 'Content-Type': 'application/json' },
    })

    switch (data.status) {
      case 200:
        dispatch(adminEditUserSlice.actions.updateStates(data.body))
        return { success: true, message: '', data: data.body }
      default:
        return { success: false, message: data.message }
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const editUser = createAsyncThunk(
  'adminEditUser/editUser',
  async (payload: { json: Object }, { getState, dispatch }) => {
    const { json } = payload
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    const data = await apiHelper._patchData({
      url: EnvironmentVars.adminUser,
      token,
      body: JSON.stringify(json),
    })

    switch (data.status) {
      case 200:
        dispatch(getAvailableUsers())
        return { success: true, message: 'Changes were successfully applied!' }
      default:
        return { success: false, message: data.message }
    }
  },
  { condition: (_, { getState }) => selectToken(getState() as RootState) != undefined }
)

export const submitForm = createAsyncThunk(
  'adminEditUser/submitForm',
  async (payload: { data: UserApiDataOrgs }, { getState, dispatch }) => {
    const { data } = payload
    const currentState = getState() as RootState
    const selectedOrganizations = selectSelectedOrganizations(currentState)
    const apiData = selectApiData(currentState)

    if (selectedOrganizations.length !== 0) {
      let submitOrgs = [...selectedOrganizations].map((org) => ({ ...org }))
      submitOrgs.forEach((elm) => delete elm.id)
      const tempData = organizationParser(data, submitOrgs, apiData)
      let res = await dispatch(editUser({ json: tempData }))
      if ((res.payload as any).success) {
        return { submitAttempt: false, success: true, message: 'User Updated Successfully' }
      } else {
        return { submitAttempt: false, success: false, message: (res.payload as any).message }
      }
    } else {
      return { submitAttempt: true, success: false, message: 'Please fill out all required fields' }
    }
  }
)

export const adminEditUserSlice = createSlice({
  name: 'adminEditUser',
  initialState: {
    loading: false,
    value: initialState,
  },
  reducers: {
    clear: (state) => {
      state.value = initialState
    },
    updateOrganizations: (state, action) => {
      let newOrganizations = []
      for (const name of action.payload) {
        if (state.value.selectedOrganizations.some((e) => e.name === name.name)) {
          newOrganizations.push(state.value.selectedOrganizations.find((e) => e.name === name.name))
        } else {
          const tempName = { ...name }
          tempName.role = state.value.availableRoles[0].role
          newOrganizations.push(tempName)
        }
      }
      state.value.selectedOrganizations = newOrganizations
      state.value.selectedOrganizationNames = action.payload
    },
    setSelectedRole: (state, action) => {
      const selectedOrganizations = [...state.value.selectedOrganizations]
      const { name, role } = action.payload
      const index = selectedOrganizations.findIndex((org) => org.name === name)
      selectedOrganizations[index].role = role
      state.value.selectedOrganizations = selectedOrganizations
    },
    updateStates: (state, action) => {
      const data = action.payload
      if (Object.keys(data).length !== 0) {
        let orgData: Array<{ id: number; name: string }> = []
        data.allowed_selections.organizations.forEach((org: string, index: number) =>
          orgData.push({ id: index, name: org })
        )
        state.value.organizationNames = orgData

        let roleData: Array<{ role: string }> = []
        data.allowed_selections.roles.forEach((role: string) => roleData.push({ role }))
        state.value.availableRoles = roleData

        let tempOrganizations: Array<{ id: number; name: string; role: string }> = []
        let tempOrganizationNames: Array<{ id: number; name: string }> = []

        data.user_data.organizations.forEach((org: { name: string; role: string }, index: number) => {
          tempOrganizations.push({ id: index, name: org.name, role: org.role })
          tempOrganizationNames.push({ id: index, name: org.name })
        })

        state.value.selectedOrganizations = tempOrganizations
        state.value.selectedOrganizationNames = tempOrganizationNames
      }
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(getUserData.pending, (state) => {
        state.loading = true
      })
      .addCase(getUserData.fulfilled, (state, action: { payload: UserDataResp }) => {
        state.loading = false
        if (action.payload.success) {
          state.value.apiData = action.payload.data
        }
      })
      .addCase(getUserData.rejected, (state) => {
        state.loading = false
      })
      .addCase(editUser.pending, (state) => {
        state.loading = true
      })
      .addCase(editUser.fulfilled, (state, action) => {
        state.loading = false
      })
      .addCase(editUser.rejected, (state) => {
        state.loading = false
      })
      .addCase(submitForm.fulfilled, (state, action) => {
        state.value.submitAttempt = action.payload.submitAttempt
      })
  },
})

export const { clear, updateOrganizations, updateStates, setSelectedRole } = adminEditUserSlice.actions

export const selectLoading = (state: RootState) => state.adminEditUser.loading
export const selectSelectedOrganizationNames = (state: RootState) => state.adminEditUser.value.selectedOrganizationNames
export const selectSelectedOrganizations = (state: RootState) => state.adminEditUser.value.selectedOrganizations
export const selectOrganizationNames = (state: RootState) => state.adminEditUser.value.organizationNames
export const selectAvailableRoles = (state: RootState) => state.adminEditUser.value.availableRoles
export const selectApiData = (state: RootState) => state.adminEditUser.value.apiData
export const selectSubmitAttempt = (state: RootState) => state.adminEditUser.value.submitAttempt

export default adminEditUserSlice.reducer
