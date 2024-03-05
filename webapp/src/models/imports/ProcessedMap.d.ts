type ProcessedMap = {
  properties: MapSharedProperties;
  mapFeatureCollection: MapFeatureCollection;
  connectingLanesFeatureCollection: ConnectingLanesFeatureCollection;
};

type MapSharedProperties = {
  messageType: string;
  odeReceivedAt: string;
  originIp: string;
  intersectionName?: string;
  region?: number;
  intersectionId: number;
  msgIssueRevision: number;
  revision: number;
  refPoint: OdePosition3D;
  cti4501Conformant: Boolean;
  validationMessages: ProcessedValidationMessage[];
  laneWidth: number;
  speedLimits?: J2735RegulatorySpeedLimit[];
  mapSource: MapSource | string; //import us.dot.its.jpo.ode.model.OdeMapMetadata.MapSource;
  timeStamp: string;
};

type MapSource = "RSU" | "V2X" | "MMITSS" | "unknown";

// enum MapSource {
//     RSU,
//     V2X,
//     MMITSS,
//     unknown,
// };

type MapFeatureCollection = {
  type: "FeatureCollection";
  features: MapFeature[];
};

type MapFeature = {
  type: "Feature";
  id: number;
  geometry: GeoJSON.LineString;
  properties: MapProperties;
};

type Geometry = {
  type: string;
  coordinates: number[][];
};

type MapProperties = {
  nodes: MapNode[];
  laneId: number;
  laneName?: string;
  sharedWith: J2735LaneSharing;
  egressApproach: number;
  ingressApproach: number;
  ingressPath: boolean;
  egressPath: boolean;
  maneuvers?: J2735AllowedManeuvers;
  connectsTo?: J2735Connection[];
};

type MapNode = {
  delta: number[];
  dWidth?: number;
  dElevation?: number;
  stopLine: boolean | null;
};

type ConnectingLanesFeatureCollection = {
  type: "FeatureCollection";
  features: ConnectingLanesFeature[];
};

type ConnectingLanesUiFeatureCollection = {
  type: "FeatureCollection";
  features: ConnectingLanesUiFeature[];
};

type ConnectingLanesFeature = {
  type: "Feature";
  id: number | string;
  geometry: GeoJSON.LineString;
  properties: ConnectingLanesProperties;
};

type ConnectingLanesUiFeature = {
  type: "Feature";
  id: number | string;
  geometry: GeoJSON.LineString;
  properties: {
    signalState?: SignalState;
  };
};

type ConnectingLanesProperties = {
  signalGroupId: number | null;
  ingressLaneId: number;
  egressLaneId: number;
};
