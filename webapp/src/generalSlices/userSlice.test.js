import reducer from './userSlice'

describe('user reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      loading: false,
      value: {
        authLoginData: null,
        organization: undefined,
        loginFailure: false,
      },
    })
  })
})
