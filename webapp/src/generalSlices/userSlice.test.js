import reducer from './userSlice'
import {
  // async thunks
  keycloakLogin,

  // reducers
  logout,
  changeOrganization,
  setLoading,
  setLoginFailure,

  // selectors
  selectAuthLoginData,
  selectToken,
  selectRole,
  selectOrganizationName,
  selectName,
  selectEmail,
  selectSuperUser,
  selectTokenExpiration,
  selectLoginFailure,
  selectLoading,
  selectLoadingGlobal,
} from './userSlice'
import AuthApi from '../apis/auth-api'
import { UserManager, LocalStorageManager } from '../managers'

describe('user reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      loading: true,
      value: {
        authLoginData: null,
        organization: undefined,
        loginFailure: false,
        kcFailure: false,
      },
    })
  })
})

describe('async thunks', () => {
  const initialState = {
    loading: null,
    bsmLoading: null,
    requestOut: null,
    value: {
      selectedRsu: null,
      rsuData: null,
      rsuOnlineStatus: null,
      rsuCounts: null,
      countList: null,
      currentSort: null,
      startDate: null,
      endDate: null,
      heatMapData: {
        features: [],
        type: 'FeatureCollection',
      },
      messageLoading: null,
      warningMessage: null,
      msgType: null,
      rsuMapData: null,
      mapList: null,
      mapDate: null,
      displayMap: null,
      bsmStart: null,
      bsmEnd: null,
      addPoint: null,
      bsmCoordinates: null,
      bsmData: null,
      bsmDateError: null,
      bsmFilter: null,
      bsmFilterStep: null,
      bsmFilterOffset: null,
      issScmsStatusData: null,
      ssmDisplay: null,
      srmSsmList: null,
      selectedSrm: null,
    },
  }

  beforeAll(() => {
    jest.mock('../apis/auth-api.js')
    jest.mock('../managers.js')
  })

  afterAll(() => {
    jest.unmock('../apis/auth-api.js')
    jest.unmock('../managers.js')
  })

  describe('login', () => {
    it('returns and calls the api correctly', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn()
      const kcToken = 'token'
      const action = keycloakLogin(kcToken)

      const data = { data: 'testingData' }
      AuthApi.logIn = jest.fn().mockReturnValue(JSON.stringify(data))
      Date.now = jest.fn(() => new Date(Date.UTC(2022, 1, 1)).valueOf())
      try {
        let resp = await action(dispatch, getState, undefined)
        expect(resp.payload).toEqual({
          data: data,
          token: kcToken,
          expires_at: Date.now() + 590000,
        })
        expect(AuthApi.logIn).toHaveBeenCalledWith('token')
      } catch (e) {
        Date.now.mockClear()
        throw e
      }
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
      const state = reducer(initialState, {
        type: 'user/login/pending',
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: {
          ...initialState.value,
        },
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      const loading = false
      const loginFailure = false
      const organization = { name: 'organizationName' }
      const authLoginData = { data: { organizations: [organization] } }
      LocalStorageManager.setAuthData = jest.fn()
      const state = reducer(initialState, {
        type: 'user/login/fulfilled',
        payload: authLoginData,
      })

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, loginFailure, organization, authLoginData },
      })
      expect(LocalStorageManager.setAuthData).toHaveBeenCalledWith(authLoginData)
    })

    it('Updates the state correctly rejected', async () => {
      const loading = false
      const loginFailure = true
      LocalStorageManager.removeAuthData = jest.fn()
      const state = reducer(initialState, {
        type: 'user/login/rejected',
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value, loginFailure } })
      expect(LocalStorageManager.removeAuthData).toHaveBeenCalled()
    })
  })
})

describe('reducers', () => {
  const initialState = {
    loading: null,
    value: {
      authLoginData: null,
      organization: null,
      loginFailure: null,
    },
  }

  it('logout reducer updates state correctly', async () => {
    const authLoginData = null
    const organization = null
    LocalStorageManager.removeAuthData = jest.fn()
    expect(
      reducer(
        {
          ...initialState,
          value: { ...initialState.value, authLoginData: 'authLoginData', organization: 'organization' },
        },
        logout()
      )
    ).toEqual({
      ...initialState,
      value: { ...initialState.value, authLoginData, organization },
    })
    expect(LocalStorageManager.removeAuthData).toHaveBeenCalled()
  })

  it('changeOrganization reducer updates state correctly', async () => {
    const organization = 'organization'
    UserManager.getOrganization = jest.fn().mockReturnValue(organization)
    expect(
      reducer(
        {
          ...initialState,
          value: { ...initialState.value, authLoginData: 'authLoginData' },
        },
        changeOrganization('payload')
      )
    ).toEqual({
      ...initialState,
      value: { ...initialState.value, organization, authLoginData: 'authLoginData' },
    })
    expect(UserManager.getOrganization).toHaveBeenCalledWith('authLoginData', 'payload')

    UserManager.getOrganization = jest.fn().mockReturnValue(null)
    expect(
      reducer(
        {
          ...initialState,
          value: { ...initialState.value, organization, authLoginData: 'authLoginData' },
        },
        changeOrganization('payload')
      )
    ).toEqual({
      ...initialState,
      value: { ...initialState.value, organization, authLoginData: 'authLoginData' },
    })
    expect(UserManager.getOrganization).toHaveBeenCalledWith('authLoginData', 'payload')
  })

  it('setLoading reducer updates state correctly', async () => {
    const loading = 'loading'
    expect(reducer(initialState, setLoading(loading))).toEqual({
      ...initialState,
      loading,
      value: { ...initialState.value },
    })
  })

  it('setLoginFailure reducer updates state correctly', async () => {
    const loginFailure = 'loginFailure'
    expect(reducer(initialState, setLoginFailure(loginFailure))).toEqual({
      ...initialState,
      value: { ...initialState.value, loginFailure },
    })
  })
})

describe('selectors', () => {
  const initialState = {
    loading: 'loading',
    value: {
      organization: {
        role: 'role',
        name: 'organizationName',
      },
      authLoginData: {
        token: 'token',
        data: {
          name: 'name',
          email: 'email',
          super_user: 'superUser',
        },
        expires_at: 'expires_at',
      },
      loginFailure: 'loginFailure',
    },
  }
  const state = { user: initialState, rsu: { loading: false }, config: { loading: false } }

  it('selectors return the correct value', async () => {
    expect(selectAuthLoginData(state)).toEqual(initialState.value.authLoginData)
    expect(selectToken(state)).toEqual('token')
    expect(selectRole(state)).toEqual('role')
    expect(selectOrganizationName(state)).toEqual('organizationName')
    expect(selectName(state)).toEqual('name')
    expect(selectEmail(state)).toEqual('email')
    expect(selectSuperUser(state)).toEqual('superUser')
    expect(selectTokenExpiration(state)).toEqual('expires_at')
    expect(selectLoginFailure(state)).toEqual('loginFailure')
    expect(selectLoading(state)).toEqual('loading')
  })

  it('loadingGlobal selector returns the correct value', async () => {
    const loadingState = {
      user: { loading: false },
      rsu: { loading: false },
      config: { loading: false },
      abcdefg: { loading: false },
    }
    expect(selectLoadingGlobal(loadingState)).toEqual(false)
    expect(selectLoadingGlobal({ ...loadingState, user: { loading: true } })).toEqual(true)
  })
})
