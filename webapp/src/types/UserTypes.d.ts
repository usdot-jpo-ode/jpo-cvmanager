type AuthLoginData = {
  data: {
    name: string
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
