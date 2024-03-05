type J2735GenericLane = {
  laneID?: number;
  name?: String;
  ingressApproach?: number;
  egressApproach?: number;
  laneAttributes?: J2735LaneAttributes;
  maneuvers?: J2735AllowedManeuvers;
  nodeList?: J2735NodeListXY;
  connectsTo?: J2735Connection[];
  overlays?: J2735OverlayLaneList;
};

type J2735OverlayLaneList = {
  laneIds?: number[];
};

type J2735ConnectsToList = {
  connectsTo?: J2735Connection[];
};

type J2735Connection = {
  connectingLane?: J2735ConnectingLane;
  remoteIntersection: J2735IntersectionReferenceID | null;
  signalGroup: number | null;
  userClass: number | null;
  connectionID: number;
};

type J2735ConnectingLane = {
  lane?: number;
  maneuver: J2735AllowedManeuvers;
};

type J2735IntersectionReferenceID = {
  region?: number;
  id?: number;
};

type J2735LaneAttributes = {
  directionalUse?: J2735BitString;
  shareWith: J2735BitString;
  laneType: J2735LaneTypeAttributes;
};

type J2735LaneTypeAttributes = {
  vehicle?: J2735BitString; // motor vehicle lanes - J2735LaneAttributesVehicle
  crosswalk: J2735BitString; // pedestrian crosswalks - J2735LaneAttributesCrosswalk
  bikeLane: J2735BitString; // bike lanes - J2735LaneAttributesBike
  sidewalk: J2735BitString; // pedestrian sidewalk paths - J2735LaneAttributesSidewalk
  median: J2735BitString; // medians & channelization - J2735LaneAttributesBarrier
  striping: J2735BitString; // roadway markings - J2735LaneAttributesStriping
  trackedVehicle: J2735BitString; // trains and trolleys - J2735LaneAttributesTrackedVehicle
  parking: J2735BitString; // parking and stopping lanes - J2735LaneAttributesParking
};

type J2735BitString = string;

type J2735NodeListXY = {
  nodes?: J2735NodeSetXY;
  computed?: J2735ComputedLane;
};

type J2735ComputedLane = {
  referenceLaneId?: number;
  offsetXaxis?: number; // could have an object with min and max inside of it
  offsetYaxis?: number; // could have an object with min and max inside of it
  rotateXY?: number;
  scaleXaxis?: number;
  scaleYaxis?: number;
};

type J2735NodeSetXY = {
  NodeXY?: J2735NodeXY[];
};

type J2735NodeXY = {
  delta?: J2735NodeOffsetPointXY;
  attributes?: J2735NodeAttributeSetXY;
};

type J2735NodeOffsetPointXY = {
  nodeXY1?: J2735Node_XY;
  nodeXY2: J2735Node_XY;
  nodeXY3: J2735Node_XY;
  nodeXY4: J2735Node_XY;
  nodeXY5: J2735Node_XY;
  nodeXY6: J2735Node_XY;
};

type J2735Node_XY = {
  x?: number;
  y?: number;
};

type J2735NodeAttributeSetXY = {
  localNode?: J2735NodeAttributeXYList;
  disabled: J2735SegmentAttributeXYList;
  enabled: J2735SegmentAttributeXYList;
  data: J2735LaneDataAttributeList;
  dWidth: number;
  dElevation: number;
};

type J2735NodeAttributeXYList = {
  localNode?: J2735NodeAttributeXYList;
};

type J2735SegmentAttributeXYList = {
  segAttrList?: J2735SegmentAttributeXY[];
};

type J2735LaneDataAttributeList = {
  localNode?: J2735LaneDataAttribute;
};

type J2735LaneDataAttribute = {
  pathEndPointAngle?: number;
  laneCrownPointCenter: number;
  laneCrownPointLeft: number;
  laneCrownPointRight: number;
  laneAngle: number;
  speedLimits: J2735SpeedLimitList;
};

type J2735SpeedLimitList = {
  speedLimits?: J2735RegulatorySpeedLimit[];
};

type J2735RegulatorySpeedLimit = {
  type?: J2735SpeedLimitType;
  speed?: number;
};

type J2735SpeedLimitType =
  | "unknown"
  | "maxSpeedInSchoolZone"
  | "maxSpeedInSchoolZoneWhenChildrenArePresent"
  | "maxSpeedInConstructionZone"
  | "vehicleMinSpeed"
  | "vehicleMaxSpeed"
  | "vehicleNightMaxSpeed"
  | "truckMinSpeed"
  | "truckMaxSpeed"
  | "truckNightMaxSpeed"
  | "vehiclesWithTrailersMinSpeed"
  | "vehiclesWithTrailersMaxSpeed"
  | "vehiclesWithTrailersNightMaxSpeed";

type J2735SegmentAttributeXY =
  | "reserved"
  | "doNotBlock"
  | "whiteLine"
  | "mergingLaneLeft"
  | "mergingLaneRight"
  | "curbOnLeft"
  | "curbOnRight"
  | "loadingzoneOnLeft"
  | "loadingzoneOnRight"
  | "turnOutPointOnLeft"
  | "turnOutPointOnRight"
  | "adjacentParkingOnLeft"
  | "adjacentParkingOnRight"
  | "adjacentBikeLaneOnLeft"
  | "adjacentBikeLaneOnRight"
  | "sharedBikeLane"
  | "bikeBoxInFront"
  | "transitStopOnLeft"
  | "transitStopOnRight"
  | "transitStopInLane"
  | "sharedWithTrackedVehicle"
  | "safeIsland"
  | "lowCurbsPresent"
  | "rumbleStripPresent"
  | "audibleSignalingPresent"
  | "adaptiveTimingPresent"
  | "rfSignalRequestPresent"
  | "partialCurbIntrusion"
  | "taperToLeft"
  | "taperToRight"
  | "taperToCenterLine"
  | "headInParking"
  | "freeParking"
  | "timeRestrictionsOnParking"
  | "costToPark"
  | "midBlockCurbPresent"
  | "unEvenPavementPresent";
