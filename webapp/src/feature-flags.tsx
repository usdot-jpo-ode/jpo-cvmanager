import React from 'react'
import EnvironmentVars from './EnvironmentVars'
import { ReactJSXElement } from '@emotion/react/types/jsx-namespace'
import { Navigate } from 'react-router-dom'

export const IntersectionRouteGuard = ({ children, condition }: { children: ReactJSXElement; condition? }) => {
  // Re-direct to home page if intersection pages are disabled
  const isAccessAllowed = evaluateFeatureFlags('intersection')
  return isAccessAllowed && condition ? children : <Navigate to="/" />
}

export const RsuRouteGuard = ({ children, condition }: { children: ReactJSXElement; condition? }) => {
  // Re-direct to home page if rsu pages are disabled
  const isAccessAllowed = evaluateFeatureFlags('rsu')
  return isAccessAllowed && condition ? children : <Navigate to="/" />
}

export const ConditionalRenderRsu: React.FC<{
  children: React.ReactNode // Specify the type for children prop
}> = ({ children }) => {
  // Check if any child has a tag that should be removed
  const childrenArray = React.Children.toArray(children)
  const shouldRender = childrenArray.map((child) => {
    return evaluateFeatureFlags('rsu') ? null : child
  })

  return <>{shouldRender}</>
}

export const ConditionalRenderIntersection: React.FC<{
  children: React.ReactNode // Specify the type for children prop
}> = ({ children }) => {
  // Check if any child has a tag that should be removed
  const childrenArray = React.Children.toArray(children)
  const shouldRender = childrenArray.map((child) => {
    return evaluateFeatureFlags('intersection') ? null : child
  })

  return <>{shouldRender}</>
}

export const evaluateFeatureFlags = (tag?: FEATURE_KEY): boolean => {
  // Evaluage list of tags against environment variable feature flags. If tag is present, and ENABLED_FEATURE is false, return false
  if (!tag) {
    return true
  } else if (!EnvironmentVars.ENABLE_RSU_PAGES) {
    return tag !== 'rsu'
  } else if (!EnvironmentVars.ENABLE_INTERSECTION_PAGES) {
    return tag !== 'intersection'
  }
  return true
}
