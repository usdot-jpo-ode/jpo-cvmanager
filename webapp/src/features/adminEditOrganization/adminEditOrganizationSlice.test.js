import reducer from './adminEditOrganizationSlice'

describe('admin add User reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      loading: false,
      value: {
        successMsg: '',
        errorState: false,
        errorMsg: '',
      },
    })
  })
})
