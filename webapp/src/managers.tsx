import secureLocalStorage from 'react-secure-storage'

const AUTH_DATA_LOCAL_STORAGE_KEY = 'authLoginData'
const AUTH_DATA_SECURE_STORAGE_KEY = 'secureAuthLoginData'

const LocalStorageManager = {
  getAuthData: (): AuthLoginData | null => {
    let authData = null
    if (localStorage.getItem(AUTH_DATA_LOCAL_STORAGE_KEY) !== 'undefined') {
      authData = JSON.parse(localStorage.getItem(AUTH_DATA_LOCAL_STORAGE_KEY))
    }
    return authData
  },
  setAuthData: (authData: AuthLoginData) => {
    return localStorage.setItem(AUTH_DATA_LOCAL_STORAGE_KEY, JSON.stringify(authData))
  },
  removeAuthData: () => {
    return localStorage.removeItem(AUTH_DATA_LOCAL_STORAGE_KEY)
  },
}

const UserManager = {
  getOrganization: (authLoginData: AuthLoginData, organizationName: string) => {
    let updatedOrg = null
    for (var i = 0; i < authLoginData.data.organizations.length; i++) {
      if (organizationName === authLoginData.data.organizations[i].name) {
        updatedOrg = authLoginData.data.organizations[i]
      }
    }
    return updatedOrg
  },
  isLoginActive: (authLoginData: AuthLoginData): boolean => {
    return authLoginData != undefined && Date.now() < authLoginData.expires_at
  },
}

const SecureStorageManager = {
  getUserRole: () => {
    let authData = null
    if (secureLocalStorage.getItem(AUTH_DATA_SECURE_STORAGE_KEY) !== 'undefined') {
      authData = JSON.parse(String(secureLocalStorage.getItem(AUTH_DATA_SECURE_STORAGE_KEY)))
    }
    if (authData && authData['role'] !== 'undefined') {
      return authData['role']
    }
  },
  setUserRole: (authData) => {
    return secureLocalStorage.setItem(
      AUTH_DATA_SECURE_STORAGE_KEY,
      JSON.stringify(authData['data']['organizations'][0])
    )
  },
  removeUserRole: () => {
    return secureLocalStorage.removeItem(AUTH_DATA_SECURE_STORAGE_KEY)
  },
}

export { UserManager, LocalStorageManager, SecureStorageManager }
