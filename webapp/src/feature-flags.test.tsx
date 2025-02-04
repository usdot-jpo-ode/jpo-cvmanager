import React from 'react'
import { render } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import {
  RsuRouteGuard,
  ConditionalRenderRsu,
  ConditionalRenderIntersection,
  IntersectionRouteGuard,
  WzdxRouteGuard,
  ConditionalRenderWzdx,
  evaluateFeatureFlags,
} from './feature-flags'
import '@testing-library/jest-dom'

// Mock the EnvironmentVars module
jest.mock('./EnvironmentVars', () => ({
  __esModule: true,
  default: {
    ENABLE_RSU_FEATURES: true,
    ENABLE_INTERSECTION_FEATURES: true,
    ENABLE_WZDX_FEATURES: true,
  },
}))

const EnvironmentVars = require('./EnvironmentVars').default

describe('Feature Flags', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  test('RsuRouteGuard allows access when feature is enabled', () => {
    EnvironmentVars.ENABLE_RSU_FEATURES = true
    const { getByText, queryByText } = render(
      <MemoryRouter initialEntries={['/rsu']}>
        <Routes>
          <Route index element={<div>Generic Content</div>} />
          <Route
            path="rsu"
            element={
              <RsuRouteGuard>
                <div>RSU Content</div>
              </RsuRouteGuard>
            }
          />
        </Routes>
      </MemoryRouter>
    )
    expect(getByText('RSU Content')).toBeInTheDocument()
    expect(queryByText('Generic Content')).not.toBeInTheDocument()
  })

  test('RsuRouteGuard redirects when feature is disable', () => {
    EnvironmentVars.ENABLE_RSU_FEATURES = false
    const { getByText, queryByText } = render(
      <MemoryRouter initialEntries={['/rsu']}>
        <Routes>
          <Route index element={<div>Generic Content</div>} />
          <Route
            path="rsu"
            element={
              <RsuRouteGuard>
                <div>RSU Content</div>
              </RsuRouteGuard>
            }
          />
        </Routes>
      </MemoryRouter>
    )
    expect(getByText('Generic Content')).toBeInTheDocument()
    expect(queryByText('RSU Content')).not.toBeInTheDocument()
  })

  test('IntersectionRouteGuard allows access when feature is enabled', () => {
    EnvironmentVars.ENABLE_INTERSECTION_FEATURES = true
    const { getByText, queryByText } = render(
      <MemoryRouter initialEntries={['/intersection']}>
        <Routes>
          <Route index element={<div>Generic Content</div>} />
          <Route
            path="intersection"
            element={
              <IntersectionRouteGuard>
                <div>Intersection Content</div>
              </IntersectionRouteGuard>
            }
          />
        </Routes>
      </MemoryRouter>
    )
    expect(getByText('Intersection Content')).toBeInTheDocument()
    expect(queryByText('Generic Content')).not.toBeInTheDocument()
  })

  test('IntersectionRouteGuard redirects when feature is disabled', () => {
    EnvironmentVars.ENABLE_INTERSECTION_FEATURES = false
    const { getByText, queryByText } = render(
      <MemoryRouter initialEntries={['/intersection']}>
        <Routes>
          <Route index element={<div>Generic Content</div>} />
          <Route
            path="intersection"
            element={
              <IntersectionRouteGuard>
                <div>Intersection Content</div>
              </IntersectionRouteGuard>
            }
          />
        </Routes>
      </MemoryRouter>
    )
    expect(getByText('Generic Content')).toBeInTheDocument()
    expect(queryByText('Intersection Content')).not.toBeInTheDocument()
  })

  test('WzdxRouteGuard allows access when feature is enabled', () => {
    EnvironmentVars.ENABLE_WZDX_FEATURES = true
    const { getByText, queryByText } = render(
      <MemoryRouter initialEntries={['/wzdx']}>
        <Routes>
          <Route index element={<div>Generic Content</div>} />
          <Route
            path="wzdx"
            element={
              <WzdxRouteGuard>
                <div>WZDx Content</div>
              </WzdxRouteGuard>
            }
          />
        </Routes>
      </MemoryRouter>
    )
    expect(getByText('WZDx Content')).toBeInTheDocument()
    expect(queryByText('Generic Content')).not.toBeInTheDocument()
  })

  test('WzdxRouteGuard redirects when feature is disabled', () => {
    EnvironmentVars.ENABLE_WZDX_FEATURES = false
    const { getByText, queryByText } = render(
      <MemoryRouter initialEntries={['/wzdx']}>
        <Routes>
          <Route index element={<div>Generic Content</div>} />
          <Route
            path="wzdx"
            element={
              <WzdxRouteGuard>
                <div>WZDx Content</div>
              </WzdxRouteGuard>
            }
          />
        </Routes>
      </MemoryRouter>
    )
    expect(getByText('Generic Content')).toBeInTheDocument()
    expect(queryByText('WZDx Content')).not.toBeInTheDocument()
  })

  test('ConditionalRenderRsu renders children when feature is enabled', () => {
    EnvironmentVars.ENABLE_RSU_FEATURES = true
    const { getByText } = render(
      <ConditionalRenderRsu>
        <div>RSU Content</div>
      </ConditionalRenderRsu>
    )
    expect(getByText('RSU Content')).toBeInTheDocument()
  })

  test('ConditionalRenderRsu does not render children when feature is disabled', () => {
    EnvironmentVars.ENABLE_RSU_FEATURES = false
    const { queryByText } = render(
      <ConditionalRenderRsu>
        <div>RSU Content</div>
      </ConditionalRenderRsu>
    )
    expect(queryByText('RSU Content')).not.toBeInTheDocument()
  })

  test('ConditionalRenderIntersection renders children when feature is enabled', () => {
    EnvironmentVars.ENABLE_INTERSECTION_FEATURES = true
    const { getByText } = render(
      <ConditionalRenderIntersection>
        <div>Intersection Content</div>
      </ConditionalRenderIntersection>
    )
    expect(getByText('Intersection Content')).toBeInTheDocument()
  })

  test('ConditionalRenderIntersection does not render children when feature is disabled', () => {
    EnvironmentVars.ENABLE_INTERSECTION_FEATURES = false
    const { queryByText } = render(
      <ConditionalRenderIntersection>
        <div>Intersection Content</div>
      </ConditionalRenderIntersection>
    )
    expect(queryByText('Intersection Content')).not.toBeInTheDocument()
  })

  test('ConditionalRenderWzdx renders children when feature is enabled', () => {
    EnvironmentVars.ENABLE_WZDX_FEATURES = true
    const { getByText } = render(
      <ConditionalRenderWzdx>
        <div>WZDx Content</div>
      </ConditionalRenderWzdx>
    )
    expect(getByText('WZDx Content')).toBeInTheDocument()
  })

  test('ConditionalRenderWzdx does not render children when feature is disabled', () => {
    EnvironmentVars.ENABLE_WZDX_FEATURES = false
    const { queryByText } = render(
      <ConditionalRenderWzdx>
        <div>WZDx Content</div>
      </ConditionalRenderWzdx>
    )
    expect(queryByText('WZDx Content')).not.toBeInTheDocument()
  })

  test('ConditionalRenderWzdx does not render children when feature is disabled', () => {
    EnvironmentVars.ENABLE_RSU_FEATURES = false
    EnvironmentVars.ENABLE_INTERSECTION_FEATURES = false
    EnvironmentVars.ENABLE_WZDX_FEATURES = false

    expect(evaluateFeatureFlags('rsu')).toEqual(false)
    expect(evaluateFeatureFlags('intersection')).toEqual(false)
    expect(evaluateFeatureFlags('wzdx')).toEqual(false)

    EnvironmentVars.ENABLE_RSU_FEATURES = true

    expect(evaluateFeatureFlags('rsu')).toEqual(true)
    expect(evaluateFeatureFlags('intersection')).toEqual(false)
    expect(evaluateFeatureFlags('wzdx')).toEqual(false)

    EnvironmentVars.ENABLE_INTERSECTION_FEATURES = true

    expect(evaluateFeatureFlags('rsu')).toEqual(true)
    expect(evaluateFeatureFlags('intersection')).toEqual(true)
    expect(evaluateFeatureFlags('wzdx')).toEqual(false)

    EnvironmentVars.ENABLE_WZDX_FEATURES = true

    expect(evaluateFeatureFlags('rsu')).toEqual(true)
    expect(evaluateFeatureFlags('intersection')).toEqual(true)
    expect(evaluateFeatureFlags('wzdx')).toEqual(true)
  })
})
