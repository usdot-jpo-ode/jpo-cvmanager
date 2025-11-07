type AuthLoginData = {
  data: {
    name: string
    first_name: string
    last_name: string
    email: string
    super_user: boolean
    organizations: Array<{
      name: string
      role: string
    }>
  }
  token: string
  expires_at: number
}

type UserAuthResponse = {
  email: string
  first_name: string
  last_name: string
  super_user: boolean
  organizations: Array<{
    name: string
    role: string
  }>
}

type AdminUser = {
  email: string
  first_name: string
  last_name: string
  super_user: boolean
  organizations: Array<{
    name: string
    role: string
  }>
}

type AdminUserWithId = AdminUser & {
  id: number
}

type AdminUserWithRole = AdminUser & {
  role: string
}

type AvailableRoles = {
  organizations: string[]
  roles: string[]
}
