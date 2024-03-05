type BsmFeatureCollection = {
  type: "FeatureCollection";
  features: BsmFeature[];
};

type BsmFeature = {
  type: "Feature";
  properties: J2735BsmCoreData & bsmReceivedAt;
  geometry: PointGemetry;
};

type PointGemetry = {
  type: "Point";
  coordinates: number[];
};

type bsmReceivedAt = {
  odeReceivedAt: number;
};

type BsmUiFeatureCollection = {
  type: "FeatureCollection";
  features: BsmUiFeature[];
};

type BsmUiFeature = {
  type: "Feature";
  properties: BsmUiProperties;
  geometry: PointGemetry;
};

type BsmUiProperties = {
  id: string;
  secMark: number;
  speed: number;
  heading: number;
};
