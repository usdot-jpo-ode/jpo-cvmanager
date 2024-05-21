import { getBearingBetweenPoints } from './map-utils'
import * as turf from '@turf/turf'

export const parseMapSignalGroups = (mapMessage: ProcessedMap): SignalStateFeatureCollection => {
  const features: SignalStateFeature[] = []

  mapMessage?.mapFeatureCollection?.features?.forEach((mapFeature: MapFeature) => {
    // Find non-null signal group. connectsTo can have multiple entries, but only 1 may be non-null
    var signalGroup: number | undefined = undefined
    mapFeature?.properties?.connectsTo?.forEach((connection: J2735Connection) => {
      if (connection?.signalGroup) signalGroup = connection?.signalGroup
    })

    if (!mapFeature.properties.ingressApproach || !signalGroup) {
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
    type: 'FeatureCollection' as 'FeatureCollection',
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
    type: 'FeatureCollection' as 'FeatureCollection',
    features: features,
  }
  switch (notification.notificationType) {
    case 'ConnectionOfTravelNotification':
      // TODO: Re-add once more notification data is available
      // const connTravelNotification = notification as ConnectionOfTravelNotification;
      // const connTravelAssessmentGroups = connTravelNotification.assessment.connectionOfTravelAssessmentGroups;
      // connTravelAssessmentGroups?.forEach((assessmentGroup) => {
      //   const ingressLocation: number[] | undefined = connectingLanes.features.find(
      //     (connectingLaneFeature: MapFeature) => {
      //       return connectingLaneFeature.properties.laneId === assessmentGroup.ingressLaneID;
      //     }
      //   )?.geometry.coordinates[0];
      //   const egressLocation: number[] | undefined = connectingLanes.features.find(
      //     (connectingLaneFeature: MapFeature) => {
      //       return connectingLaneFeature.properties.laneId === assessmentGroup.egressLaneID;
      //     }
      //   )?.geometry.coordinates[0];
      //   if (!ingressLocation || !egressLocation) return;
      //   const marker = {
      //     type: "Feature",
      //     properties: {
      //       description: `${connTravelNotification.notificationText}, egress lane ${assessmentGroup.egressLaneID}, ingress lane ${assessmentGroup.ingressLaneID}, connection ID ${assessmentGroup.connectionID}, event count ${assessmentGroup.eventCount}`,
      //       title: connTravelNotification.notificationType,
      //     },
      //     geometry: {
      //       type: "LineString",
      //       coordinates: [ingressLocation, egressLocation],
      //     },
      //   };
      //   markerCollection.features.push(marker);
      // });
      break
    case 'IntersectionReferenceAlignmentNotification':
      // No markers for this notification
      break
    case 'LaneDirectionOfTravelNotification':
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
    case 'SignalGroupAlignmentNotification':
      // No markers for this notification
      break
    case 'SignalStateConflictNotification':
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
    case 'TimeChangeDetailsNotification':
      // No markers for this notification
      break
    case 'KafkaStreamsAnomalyNotification':
      // No markers for this notification
      break
    case 'BroadcastRateNotification':
      // No markers for this notification
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
    timedSignalGroups[Date.parse(spat.odeReceivedAt)] = spat.states.map((state) => {
      return {
        signalGroup: state.signalGroup,
        state: state.stateTimeSpeed?.[0]?.eventState as SignalState,
      }
    })
  })
  return timedSignalGroups
}

export const parseBsmToGeojson = (bsmData: OdeBsmData[]): BsmFeatureCollection => {
  return {
    type: 'FeatureCollection' as 'FeatureCollection',
    features: bsmData.map((bsm) => {
      return {
        type: 'Feature',
        properties: {
          ...bsm.payload.data.coreData,
          odeReceivedAt: new Date(bsm.metadata.odeReceivedAt as string).getTime() / 1000,
        },
        geometry: {
          type: 'Point',
          coordinates: [bsm.payload.data.coreData.position.longitude, bsm.payload.data.coreData.position.latitude],
        },
      }
    }),
  }
}
export const addConnections = (
  connectingLanes: ConnectingLanesFeatureCollection,
  signalGroups: SpatSignalGroup[],
  mapFeatures: MapFeatureCollection
): ConnectingLanesFeatureCollectionWithSignalState => {
  //bounding box representing the edges of the intersection
  var bbox = turf.bbox(connectingLanes)

  //for each connecting lane, fetch its ingress and egress lanes
  connectingLanes = {
    ...connectingLanes,
    features: connectingLanes.features
      ?.map((connectionFeature: ConnectingLanesFeature) => {
        var ingressLaneId = connectionFeature.properties.ingressLaneId
        var egressLaneId = connectionFeature.properties.egressLaneId
        var ingressLane = mapFeatures.features.find((feature) => feature.id === ingressLaneId)
        var egressLane = mapFeatures.features.find((feature) => feature.id === egressLaneId)

        if (ingressLane && egressLane) {
          var ingressCoords = ingressLane.geometry.coordinates
          var egressCoords = egressLane.geometry.coordinates

          var ingressBearing = turf.bearing(ingressCoords[1], ingressCoords[0])
          var egressBearing = turf.bearing(egressCoords[1], egressCoords[0])

          //project the ingress/egress lanes through the intersection to the edge of the bbox
          var ingressLine = turf.lineString([
            ingressCoords[0],
            turf.destination(ingressCoords[0], 0.05, ingressBearing).geometry.coordinates,
          ])
          var egressLine = turf.lineString([
            egressCoords[0],
            turf.destination(egressCoords[0], 0.05, egressBearing).geometry.coordinates,
          ])
          var clippedIngress = turf.bboxClip(ingressLine, bbox)
          var clippedEgress = turf.bboxClip(egressLine, bbox)

          //find the intersection point of the projected lanes, if it exists
          var intersect = turf.lineIntersect(clippedIngress.geometry, clippedEgress.geometry)

          //if the lanes intersect within the intersection, this is a ~90 degree turn and we add 1 more point to round the curve
          if (intersect.features.length > 0) {
            var intersectPoint = intersect.features[0].geometry.coordinates
            //the intersection would overshoot the curve, so curveMidpoint is a weighted average the intersection and connectingLanes edges
            var curveMidpoint = turf.centroid(
              turf.points([ingressCoords[0], egressCoords[0], intersectPoint, intersectPoint, intersectPoint])
            )

            var connectingLaneLine = turf.lineString([
              ingressCoords[0],
              curveMidpoint.geometry.coordinates,
              egressCoords[0],
            ])
            var curve = turf.bezierSpline(connectingLaneLine)
            connectionFeature = { ...connectionFeature, geometry: curve.geometry }
          }

          //If the ingress and egress lanes are going in generally opposite directions and didn't intersect, use the U-turn calculations
          else if (Math.abs(ingressBearing - egressBearing) < 45) {
            //this formula was found experimentally to give a round curve and allow parallel curving lanes to not intersect
            var leadupLength = Math.min(turf.distance(ingressCoords[0], egressCoords[0]) * -7 + 0.045, -0.02)

            var normalizedIngressPoint = turf.destination(ingressCoords[0], leadupLength, ingressBearing)
            var normalizedEgressPoint = turf.destination(egressCoords[0], leadupLength, egressBearing)
            var connectingLaneLine = turf.lineString([
              normalizedIngressPoint.geometry.coordinates,
              ingressCoords[0],
              egressCoords[0],
              normalizedEgressPoint.geometry.coordinates,
            ])

            var rawCurve = turf.bezierSpline(connectingLaneLine)
            //slice the curve back to remove the redundant ends
            var curve = turf.lineSlice(ingressCoords[0], egressCoords[0], rawCurve)
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
