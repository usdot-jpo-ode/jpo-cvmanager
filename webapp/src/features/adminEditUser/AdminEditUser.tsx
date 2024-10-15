import React, { useEffect, useState } from 'react'
import { Form } from 'react-bootstrap'
import { useForm } from 'react-hook-form'
import { Multiselect, DropdownList } from 'react-widgets'
import {
  selectSelectedOrganizationNames,
  selectSelectedOrganizations,
  selectOrganizationNames,
  selectAvailableRoles,
  selectSubmitAttempt,
  selectApiData,
  setSelectedRole,

  // actions
  getUserData,
  submitForm,
  updateOrganizations,
  UserApiDataOrgs,
} from './adminEditUserSlice'

import '../adminRsuTab/Admin.css'
import 'react-widgets/styles.css'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { getAvailableUsers, selectTableData } from '../adminUserTab/adminUserTabSlice'
import { DialogActions, DialogContent, DialogTitle, Typography } from '@mui/material'
import Dialog from '@mui/material/Dialog'
import toast from 'react-hot-toast'
import { useAppDispatch, useAppSelector } from '../../hooks'

const AdminEditUser = () => {
  const dispatch = useAppDispatch()
  const selectedOrganizationNames = useAppSelector(selectSelectedOrganizationNames)
  const selectedOrganizations = useAppSelector(selectSelectedOrganizations)
  const organizationNames = useAppSelector(selectOrganizationNames)
  const availableRoles = useAppSelector(selectAvailableRoles)
  const apiData = useAppSelector(selectApiData)
  const submitAttempt = useAppSelector(selectSubmitAttempt)
  const userTableData = useAppSelector(selectTableData)
  const [open, setOpen] = useState(true)
  const navigate = useNavigate()
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
      dispatch(getUserData(email))
    }
  }, [email, userTableData, dispatch])

  useEffect(() => {
    dispatch(getAvailableUsers())
  }, [dispatch])

  useEffect(() => {
    const currUser = (userTableData ?? []).find((user: AdminUserWithId) => user.email === email)
    if (currUser) {
      setValue('orig_email', currUser.email)
      setValue('email', currUser.email)
      setValue('first_name', currUser.first_name)
      setValue('last_name', currUser.last_name)
      setValue('super_user', currUser.super_user.toString())
    }
  }, [apiData, setValue])

  const onSubmit = (data: UserApiDataOrgs) => {
    dispatch(submitForm({ data })).then((data: any) => {
      if (data.payload.success) {
        toast.success('User updated successfully')
      } else {
        toast.error('Failed to update user: ' + data.payload.message)
      }
    })
    setOpen(false)
    navigate('/dashboard/admin/users')
  }

  return (
    <Dialog open={open}>
      <DialogTitle>Edit User</DialogTitle>
      <DialogContent>
        {Object.keys(apiData ?? {}).length != 0 ? (
          <Form
            id="edit-user-form"
            onSubmit={handleSubmit(onSubmit)}
            style={{ fontFamily: 'Arial, Helvetica, sans-serif' }}
          >
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
              <Form.Check label=" Super User" type="switch" {...register('super_user')} style={{ color: '#fff' }} />
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
              <p className="error-msg" role="alert">
                Must select at least one organization
              </p>
            )}
          </Form>
        ) : (
          <Typography variant={'h4'} style={{ color: '#fff' }}>
            Unknown email address. Either this user does not exist, or you do not have permissions to view them.{' '}
            <Link to="../">Users</Link>
          </Typography>
        )}
      </DialogContent>
      <DialogActions>
        <button
          onClick={() => {
            setOpen(false)
            navigate('/dashboard/admin/users')
          }}
          className="admin-button"
        >
          Close
        </button>
        <button form="edit-user-form" type="submit" className="admin-button">
          Apply Changes
        </button>
      </DialogActions>
    </Dialog>
  )
}

export default AdminEditUser
