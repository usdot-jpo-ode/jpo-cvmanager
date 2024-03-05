type ParsedJWT = {
  exp: number;
  iat: number;
  jti: string;
  iss: string;
  aud: string[];
  sub: string;
  typ: "Bearer";
  azp: string;
  session_state: string;
  acr: string;
  "allowed-origins": string[];
  realm_access: {
    roles: ROLES_TYPE[];
  };
  resource_access: {
    "realm-management": {
      roles: string[];
    };
    account: {
      roles: string[];
    };
  };
  scope: string;
  sid: string;
  email_verified: boolean;
  name: string;
  preferred_username: string;
  given_name: string;
  family_name: string;
};

type ROLES_TYPE = "ADMIN" | "USER" | "default-roles-conflictvisualizer" | "offline_access" | "uma_authorization";

// {
//     "exp": 1697731084,
//     "iat": 1697727484,
//     "jti": "ab4f4164-878b-4884-85cb-e469c25ff59f",
//     "iss": "http://172.30.103.166:8084/realms/conflictvisualizer",
//     "aud": [
//       "realm-management",
//       "account"
//     ],
//     "sub": "a209224a-a59e-42bd-9bb1-527e76662b88",
//     "typ": "Bearer",
//     "azp": "conflictvisualizer-gui",
//     "session_state": "49405f13-ccc1-4527-a2ee-515c926e0ac1",
//     "acr": "1",
//     "allowed-origins": [
//       "http://172.30.103.166:3000"
//     ],
//     "realm_access": {
//       "roles": [
//         "offline_access",
//         "default-roles-conflictvisualizer",
//         "uma_authorization",
//         "ADMIN"
//       ]
//     },
//     "resource_access": {
//       "realm-management": {
//         "roles": [
//           "view-realm",
//           "view-identity-providers",
//           "manage-identity-providers",
//           "impersonation",
//           "realm-admin",
//           "create-client",
//           "manage-users",
//           "query-realms",
//           "view-authorization",
//           "query-clients",
//           "query-users",
//           "manage-events",
//           "manage-realm",
//           "view-events",
//           "view-users",
//           "view-clients",
//           "manage-authorization",
//           "manage-clients",
//           "query-groups"
//         ]
//       },
//       "account": {
//         "roles": [
//           "manage-account",
//           "manage-account-links",
//           "view-profile"
//         ]
//       }
//     },
//     "scope": "profile email",
//     "sid": "49405f13-ccc1-4527-a2ee-515c926e0ac1",
//     "email_verified": true,
//     "name": "admin cimms",
//     "preferred_username": "admin@cimms.com",
//     "given_name": "admin",
//     "family_name": "cimms"
//   }
