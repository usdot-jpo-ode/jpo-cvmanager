import { UserManager, LocalStorageManager } from "./managers";

class LocalStorageMock {
  constructor() {
    this.store = {};
  }

  clear() {
    this.store = {};
  }

  getItem(key) {
    return this.store[key] || null;
  }

  setItem(key, value) {
    this.store[key] = String(value);
  }

  removeItem(key) {
    delete this.store[key];
  }
}

global.localStorage = new LocalStorageMock();

test("UserManager correctly checks if login is active", () => {
  let authLoginData = {};
  expect(UserManager.isLoginActive(authLoginData)).toBe(false);

  // get time 5 minutes ago
  authLoginData = { expires_at: Date.now() - 1000 * 60 * 5 };
  expect(UserManager.isLoginActive(authLoginData)).toBe(false);

  // get time 5 minutes from now
  authLoginData = { expires_at: Date.now() + 1000 * 60 * 5 };
  expect(UserManager.isLoginActive(authLoginData)).toBe(true);
});

// write a test for the UserManager.getOrganization function
test("UserManager correctly gets the organization", () => {
  const authLoginData = {
    data: {
      organizations: [
        {
          name: "test1",
        },
        {
          name: "test2",
        },
      ],
    },
  };

  const organization = UserManager.getOrganization(authLoginData, "test2");

  expect(organization).toEqual(authLoginData.data.organizations[1]);
});

test("LocalStorageManager correctly sets and gets auth data", () => {
  const authData = { test: "test" };
  LocalStorageManager.setAuthData(authData);
  expect(LocalStorageManager.getAuthData()).toEqual(authData);
});

test("LocalStorageManager correctly removes auth data", () => {
  const authData = { test: "test" };
  LocalStorageManager.setAuthData(authData);
  LocalStorageManager.removeAuthData();
  expect(LocalStorageManager.getAuthData()).toBe(null);
});
