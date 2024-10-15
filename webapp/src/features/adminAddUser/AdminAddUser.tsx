import React, { useEffect, useState } from 'react'
import { Form } from 'react-bootstrap'
import { useForm } from 'react-hook-form'
import { Multiselect, DropdownList } from 'react-widgets'
import {
  selectSelectedOrganizationNames,
  selectSelectedOrganizations,
  selectOrganizationNames,
  selectAvailableRoles,
  selectApiData,
  selectSubmitAttempt,

  // actions
  getUserData,
  setSelectedRole,
  updateOrganizationNamesApiData,
  updateAvailableRolesApiData,
  updateOrganizations,
  submitForm,
  AdminUserForm,
} from './adminAddUserSlice'

import '../adminRsuTab/Admin.css'
import 'react-widgets/styles.css'
import toast from 'react-hot-toast'
import { useNavigate } from 'react-router-dom'
import Dialog from '@mui/material/Dialog'
import { DialogActions, DialogContent, DialogTitle } from '@mui/material'
import { useAppDispatch, useAppSelector } from '../../hooks'

const AdminAddUser = () => {
  const dispatch = useAppDispatch()
  const selectedOrganizationNames = useAppSelector(selectSelectedOrganizationNames)
  const selectedOrganizations = useAppSelector(selectSelectedOrganizations)
  const organizationNames = useAppSelector(selectOrganizationNames)
  const availableRoles = useAppSelector(selectAvailableRoles)
  const apiData = useAppSelector(selectApiData)
  const submitAttempt = useAppSelector(selectSubmitAttempt)
  const [open, setOpen] = useState(true)
  const navigate = useNavigate()
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<AdminUserForm>()

  useEffect(() => {
    dispatch(getUserData())
  }, [dispatch])

  useEffect(() => {
    dispatch(updateOrganizationNamesApiData())
    dispatch(updateAvailableRolesApiData())
  }, [apiData, dispatch])

  const onSubmit = (data: AdminUserForm) => {
    dispatch(submitForm({ data, reset })).then((data: any) => {
      if (data.payload.success) {
        toast.success('User added successfully')
      } else {
        toast.error('Failed to add user: ' + data.payload.message)
      }
    })
    setOpen(false)
    navigate('/dashboard/admin/users')
  }

  return (
    <Dialog open={open}>
      <DialogTitle>Add User</DialogTitle>
      <DialogContent>
        <Form
          id="add-user-form"
          onSubmit={handleSubmit(onSubmit)}
          style={{ fontFamily: 'Arial, Helvetica, sans-serif' }}
        >
          <Form.Group className="mb-3" controlId="email">
            <Form.Label>Email</Form.Label>
            <Form.Control
              type="email"
              placeholder="Enter user email (Required)"
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
              placeholder="Enter user's first name (Required)"
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
              placeholder="Enter user's last name (Required)"
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
              dataKey="id"
              textField="name"
              placeholder="Select organizations (Required)"
              data={organizationNames}
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
                      placeholder="Select Role"
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
        <button form="add-user-form" type="submit" className="admin-button">
          Add User
        </button>
      </DialogActions>
    </Dialog>
  )
}

export default AdminAddUser
