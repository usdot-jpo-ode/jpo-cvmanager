import { render } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import {
  RsuRouteGuard,
  ConditionalRenderRsu,
  ConditionalRenderIntersection,
  IntersectionRouteGuard,
  WzdxRouteGuard,
  ConditionalRenderWzdx,
  applyFlagsToList,
  evaluateFeatureFlags,
  ConditionalRenderHaas,
  ConditionalRenderRsuStatusMonitor,
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

import EnvironmentVars from './EnvironmentVars'

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

  test('ConditionalRenderHaas renders children when feature is enabled', () => {
    EnvironmentVars.ENABLE_HAAS_FEATURES = true
    const { getByText } = render(
      <ConditionalRenderHaas>
        <div>HAAS Content</div>
      </ConditionalRenderHaas>
    )
    expect(getByText('HAAS Content')).toBeInTheDocument()
  })

  test('ConditionalRenderHaas does not render children when feature is disabled', () => {
    EnvironmentVars.ENABLE_HAAS_FEATURES = false
    const { queryByText } = render(
      <ConditionalRenderHaas>
        <div>HAAS Content</div>
      </ConditionalRenderHaas>
    )
    expect(queryByText('HAAS Content')).not.toBeInTheDocument()
  })

  test('ConditionalRenderRsuStatusMonitor renders children when feature is enabled', () => {
    EnvironmentVars.ENABLE_RSU_STATUS_MONITOR_FEATURES = true
    const { getByText } = render(
      <ConditionalRenderRsuStatusMonitor>
        <div>RSU Status Monitor Content</div>
      </ConditionalRenderRsuStatusMonitor>
    )
    expect(getByText('RSU Status Monitor Content')).toBeInTheDocument()
  })

  test('ConditionalRenderRsuStatusMonitor does not render children when feature is disabled', () => {
    EnvironmentVars.ENABLE_RSU_STATUS_MONITOR_FEATURES = false
    const { queryByText } = render(
      <ConditionalRenderRsuStatusMonitor>
        <div>RSU Status Monitor Content</div>
      </ConditionalRenderRsuStatusMonitor>
    )
    expect(queryByText('RSU Status Monitor Content')).not.toBeInTheDocument()
  })

  test('applyFlagsToList keeps items whose flags are enabled', () => {
    EnvironmentVars.ENABLE_RSU_FEATURES = true
    EnvironmentVars.ENABLE_INTERSECTION_FEATURES = false

    const items = [
      { id: 'rsu-item', tag: 'rsu' as const },
      { id: 'intersection-item', tag: 'intersection' as const },
    ]

    expect(applyFlagsToList(items)).toEqual([{ id: 'rsu-item', tag: 'rsu' }])
  })

  test('applyFlagsToList keeps untagged items', () => {
    EnvironmentVars.ENABLE_RSU_FEATURES = false

    const items = [{ id: 'always-visible' }, { id: 'rsu-item', tag: 'rsu' as const }]

    expect(applyFlagsToList(items)).toEqual([{ id: 'always-visible' }])
  })

  test('applyFlagsToList returns the full list when all matching flags are enabled', () => {
    EnvironmentVars.ENABLE_RSU_FEATURES = true
    EnvironmentVars.ENABLE_HAAS_FEATURES = true

    const items = [
      { id: 'rsu-item', tag: 'rsu' as const },
      { id: 'haas-item', tag: 'haas' as const },
      { id: 'always-visible' },
    ]

    expect(applyFlagsToList(items)).toEqual(items)
  })

  test('evaluateFeatureFlags correctly handles environment variable values', () => {
    EnvironmentVars.ENABLE_RSU_FEATURES = false
    EnvironmentVars.ENABLE_INTERSECTION_FEATURES = false
    EnvironmentVars.ENABLE_WZDX_FEATURES = false
    EnvironmentVars.ENABLE_HAAS_FEATURES = false
    EnvironmentVars.ENABLE_RSU_STATUS_MONITOR_FEATURES = false

    expect(evaluateFeatureFlags('rsu')).toEqual(false)
    expect(evaluateFeatureFlags('intersection')).toEqual(false)
    expect(evaluateFeatureFlags('wzdx')).toEqual(false)
    expect(evaluateFeatureFlags('haas')).toEqual(false)
    expect(evaluateFeatureFlags('rsuStatusMonitor')).toEqual(false)

    EnvironmentVars.ENABLE_RSU_FEATURES = true

    expect(evaluateFeatureFlags('rsu')).toEqual(true)
    expect(evaluateFeatureFlags('intersection')).toEqual(false)
    expect(evaluateFeatureFlags('wzdx')).toEqual(false)
    expect(evaluateFeatureFlags('haas')).toEqual(false)
    expect(evaluateFeatureFlags('rsuStatusMonitor')).toEqual(false)

    EnvironmentVars.ENABLE_INTERSECTION_FEATURES = true

    expect(evaluateFeatureFlags('rsu')).toEqual(true)
    expect(evaluateFeatureFlags('intersection')).toEqual(true)
    expect(evaluateFeatureFlags('wzdx')).toEqual(false)
    expect(evaluateFeatureFlags('haas')).toEqual(false)
    expect(evaluateFeatureFlags('rsuStatusMonitor')).toEqual(false)

    EnvironmentVars.ENABLE_WZDX_FEATURES = true

    expect(evaluateFeatureFlags('rsu')).toEqual(true)
    expect(evaluateFeatureFlags('intersection')).toEqual(true)
    expect(evaluateFeatureFlags('wzdx')).toEqual(true)
    expect(evaluateFeatureFlags('haas')).toEqual(false)
    expect(evaluateFeatureFlags('rsuStatusMonitor')).toEqual(false)

    EnvironmentVars.ENABLE_HAAS_FEATURES = true

    expect(evaluateFeatureFlags('rsu')).toEqual(true)
    expect(evaluateFeatureFlags('intersection')).toEqual(true)
    expect(evaluateFeatureFlags('wzdx')).toEqual(true)
    expect(evaluateFeatureFlags('haas')).toEqual(true)
    expect(evaluateFeatureFlags('rsuStatusMonitor')).toEqual(false)

    EnvironmentVars.ENABLE_RSU_STATUS_MONITOR_FEATURES = true

    expect(evaluateFeatureFlags('rsuStatusMonitor')).toEqual(true)

    expect(evaluateFeatureFlags('rsu')).toEqual(true)
    expect(evaluateFeatureFlags('intersection')).toEqual(true)
    expect(evaluateFeatureFlags('wzdx')).toEqual(true)
    expect(evaluateFeatureFlags('haas')).toEqual(true)
    expect(evaluateFeatureFlags('rsuStatusMonitor')).toEqual(true)
  })
})
