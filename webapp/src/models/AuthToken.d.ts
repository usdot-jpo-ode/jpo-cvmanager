export interface AuthToken {
  /** Expiration time (Unix timestamp in seconds) */
  exp: number
  /** Issued at time (Unix timestamp in seconds) */
  iat: number
  /** JWT ID - unique identifier for this token */
  jti: string
  /** Issuer - the Keycloak realm that issued this token */
  iss: string
  /** Audience - intended recipient of the token */
  aud: string
  /** Subject - unique identifier for the user */
  sub: string
  /** Token type */
  typ: 'Bearer'
  /** Authorized party - the client that requested the token */
  azp: string
  /** Session ID */
  sid: string
  /** Authentication Context Class Reference */
  acr: string
  /** Allowed origins for CORS */
  'allowed-origins': string[]
  /** Realm-level role assignments */
  realm_access: {
    roles: string[]
  }
  /** Client-level role assignments */
  resource_access: {
    [clientId: string]: {
      roles: string[]
    }
  }
  /** OAuth scopes granted */
  scope: string
  /** Whether the user's email has been verified */
  email_verified: boolean
  /** User's full name */
  name: string
  /** Username (typically email) */
  preferred_username: string
  /** User's first name */
  given_name: string
  /** User's last name */
  family_name: string
  /** Custom CVManager-specific data */
  cvmanager_data: {
    /** Whether user is a super user (1 = true, 0 = false) */
    super_user: '0' | '1'
    /** Organizations the user belongs to and their roles */
    organizations: Array<{
      /** Organization name */
      org: string
      /** User's role in the organization */
      role: string
    }>
    /** Timestamp when user was created (Unix timestamp in milliseconds) */
    user_created_timestamp: number
  }
  /** User's email address */
  email: string
}

export interface DecodedAuthToken extends AuthToken {}
