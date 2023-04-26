import reducer from './adminEditRsuSlice'

describe('admin edit RSU reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      loading: false,
      value: {
        successMsg: '',
        apiData: {},
        errorState: false,
        errorMsg: '',
        primaryRoutes: [],
        selectedRoute: '',
        otherRouteDisabled: true,
        rsuModels: [],
        selectedModel: '',
        sshCredentialGroups: [],
        selectedSshGroup: '',
        snmpCredentialGroups: [],
        selectedSnmpGroup: '',
        organizations: [],
        selectedOrganizations: [],
        submitAttempt: false,
      },
    })
  })
})
