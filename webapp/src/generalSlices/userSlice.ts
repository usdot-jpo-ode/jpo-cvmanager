import { PayloadAction, createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import AuthApi from '../apis/auth-api'
import { UserManager, LocalStorageManager, SecureStorageManager } from '../managers'
import { RootState } from '../store'

const authDataLocalStorage = LocalStorageManager.getAuthData()
const authLoginData = UserManager.isLoginActive(authDataLocalStorage) ? authDataLocalStorage : null

export const keycloakLogin = createAsyncThunk('user/login', async (token: string, { dispatch, rejectWithValue }) => {
  try {
    if (token) {
      const response = await AuthApi.logIn(token)
      switch (response.status) {
        case 200:
          let authLoginData = {
            data: JSON.parse(response.json.toString()),
            token: token,
            expires_at: Date.now() + 590000,
          }
          return authLoginData
        case 400:
          console.debug('400')
          return rejectWithValue('Login Unsuccessful: Bad Request')
        case 401:
          console.debug('401')
          return rejectWithValue('Login Unsuccessful: User Unauthorized Please Contact Support')
        case 403:
          console.debug('403')
          return rejectWithValue('Login Unsuccessful: Access Forbidden')
        case 404:
          console.debug('404')
          return rejectWithValue('Login Unsuccessful: Authentication API Not Found')
        default:
          console.debug('Token Failure')
          return rejectWithValue('Login Unsuccessful: Unknown Error Occurred')
      }
    } else {
      console.error('null token')
      return rejectWithValue('Login Unsuccessful: No KeyCloak Token Please Refresh')
    }
  } catch (exception_var) {
    console.debug('exception', exception_var)
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
      kcFailure: false,
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
      state.value.organization =
        UserManager.getOrganization(state.value.authLoginData, action.payload) ?? state.value.organization
    },
    setLoading: (state, action) => {
      state.loading = action.payload
    },
    setLoginFailure: (state, action) => {
      console.debug('setLoginFailure: ', action.payload)
      state.value.loginFailure = action.payload
    },
    setKcFailure: (state, action) => {
      state.value.kcFailure = action.payload
      state.loading = false
    },
    setLoginMessage: (state, action) => {
      state.value.loginMessage = action.payload
    },
    setRouteNotFound: (state, action) => {
      console.log('setRouteNotFound: ', action.payload)
      state.value.routeNotFound = action.payload
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(keycloakLogin.pending, (state) => {
        console.debug('keycloakLogin.pending')
        state.value.loginMessage = ''
        state.loading = true
      })
      .addCase(keycloakLogin.fulfilled, (state, action) => {
        console.debug('keycloakLogin.fulfilled', action)
        state.loading = false
        state.value.loginMessage = ''
        state.value.loginFailure = false
        state.value.authLoginData = action.payload
        state.value.organization = action.payload?.data?.organizations?.[0]
        LocalStorageManager.setAuthData(action.payload)
        SecureStorageManager.setUserRole(action.payload)
      })
      .addCase(keycloakLogin.rejected, (state, action: PayloadAction<unknown>) => {
        console.debug('keycloakLogin.rejected')
        state.loading = false
        state.value.loginFailure = true
        state.value.loginMessage = action.payload as string
        LocalStorageManager.removeAuthData()
        SecureStorageManager.removeUserRole()
      })
  },
})

export const { logout, changeOrganization, setLoading, setLoginFailure, setKcFailure, setRouteNotFound } =
  userSlice.actions

export const selectAuthLoginData = (state: RootState) => state.user.value.authLoginData
export const selectToken = (state: RootState) => state.user.value.authLoginData.token
export const selectRole = (state: RootState) => state.user.value.organization?.role
export const selectOrganizationName = (state: RootState) => state.user.value.organization?.name
export const selectName = (state: RootState) => state.user.value.authLoginData?.data?.name
export const selectEmail = (state: RootState) => state.user.value.authLoginData?.data?.email
export const selectSuperUser = (state: RootState) => state.user.value.authLoginData?.data?.super_user
export const selectReceiveErrorEmails = (state: RootState) => state.user.value.authLoginData?.data?.receive_error_emails
export const selectTokenExpiration = (state: RootState) => state.user.value.authLoginData?.expires_at
export const selectLoginFailure = (state: RootState) => state.user.value.loginFailure
export const selectKcFailure = (state: RootState) => state.user.value.kcFailure
export const selectLoginMessage = (state: RootState) => state.user.value.loginMessage
export const selectRouteNotFound = (state: RootState) => state.user.value.routeNotFound
export const selectLoading = (state: RootState) => state.user.loading
export const selectLoadingGlobal = (state: RootState) => {
  let loading = false
  for (const [key, value] of Object.entries(state)) {
    const valueObj = value as Object
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
