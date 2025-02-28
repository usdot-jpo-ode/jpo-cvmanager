import { Feature } from 'geojson'

export interface MooveAiFeature extends Feature<MooveAiGeometry, MooveAiProperties> {
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
