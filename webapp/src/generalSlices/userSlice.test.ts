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
  selectReceiveErrorEmails,
  selectTokenExpiration,
  selectLoginFailure,
  selectLoading,
  selectLoadingGlobal,
} from './userSlice'
import AuthApi from '../apis/auth-api'
import { UserManager, LocalStorageManager } from '../managers'
import { setupJestCanvasMock } from 'jest-canvas-mock'

beforeEach(() => {
  jest.resetAllMocks()
  setupJestCanvasMock()
})
import { RootState } from '../store'

describe('user reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      loading: true,
      value: {
        authLoginData: null,
        organization: undefined,
        loginFailure: false,
        kcFailure: false,
        loginMessage: '',
        routeNotFound: false,
      },
    })
  })
})

describe('async thunks', () => {
  const initialState: RootState['user'] = {
    loading: null,
    value: {
      authLoginData: null,
      organization: null,
      loginFailure: undefined,
      kcFailure: null,
      loginMessage: '',
      routeNotFound: false,
    },
  }

  beforeAll(() => {
    jest.mock('../apis/auth-api')
    jest.mock('../managers')
  })

  afterAll(() => {
    jest.unmock('../apis/auth-api')
    jest.unmock('../managers')
  })

  describe('login', () => {
    it('returns and calls the api correctly', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn()
      const kcToken = 'token'
      const action = keycloakLogin(kcToken)
      const testData = JSON.stringify({ data: 'testingData' })
      const data = { json: testData, status: 200 }
      AuthApi.logIn = jest.fn().mockReturnValue(data)
      Date.now = jest.fn(() => new Date(Date.UTC(2022, 1, 1)).valueOf())
      try {
        let resp = await action(dispatch, getState, undefined)
        expect(resp.payload).toEqual({
          data: JSON.parse(data.json),
          token: kcToken,
          expires_at: Date.now() + 590000,
        })
        expect(AuthApi.logIn).toHaveBeenCalledWith('token')
      } catch (e) {
        ;(Date.now as any).mockClear()
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
      const loginMessage = 'error message'
      LocalStorageManager.removeAuthData = jest.fn()
      const state = reducer(initialState, {
        type: 'user/login/rejected',
        payload: loginMessage,
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value, loginFailure, loginMessage } })
      expect(LocalStorageManager.removeAuthData).toHaveBeenCalled()
    })
  })
})

describe('reducers', () => {
  const initialState: RootState['user'] = {
    loading: null,
    value: {
      authLoginData: null,
      organization: null,
      loginFailure: null,
      loginMessage: '',
      kcFailure: null,
      routeNotFound: false,
    },
  }

  it('logout reducer updates state correctly', async () => {
    const authLoginData = null as any
    const organization = null as any
    LocalStorageManager.removeAuthData = jest.fn()
    expect(
      reducer(
        {
          ...initialState,
          value: { ...initialState.value, authLoginData: 'authLoginData', organization: 'organization' },
        } as any,
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
        } as any,
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
        } as any,
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
          receive_error_emails: 'receiveErrorEmails',
        },
        expires_at: 'expires_at',
      },
      loginFailure: 'loginFailure',
    },
  }
  const state = { user: initialState, rsu: { loading: false }, config: { loading: false } } as any

  it('selectors return the correct value', async () => {
    expect(selectAuthLoginData(state)).toEqual(initialState.value.authLoginData)
    expect(selectToken(state)).toEqual('token')
    expect(selectRole(state)).toEqual('role')
    expect(selectOrganizationName(state)).toEqual('organizationName')
    expect(selectName(state)).toEqual('name')
    expect(selectEmail(state)).toEqual('email')
    expect(selectSuperUser(state)).toEqual('superUser')
    expect(selectReceiveErrorEmails(state)).toEqual('receiveErrorEmails')
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
    } as any
    expect(selectLoadingGlobal(loadingState)).toEqual(false)
    expect(selectLoadingGlobal({ ...loadingState, user: { loading: true } })).toEqual(true)
  })
})
