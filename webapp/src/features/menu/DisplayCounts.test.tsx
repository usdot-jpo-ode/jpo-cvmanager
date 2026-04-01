import React from 'react'
import { render } from '@testing-library/react'
import DisplayCounts from './DisplayCounts'
import { Provider } from 'react-redux'
import { ThemeProvider } from '@mui/material'
import { testTheme } from '../../styles'
import { setupStore } from '../../store'
import { MockLocalizationProvider, replaceChaoticIds } from '../../utils/test-utils'
import { MessageType } from '../../models/MessageTypes'

// // Mock the @mui/x-date-pickers module
vi.mock('@mui/x-date-pickers', async () => {
  const actual: any = await vi.importActual('@mui/x-date-pickers')
  return {
    ...actual,
    LocalizationProvider: MockLocalizationProvider,
  }
})

// Mock the dayjs library with timezone support
vi.mock('dayjs', async () => {
  const actualDayjs: any = await vi.importActual('dayjs')
  const utc = await import('dayjs/plugin/utc')
  const timezone = await import('dayjs/plugin/timezone')

  // Extend dayjs with required plugins
  actualDayjs.extend(utc.default)
  actualDayjs.extend(timezone.default)

  // Create mock function
  const mockDayjs = (date?: any) => {
    const instance = date ? actualDayjs(date) : actualDayjs()
    return instance.tz('America/Denver')
  }

  // Copy all static methods and properties from actual dayjs
  Object.keys(actualDayjs).forEach((key) => {
    if (!(key in mockDayjs)) {
      mockDayjs[key] = actualDayjs[key]
    }
  })

  // Ensure timezone method is available
  mockDayjs.extend = actualDayjs.extend
  mockDayjs.Ls = actualDayjs.Ls

  return {
    default: mockDayjs,
    ...actualDayjs,
  }
})

it('should take a snapshot', () => {
  const { container } = render(
    <ThemeProvider theme={testTheme}>
      <Provider
        store={setupStore({
          menu: {
            value: {
              countsStartDate: new Date('2024-04-09T00:00:00Z'),
              countsEndDate: new Date('2024-04-10T00:00:00Z'),
              countsMsgType: 'BSM' as MessageType,
              display: 'displayCounts',
              mapMenuSelection: ['Display Message Counts'],
            },
          },
        })}
      >
        <DisplayCounts />
      </Provider>
    </ThemeProvider>
  )

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
