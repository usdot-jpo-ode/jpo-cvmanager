import {
  addBsmTimestampsAndSortAscending,
  addConnections,
  createMarkerForNotification,
  generateSignalStateFeatureCollection,
  isValidDate,
  parseMapSignalGroups,
  parseSpatSignalGroups,
} from './message-utils'

import mapDataRaw from '../../../../utils/test_data/intersection_12109_MAP_data.json'
import spatData from '../../../../utils/test_data/intersection_12109_SPAT_data.json'
import bsmData from '../../../../utils/test_data/intersection_12109_BSM_data.json'

describe('message-utils', () => {
  const mapData = mapDataRaw[0] as unknown as ProcessedMap

  it('parseMapSignalGroups builds point features with signal groups', () => {
    const result = parseMapSignalGroups(mapData)

    expect(result.type).toBe('FeatureCollection')
    expect(result.features.length).toBeGreaterThan(0)
    expect(result.features[0].geometry.type).toBe('Point')
    expect(result.features[0].properties.signalGroup).toBeDefined()
  })

  it('createMarkerForNotification builds a point marker for signal state conflict notifications', () => {
    const center = [-105.0908854, 39.5880413]
    const notification = {
      notificationType: 'SignalStateConflictNotification',
      notificationText: 'Conflict detected',
      event: {
        conflictType: 'TEST_CONFLICT',
        firstConflictingSignalState: 'STOP_AND_REMAIN',
        firstConflictingSignalGroup: 2,
        secondConflictingSignalState: 'PROTECTED_CLEARANCE',
        secondConflictingSignalGroup: 6,
      },
    } as unknown as MessageMonitor.Notification

    const result = createMarkerForNotification(center, notification, mapData.mapFeatureCollection)

    expect(result.type).toBe('FeatureCollection')
    expect(result.features).toHaveLength(1)
    expect(result.features[0].geometry.type).toBe('Point')
    expect(result.features[0].geometry.coordinates).toEqual(center)
  })

  it('generateSignalStateFeatureCollection applies current signal state to existing signal features', () => {
    const prevSignalStates = parseMapSignalGroups(mapData)
    const signalGroup = prevSignalStates.features[0].properties.signalGroup

    const result = generateSignalStateFeatureCollection(prevSignalStates, [
      { signalGroup, state: 'STOP_AND_REMAIN' },
    ])

    expect(result.features[0].properties.signalState).toBe('STOP_AND_REMAIN')
  })

  it('parseSpatSignalGroups groups states by timestamp', () => {
    const result = parseSpatSignalGroups(spatData as unknown as ProcessedSpat[])
    const firstTimestamp = spatData[0].utcTimeStamp

    expect(result[firstTimestamp]).toBeDefined()
    expect(result[firstTimestamp][0].signalGroup).toBe(spatData[0].states[0].signalGroup)
  })

  it('addBsmTimestampsAndSortAscending derives epoch seconds from odeReceivedAt and sorts ascending', () => {
    const result = addBsmTimestampsAndSortAscending(bsmData as unknown as BsmFeatureCollection)
    const expectedEpochSeconds = Date.parse(bsmData.features[0].properties.odeReceivedAt) / 1000

    expect(result.features[0].properties.odeReceivedAtEpochSeconds).toBe(expectedEpochSeconds)
    // Check that the features are sorted ascending by odeReceivedAtEpochSeconds
    for (let i = 1; i < result.features.length; i++) {
      expect(result.features[i].properties.odeReceivedAtEpochSeconds).toBeGreaterThanOrEqual(
        result.features[i - 1].properties.odeReceivedAtEpochSeconds
      )
    }
  })

  it('isValidDate returns true for a valid Date', () => {
    expect(isValidDate(new Date('2025-10-15T23:49:47.376Z'))).toBe(true)
  })

  it('addConnections enriches connecting lanes with signal state', () => {
    const firstSpat = spatData[0] as unknown as ProcessedSpat
    const signalGroups = firstSpat.states.map((state) => ({
      signalGroup: state.signalGroup,
      state: state.stateTimeSpeed[0]?.eventState as SignalState,
    }))

    const result = addConnections(mapData.connectingLanesFeatureCollection, signalGroups, mapData.mapFeatureCollection)

    expect(result.type).toBe('FeatureCollection')
    expect(result.features.length).toBeGreaterThan(0)
    expect(result.features.some((feature) => feature.properties.signalState !== undefined)).toBe(true)
  })
})
