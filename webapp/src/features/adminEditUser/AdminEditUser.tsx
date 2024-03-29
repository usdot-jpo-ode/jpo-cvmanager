import React, { useEffect } from 'react'
import { Form } from 'react-bootstrap'
import { useForm } from 'react-hook-form'
import { Multiselect, DropdownList } from 'react-widgets'
import {
  selectSuccessMsg,
  selectSelectedOrganizationNames,
  selectSelectedOrganizations,
  selectOrganizationNames,
  selectAvailableRoles,
  selectErrorState,
  selectErrorMsg,
  selectSubmitAttempt,
  selectApiData,
  setSelectedRole,

  // actions
  getUserData,
  submitForm,
  updateOrganizations,
  UserApiDataOrgs,
} from './adminEditUserSlice'
import { useSelector, useDispatch } from 'react-redux'

import '../adminRsuTab/Admin.css'
import 'react-widgets/styles.css'
import { ThunkDispatch, AnyAction } from '@reduxjs/toolkit'
import { RootState } from '../../store'
import { Link, useParams } from 'react-router-dom'
import { getAvailableUsers, selectTableData } from '../adminUserTab/adminUserTabSlice'
import { ThemeProvider, Typography } from '@mui/material'
import { theme } from '../../styles'

const AdminEditUser = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const successMsg = useSelector(selectSuccessMsg)
  const selectedOrganizationNames = useSelector(selectSelectedOrganizationNames)
  const selectedOrganizations = useSelector(selectSelectedOrganizations)
  const organizationNames = useSelector(selectOrganizationNames)
  const availableRoles = useSelector(selectAvailableRoles)
  const apiData = useSelector(selectApiData)
  const errorState = useSelector(selectErrorState)
  const errorMsg = useSelector(selectErrorMsg)
  const submitAttempt = useSelector(selectSubmitAttempt)
  const userTableData = useSelector(selectTableData)
  const {
    register,
    handleSubmit,
    formState: { errors },
    setValue,
  } = useForm({
    defaultValues: {
      orig_email: '',
      email: '',
      first_name: '',
      last_name: '',
      super_user: '',
      receive_error_emails: '',
      organizations_to_add: [],
      organizations_to_modify: [],
      organizations_to_remove: [],
    },
  })

  const { email } = useParams<{ email: string }>()

  useEffect(() => {
    if (
      (userTableData ?? []).find((user: AdminUserWithId) => user.email === email) &&
      Object.keys(apiData ?? {}).length == 0
    ) {
      console.log('getUserData')
      dispatch(getUserData(email))
    }
    console.log(
      'useEffect getUserData',
      email,
      userTableData,
      apiData,
      (userTableData ?? []).find((user: AdminUserWithId) => user.email === email),
      Object.keys(apiData ?? {}),
      Object.keys(apiData ?? {}).length,
      (userTableData ?? []).find((user: AdminUserWithId) => user.email === email) &&
        Object.keys(apiData ?? {}).length == 0
    )
  }, [email, userTableData, dispatch])

  useEffect(() => {
    dispatch(getAvailableUsers())
  }, [dispatch])

  useEffect(() => {
    if (apiData && Object.keys(apiData).length !== 0) {
      setValue('orig_email', apiData.user_data.email)
      setValue('email', apiData.user_data.email)
      setValue('first_name', apiData.user_data.first_name)
      setValue('last_name', apiData.user_data.last_name)
      setValue('super_user', apiData.user_data.super_user.toString())
      setValue('receive_error_emails', apiData.user_data.receive_error_emails.toString())
    }
    console.log('useEffect apiData', email, userTableData, apiData)
  }, [apiData, setValue])

  const onSubmit = (data: UserApiDataOrgs) => {
    dispatch(submitForm({ data }))
  }

  console.log('render', email, userTableData, apiData, Object.keys(apiData ?? {}).length)

  return (
    <div>
      {Object.keys(apiData ?? {}).length != 0 ? (
        <Form onSubmit={handleSubmit(onSubmit)}>
          <Form.Group className="mb-3" controlId="email">
            <Form.Label>Email</Form.Label>
            <Form.Control
              type="email"
              placeholder="Enter user email"
              {...register('email', {
                required: 'Please enter user email',
                pattern: {
                  value: /^[^@ ]+@[^@ ]+\.[^@ .]{2,}$/,
                  message: 'Please enter a valid email',
                },
              })}
            />
            {errors.email && (
              <p className="errorMsg" role="alert">
                {errors.email.message}
              </p>
            )}
          </Form.Group>

          <Form.Group className="mb-3" controlId="first_name">
            <Form.Label>First Name</Form.Label>
            <Form.Control
              type="text"
              placeholder="Enter user's first name"
              {...register('first_name', {
                required: "Please enter user's first name",
              })}
            />
            {errors.first_name && (
              <p className="errorMsg" role="alert">
                {errors.first_name.message}
              </p>
            )}
          </Form.Group>

          <Form.Group className="mb-3" controlId="last_name">
            <Form.Label>Last Name</Form.Label>
            <Form.Control
              type="text"
              placeholder="Enter user's last name"
              {...register('last_name', {
                required: "Please enter user's last name",
              })}
            />
            {errors.last_name && (
              <p className="errorMsg" role="alert">
                {errors.last_name.message}
              </p>
            )}
          </Form.Group>

          <Form.Group className="mb-3" controlId="super_user">
            <Form.Check label=" Super User" type="switch" {...register('super_user')} />
          </Form.Group>

          <Form.Group className="mb-3" controlId="receive_error_emails">
            <Form.Check label=" Receive Error Emails" type="switch" {...register('receive_error_emails')} />
          </Form.Group>

          <Form.Group className="mb-3" controlId="organizations">
            <Form.Label>Organizations</Form.Label>
            <Multiselect
              className="form-multiselect"
              dataKey="name"
              textField="name"
              data={organizationNames}
              placeholder="Select organizations"
              value={selectedOrganizationNames}
              onChange={(value) => {
                dispatch(updateOrganizations(value))
              }}
            />
          </Form.Group>

          {selectedOrganizations.length > 0 && (
            <Form.Group className="mb-3" controlId="roles">
              <Form.Label>Roles</Form.Label>
              <p className="spacer" />
              {selectedOrganizations.map((organization) => {
                let role = { role: organization.role }

                return (
                  <Form.Group className="mb-3" controlId={organization.id.toString()}>
                    <Form.Label>{organization.name}</Form.Label>
                    <DropdownList
                      className="form-dropdown"
                      dataKey="role"
                      textField="role"
                      data={availableRoles}
                      value={role}
                      onChange={(value) => {
                        dispatch(setSelectedRole({ ...organization, role: value.role }))
                      }}
                    />
                  </Form.Group>
                )
              })}
            </Form.Group>
          )}

          {selectedOrganizations.length === 0 && submitAttempt && (
            <p className="error-msg">Must select at least one organization</p>
          )}

          {successMsg && (
            <p className="success-msg" role="status">
              {successMsg}
            </p>
          )}
          {errorState && (
            <p className="error-msg" role="alert">
              Failed to apply changes due to error: {errorMsg}
            </p>
          )}
          <div className="form-control">
            <label></label>
            <button type="submit" className="admin-button">
              Apply Changes
            </button>
          </div>
        </Form>
      ) : (
        <Typography variant={'h4'} style={{ color: '#fff' }}>
          Unknown email address. Either this user does not exist, or you do not have permissions to view them.{' '}
          <Link to="../">Users</Link>
        </Typography>
      )}
    </div>
  )
}

export default AdminEditUser
