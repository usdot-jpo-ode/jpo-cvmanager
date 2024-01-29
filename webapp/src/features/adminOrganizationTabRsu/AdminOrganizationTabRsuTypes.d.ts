import { AdminOrgRsu } from '../adminOrganizationTab/adminOrganizationTabSlice'

export type AdminOrgRsuWithId = AdminOrgRsu & {
  id?: number
}

export type AdminOrgTabRsuAddMultiple = {
  rsuList: AdminOrgRsu[]
  selectedOrg: string
  updateTableData: (org: string) => void
}

export type AdminOrgRsuDeleteSingle = {
  rsu: AdminOrgRsu
  selectedOrg: string
  updateTableData: (org: string) => void
}

export type AdminOrgRsuDeleteMultiple = {
  rows: AdminOrgRsu[]
  selectedOrg: string
  updateTableData: (org: string) => void
}
