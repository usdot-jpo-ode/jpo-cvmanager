// ProtectedRoute.js
import React from 'react'
import { Navigate } from 'react-router-dom'
import EnvironmentVars from '../EnvironmentVars'
import { ReactJSXElement } from '@emotion/react/types/jsx-namespace'

const RsuRouteGuard = ({ children, condition }: { children: ReactJSXElement; condition? }) => {
  // Check your environment variable here
  const isAccessAllowed = EnvironmentVars.ENABLE_RSU_PAGES

  return isAccessAllowed && condition ? children : <Navigate to="/" />
}

export default RsuRouteGuard
