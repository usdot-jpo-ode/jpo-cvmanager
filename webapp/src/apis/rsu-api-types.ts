export type RsuInfo = {
  rsuList: Array<{
    id: number
    type: 'Feature'
    geometry: {
      type: 'Point'
      coordinates: Array<number>
    }
    properties: {
      rsu_id: number
      milepost: number
      geography: string
      model_name: string
      ipv4_adddress: string
      primary_route: string
      serial_number: string
      manufacturer_name: string
    }
  }>
}

export type RsuOnlineStatus = {
  [ip: string]: {
    current_status: 'online' | 'offline' | 'unstable'
    last_online: string | undefined
  }
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

export type RsuMapInfo = string[]

// No response used, this method does not appear to be used
export type SsmSrmData = {}

export type IssScmsStatus = {
  [ip: string]: {
    health: 0 | 1
    expiration: string
  }
}

export type BsmDataPostBody = {
  start: string
  end: string
  geometry: number[][]
}

export type RsuCommandPostBody = {
  command: 'rsufwdsnmpwalk' | 'rsufwdsnmpset' | 'rsufwdsnmpset-del' | 'snmpFilter' | 'reboot'
  rsu_ip: string[]
  args: Object
}

export type ApiMsgResp = { message: string }
export type ApiMsgRespWithCodes = {
  body: Object
  status: number
  message: string
}
