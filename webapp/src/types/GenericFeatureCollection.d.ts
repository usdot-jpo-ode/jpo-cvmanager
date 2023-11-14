export type GenericFeatureCollection = {
  type: 'FeatureCollection'
  features: Array<GenericFeature>
}

export type GenericFeature = {
  id?: number | string
  type: 'Feature'
  geometry: GenericFeatureGeometry
  properties: Object
}

export type GenericFeatureGeometry =
  | {
      type: 'Point'
      coordinates: number[]
    }
  | {
      type: 'LineString'
      coordinates: number[][]
    }
  | {
      type: 'MultiPoint'
      coordinates: number[][]
    }
  | {
      type: 'Polygon'
      coordinates: number[][] | number[][][]
    }
