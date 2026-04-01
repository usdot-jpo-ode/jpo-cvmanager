import 'vitest-canvas-mock'
import { TextEncoder, TextDecoder } from 'util'
import { vi } from 'vitest'

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

vi.mock('luxon', async () => {
  const actualLuxon: any = await vi.importActual('luxon')
  return {
    ...actualLuxon,
    DateTime: {
      ...actualLuxon.DateTime,
      local: (...args: any[]) => {
        if (args.length > 0) {
          return actualLuxon.DateTime.local(...args)
        }
        return actualLuxon.DateTime.fromISO('2024-04-10T00:00:00.000+00:00').setZone('America/Denver')
      },
      fromISO: actualLuxon.DateTime.fromISO,
      fromJSDate: actualLuxon.DateTime.fromJSDate,
    },
  }
})

// the new version of jspdf (4.0.0) requires this to be defined during tests
global.TextEncoder = TextEncoder
global.TextDecoder = TextDecoder as any
