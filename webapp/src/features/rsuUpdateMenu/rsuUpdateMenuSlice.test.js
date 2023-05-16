import reducer from './rsuUpdateMenuSlice'
import {
  // async thunks
  updateRsuData,
  performOSUpdate,
  performFWUpdate,

  // selectors
  selectLoading,
  selectChecked,
  selectOsUpdateAvailable,
  selectFwUpdateAvailable,
} from './rsuUpdateMenuSlice'
import apiHelper from '../../apis/api-helper'
import EnvironmentVars from '../../EnvironmentVars'

describe('RSU update menu reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      loading: false,
      value: {
        checked: false,
        osUpdateAvailable: [],
        fwUpdateAvailable: [],
      },
    })
  })
})
