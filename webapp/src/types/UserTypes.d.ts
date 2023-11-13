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
