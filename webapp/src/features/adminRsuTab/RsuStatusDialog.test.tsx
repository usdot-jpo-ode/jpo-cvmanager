import React from 'react'
import { render } from '@testing-library/react'
import RsuStatusDialog from './RsuStatusDialog'
import { Provider } from 'react-redux'
import { ThemeProvider } from '@mui/material'
import { testTheme } from '../../styles'
import { setupStore } from '../../store'
import { replaceChaoticIds } from '../../utils/test-utils'

beforeAll(() => {
  // Mock the Date object to always return a fixed date
  jest.useFakeTimers('modern')
  jest.setSystemTime(new Date('2025-12-05T00:00:00Z'))
})

afterAll(() => {
  // Restore the Date object to its original behavior
  jest.useRealTimers()
})

it('should take a snapshot', () => {
  render(
    <ThemeProvider theme={testTheme}>
      <Provider
        store={setupStore({
          user: { value: { authLoginData: { token: '' } } },
        })}
      >
        <RsuStatusDialog open={true} onClose={() => {}} rsuIp="10.0.0.180" token="test-token" />
      </Provider>
    </ThemeProvider>
  )

  expect(replaceChaoticIds(document.body)).toMatchSnapshot()
})
