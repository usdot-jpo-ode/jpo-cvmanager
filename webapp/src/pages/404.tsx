import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import React, { useEffect } from 'react'
import { useDispatch } from 'react-redux'
import { Link } from 'react-router-dom'
import { RootState } from '../store'
import { setRouteNotFound } from '../generalSlices/userSlice'

export const NotFoundRedirect = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()

  useEffect(() => {
    console.log("setRouteNotFound Route doesn't exist")
    dispatch(setRouteNotFound(true))
  }, [])

  return (
    <div>
      <h1>
        This route does not exist. Please return to the home page:{' '}
        <Link to="/" onClick={() => dispatch(setRouteNotFound(false))}>
          Home
        </Link>
      </h1>
    </div>
  )
}

export const AdminNotFoundRedirect = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()

  useEffect(() => {
    console.log("setRouteNotFound Route doesn't exist")
    dispatch(setRouteNotFound(true))
  }, [])

  return (
    <div>
      <h1>
        You do not have permission to view this page. Please return to public pages:{' '}
        <Link to="/" onClick={() => dispatch(setRouteNotFound(false))}>
          Home
        </Link>
      </h1>
    </div>
  )
}

export default function NotFound() {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  return (
    <div>
      <h1>
        This route does not exist. Please return to the home page:{' '}
        <Link to="/" onClick={() => dispatch(setRouteNotFound(false))}>
          Home
        </Link>
      </h1>
    </div>
  )
}
