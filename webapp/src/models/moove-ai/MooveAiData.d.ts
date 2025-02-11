export interface MooveAiFeature {
  type: 'Feature'
  properties: MooveAiProperties
  geometry: MooveAiGeometry
}

export interface MooveAiProperties {
  segment_id: string
  total_hard_brake_count: number
}

export interface MooveAiGeometry {
  type: 'LineString'
  coordinates: number[][]
}
