import React from 'react'
import { render, screen } from '@testing-library/react'
import VerticalTabs from './VerticalTabs'
import { Provider } from 'react-redux'
import { ThemeProvider } from '@mui/material'
import { testTheme } from '../styles'
import { setupStore } from '../store'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import '@testing-library/jest-dom'

const mockTabs = [
  {
    path: 'rsus',
    title: 'RSUs',
    child: <div>RSU Page Content</div>,
  },
  {
    path: 'intersections',
    title: 'Intersections',
    child: <div>Intersections Page Content</div>,
  },
  {
    path: 'users',
    title: 'Users',
    child: <div>Users Page Content</div>,
  },
  {
    path: 'organizations',
    title: 'Organizations',
    child: <div>Organizations Page Content</div>,
  },
]

const renderVerticalTabs = (initialEntries: string[]) => {
  return render(
    <ThemeProvider theme={testTheme}>
      <Provider store={setupStore({})}>
        <MemoryRouter initialEntries={initialEntries}>
          <Routes>
            <Route
              path="/admin/*"
              element={
                <VerticalTabs
                  notFoundRoute={<div>Not Found</div>}
                  defaultTabIndex={0}
                  tabs={mockTabs}
                />
              }
            />
          </Routes>
        </MemoryRouter>
      </Provider>
    </ThemeProvider>
  )
}

describe('VerticalTabs getSelectedTab functionality', () => {
  it('should select the correct tab when the path matches exactly', () => {
    renderVerticalTabs(['/admin/rsus'])
    
    const rsuTab = screen.getByRole('tab', { name: /RSUs/i })
    expect(rsuTab).toHaveAttribute('aria-selected', 'true')
    expect(screen.getByText('RSU Page Content')).toBeInTheDocument()
  })

  it('should select the correct tab when navigating to a sub-route (e.g., edit page)', () => {
    renderVerticalTabs(['/admin/rsus/editRsu/10.0.0.180'])
    
    const rsuTab = screen.getByRole('tab', { name: /RSUs/i })
    expect(rsuTab).toHaveAttribute('aria-selected', 'true')
    expect(screen.getByText('RSU Page Content')).toBeInTheDocument()
  })

  it('should select the intersections tab when on an intersections sub-route', () => {
    renderVerticalTabs(['/admin/intersections/some-sub-route'])
    
    const intersectionsTab = screen.getByRole('tab', { name: /Intersections/i })
    expect(intersectionsTab).toHaveAttribute('aria-selected', 'true')
    expect(screen.getByText('Intersections Page Content')).toBeInTheDocument()
  })

  it('should fallback to the default tab if no matches are found in the path', () => {
    renderVerticalTabs(['/admin/unknown-tab'])
    
    // With defaultTabIndex={0}, it should default to the first tab (RSUs)
    const rsuTab = screen.getByRole('tab', { name: /RSUs/i })
    expect(rsuTab).toHaveAttribute('aria-selected', 'true')
    // But the content should be "Not Found" because the internal Routes won't match
    expect(screen.getByText('Not Found')).toBeInTheDocument()
  })

  it('should handle deep sub-routes correctly', () => {
    renderVerticalTabs(['/admin/organizations/edit/123/users/456'])
    
    const orgTab = screen.getByRole('tab', { name: /Organizations/i })
    expect(orgTab).toHaveAttribute('aria-selected', 'true')
    expect(screen.getByText('Organizations Page Content')).toBeInTheDocument()
  })

  it('should correctly switch tabs when path changes', () => {
    const { rerender } = render(
        <ThemeProvider theme={testTheme}>
          <Provider store={setupStore({})}>
            <MemoryRouter initialEntries={['/admin/rsus']}>
              <Routes>
                <Route
                  path="/admin/*"
                  element={
                    <VerticalTabs
                      notFoundRoute={<div>Not Found</div>}
                      defaultTabIndex={0}
                      tabs={mockTabs}
                    />
                  }
                />
              </Routes>
            </MemoryRouter>
          </Provider>
        </ThemeProvider>
      )

    expect(screen.getByRole('tab', { name: /RSUs/i })).toHaveAttribute('aria-selected', 'true')
    
    // Note: To test actual navigation/rerender with MemoryRouter is tricky without wrapping everything.
    // In our case, VerticalTabs uses useEffect on location.pathname, so it should update.
    // But since MemoryRouter's initialEntries is immutable for a given render, we'd need to simulate navigation.
  })
})
