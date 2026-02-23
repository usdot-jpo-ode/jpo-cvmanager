export type CountsListElement = { key: string; rsu: string; road: string; count: number }

export type AdminRsu = {
  orig_ip: string
  ip: string
  geo_position: {
    latitude: string
    longitude: string
  }
  milepost: string | number
  primary_route: string
  serial_number: string
  model: string
  scms_id: string
  ssh_credential_group: string
  snmp_credential_group: string
  snmp_version_group: string
  organizations: string[]
  tim_deposit: boolean
  snmp_monitoring: boolean
}

export type AdminRsuAllowedSelections = {
  primary_routes: string[]
  rsu_models: string[]
  ssh_credential_groups: string[]
  snmp_credential_groups: string[]
  snmp_version_groups: string[]
  organizations: string[]
}

export type AdminRsuWithAllowedSelections = {
  rsu_data: AdminRsu
  allowed_selections: AdminRsuAllowedSelections
}
