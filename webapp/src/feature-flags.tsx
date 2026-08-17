import React from 'react'
import EnvironmentVars from './EnvironmentVars'
import { Navigate } from 'react-router-dom'

export const RsuRouteGuard = ({ children }: { children: React.ReactElement; condition? }) => {
  const isAccessAllowed = evaluateFeatureFlags('rsu')
  return isAccessAllowed ? children : <Navigate to="/" />
}

export const IntersectionRouteGuard = ({ children }: { children: React.ReactElement; condition? }) => {
  const isAccessAllowed = evaluateFeatureFlags('intersection')
  return isAccessAllowed ? children : <Navigate to="/" />
}

export const WzdxRouteGuard = ({ children }: { children: React.ReactElement; condition? }) => {
  const isAccessAllowed = evaluateFeatureFlags('wzdx')
  return isAccessAllowed ? children : <Navigate to="/" />
}

export const HaasRouteGuard = ({ children }: { children: React.ReactElement; condition? }) => {
  const isAccessAllowed = evaluateFeatureFlags('haas')
  return isAccessAllowed ? children : <Navigate to="/" />
}

export const ConditionalRenderRsu: React.FC<{
  children: React.ReactNode // Specify the type for children prop
}> = ({ children }) => {
  const shouldRender = React.Children.map(children, (child) => {
    return !evaluateFeatureFlags('rsu') ? null : child
  })

  return <>{shouldRender}</>
}

export const ConditionalRenderIntersection: React.FC<{
  children: React.ReactNode // Specify the type for children prop
}> = ({ children }) => {
  const shouldRender = React.Children.map(children, (child) => {
    return !evaluateFeatureFlags('intersection') ? null : child
  })

  return <>{shouldRender}</>
}

export const ConditionalRenderWzdx: React.FC<{
  children: React.ReactNode // Specify the type for children prop
}> = ({ children }) => {
  const shouldRender = React.Children.map(children, (child) => {
    return !evaluateFeatureFlags('wzdx') ? null : child
  })

  return <>{shouldRender}</>
}

export const ConditionalRenderHaas: React.FC<{
  children: React.ReactNode // Specify the type for children prop
}> = ({ children }) => {
  const shouldRender = React.Children.map(children, (child) => {
    return !evaluateFeatureFlags('haas') ? null : child
  })

  return <>{shouldRender}</>
}

export const evaluateFeatureFlags = (tag?: FEATURE_KEY): boolean => {
  // Evaluate list of tags against environment variable feature flags. If tag is present, and ENABLED_FEATURE is false, return false
  if (!tag) {
    return true
  } else if (tag === 'rsu' && !EnvironmentVars.ENABLE_RSU_FEATURES) {
    return false
  } else if (tag === 'intersection' && !EnvironmentVars.ENABLE_INTERSECTION_FEATURES) {
    return false
  } else if (tag === 'wzdx' && !EnvironmentVars.ENABLE_WZDX_FEATURES) {
    return false
  } else if (tag === 'haas' && !EnvironmentVars.ENABLE_HAAS_FEATURES) {
    return false
  }
  return true
}
