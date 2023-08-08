import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import AuthApi from '../apis/auth-api'
import { UserManager, LocalStorageManager } from '../managers'

const authDataLocalStorage = LocalStorageManager.getAuthData()
const authLoginData = UserManager.isLoginActive(authDataLocalStorage) ? authDataLocalStorage : null

export const keycloakLogin = createAsyncThunk('user/login', async (token, { dispatch }) => {
  try {
    if (token) {
      const data = await AuthApi.logIn(token)
      let authLoginData = {
        data: JSON.parse(data),
        token: token,
        expires_at: Date.now() + 590000,
      }
      return authLoginData
    } else {
      console.log('null token')
      throw new Error('Token is null')
    }
  } catch (exception_var) {
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
    },
  },
  reducers: {
    logout: (state) => {
      state.value.authLoginData = null
      state.value.organization = null
      LocalStorageManager.removeAuthData()
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
  },
  extraReducers: (builder) => {
    builder
      .addCase(keycloakLogin.pending, (state) => {
        console.debug('keycloakLogin.pending')
        state.loading = true
      })
      .addCase(keycloakLogin.fulfilled, (state, action) => {
        console.debug('keycloakLogin.fulfilled', action)
        state.loading = false
        state.value.loginFailure = false
        state.value.authLoginData = action.payload
        state.value.organization = action.payload?.data?.organizations?.[0]
        LocalStorageManager.setAuthData(action.payload)
      })
      .addCase(keycloakLogin.rejected, (state) => {
        console.debug('keycloakLogin.rejected')
        state.loading = false
        state.value.loginFailure = true
        LocalStorageManager.removeAuthData()
      })
  },
})

export const { logout, changeOrganization, setLoading, setLoginFailure, setKcFailure } = userSlice.actions

export const selectAuthLoginData = (state) => state.user.value.authLoginData
export const selectToken = (state) => state.user.value.authLoginData.token
export const selectRole = (state) => state.user.value.organization?.role
export const selectOrganizationName = (state) => state.user.value.organization?.name
export const selectName = (state) => state.user.value.authLoginData?.data?.name
export const selectEmail = (state) => state.user.value.authLoginData?.data?.email
export const selectSuperUser = (state) => state.user.value.authLoginData?.data?.super_user
export const selectTokenExpiration = (state) => state.user.value.authLoginData?.expires_at
export const selectLoginFailure = (state) => state.user.value.loginFailure
export const selectKcFailure = (state) => state.user.value.kcFailure
export const selectLoading = (state) => state.user.loading
export const selectLoadingGlobal = (state) => {
  let loading = false
  for (const [key, value] of Object.entries(state)) {
    if (value.loading) {
      loading = true
      break
    }
  }
  return loading
}

export default userSlice.reducer
