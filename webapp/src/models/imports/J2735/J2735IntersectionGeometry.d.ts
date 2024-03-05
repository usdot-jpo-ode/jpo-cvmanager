type J2735IntersectionGeometry = {
  name?: String;
  id?: J2735IntersectionReferenceID;
  revision?: number;
  refPoint?: OdePosition3D;
  laneWidth?: number;
  speedLimits?: J2735SpeedLimitList;
  laneSet?: J2735LaneList;
};

type J2735LaneList = {
  genericLane?: J2735GenericLane[];
};
