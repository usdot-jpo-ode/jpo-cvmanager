import { configureStore } from '@reduxjs/toolkit'
import userReducer from '../../generalSlices/userSlice'
import { vi } from 'vitest'

vi.doMock('../../EnvironmentVars', () => ({
  EnvironmentVars: {
    CVIZ_API_SERVER_URL: 'http://localhost:8080',
  },
}))

import { userApiSlice } from './userApiSlice'

const makeStore = () =>
  configureStore({
    reducer: {
      user: userReducer,
      [userApiSlice.reducerPath]: userApiSlice.reducer,
    },
    preloadedState: {
      user: {
        loading: false,
        value: {
          authLoginData: {
            data: {
              name: 'Test User',
              first_name: 'Test',
              last_name: 'User',
              email: 'test@example.com',
              super_user: false,
              organizations: [],
            },
            token: 'mock-token',
            expires_at: 0,
          },
          organization: { organization: 'OrgA', role: 'ADMIN' },
          loginFailure: false,
          loginMessage: '',
          routeNotFound: false,
        },
      },
    },
    middleware: (gDM) => gDM().concat(userApiSlice.middleware),
  })

const mockJsonResponse = (body: unknown, status = 200) =>
  Promise.resolve(
    new Response(JSON.stringify(body), {
      status,
      headers: { 'Content-Type': 'application/json' },
    })
  )

describe('userApiSlice', () => {
  beforeEach(() => {
    jest.clearAllMocks()
    global.fetch = jest.fn()
  })

  test('getUsers', async () => {
    ;(global.fetch as jest.Mock).mockImplementationOnce(() =>
      mockJsonResponse({ content: [], page: 0, size: 100, totalElements: 0, totalPages: 0 })
    )
    const store = makeStore()

    const action = store.dispatch(
      userApiSlice.endpoints.getUsers.initiate({
        organization: 'OrgA',
        page: 0,
        size: 100,
        sort: 'first_name,asc',
        search: '',
      })
    )
    await action.unwrap()

    const req = (global.fetch as jest.Mock).mock.calls[0][0] as Request
    expect(req.url).toBe('http://localhost:8080/users?page=0&size=100&sort=first_name%2Casc&search=')
    expect(req.method).toBe('GET')
    expect(req.headers.get('Authorization')).toBe('Bearer mock-token')
    expect(req.headers.get('Organization')).toBe('OrgA')
    action.unsubscribe()
  })

  test('getUser', async () => {
    ;(global.fetch as jest.Mock).mockImplementationOnce(() => mockJsonResponse({ email: 'a@b.com' }))
    const store = makeStore()

    const action = store.dispatch(userApiSlice.endpoints.getUser.initiate('a@b.com'))
    await action.unwrap()

    const req = (global.fetch as jest.Mock).mock.calls[0][0] as Request
    expect(req.url).toBe('http://localhost:8080/users/a@b.com')
    expect(req.method).toBe('GET')
    action.unsubscribe()
  })

  test('getUserAllowedSelections', async () => {
    ;(global.fetch as jest.Mock).mockImplementationOnce(() => mockJsonResponse({ organizations: [], roles: [] }))
    const store = makeStore()

    const action = store.dispatch(userApiSlice.endpoints.getUserAllowedSelections.initiate())
    await action.unwrap()

    const req = (global.fetch as jest.Mock).mock.calls[0][0] as Request
    expect(req.url).toBe('http://localhost:8080/users/allowed-selections')
    expect(req.method).toBe('GET')
    action.unsubscribe()
  })

  test('getRoles', async () => {
    ;(global.fetch as jest.Mock).mockImplementationOnce(() => mockJsonResponse(['USER', 'ADMIN']))
    const store = makeStore()

    const action = store.dispatch(userApiSlice.endpoints.getRoles.initiate())
    await action.unwrap()

    const req = (global.fetch as jest.Mock).mock.calls[0][0] as Request
    expect(req.url).toBe('http://localhost:8080/users/roles')
    expect(req.method).toBe('GET')
    action.unsubscribe()
  })

  test('createUser', async () => {
    ;(global.fetch as jest.Mock).mockImplementationOnce(() => mockJsonResponse({}))
    const store = makeStore()
    const body = { email: 'new@user.com', first_name: 'New' } as any

    await store.dispatch(userApiSlice.endpoints.createUser.initiate(body)).unwrap()

    const req = (global.fetch as jest.Mock).mock.calls[0][0] as Request
    expect(req.url).toBe('http://localhost:8080/users')
    expect(req.method).toBe('POST')
    expect(await req.clone().json()).toEqual(body)
  })

  test('patchUser', async () => {
    ;(global.fetch as jest.Mock).mockImplementationOnce(() => mockJsonResponse({}))
    const store = makeStore()

    await store
      .dispatch(
        userApiSlice.endpoints.patchUser.initiate({
          email: 'u@x.com',
          patch: { first_name: 'Updated' } as any,
        })
      )
      .unwrap()

    const req = (global.fetch as jest.Mock).mock.calls[0][0] as Request
    expect(req.url).toBe('http://localhost:8080/users/u@x.com')
    expect(req.method).toBe('PATCH')
    expect(await req.clone().json()).toEqual({ origin_ip: 'u@x.com', first_name: 'Updated' })
  })

  test('deleteUser', async () => {
    ;(global.fetch as jest.Mock).mockImplementationOnce(() => mockJsonResponse({}))
    const store = makeStore()

    await store.dispatch(userApiSlice.endpoints.deleteUser.initiate('gone@x.com')).unwrap()

    const req = (global.fetch as jest.Mock).mock.calls[0][0] as Request
    expect(req.url).toBe('http://localhost:8080/users/gone@x.com')
    expect(req.method).toBe('DELETE')
  })

  test('deleteMultipleUsers', async () => {
    ;(global.fetch as jest.Mock).mockImplementationOnce(() => mockJsonResponse({}))
    const store = makeStore()
    const emails = ['a@x.com', 'b@x.com']

    await store.dispatch(userApiSlice.endpoints.deleteMultipleUsers.initiate(emails)).unwrap()

    const req = (global.fetch as jest.Mock).mock.calls[0][0] as Request
    expect(req.url).toBe('http://localhost:8080/users/batch')
    expect(req.method).toBe('DELETE')
    expect(await req.clone().json()).toEqual(emails)
  })
})
