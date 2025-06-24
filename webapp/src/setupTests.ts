import 'jest-canvas-mock'

// adds the 'fetchMock' global variable and rewires 'fetch' global to call 'fetchMock' instead of the real implementation
import fetchMock from 'jest-fetch-mock'
fetchMock.enableMocks()
// changes default behavior of fetchMock to use the real 'fetch' implementation and not mock responses
fetchMock.dontMock()
// browser mocks
const localStorageMock = (function () {
  let store = {}
  return {
    getItem: function (key) {
      return store[key] || null
    },
    setItem: function (key, value) {
      store[key] = value.toString()
    },
    removeItem: function (key) {
      delete store[key]
    },
    clear: function () {
      store = {}
    },
  }
})()

Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
})

jest.mock('luxon', () => {
  const actualLuxon = jest.requireActual('luxon')
  return {
    ...actualLuxon,
    DateTime: {
      ...actualLuxon.DateTime,
      local: () => actualLuxon.DateTime.fromISO('2024-04-10T00:00:00.000+00:00').setZone('America/Denver'),
      fromISO: actualLuxon.DateTime.fromISO,
      fromJSDate: actualLuxon.DateTime.fromJSDate,
    },
  }
})
