import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { Provider } from 'react-redux'
import { ThemeProvider } from '@mui/material'
import { testTheme } from './styles'
import { setupStore } from './store'
import { replaceChaoticIds } from './utils/test-utils'
import Unsubscribe from './Unsubscribe'
import { MemoryRouter } from 'react-router-dom'
import { EmailUnsubscribeGetResponse } from './models/email-subscriptions'
import { vi } from 'vitest'

const mockUseGetEmailSubscriptionsQuery = vi.fn()
const mockUnwrap = vi.fn()
const mockUpdateEmailSubscriptions = vi.fn(() => ({ unwrap: mockUnwrap }))

vi.mock('./features/api/unsubscribeApiSlice', async (importOriginal) => {
  const actual = await importOriginal() as any
  return {
    ...actual,
    useGetEmailSubscriptionsQuery: (...args: unknown[]) => mockUseGetEmailSubscriptionsQuery(...args),
    useUpdateEmailSubscriptionsMutation: () => [mockUpdateEmailSubscriptions],
  }
})

const mockResponse: EmailUnsubscribeGetResponse = {
  email: 'test@example.com',
  subscriptions: [
    {
      category: 'Incident Alerts',
      description: 'Notifications for incident activity',
      required_role: 'user',
      immediate: true,
      hourly: false,
      daily: false,
      weekly: false,
      monthly: false,
      supports_immediate: true,
      supports_hourly: false,
      supports_daily: true,
      supports_weekly: false,
      supports_monthly: false,
    },
  ],
}

beforeEach(() => {
  vi.clearAllMocks()

  mockUseGetEmailSubscriptionsQuery.mockReturnValue({
    data: mockResponse,
    isLoading: false,
    isFetching: false,
    isError: false,
    error: undefined,
  })

  mockUnwrap.mockResolvedValue(null)
})

it('should take a snapshot', () => {
  const { container } = render(
    <ThemeProvider theme={testTheme}>
      <Provider store={setupStore({})}>
        <MemoryRouter initialEntries={['/?token=fake-test-token']}>
          <Unsubscribe />
        </MemoryRouter>
      </Provider>
    </ThemeProvider>
  )

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})

it('loads using token and renders returned email subscriptions', () => {
  render(
    <ThemeProvider theme={testTheme}>
      <Provider store={setupStore({})}>
        <MemoryRouter initialEntries={['/?token=fake-test-token']}>
          <Unsubscribe />
        </MemoryRouter>
      </Provider>
    </ThemeProvider>
  )

  expect(mockUseGetEmailSubscriptionsQuery).toHaveBeenCalledWith('fake-test-token', { skip: false })
  expect(screen.getByText('Manage Your Email Subscriptions')).toBeInTheDocument()
  expect(screen.getByText('Incident Alerts')).toBeInTheDocument()
  expect(screen.getByText('Notifications for incident activity')).toBeInTheDocument()
})

it('updates subscriptions through UI and submits expected payload', async () => {
  render(
    <ThemeProvider theme={testTheme}>
      <Provider store={setupStore({})}>
        <MemoryRouter initialEntries={['/?token=fake-test-token']}>
          <Unsubscribe />
        </MemoryRouter>
      </Provider>
    </ThemeProvider>
  )

  const dailyCheckbox = screen.getByLabelText('Daily')
  fireEvent.click(dailyCheckbox)
  fireEvent.click(screen.getByRole('button', { name: 'Save Preferences' }))

  await waitFor(() => {
    expect(mockUpdateEmailSubscriptions).toHaveBeenCalledWith({
      token: 'fake-test-token',
      subscriptions: [
        {
          category: 'Incident Alerts',
          description: 'Notifications for incident activity',
          required_role: 'user',
          immediate: true,
          hourly: false,
          daily: true,
          weekly: false,
          monthly: false,
          supports_immediate: true,
          supports_hourly: false,
          supports_daily: true,
          supports_weekly: false,
          supports_monthly: false,
        },
      ],
    })
  })
})
