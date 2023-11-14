export interface WZDxWorkZoneFeed {
  feed_info: WZDxFeedInfo
  type: 'FeatureCollection'
  features: WZDxFeature[]
}

export interface WZDxFeedInfo {
  update_date: string
  publisher: string
  contact_name: string
  contact_email: string
  update_frequency: number
  version: string
  license: string
  data_sources: WZDxDataSource[]
}

export interface WZDxDataSource {
  data_source_id: string
  organization_name: string
  contact_name: string
  contact_email: string
  update_frequency: number
  update_date: string
}

export interface WZDxFeature {
  id: string
  type: 'Feature'
  properties: WZDxProperties
  geometry: WZDxGeometry
}

export interface WZDxProperties {
  core_details: WZDxCoreDetails
  beginning_milepost?: number
  ending_milepost?: number
  is_start_position_verified: boolean
  is_end_position_verified: boolean
  start_date: string
  end_date: string
  location_method: string
  is_start_date_verified: boolean
  is_end_date_verified: boolean
  vehicle_impact: string
  reduced_speed_limit_kph?: number
  beginning_cross_street?: string
  ending_cross_street?: string
  work_zone_type?: string
  lanes?: WZDxLane[]
  worker_presence?: WZDxWorkerPresence
  restrictions?: any[]
  types_of_work?: WZDxTypesOfWork[]

  // Added by jacob
  table?: JSX.Element
}

export interface WZDxCoreDetails {
  data_source_id: string
  event_type: string
  road_names: string[]
  direction: string
  description: string
  creation_date: string
  update_date: string
  name?: string
  related_road_events?: WZDxRelatedRoadEvent[]
}

export interface WZDxRelatedRoadEvent {
  type: string
  id: string
}

export interface WZDxLane {
  order: number
  status: string
  type: string
  restrictions?: WZDxRestriction[]
}

export interface WZDxRestriction {
  type: string
  value: number
  unit: string
}

export interface WZDxWorkerPresence {
  are_workers_present: boolean
  method?: string
  worker_presence_last_confirmed_date?: string
  confidence?: string
  definition?: string[]
}

export interface WZDxTypesOfWork {
  type_name: string
  is_architectural_change: boolean
}

export interface WZDxGeometry {
  type: 'LineString'
  coordinates: number[][]
}
