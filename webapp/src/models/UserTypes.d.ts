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

// {
//   "email": "bpayne@trihydro.com",
//   "first_name": "Brandon",
//   "last_name": "Payne",
//   "super_user": true,
//   "organizations": [
//     { "name": "CDOT CV", "role": "admin" },
//     { "name": "Region 1", "role": "admin" }
//   ]
// },

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
