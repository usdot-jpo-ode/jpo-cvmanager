export type RsuProperties = {
  rsu_id: number
  milepost: number
  geography: string
  model_name: string
  ipv4_address: string
  primary_route: string
  serial_number: string
  manufacturer_name: string
}

export type RsuInfo = {
  rsuList: Array<{
    id: number
    type: 'Feature'
    geometry: {
      type: 'Point'
      coordinates: Array<number>
    }
    properties: RsuProperties
  }>
}

export type RsuOnlineStatus = 'online' | 'offline' | 'unstable'

export type RsuOnlineStatusRespMultiple = {
  [ip: string]: {
    current_status: RsuOnlineStatus
    last_online: string | undefined
  }
}
export type RsuOnlineStatusRespSingle = {
  ip: string
  current_status: RsuOnlineStatus
  last_online: string | undefined
}

export type RsuCounts = {
  [ip: string]: {
    road: string
    count: number
  }
}

// No response used, this method does not appear to be used
export type GetRsuUserAuthResp = {}

// No response used, this method does not appear to be used
export type GetRsuCommandResp = {}

export type RsuMapInfo = {
  geojson: GeoJSON.FeatureCollection<GeoJSON.Geometry>
  date: string
}
export type RsuMapInfoIpList = string[]

// No response used, this method does not appear to be used
export type SsmSrmData = Array<{
  time: string
  ip: string
  requestId: string
  role: string
  lat: number
  long: number
  type: string
  status: string
}>

export type IssScmsStatus = {
  [ip: string]: {
    health: '0' | '1'
    expiration: string
  }
}

export type BsmDataPostBody = {
  start: string
  end: string
  geometry: number[][]
}

export type RsuCommandPostBody = {
  command:
    | 'rsufwdsnmpwalk'
    | 'rsufwdsnmpset'
    | 'rsufwdsnmpset-del'
    | 'snmpFilter'
    | 'reboot'
    | 'upgrade-rsu'
    | 'upgrade-check'
  rsu_ip: string[]
  args: Object
}

export type ApiMsgResp = { message: string }
export type ApiMsgRespWithCodes<T> = {
  body: T
  status: number
  message: string
}

export type SnmpFwdWalkConfig = {
  'Message Type': string
  IP: string
  Port: number
  Protocol: string
  RSSI: number
  Frequency: number
  'Start DateTime': string
  'End DateTime': string
  Forwarding: string
  'Config Active': string
}
