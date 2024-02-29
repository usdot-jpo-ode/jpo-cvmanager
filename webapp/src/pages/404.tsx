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
      <h1>Oops! You seem to be lost.</h1>
      <p>Here are some helpful links:</p>
      <Link to="/" onClick={() => dispatch(setRouteNotFound(false))}>
        Home
      </Link>
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
      <h1>You do not have permission to view this page</h1>
      <p>Return to public pages:</p>
      <Link to="/" onClick={() => dispatch(setRouteNotFound(false))}>
        Home
      </Link>
    </div>
  )
}

export default function NotFound() {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  return (
    <div>
      <h1>Oops! You seem to be lost.</h1>
      <p>Here are some helpful links:</p>
      <Link to="/" onClick={() => dispatch(setRouteNotFound(false))}>
        Home
      </Link>
    </div>
  )
}
