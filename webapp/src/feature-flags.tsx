import React from 'react'
import EnvironmentVars from './EnvironmentVars'
import { ReactJSXElement } from '@emotion/react/types/jsx-namespace'
import { Navigate } from 'react-router-dom'

export const IntersectionRouteGuard = ({ children }: { children: ReactJSXElement; condition? }) => {
  // Re-direct to home page if intersection pages are disabled
  const isAccessAllowed = evaluateFeatureFlags('intersection')
  return isAccessAllowed ? children : <Navigate to="/" />
}

export const RsuRouteGuard = ({ children }: { children: ReactJSXElement; condition? }) => {
  // Re-direct to home page if rsu pages are disabled
  const isAccessAllowed = evaluateFeatureFlags('rsu')
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

export const evaluateFeatureFlags = (tag?: FEATURE_KEY): boolean => {
  // Evaluage list of tags against environment variable feature flags. If tag is present, and ENABLED_FEATURE is false, return false
  if (!tag) {
    return true
  } else if (tag === 'rsu' && !EnvironmentVars.ENABLE_RSU_PAGES) {
    return false
  } else if (tag === 'intersection' && !EnvironmentVars.ENABLE_INTERSECTION_PAGES) {
    return false
  }
  return true
}
