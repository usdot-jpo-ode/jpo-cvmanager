import { UserManager, LocalStorageManager } from './managers'

class LocalStorageMock {
  store: { [key: string]: any } = {}
  length = 0
  constructor() {
    this.store = undefined
  }

  clear() {
    this.store = undefined
  }

  getItem(key: string) {
    return this.store[key] || null
  }

  setItem(key: string, value: any) {
    this.store[key] = String(value)
  }

  removeItem(key: string) {
    delete this.store[key]
  }

  key(index: number) {
    return Object.keys(this.store)[index]
  }
}

test('UserManager correctly checks if login is active', () => {
  let authLoginData: AuthLoginData = undefined
  expect(UserManager.isLoginActive(authLoginData)).toBe(false)

  // get time 5 minutes ago
  authLoginData = { expires_at: Date.now() - 1000 * 60 * 5, data: undefined, token: undefined }
  expect(UserManager.isLoginActive(authLoginData)).toBe(false)

  // get time 5 minutes from now
  authLoginData = { expires_at: Date.now() + 1000 * 60 * 5, data: undefined, token: undefined }
  expect(UserManager.isLoginActive(authLoginData)).toBe(true)
})

// write a test for the UserManager.getOrganization function
test('UserManager correctly gets the organization', () => {
  const authLoginData: AuthLoginData = {
    data: {
      name: undefined,
      email: undefined,
      super_user: undefined,
      organizations: [
        {
          name: 'test1',
          role: 'role',
        },
        {
          name: 'test2',
          role: 'role',
        },
      ],
    },
    token: undefined,
    expires_at: undefined,
  }

  const organization = UserManager.getOrganization(authLoginData, 'test2')

  expect(organization).toEqual(authLoginData.data.organizations[1])
})

test('LocalStorageManager correctly sets and gets auth data', () => {
  const authData: AuthLoginData = { token: 'test', data: undefined, expires_at: undefined }
  LocalStorageManager.setAuthData(authData)
  expect(LocalStorageManager.getAuthData()).toEqual(authData)
})

test('LocalStorageManager correctly removes auth data', () => {
  const authData: AuthLoginData = { token: 'test', data: undefined, expires_at: undefined }
  LocalStorageManager.setAuthData(authData)
  LocalStorageManager.removeAuthData()
  expect(LocalStorageManager.getAuthData()).toBe(null)
})

// Tests for UserManager.isSuperUser
describe('UserManager.isSuperUser', () => {
  test('returns true when super_user is true', () => {
    const authLoginData: AuthLoginData = {
      data: {
        name: 'Test User',
        email: 'test@example.com',
        super_user: true,
        organizations: [],
      },
      token: 'test-token',
      expires_at: Date.now() + 1000 * 60 * 5,
    }

    expect(UserManager.isSuperUser(authLoginData)).toBe(true)
  })

  test('returns false when super_user is false', () => {
    const authLoginData: AuthLoginData = {
      data: {
        name: 'Test User',
        email: 'test@example.com',
        super_user: false,
        organizations: [],
      },
      token: 'test-token',
      expires_at: Date.now() + 1000 * 60 * 5,
    }

    expect(UserManager.isSuperUser(authLoginData)).toBe(false)
  })

  test('returns false when authLoginData is null', () => {
    expect(UserManager.isSuperUser(null)).toBe(false)
  })

  test('returns false when authLoginData is undefined', () => {
    expect(UserManager.isSuperUser(undefined)).toBe(false)
  })

  test('returns false when data is undefined', () => {
    const authLoginData: AuthLoginData = {
      data: undefined,
      token: 'test-token',
      expires_at: Date.now() + 1000 * 60 * 5,
    }

    expect(UserManager.isSuperUser(authLoginData)).toBe(false)
  })

  test('returns false when super_user is undefined', () => {
    const authLoginData: AuthLoginData = {
      data: {
        name: 'Test User',
        email: 'test@example.com',
        super_user: undefined,
        organizations: [],
      },
      token: 'test-token',
      expires_at: Date.now() + 1000 * 60 * 5,
    }

    expect(UserManager.isSuperUser(authLoginData)).toBe(false)
  })
})

// Tests for LocalStorageManager.getIsSuperUser
describe('LocalStorageManager.getIsSuperUser', () => {
  beforeEach(() => {
    // Clear localStorage before each test
    localStorage.clear()
  })

  test('returns true when stored user is super user', () => {
    const authData: AuthLoginData = {
      data: {
        name: 'Super User',
        email: 'super@example.com',
        super_user: true,
        organizations: [],
      },
      token: 'test-token',
      expires_at: Date.now() + 1000 * 60 * 5,
    }

    LocalStorageManager.setAuthData(authData)
    expect(LocalStorageManager.getIsSuperUser()).toBe(true)
  })

  test('returns false when stored user is not super user', () => {
    const authData: AuthLoginData = {
      data: {
        name: 'Regular User',
        email: 'user@example.com',
        super_user: false,
        organizations: [],
      },
      token: 'test-token',
      expires_at: Date.now() + 1000 * 60 * 5,
    }

    LocalStorageManager.setAuthData(authData)
    expect(LocalStorageManager.getIsSuperUser()).toBe(false)
  })

  test('returns false when no auth data is stored', () => {
    expect(LocalStorageManager.getIsSuperUser()).toBe(false)
  })

  test('returns false when localStorage contains undefined string', () => {
    localStorage.setItem('authLoginData', 'undefined')
    expect(LocalStorageManager.getIsSuperUser()).toBe(false)
  })

  test('returns false when stored auth data has no super_user field', () => {
    const authData: AuthLoginData = {
      data: {
        name: 'User',
        email: 'user@example.com',
        super_user: undefined,
        organizations: [],
      },
      token: 'test-token',
      expires_at: Date.now() + 1000 * 60 * 5,
    }

    LocalStorageManager.setAuthData(authData)
    expect(LocalStorageManager.getIsSuperUser()).toBe(false)
  })

  test('returns false after auth data is removed', () => {
    const authData: AuthLoginData = {
      data: {
        name: 'Super User',
        email: 'super@example.com',
        super_user: true,
        organizations: [],
      },
      token: 'test-token',
      expires_at: Date.now() + 1000 * 60 * 5,
    }

    LocalStorageManager.setAuthData(authData)
    expect(LocalStorageManager.getIsSuperUser()).toBe(true)
    
    LocalStorageManager.removeAuthData()
    expect(LocalStorageManager.getIsSuperUser()).toBe(false)
  })
})


