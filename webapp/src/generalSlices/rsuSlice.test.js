import reducer from './rsuSlice'

describe('rsu reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      loading: false,
      bsmLoading: false,
      requestOut: false,
      value: {
        selectedRsu: null,
        rsuData: [],
        rsuOnlineStatus: {},
        rsuCounts: {},
        countList: [],
        currentSort: '',
        startDate: '',
        endDate: '',
        messageLoading: false,
        warningMessage: false,
        msgType: 'BSM',
        rsuMapData: {},
        mapList: [],
        mapDate: '',
        displayMap: false,
        bsmStart: '',
        bsmEnd: '',
        addPoint: false,
        bsmCoordinates: [],
        bsmData: [],
        bsmDateError: false,
        bsmFilter: false,
        bsmFilterStep: 30,
        bsmFilterOffset: 0,
        issScmsStatusData: {},
        ssmDisplay: false,
        srmSsmList: [],
        selectedSrm: [],
        heatMapData: {
          type: 'FeatureCollection',
          features: [],
        },
      },
    })
  })
})
