import reducer, {
  // thunks
  toggleMapMenuSelection,

  // reducers
  setCountsMsgType,
  setCountsStartDate,
  setCountsEndDate,
  setDisplay,

  // selectors
  selectLoading,
  selectCountsMsgType,
  selectDisplayCounts,
  selectDisplayRsuErrors,
  selectMenuSelection,
  selectCountsStartDate,
  selectCountsEndDate,
} from './menuSlice'
import { RootState } from '../../store'
import dayjs from 'dayjs'
import { configureStore } from '@reduxjs/toolkit'
import { vi } from 'vitest'

// Mock dayjs to ensure consistent test results
vi.mock('dayjs', async (importOriginal) => {
  const actualDayjs: any = await importOriginal()
  const defaultExport = actualDayjs.default || actualDayjs
  const mockNow = defaultExport('2024-01-15T12:00:00.000Z')

  const dayjsMock = vi.fn((date?: any) => {
    if (date) {
      return defaultExport(date)
    }
    return mockNow
  })

  // Copy all dayjs methods
  Object.keys(defaultExport).forEach((key) => {
    dayjsMock[key] = defaultExport[key]
  })

  return {
    default: dayjsMock,
  }
})

describe('menu reducer', () => {
  it('should handle initial state', () => {
    const expected = {
      loading: false,
      value: {
        countsMsgType: 'BSM',
        countsStartDate: expect.any(Date),
        countsEndDate: expect.any(Date),
        displayCounts: false,
        displayRsuErrors: false,
        menuSelection: [],
      },
    }

    const actual = reducer(undefined, { type: 'unknown' })
    expect(actual).toEqual(expected)

    // Verify dates are set correctly (yesterday and today)
    const startDate = new Date(actual.value.countsStartDate)
    const endDate = new Date(actual.value.countsEndDate)
    expect(endDate.getTime() - startDate.getTime()).toBe(24 * 60 * 60 * 1000) // 1 day difference
  })
})

describe('reducers', () => {
  const initialState: RootState['menu'] = {
    loading: false,
    value: {
      countsMsgType: 'BSM',
      countsStartDate: new Date('2024-01-14T12:00:00.000Z'),
      countsEndDate: new Date('2024-01-15T12:00:00.000Z'),
      displayCounts: false,
      displayRsuErrors: false,
      menuSelection: [],
    },
  }

  it('setCountsMsgType reducer updates state correctly', () => {
    const newMsgType = 'SPaT'
    expect(reducer(initialState, setCountsMsgType(newMsgType))).toEqual({
      ...initialState,
      value: { ...initialState.value, countsMsgType: newMsgType },
    })
  })

  it('setCountsMsgType handles different message types', () => {
    const messageTypes = ['BSM', 'SPaT', 'MAP', 'SSM', 'SRM', 'TIM', 'PSM']

    messageTypes.forEach((msgType) => {
      const result = reducer(initialState, setCountsMsgType(msgType as any))
      expect(result.value.countsMsgType).toBe(msgType)
    })
  })

  it('setCountsStartDate reducer updates state correctly', () => {
    const newStartDate = new Date('2024-01-10T12:00:00.000Z')
    expect(reducer(initialState, setCountsStartDate(newStartDate))).toEqual({
      ...initialState,
      value: { ...initialState.value, countsStartDate: newStartDate },
    })
  })

  it('setCountsEndDate reducer updates state correctly', () => {
    const newEndDate = new Date('2024-01-20T12:00:00.000Z')
    expect(reducer(initialState, setCountsEndDate(newEndDate))).toEqual({
      ...initialState,
      value: { ...initialState.value, countsEndDate: newEndDate },
    })
  })

  it('setDisplay reducer updates displayCounts correctly', () => {
    expect(reducer(initialState, setDisplay('displayCounts'))).toEqual({
      ...initialState,
      value: {
        ...initialState.value,
        displayCounts: true,
        displayRsuErrors: false,
      },
    })
  })

  it('setDisplay reducer updates displayRsuErrors correctly', () => {
    expect(reducer(initialState, setDisplay('displayRsuErrors'))).toEqual({
      ...initialState,
      value: {
        ...initialState.value,
        displayCounts: false,
        displayRsuErrors: true,
      },
    })
  })

  it('setDisplay reducer sets both to false for other values', () => {
    expect(reducer(initialState, setDisplay('somethingElse'))).toEqual({
      ...initialState,
      value: {
        ...initialState.value,
        displayCounts: false,
        displayRsuErrors: false,
      },
    })

    expect(reducer(initialState, setDisplay(null))).toEqual({
      ...initialState,
      value: {
        ...initialState.value,
        displayCounts: false,
        displayRsuErrors: false,
      },
    })
  })
})

describe('thunks', () => {
  let store: ReturnType<typeof configureStore>

  beforeEach(() => {
    store = configureStore({
      reducer: {
        menu: reducer,
      },
    })
  })

  describe('toggleMapMenuSelection', () => {
    it('adds "Display Message Counts" to menu selection and sets displayCounts', async () => {
      const result = await store.dispatch(
        toggleMapMenuSelection('Display Message Counts')
      )

      expect(result.payload).toEqual(['Display Message Counts'])

      const state = store.getState() as RootState
      expect(state.menu.value.menuSelection).toEqual(['Display Message Counts'])
      expect(state.menu.value.displayCounts).toBe(true)
      expect(state.menu.value.displayRsuErrors).toBe(false)
    })

    it('adds "Display RSU Status" to menu selection and sets displayRsuErrors', async () => {
      const result = await store.dispatch(
        toggleMapMenuSelection('Display RSU Status')
      )

      expect(result.payload).toEqual(['Display RSU Status'])

      const state = store.getState() as RootState
      expect(state.menu.value.menuSelection).toEqual(['Display RSU Status'])
      expect(state.menu.value.displayCounts).toBe(false)
      expect(state.menu.value.displayRsuErrors).toBe(true)
    })

    it('removes "Display Message Counts" from menu selection', async () => {
      // First add it
      await store.dispatch(toggleMapMenuSelection('Display Message Counts'))

      // Then remove it
      const result = await store.dispatch(
        toggleMapMenuSelection('Display Message Counts')
      )

      expect(result.payload).toEqual([])

      const state = store.getState() as RootState
      expect(state.menu.value.menuSelection).toEqual([])
      expect(state.menu.value.displayCounts).toBe(false)
      expect(state.menu.value.displayRsuErrors).toBe(false)
    })

    it('switches from "Display RSU Status" to "Display Message Counts"', async () => {
      // First add RSU Status
      await store.dispatch(toggleMapMenuSelection('Display RSU Status'))

      // Then toggle Message Counts (should replace RSU Status)
      const result = await store.dispatch(
        toggleMapMenuSelection('Display Message Counts')
      )

      expect(result.payload).toEqual(['Display Message Counts'])

      const state = store.getState() as RootState
      expect(state.menu.value.menuSelection).toEqual(['Display Message Counts'])
      expect(state.menu.value.displayCounts).toBe(true)
      expect(state.menu.value.displayRsuErrors).toBe(false)
    })

    it('switches from "Display Message Counts" to "Display RSU Status"', async () => {
      // First add Message Counts
      await store.dispatch(toggleMapMenuSelection('Display Message Counts'))

      // Then toggle RSU Status (should replace Message Counts)
      const result = await store.dispatch(
        toggleMapMenuSelection('Display RSU Status')
      )

      expect(result.payload).toEqual(['Display RSU Status'])

      const state = store.getState() as RootState
      expect(state.menu.value.menuSelection).toEqual(['Display RSU Status'])
      expect(state.menu.value.displayCounts).toBe(false)
      expect(state.menu.value.displayRsuErrors).toBe(true)
    })

    it('handles adding other menu items without affecting displays', async () => {
      const result = await store.dispatch(
        toggleMapMenuSelection('Some Other Item')
      )

      expect(result.payload).toEqual(['Some Other Item'])

      const state = store.getState() as RootState
      expect(state.menu.value.menuSelection).toEqual(['Some Other Item'])
      expect(state.menu.value.displayCounts).toBe(false)
      expect(state.menu.value.displayRsuErrors).toBe(false)
    })

    it('can toggle other menu items on and off', async () => {
      // Add item
      await store.dispatch(toggleMapMenuSelection('Some Other Item'))
      let state = store.getState() as RootState
      expect(state.menu.value.menuSelection).toEqual(['Some Other Item'])

      // Remove item
      await store.dispatch(toggleMapMenuSelection('Some Other Item'))
      state = store.getState() as RootState
      expect(state.menu.value.menuSelection).toEqual([])
    })

    it('maintains other selections when toggling display items', async () => {
      // Add other item first
      await store.dispatch(toggleMapMenuSelection('Other Item'))

      // Add Display Message Counts
      const result = await store.dispatch(
        toggleMapMenuSelection('Display Message Counts')
      )

      expect(result.payload).toContain('Other Item')
      expect(result.payload).toContain('Display Message Counts')

      const state = store.getState() as RootState
      expect(state.menu.value.menuSelection.length).toBe(2)
    })
  })
})

describe('selectors', () => {
  const mockStartDate = new Date('2024-01-14T12:00:00.000Z')
  const mockEndDate = new Date('2024-01-15T12:00:00.000Z')

  const initialState: RootState = {
    menu: {
      loading: true,
      value: {
        countsMsgType: 'SPaT',
        countsStartDate: mockStartDate,
        countsEndDate: mockEndDate,
        displayCounts: true,
        displayRsuErrors: false,
        menuSelection: ['Display Message Counts', 'Other Item'],
      },
    },
  } as RootState

  it('selectLoading returns the correct value', () => {
    expect(selectLoading(initialState)).toBe(true)

    const falseState = {
      ...initialState,
      menu: { ...initialState.menu, loading: false },
    }
    expect(selectLoading(falseState)).toBe(false)
  })

  it('selectCountsMsgType returns the correct value', () => {
    expect(selectCountsMsgType(initialState)).toBe('SPaT')

    const bsmState = {
      ...initialState,
      menu: {
        ...initialState.menu,
        value: { ...initialState.menu.value, countsMsgType: 'BSM' as any },
      },
    }
    expect(selectCountsMsgType(bsmState)).toBe('BSM')
  })

  it('selectDisplayCounts returns the correct value', () => {
    expect(selectDisplayCounts(initialState)).toBe(true)

    const falseState = {
      ...initialState,
      menu: {
        ...initialState.menu,
        value: { ...initialState.menu.value, displayCounts: false },
      },
    }
    expect(selectDisplayCounts(falseState)).toBe(false)
  })

  it('selectDisplayRsuErrors returns the correct value', () => {
    expect(selectDisplayRsuErrors(initialState)).toBe(false)

    const trueState = {
      ...initialState,
      menu: {
        ...initialState.menu,
        value: { ...initialState.menu.value, displayRsuErrors: true },
      },
    }
    expect(selectDisplayRsuErrors(trueState)).toBe(true)
  })

  it('selectMenuSelection returns the correct value', () => {
    expect(selectMenuSelection(initialState)).toEqual([
      'Display Message Counts',
      'Other Item',
    ])

    const emptyState = {
      ...initialState,
      menu: {
        ...initialState.menu,
        value: { ...initialState.menu.value, menuSelection: [] },
      },
    }
    expect(selectMenuSelection(emptyState)).toEqual([])
  })

  it('selectCountsStartDate returns the correct value', () => {
    expect(selectCountsStartDate(initialState)).toEqual(mockStartDate)
  })

  it('selectCountsEndDate returns the correct value', () => {
    expect(selectCountsEndDate(initialState)).toEqual(mockEndDate)
  })

  it('all selectors handle undefined state gracefully', () => {
    const undefinedState = {
      menu: {
        loading: false,
        value: {
          countsMsgType: undefined,
          countsStartDate: undefined,
          countsEndDate: undefined,
          displayCounts: undefined,
          displayRsuErrors: undefined,
          menuSelection: undefined,
        },
      },
    } as any

    // Selectors should not throw, but return undefined values
    expect(() => selectLoading(undefinedState)).not.toThrow()
    expect(() => selectCountsMsgType(undefinedState)).not.toThrow()
    expect(() => selectDisplayCounts(undefinedState)).not.toThrow()
    expect(() => selectDisplayRsuErrors(undefinedState)).not.toThrow()
    expect(() => selectMenuSelection(undefinedState)).not.toThrow()
    expect(() => selectCountsStartDate(undefinedState)).not.toThrow()
    expect(() => selectCountsEndDate(undefinedState)).not.toThrow()
  })
})
