import reducer from './adminEditUserSlice'

describe('admin edit User reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      loading: false,
      value: {
        successMsg: '',
        selectedOrganizationNames: [],
        selectedOrganizations: [],
        organizationNames: [],
        availableRoles: [],
        apiData: {},
        errorState: false,
        errorMsg: '',
        submitAttempt: false,
      },
    })
  })
})
