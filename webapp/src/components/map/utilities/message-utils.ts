import { getBearingBetweenPoints } from "./map-utils";

export const parseMapSignalGroups = (mapMessage: ProcessedMap): SignalStateFeatureCollection => {
  const features: SignalStateFeature[] = [];

  mapMessage?.mapFeatureCollection?.features?.forEach((mapFeature: MapFeature) => {
    if (!mapFeature.properties.ingressApproach || !mapFeature?.properties?.connectsTo?.[0]?.signalGroup) {
      return;
    }
    const coords = mapFeature.geometry.coordinates.slice(0, 2);
    features.push({
      type: "Feature",
      properties: {
        signalGroup: mapFeature.properties.connectsTo[0].signalGroup,
        intersectionId: mapMessage.properties.intersectionId,
        orientation: getBearingBetweenPoints(coords[1], coords[0]),
        signalState: "UNAVAILABLE",
      },
      geometry: {
        type: "Point",
        coordinates: mapFeature.geometry.coordinates[0],
      },
    });
  });

  return {
    type: "FeatureCollection" as "FeatureCollection",
    features: features,
  };
};

export const createMarkerForNotification = (
  center: number[],
  notification: MessageMonitor.Notification,
  connectingLanes: MapFeatureCollection
) => {
  const features: any[] = [];
  const markerCollection = {
    type: "FeatureCollection" as "FeatureCollection",
    features: features,
  };
  switch (notification.notificationType) {
    case "ConnectionOfTravelNotification":
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
      break;
    case "IntersectionReferenceAlignmentNotification":
      // No markers for this notification
      break;
    case "LaneDirectionOfTravelNotification":
      const laneDirTravelNotification = notification as LaneDirectionOfTravelNotification;
      const laneDirTravelAssessmentGroups = laneDirTravelNotification.assessment.laneDirectionOfTravelAssessmentGroup;
      laneDirTravelAssessmentGroups?.forEach((assessmentGroup) => {
        const laneLocation: number[] | undefined = connectingLanes.features.find(
          (connectingLaneFeature: MapFeature) => {
            return connectingLaneFeature.properties.laneId === assessmentGroup.laneID;
          }
        )?.geometry.coordinates[0];
        if (!laneLocation) return;
        const numEvents = assessmentGroup.inToleranceEvents + assessmentGroup.outOfToleranceEvents;
        const eventsRatio = assessmentGroup.inToleranceEvents / numEvents;
        const marker = {
          type: "Feature",
          properties: {
            description: `${laneDirTravelNotification.notificationText}, lane ID ${assessmentGroup.laneID}, in tolerance events ${eventsRatio} (${assessmentGroup.inToleranceEvents}/${numEvents})`,
            title: laneDirTravelNotification.notificationType,
          },
          geometry: {
            type: "Point",
            coordinates: laneLocation,
          },
        };
        markerCollection.features.push(marker);
      });
      break;
    case "SignalGroupAlignmentNotification":
      // No markers for this notification
      break;
    case "SignalStateConflictNotification":
      const sigStateConflictNotification = notification as SignalStateConflictNotification;
      const sigStateConflictEvent = sigStateConflictNotification.event;
      const sigStateConflictMarker = {
        type: "Feature",
        properties: {
          description: `${sigStateConflictNotification.notificationText}, Conflict type ${sigStateConflictEvent.conflictType}, First conflicting signal state ${sigStateConflictEvent.firstConflictingSignalState} of group ${sigStateConflictEvent.firstConflictingSignalGroup}, Second conflicting signal state ${sigStateConflictEvent.secondConflictingSignalState} of group ${sigStateConflictEvent.secondConflictingSignalGroup}`,
          title: sigStateConflictNotification.notificationType,
        },
        geometry: {
          type: "Point",
          coordinates: center,
        },
      };
      markerCollection.features.push(sigStateConflictMarker);
      break;
    case "TimeChangeDetailsNotification":
      // No markers for this notification
      break;
    case "KafkaStreamsAnomalyNotification":
      // No markers for this notification
      break;
    case "BroadcastRateNotification":
      // No markers for this notification
      break;
  }
  return markerCollection;
};

export const addConnections = (
  connectingLanes: ConnectingLanesFeatureCollection,
  signalGroups: SpatSignalGroup[]
): ConnectingLanesUiFeatureCollection => {
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
  };
};

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
          "UNAVAILABLE",
      },
    })),
  };
};

export const parseSpatSignalGroups = (spats: ProcessedSpat[]): SpatSignalGroups => {
  const timedSignalGroups: SpatSignalGroups = {};
  spats?.forEach((spat: ProcessedSpat) => {
    timedSignalGroups[Date.parse(spat.odeReceivedAt)] = spat.states.map((state) => {
      return {
        signalGroup: state.signalGroup,
        state: state.stateTimeSpeed?.[0]?.eventState as SignalState,
      };
    });
  });
  return timedSignalGroups;
};

export const parseBsmToGeojson = (bsmData: OdeBsmData[]): BsmFeatureCollection => {
  return {
    type: "FeatureCollection" as "FeatureCollection",
    features: bsmData.map((bsm) => {
      return {
        type: "Feature",
        properties: {
          ...bsm.payload.data.coreData,
          odeReceivedAt: new Date(bsm.metadata.odeReceivedAt as string).getTime() / 1000,
        },
        geometry: {
          type: "Point",
          coordinates: [bsm.payload.data.coreData.position.longitude, bsm.payload.data.coreData.position.latitude],
        },
      };
    }),
  };
};
