type ProcessedMap = {
  properties: MapSharedProperties
  mapFeatureCollection: MapFeatureCollection
  connectingLanesFeatureCollection: ConnectingLanesFeatureCollection
}

type MapSharedProperties = {
  schemaVersion: number
  messageType: 'MAP'
  odeReceivedAt: number
  originIp: string
  intersectionName?: string
  region?: number
  intersectionId: number
  msgIssueRevision: number
  revision: number
  refPoint: OdePosition3D
  cti4501Conformant: boolean
  validationMessages: ProcessedValidationMessage[]
  laneWidth: number
  speedLimits?: J2735RegulatorySpeedLimit[]
  mapSource: MapSource | string //import us.dot.its.jpo.ode.model.OdeMapMetadata.MapSource;
  timeStamp: string
}

type MapSource = 'RSU' | 'V2X' | 'MMITSS' | 'unknown'

type MapFeatureCollection = {
  type: 'FeatureCollection'
  features: MapFeature[]
}

type MapFeature = {
  type: 'Feature'
  id: number
  geometry: GeoJSON.LineString
  properties: MapProperties
}

type Geometry = {
  type: string
  coordinates: number[][]
}

type MapProperties = {
  nodes: MapNode[]
  laneId: number
  laneName?: string
  laneType?: object
  sharedWith: J2735LaneSharing
  egressApproach: number
  ingressApproach: number
  ingressPath: boolean
  egressPath: boolean
  maneuvers?: J2735AllowedManeuvers
  connectsTo?: J2735Connection[]
}

type MapNode = {
  delta: number[]
  dWidth?: number
  dElevation?: number
  stopLine: boolean | null
}

type ConnectingLanesFeatureCollection = {
  type: 'FeatureCollection'
  features: ConnectingLanesFeature[]
}

type ConnectingLanesFeatureCollectionWithSignalState = {
  type: 'FeatureCollection'
  features: ConnectingLanesFeatureWithSignalState[]
}

type ConnectingLanesFeature = {
  type: 'Feature'
  id: number | string
  geometry: GeoJSON.LineString
  properties: ConnectingLanesProperties
}

type ConnectingLanesFeatureWithSignalState = {
  type: 'Feature'
  id: number | string
  geometry: GeoJSON.LineString
  properties: ConnectingLanesProperties & {
    signalState?: SignalState
  }
}

type ConnectingLanesProperties = {
  signalGroupId: number | null
  ingressLaneId: number
  egressLaneId: number
}

type LaneTypeAttributes = {
  vehicle?: LaneAttributes_Vehicle
  crosswalk?: LaneAttributes_Crosswalk
  bikeLane?: LaneAttributes_Bike
  sidewalk?: LaneAttributes_Sidewalk
  median?: LaneAttributes_Barrier
  striping?: LaneAttributes_Striping
  trackedVehicle?: LaneAttributes_TrackedVehicle
  parking?: LaneAttributes_Parking
}

type LaneAttributes_Vehicle = {
  isVehicleRevocableLane: boolean // this lane may be activated or not based on the current SPAT message contents if not asserted, the lane is ALWAYS present
  isVehicleFlyOverLane: boolean // path of lane is not at grade
  hovLaneUseOnly: boolean
  restrictedToBusUse: boolean
  restrictedToTaxiUse: boolean
  restrictedFromPublicUse: boolean
  hasIRbeaconCoverage: boolean
  permissionOnRequest: boolean // to inform about a lane for e-cars
}

type LaneAttributes_Crosswalk = {
  crosswalkRevocableLane: boolean // this lane may be activated or not based
  // on the current SPAT message contents
  // if not asserted, the lane is ALWAYS present
  bicyleUseAllowed: boolean // The path allows bicycle traffic,
  // if not set, this mode is prohibited
  isXwalkFlyOverLane: boolean // path of lane is not at grade
  fixedCycleTime: boolean // ped walk phases use preset times
  // i.e., there is not a ‘push to cross’ button
  biDirectionalCycleTimes: boolean // ped walk phases use different SignalGroupID
  // for each direction. The first SignalGroupID
  // in the first Connection represents ‘inbound’
  // flow (the direction of travel towards the first
  // node point) while second SignalGroupID in the
  // next Connection entry represents the ‘outbound’
  // flow. And use of RestrictionClassID entries
  // in the Connect follow this same pattern in pairs.
  hasPushToWalkButton: boolean // Has a demand input
  audioSupport: boolean // audio crossing cues present
  rfSignalRequestPresent: boolean // Supports RF push to walk technologies
  unsignalizedSegmentsPresent: boolean // The lane path consists of one of more segments that are not signal controlled
}

type LaneAttributes_Bike = {
  bikeRevocableLane: boolean // this lane may be activated or not based
  // based on the current SPAT message contents
  // if not asserted, the lane is ALWAYS present
  pedestrianUseAllowed: boolean // The path allows pedestrian traffic,
  // if not set, this mode is prohibited
  isBikeFlyOverLane: boolean // path of lane is not at grade
  fixedCycleTime: boolean // the phases use preset times
  // i.e., there is not a ‘push to cross’ button
  biDirectionalCycleTimes: boolean // ped walk phases use different SignalGroupID
  // for each direction. The first SignalGroupID
  // in the first Connection represents ‘inbound’
  // flow (the direction of travel towards the first
  // node point) while second SignalGroupID in the
  // next Connection entry represents the ‘outbound’
  // flow. And use of RestrictionClassID entries
}

type LaneAttributes_Sidewalk = {
  sidewalkRevocableLane: boolean // this lane may be activated or not based
  // on the current SPAT message contents
  // if not asserted, the lane is ALWAYS present
  bicyleUseAllowed: boolean // The path allows bicycle traffic,
  // if not set, this mode is prohibited
  isSidewalkFlyOverLane: boolean // path of lane is not at grade
  walkBikes: boolean // bike traffic must dismount and walk
}

type LaneAttributes_Barrier = {
  medianRevocableLane: boolean // this lane may be activated or not based
  // based on the current SPAT message contents
  // if not asserted, the lane is ALWAYS present
  median: boolean
  whiteLineHashing: boolean
  stripedLines: boolean
  doubleStripedLines: boolean
  trafficCones: boolean
  constructionBarrier: boolean
  trafficChannels: boolean
  lowCurbs: boolean
  highCurbs: boolean
}

type LaneAttributes_Striping = {
  stripeToConnectingLanesRevocableLane: boolean // this lane may be activated or not activated based
  // on the current SPAT message contents
  // if not asserted, the lane is ALWAYS present
  stripeDrawOnLeft: boolean
  stripeDrawOnRight: boolean // which side of lane to mark
  stripeToConnectingLanesLeft: boolean
  stripeToConnectingLanesRight: boolean
  stripeToConnectingLanesAhead: boolean
  // the stripe type should be
  // presented to the user visually
  // to reflect stripes in the
  // intersection for the type of
  // movement indicated
}

type LaneAttributes_TrackedVehicle = {
  specRevicableLane: boolean // this lane may be activated or not based
  // on the current SPAT message contents
  // if not asserted, the lane is ALWAYS present
  specCommuterRailRoadTrack: boolean
  specLightRailRoadTrack: boolean
  specHeavyRailRoadTrack: boolean
  specOtherRailType: boolean
}

type LaneAttributes_Parking = {
  parkingRevocableLane: boolean // this lane may be activated or not based
  // on the current SPAT message contents
  // if not asserted, the lane is ALWAYS present
  parallelParkingInUse: boolean
  headInParkingInUse: boolean
  doNotParkZone: boolean
  // used to denote fire hydrants as well as
  // short disruptions in a parking zone
  parkingForBusUse: boolean
  parkingForTaxiUse: boolean
  noPublicParkingUse: boolean
  // private parking, as in front of
  // private property
}
