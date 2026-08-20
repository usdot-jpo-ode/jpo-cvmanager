import { LaneDirectionOfTravelNotification } from '../../../../models/jpo-conflictmonitor/notifications/LaneDirectionOfTravelNotification'
import { getTimestamp } from '../map-component'
import { getBearingBetweenPoints } from './map-utils'
import * as turf from '@turf/turf'

export const parseMapSignalGroups = (mapMessage: ProcessedMap): SignalStateFeatureCollection => {
  const features: SignalStateFeature[] = []

  mapMessage?.mapFeatureCollection?.features?.forEach((mapFeature: MapFeature) => {
    // Find non-null signal group. connectsTo can have multiple entries, but only 1 may be non-null
    let signalGroup: number | undefined = undefined
    mapFeature?.properties?.connectsTo?.forEach((connection: J2735Connection) => {
      if (connection?.signalGroup) signalGroup = connection?.signalGroup
    })

    if (!signalGroup) {
        console.warn(`No signal group found for map feature ${mapFeature.id}, skipping signal state feature creation`)
      return
    }
    const coords = mapFeature.geometry.coordinates.slice(0, 2)
    features.push({
      type: 'Feature',
      properties: {
        signalGroup: signalGroup,
        intersectionId: mapMessage.properties.intersectionId,
        orientation: getBearingBetweenPoints(coords[1], coords[0]),
        signalState: 'UNAVAILABLE',
      },
      geometry: {
        type: 'Point',
        coordinates: mapFeature.geometry.coordinates[0],
      },
    })
  })

  return {
    type: 'FeatureCollection' as const,
    features: features,
  }
}

export const createMarkerForNotification = (
  center: number[],
  notification: MessageMonitor.Notification,
  connectingLanes: MapFeatureCollection
) => {
  const features: any[] = []
  const markerCollection = {
    type: 'FeatureCollection' as const,
    features: features,
  }
  switch (notification.notificationType) {
    case 'ConnectionOfTravelNotification':
      console.warn('ConnectionOfTravelNotification type does not have a graphical display yet')
      break
    case 'IntersectionReferenceAlignmentNotification':
      console.warn('IntersectionReferenceAlignmentNotification type does not have a graphical display yet')
      break
    case 'LaneDirectionOfTravelNotification': {
      const laneDirTravelNotification = notification as LaneDirectionOfTravelNotification
      const laneDirTravelAssessmentGroups = laneDirTravelNotification.assessment.laneDirectionOfTravelAssessmentGroup
      laneDirTravelAssessmentGroups?.forEach((assessmentGroup) => {
        const laneLocation: number[] | undefined = connectingLanes.features.find(
          (connectingLaneFeature: MapFeature) => {
            return connectingLaneFeature.properties.laneId === assessmentGroup.laneID
          }
        )?.geometry.coordinates[0]
        if (!laneLocation) return
        const numEvents = assessmentGroup.inToleranceEvents + assessmentGroup.outOfToleranceEvents
        const eventsRatio = assessmentGroup.inToleranceEvents / numEvents
        const marker = {
          type: 'Feature',
          properties: {
            description: `${laneDirTravelNotification.notificationText}, lane ID ${assessmentGroup.laneID}, in tolerance events ${eventsRatio} (${assessmentGroup.inToleranceEvents}/${numEvents})`,
            title: laneDirTravelNotification.notificationType,
          },
          geometry: {
            type: 'Point',
            coordinates: laneLocation,
          },
        }
        markerCollection.features.push(marker)
      })
      break
    }
    case 'SignalGroupAlignmentNotification':
      console.warn('SignalGroupAlignmentNotification type does not have a graphical display yet')
      break
    case 'SignalStateConflictNotification': {
      const sigStateConflictNotification = notification as SignalStateConflictNotification
      const sigStateConflictEvent = sigStateConflictNotification.event
      const sigStateConflictMarker = {
        type: 'Feature',
        properties: {
          description: `${sigStateConflictNotification.notificationText}, Conflict type ${sigStateConflictEvent.conflictType}, First conflicting signal state ${sigStateConflictEvent.firstConflictingSignalState} of group ${sigStateConflictEvent.firstConflictingSignalGroup}, Second conflicting signal state ${sigStateConflictEvent.secondConflictingSignalState} of group ${sigStateConflictEvent.secondConflictingSignalGroup}`,
          title: sigStateConflictNotification.notificationType,
        },
        geometry: {
          type: 'Point',
          coordinates: center,
        },
      }
      markerCollection.features.push(sigStateConflictMarker)
      break
    }
    case 'TimeChangeDetailsNotification':
      console.warn('TimeChangeDetailsNotification type does not have a graphical display yet')
      break
    case 'KafkaStreamsAnomalyNotification':
      console.warn('KafkaStreamsAnomalyNotification type does not have a graphical display yet')
      break
    case 'BroadcastRateNotification':
      console.warn('BroadcastRateNotification type does not have a graphical display yet')
      break
  }
  return markerCollection
}

export const generateSignalStateFeatureCollection = (
  prevSignalStates: SignalStateFeatureCollection,
  signalGroups: SpatSignalGroup[]
): SignalStateFeatureCollection => {
  return {
    ...prevSignalStates,
    features: (prevSignalStates?.features ?? []).map((feature) => ({
      ...feature,
      properties: {
        ...feature.properties,
        signalState:
          signalGroups?.find((signalGroup) => signalGroup.signalGroup == feature.properties.signalGroup)?.state ??
          'UNAVAILABLE',
      },
    })),
  }
}

export const parseSpatSignalGroups = (spats: ProcessedSpat[]): SpatSignalGroups => {
  const timedSignalGroups: SpatSignalGroups = {}
  spats?.forEach((spat: ProcessedSpat) => {
    timedSignalGroups[getTimestamp(spat.utcTimeStamp)] = spat.states.map((state) => {
      return {
        signalGroup: state.signalGroup,
        state: state.stateTimeSpeed?.[0]?.eventState as SignalState,
      }
    })
  })
  return timedSignalGroups
}

// Additionally sort ascending, earliest first
export const addMapTimestampsAndSortAscending = (maps: ProcessedMap[]): ProcessedMap[] =>
  maps
    .map((map) => ({
      ...map,
      properties: {
        ...map.properties,
        timeStamp: getTimestamp(map.properties.timeStamp),
        odeReceivedAt: getTimestamp(map.properties.odeReceivedAt),
      },
    }))
    .sort((a, b) => a.properties.timeStamp - b.properties.timeStamp)

// Additionally sort ascending, earliest first
export const addSpatTimestampsAndSortAscending = (spats: ProcessedSpat[]): ProcessedSpat[] =>
  spats
    .map((spat) => ({
      ...spat,
      utcTimeStamp: getTimestamp(spat.utcTimeStamp),
    }))
    .sort((a, b) => a.utcTimeStamp - b.utcTimeStamp)

// Additionally sort ascending, earliest first
export const addBsmTimestampsAndSortAscending = (bsmFeatureCollection: BsmFeatureCollection): BsmFeatureCollection => {
  return {
    type: 'FeatureCollection' as const,
    features: bsmFeatureCollection.features
      .map((bsm) => {
        return {
          ...bsm,
          properties: {
            ...bsm.properties,
            odeReceivedAtEpochSeconds: new Date(bsm.properties.odeReceivedAt).getTime() / 1000,
          },
        }
      })
      .sort((a, b) => a.properties.odeReceivedAtEpochSeconds - b.properties.odeReceivedAtEpochSeconds),
  }
}

// Additionally sort ascending, earliest first
export const addSsmTimestampsAndSortAscending = (ssms: ProcessedSsm[]): ProcessedSsm[] =>
  ssms
    .map((ssm) => ({
      ...ssm,
      odeReceivedAtEpochMillis: getTimestamp(ssm.odeReceivedAt),
      timeStampEpochMillis: getTimestamp(ssm.timeStamp),
    }))
    .sort((a, b) => a.timeStampEpochMillis - b.timeStampEpochMillis)

// Additionally sort ascending, earliest first
export const addSrmTimestampsAndSortAscending = (srms: ProcessedSrmFeature[]): ProcessedSrmFeature[] =>
  srms
    .map((srm) => ({
      ...srm,
      properties: {
        ...srm.properties,
        odeReceivedAtEpochMillis: getTimestamp(srm.properties.odeReceivedAt),
        timeStampEpochMillis: getTimestamp(srm.properties.timeStamp),
      },
    }))
    .sort((a, b) => a.properties.timeStampEpochMillis - b.properties.timeStampEpochMillis)

export const isValidDate = (d: Date) => {
  if (d == null) return false
  return d instanceof Date && !isNaN(d?.getTime())
}

export const addConnections = (
  connectingLanes: ConnectingLanesFeatureCollection,
  signalGroups: SpatSignalGroup[],
  mapFeatures: MapFeatureCollection
): ConnectingLanesFeatureCollectionWithSignalState => {
  //bounding box representing the edges of the intersection
  const bbox = turf.bbox(connectingLanes)

  //for each connecting lane, fetch its ingress and egress lanes
  connectingLanes = {
    ...connectingLanes,
    features: connectingLanes.features
      ?.map((connectionFeature: ConnectingLanesFeature) => {
        const ingressLaneId = connectionFeature.properties.ingressLaneId
        const egressLaneId = connectionFeature.properties.egressLaneId
        const ingressLane = mapFeatures.features.find((feature) => feature.id === ingressLaneId)
        const egressLane = mapFeatures.features.find((feature) => feature.id === egressLaneId)

        if (ingressLane && egressLane) {
          const ingressCoords = ingressLane.geometry.coordinates
          const egressCoords = egressLane.geometry.coordinates

          const ingressBearing = turf.bearing(ingressCoords[1], ingressCoords[0])
          const egressBearing = turf.bearing(egressCoords[1], egressCoords[0])

          //project the ingress/egress lanes through the intersection to the edge of the bbox
          const ingressLine = turf.lineString([
            ingressCoords[0],
            turf.destination(ingressCoords[0], 0.05, ingressBearing).geometry.coordinates,
          ])
          const egressLine = turf.lineString([
            egressCoords[0],
            turf.destination(egressCoords[0], 0.05, egressBearing).geometry.coordinates,
          ])
          const clippedIngress = turf.bboxClip(ingressLine, bbox)
          const clippedEgress = turf.bboxClip(egressLine, bbox)

          //find the intersection point of the projected lanes, if it exists
          const intersect = turf.lineIntersect(clippedIngress.geometry, clippedEgress.geometry)

          //if the lanes intersect within the intersection, this is a ~90 degree turn and we add 1 more point to round the curve
          if (intersect.features.length > 0) {
            const intersectPoint = intersect.features[0].geometry.coordinates
            //the intersection would overshoot the curve, so curveMidpoint is a weighted average the intersection and connectingLanes edges
            const curveMidpoint = turf.centroid(
              turf.points([ingressCoords[0], egressCoords[0], intersectPoint, intersectPoint, intersectPoint])
            )

            const connectingLaneLine = turf.lineString([
              ingressCoords[0],
              curveMidpoint.geometry.coordinates,
              egressCoords[0],
            ])
            const curve = turf.bezierSpline(connectingLaneLine)
            connectionFeature = { ...connectionFeature, geometry: curve.geometry }
          }

          //If the ingress and egress lanes are going in generally opposite directions and didn't intersect, use the U-turn calculations
          else if (Math.abs(ingressBearing - egressBearing) < 45) {
            //this formula was found experimentally to give a round curve and allow parallel curving lanes to not intersect
            const leadupLength = Math.min(turf.distance(ingressCoords[0], egressCoords[0]) * -7 + 0.045, -0.02)

            const normalizedIngressPoint = turf.destination(ingressCoords[0], leadupLength, ingressBearing)
            const normalizedEgressPoint = turf.destination(egressCoords[0], leadupLength, egressBearing)
            const connectingLaneLine = turf.lineString([
              normalizedIngressPoint.geometry.coordinates,
              ingressCoords[0],
              egressCoords[0],
              normalizedEgressPoint.geometry.coordinates,
            ])

            const rawCurve = turf.bezierSpline(connectingLaneLine)
            //slice the curve back to remove the redundant ends
            const curve = turf.lineSlice(ingressCoords[0], egressCoords[0], rawCurve)
            connectionFeature = { ...connectionFeature, geometry: curve.geometry }
          }
          //anything else is mostly straight and doesn't require a bezier curve
          return connectionFeature
        }
        return null
      })
      .filter((feature) => feature !== null) as ConnectingLanesFeature[],
  }

  return {
    ...connectingLanes,
    features: connectingLanes.features.map((feature) => ({
      ...feature,
      properties: {
        ...feature.properties,
        signalState: signalGroups.find((signalGroup) => signalGroup.signalGroup == feature.properties.signalGroupId)
          ?.state,
      },
    })),
  }
}

export const addSsmSrmToConnections = (
  connectingLanes: ConnectingLanesFeatureCollectionWithSignalState,
  ssms: ProcessedSsm[],
  srms: ProcessedSrmFeature[]
): ConnectingLanesFeatureCollectionWithSsmSrm => {
  return {
    ...connectingLanes,
    features: connectingLanes.features.map((feature) => {
      const ssmInfo: SsmInfo[] = ssms.map(getSsmInfoList).flat()
      const srmInfo: SrmInfo[] = srms.map(getSrmInfoList).flat()
      const signalStatuses = ssmInfo.filter(
        (status) =>
          status.inboundLaneID &&
          status.inboundLaneID === feature.properties.ingressLaneId &&
          (!status.outboundLaneID || status.outboundLaneID == feature.properties.egressLaneId)
        // TODO: Support linking by connection ID  || (status.inboundLaneConnectionID && status.inboundLaneConnectionID === feature.properties.)
      )
      const signalRequests = srmInfo.filter(
        (request) =>
          request.inboundLaneID &&
          request.inboundLaneID === feature.properties.ingressLaneId &&
          (!request.outboundLaneID || request.outboundLaneID == feature.properties.egressLaneId)
        // TODO: Support linking by connection ID  || (request.inboundLaneConnectionID && request.inboundLaneConnectionID === feature.properties.)
      )
      return {
        ...feature,
        properties: {
          ...feature.properties,
          signalRequests: sortSrmInfo(signalRequests),
          signalStatuses: sortSsmInfo(signalStatuses),
        },
      } as ConnectingLanesFeatureWithSsmSrm
    }),
  }
}

export const addSsmSrmToMapFeatures = (
  mapFeatures: MapFeatureCollection,
  ssms: ProcessedSsm[],
  srms: ProcessedSrmFeature[]
): MapFeatureCollectionWithSsmSrm => {
  return {
    ...mapFeatures,
    features: mapFeatures.features.map((feature) => {
      const ssmInfo: SsmInfo[] = ssms.map(getSsmInfoList).flat()
      const srmInfo: SrmInfo[] = srms.map(getSrmInfoList).flat()
      const signalStatuses = ssmInfo.filter(
        (status) =>
          (status.inboundLaneID && status.inboundLaneID == feature.properties.laneId) ||
          (status.outboundLaneID && status.outboundLaneID == feature.properties.laneId)
      )
      const signalRequests = srmInfo.filter(
        (request) =>
          (request.inboundLaneID && request.inboundLaneID == feature.properties.laneId) ||
          (request.outboundLaneID && request.outboundLaneID == feature.properties.laneId)
      )
      return {
        ...feature,
        properties: {
          ...feature.properties,
          signalRequests: sortSrmInfo(signalRequests),
          signalStatuses: sortSsmInfo(signalStatuses),
        },
      } as MapFeatureWithSsmSrm
    }),
  }
}

export const sortSsmInfo = (ssms: SsmInfo[]): SsmInfo[] => {
  // Sort descending by timestamp for separate request IDs
  // if same request ID, place higher sequence number before
  return [...ssms].sort((a, b) => {
    if (a.requestID === b.requestID) {
      return (b.sequenceNumber ?? 0) - (a.sequenceNumber ?? 0)
    }
    return (b.timeStampEpochMillis ?? 0) - (a.timeStampEpochMillis ?? 0)
  })
}

export const sortSrmInfo = (srms: SrmInfo[]): SrmInfo[] => {
  // Sort by timestamp for separate request IDs
  // if same request ID, place after by sequence number
  return [...srms].sort((a, b) => {
    if (a.requestID === b.requestID) {
      return (b.sequenceNumber ?? 0) - (a.sequenceNumber ?? 0)
    }
    return (b.timeStampEpochMillis ?? 0) - (a.timeStampEpochMillis ?? 0)
  })
}

const extractMatchingSsmStatuses = (statuses: ProcessedSignalStatus[], requests: ProcessedSignalRequest[]) => {
  const matchingStatuses: ProcessedSignalStatus[] = []
  statuses?.forEach((status) => {
    requests?.forEach((request) => {
      if (request.requestID == status.requestID) {
        matchingStatuses.push(status)
      }
    })
  })
  return matchingStatuses
}

export const addSsmStatus = (
  srmData: ProcessedSrmFeature[],
  ssmData: ProcessedSsm[]
): ProcessedSrmFeatureWithStatus[] =>
  srmData.map((srm) => {
    // Find matching SSM by vehicle ID
    let matchingSsms: ProcessedSsm[] = []
    ssmData.forEach((ssm) => {
      const matchingStatuses = extractMatchingSsmStatuses(ssm.statusList ?? [], srm.properties.requests ?? [])
      if (matchingStatuses.length > 0) {
        matchingSsms.push({ ...ssm, statusList: matchingStatuses } as ProcessedSsm)
      }
    })

    return {
      ...srm,
      properties: {
        ...srm.properties,
        ssms: matchingSsms,
      },
    } as ProcessedSrmFeatureWithStatus
  })

export const getSsmInfoList = (ssm: ProcessedSsm): SsmInfo[] =>
  ssm.statusList?.map((status) => ({
    requestInfo: {
      vehicleID: status.vehicleID,
      requesterSequenceNumber: status.requesterSequenceNumber,
      role: status.requesterRole,
      subrole: status.requesterSubrole,
      importanceLevel: status.requestImportanceLevel,
      iso3833VehicleType: status.requesterIso3833VehicleType,
      hpmsType: status.requesterHpmsType,
    },
    timeStampEpochMillis: ssm.timeStampEpochMillis,
    status: status.status,
    sequenceNumber: ssm.sequenceNumber,
    statusSequenceNumber: ssm.statusSequenceNumber,
    requestID: status.requestID,
    inboundLaneID: status.inboundOnLaneID,
    inboundLaneConnectionID: status.inboundOnLaneConnectionID,
    outboundLaneID: status.outboundOnLaneID,
    outboundLaneConnectionID: status.outboundOnLaneConnectionID,
    estimatedTimeOfArrival: status.estimatedTimeOfArrival,
    estimatedTimeOfArrivalDurationSeconds: status.estimatedTimeOfArrivalDurationSeconds,
  })) || []

export const getSrmInfoList = (srm: ProcessedSrmFeature): SrmInfo[] =>
  srm.properties.requests?.map((request) => ({
    vehicleInfo: {
      vehicleID: srm.properties.vehicleID,
      role: srm.properties.role,
      subrole: srm.properties.subrole,
      importanceLevel: srm.properties.importanceLevel,
      iso3833VehicleType: srm.properties.iso3833VehicleType,
      hpmsType: srm.properties.hpmsType,
    },
    timeStampEpochMillis: srm.properties.timeStampEpochMillis,
    sequenceNumber: srm.properties.sequenceNumber,
    requestID: request.requestID,
    priorityRequestType: request.priorityRequestType,
    inboundLaneID: request.inboundLaneID,
    inboundLaneConnectionID: request.inboundLaneConnectionID,
    outboundLaneID: request.outboundLaneID,
    outboundLaneConnectionID: request.outboundLaneConnectionID,
    estimatedTimeOfArrival: request.estimatedTimeOfArrival,
    estimatedTimeOfArrivalDurationSeconds: request.estimatedTimeOfArrivalDurationSeconds,
  })) || []
