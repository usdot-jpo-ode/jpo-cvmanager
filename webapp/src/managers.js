const AUTH_DATA_LOCAL_STORAGE_KEY = 'authLoginData'

const LocalStorageManager = {
  getAuthData: () => {
    let authData = null
    if (localStorage.getItem(AUTH_DATA_LOCAL_STORAGE_KEY) !== 'undefined') {
      authData = JSON.parse(localStorage.getItem(AUTH_DATA_LOCAL_STORAGE_KEY))
    }
    return authData
  },
  setAuthData: (authData) => {
    return localStorage.setItem(AUTH_DATA_LOCAL_STORAGE_KEY, JSON.stringify(authData))
  },
  removeAuthData: () => {
    return localStorage.removeItem(AUTH_DATA_LOCAL_STORAGE_KEY)
  },
}

const UserManager = {
  getOrganization: (authLoginData, organizationName) => {
    let updatedOrg = null
    for (var i = 0; i < authLoginData.data.organizations.length; i++) {
      if (organizationName === authLoginData.data.organizations[i].name) {
        updatedOrg = authLoginData.data.organizations[i]
      }
    }
    return updatedOrg
  },
  isLoginActive: (authLoginData) => {
    return authLoginData && Date.now() < authLoginData.expires_at
  },
}

export { UserManager, LocalStorageManager }
