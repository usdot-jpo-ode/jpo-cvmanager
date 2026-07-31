type AuthLoginData = {
  data: {
    name: string
    first_name: string
    last_name: string
    email: string
    super_user: boolean
    organizations: UserOrganization[]
  }
  token: string
  expires_at: number
}

type UserRole = 'ADMIN' | 'OPERATOR' | 'USER'

type UserOrganization = {
  role: UserRole
  organization: string
}

type UserOrganizationWithId = UserOrganization & {
  id: number
}

type UserAuthResponse = {
  email: string
  first_name: string
  last_name: string
  name: string
  super_user: boolean
  organizations: UserOrganization[]
}

type AdminUser = {
  email: string
  first_name: string
  last_name: string
  super_user: boolean
  organizations: UserOrganization[]
}

type AdminUserCreationBody = {
  email: string
  first_name: string
  last_name: string
  super_user: boolean
  organizations: UserOrganization[]
}

type AdminUserWithId = AdminUser & {
  id: number
}

type AdminUserWithRole = AdminUser & {
  role: UserRole
}

type AvailableRoles = {
  organizations: string[]
  roles: UserRole[]
}

type AdminUserAllowedSelections = {
  roles: UserRole[]
  organizations: string[]
}
