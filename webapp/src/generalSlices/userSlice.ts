import { PayloadAction, createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import AuthApi from '../apis/auth-api'
import { UserManager, LocalStorageManager, SecureStorageManager } from '../managers'
import { RootState } from '../store'

const authDataLocalStorage = LocalStorageManager.getAuthData()
const authLoginData = UserManager.isLoginActive(authDataLocalStorage) ? authDataLocalStorage : null

export const keycloakLogin = createAsyncThunk('user/login', async (token: string, { rejectWithValue }) => {
  try {
    if (token) {
      const response = await AuthApi.logIn(token)
      switch (response.status) {
        case 200:
          const authLoginData = {
            data: response.json
              ? { ...response.json, name: `${response.json.first_name} ${response.json.last_name}` }
              : null,
            token: token,
            expires_at: Date.now() + 590000,
          }
          return authLoginData
        case 400:
          return rejectWithValue('Login Unsuccessful: Bad Request')
        case 401:
          return rejectWithValue('Login Unsuccessful: User Unauthorized Please Contact Support')
        case 403:
          return rejectWithValue('Login Unsuccessful: Access Forbidden')
        case 404:
          return rejectWithValue('Login Unsuccessful: Authentication API Not Found')
        default:
          return rejectWithValue('Login Unsuccessful: Unknown Error Occurred')
      }
    } else {
      console.error('Invalid token passed to user/login')
      return rejectWithValue('Login Unsuccessful: No KeyCloak Token Please Refresh')
    }
  } catch (exception_var) {
    console.error('Exception logging in user', exception_var)
    throw exception_var
  }
})

export const userSlice = createSlice({
  name: 'user',
  initialState: {
    loading: true,
    value: {
      authLoginData: authLoginData,
      organization: authLoginData?.data?.organizations?.[0],
      loginFailure: false,
      loginMessage: '',
      routeNotFound: false,
    },
  },
  reducers: {
    logout: (state) => {
      state.value.authLoginData = null
      state.value.organization = null
      LocalStorageManager.removeAuthData()
      SecureStorageManager.removeUserRole()
    },
    changeOrganization: (state, action) => {
      const organization =
        UserManager.getOrganization(state.value.authLoginData, action.payload) ?? state.value.organization
      state.value.organization = organization
      SecureStorageManager.setUserRole({ name: organization.name, role: organization.role })
    },
    setOrganizationList: (state, action) => {
      if (action.payload.type === 'add') {
        state.value.authLoginData.data.organizations = [
          ...state.value.authLoginData.data.organizations,
          action.payload.value,
        ]
      } else if (action.payload.type === 'delete') {
        const index = state.value.authLoginData.data.organizations.findIndex(
          (org) => org.name === action.payload.value.name
        )
        if (index > -1) {
          const updatedOrgList = state.value.authLoginData.data.organizations
          updatedOrgList.splice(index, 1)
          state.value.authLoginData.data.organizations = [...updatedOrgList]
        }
      } else if (action.payload.type === 'update') {
        const index = state.value.authLoginData.data.organizations.findIndex(
          (org) => org.name === action.payload.orgName
        )
        if (index > -1) {
          const updatedOrgList = state.value.authLoginData.data.organizations
          updatedOrgList[index] = action.payload.value
          state.value.authLoginData.data.organizations = [...updatedOrgList]
        }
      }
    },
    setLoading: (state, action) => {
      state.loading = action.payload
    },
    setLoginFailure: (state, action) => {
      state.value.loginFailure = action.payload
    },
    setLoginMessage: (state, action) => {
      state.value.loginMessage = action.payload
    },
    setRouteNotFound: (state, action) => {
      state.value.routeNotFound = action.payload
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(keycloakLogin.pending, (state) => {
        state.value.loginMessage = ''
        state.loading = true
      })
      .addCase(keycloakLogin.fulfilled, (state, action) => {
        state.loading = false
        state.value.loginMessage = ''
        state.value.loginFailure = false
        state.value.authLoginData = action.payload
        state.value.organization = action.payload?.data?.organizations?.[0]
        LocalStorageManager.setAuthData(action.payload)
        SecureStorageManager.setUserRole(action.payload?.data?.organizations?.[0])
      })
      .addCase(keycloakLogin.rejected, (state, action: PayloadAction<unknown>) => {
        state.loading = false
        state.value.loginFailure = true
        state.value.loginMessage = action.payload as string
        LocalStorageManager.removeAuthData()
        SecureStorageManager.removeUserRole()
      })
  },
})

export const { logout, changeOrganization, setOrganizationList, setLoading, setLoginFailure, setRouteNotFound } =
  userSlice.actions

export const selectAuthLoginData = (state: RootState) => state.user.value.authLoginData
export const selectToken = (state: RootState) => state.user.value.authLoginData.token
export const selectRole = (state: RootState) => state.user.value.organization?.role
export const selectOrganizationName = (state: RootState) => state.user.value.organization?.name
export const selectName = (state: RootState) => state.user.value.authLoginData?.data?.name
export const selectEmail = (state: RootState) => state.user.value.authLoginData?.data?.email
export const selectSuperUser = (state: RootState) => state.user.value.authLoginData?.data?.super_user
export const selectTokenExpiration = (state: RootState) => state.user.value.authLoginData?.expires_at
export const selectLoginFailure = (state: RootState) => state.user.value.loginFailure
export const selectLoginMessage = (state: RootState) => state.user.value.loginMessage
export const selectRouteNotFound = (state: RootState) => state.user.value.routeNotFound
export const selectLoading = (state: RootState) => state.user.loading
export const selectLoadingGlobal = (state: RootState) => {
  let loading = false
  for (const [, value] of Object.entries(state)) {
    const valueObj = value as object
    if ('loading' in valueObj) {
      const valLoading = valueObj as { loading: boolean }
      if (valLoading.loading) {
        loading = true
        break
      }
    }
  }
  return loading
}

export default userSlice.reducer
