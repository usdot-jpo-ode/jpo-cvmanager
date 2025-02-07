export interface MooveAiFeature {
  type: 'Feature'
  properties: MooveAiProperties
  geometry: MooveAiGeometry
}

export interface MooveAiProperties {
  segment_id: string
  journey_id_count: number
  speed_avg: number
  total_hard_brake_count: number
  total_wiper_activated_count: number
}

export interface MooveAiGeometry {
  type: 'LineString'
  coordinates: number[][]
}
