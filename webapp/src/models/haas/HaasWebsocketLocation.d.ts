import { Feature } from 'geojson'

export interface HaasWebsocketLocationParams {
  active_only: boolean
  start_time_utc_millis: number
  end_time_utc_millis: number
  limit?: number
}

export interface HaasWebsocketLocationResponse {
  data: HaasWebsocketLocationFeatureCollection
  metadata: HaasWebsocketLocationMetadata
}

export interface HaasWebsocketLocationMetadata {
  limit: number
  returnedCount: number
  truncated: boolean
  message: string
}

export interface HaasWebsocketLocationFeatureCollection {
  type: 'FeatureCollection'
  features: HaasWebsocketLocationFeature[]
}

export interface HaasWebsocketLocationFeature extends Feature<HaasLocationGeometry, HaasLocationProperties> {
  type: 'Feature'
  geometry: HaasLocationGeometry
  properties: HaasLocationProperties
}

export interface HaasLocationProperties {
  id: string
  type: string
  detailed_type: string
  external_id: string
  start_time: string
  end_time: string
  lat: number
  lon: number
  alt: number
  street_name: string
  location_type: string
  is_active: boolean
  things_active: ThingReference[]
  things_inactive: ThingReference[]
  features: HaasFeature[]
}

export interface HaasLocationGeometry {
  type: 'Point'
  coordinates: number[]
}

export interface ThingReference {
  id: string
  external_id: string
  start_time: string
  end_time: string
}

// Internal feature and geometry types in HAAS messages
export interface HaasFeature {
  type: string
  geometry: HaasGeometry[]
}
export interface HaasGeometry {
  lat: number
  lon: number
}
