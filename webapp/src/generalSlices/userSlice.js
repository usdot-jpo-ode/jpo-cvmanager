import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import GoogleAuthApi from '../apis/google-auth-api'
import { UserManager, LocalStorageManager } from '../managers'

const authDataLocalStorage = LocalStorageManager.getAuthData()
const authLoginData = UserManager.isLoginActive(authDataLocalStorage) ? authDataLocalStorage : null

export const login = createAsyncThunk('user/login', async (googleData, { dispatch }) => {
  // The value we return becomes the `fulfilled` action payload
  // return response.data;

  try {
    const data = await GoogleAuthApi.logIn(googleData.credential)
    let authLoginData = {
      data: JSON.parse(data),
      token: googleData.credential,
      expires_at: Date.now() + 3599000,
    }
    return authLoginData
  } catch (exception_var) {
    throw exception_var
  }
})

export const userSlice = createSlice({
  name: 'user',
  initialState: {
    loading: false,
    value: {
      authLoginData: authLoginData,
      organization: authLoginData?.data?.organizations?.[0],
      loginFailure: false,
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
      state.value.loginFailure = action.payload
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(login.pending, (state) => {
        state.loading = true
      })
      .addCase(login.fulfilled, (state, action) => {
        state.loading = false
        state.value.loginFailure = false
        state.value.authLoginData = action.payload
        state.value.organization = action.payload?.data?.organizations?.[0]
        LocalStorageManager.setAuthData(action.payload)
      })
      .addCase(login.rejected, (state) => {
        state.loading = false
        state.value.loginFailure = true
        LocalStorageManager.removeAuthData()
      })
  },
})

export const { logout, changeOrganization, setLoading, setLoginFailure } = userSlice.actions

export const selectAuthLoginData = (state) => state.user.value.authLoginData
export const selectToken = (state) => state.user.value.authLoginData.token
export const selectRole = (state) => state.user.value.organization?.role
export const selectOrganizationName = (state) => state.user.value.organization?.name
export const selectName = (state) => state.user.value.authLoginData?.data?.name
export const selectEmail = (state) => state.user.value.authLoginData?.data?.email
export const selectSuperUser = (state) => state.user.value.authLoginData?.data?.super_user
export const selectTokenExpiration = (state) => state.user.value.authLoginData?.expires_at
export const selectLoginFailure = (state) => state.user.value.loginFailure
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
