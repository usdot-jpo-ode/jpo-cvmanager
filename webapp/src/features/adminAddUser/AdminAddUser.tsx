import React, { useEffect, useState } from 'react'
import { Form } from 'react-bootstrap'
import { useForm } from 'react-hook-form'
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
import { useSelector, useDispatch } from 'react-redux'

import '../adminRsuTab/Admin.css'
import 'react-widgets/styles.css'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../store'
import toast from 'react-hot-toast'
import { useNavigate } from 'react-router-dom'
import Dialog from '@mui/material/Dialog'
import CloseIcon from '@mui/icons-material/Close'
import {
  Button,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  IconButton,
  InputLabel,
  MenuItem,
  OutlinedInput,
  Select,
  Typography,
} from '@mui/material'
import { ErrorMessageText } from '../../styles/components/Messages'

const AdminAddUser = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const selectedOrganizationNames = useSelector(selectSelectedOrganizationNames)
  const selectedOrganizations = useSelector(selectSelectedOrganizations)
  const organizationNames = useSelector(selectOrganizationNames)
  const availableRoles = useSelector(selectAvailableRoles)
  const apiData = useSelector(selectApiData)
  const submitAttempt = useSelector(selectSubmitAttempt)
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
      <IconButton
        aria-label="close"
        onClick={() => {
          setOpen(false)
          navigate('..')
        }}
        sx={(theme) => ({
          position: 'absolute',
          right: 8,
          top: 8,
          color: theme.palette.text.primary,
        })}
      >
        <CloseIcon />
      </IconButton>
      <DialogContent sx={{ minWidth: '450px', maxWidth: '750px' }}>
        <Form
          id="add-user-form"
          onSubmit={handleSubmit(onSubmit)}
          style={{ fontFamily: 'Arial, Helvetica, sans-serif' }}
        >
          <Form.Group className="mb-3" controlId="email">
            <FormControl fullWidth margin="normal">
              <InputLabel htmlFor="user-email">User Email</InputLabel>
              <OutlinedInput
                id="user-email"
                type="email"
                label="User Email"
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
            </FormControl>
          </Form.Group>

          <Form.Group className="mb-3" controlId="first_name">
            <FormControl fullWidth margin="normal">
              <InputLabel htmlFor="user-first-name">First Name</InputLabel>
              <OutlinedInput
                id="user-first-name"
                type="text"
                label="First Name"
                {...register('first_name', {
                  required: "Please enter user's first name",
                })}
              />
              {errors.first_name && (
                <p className="errorMsg" role="alert">
                  {errors.first_name.message}
                </p>
              )}
            </FormControl>
          </Form.Group>

          <Form.Group className="mb-3" controlId="last_name">
            <FormControl fullWidth margin="normal">
              <InputLabel htmlFor="user-last-name">Last Name</InputLabel>
              <OutlinedInput
                id="user-last-name"
                type="text"
                label="Last Name"
                {...register('last_name', {
                  required: "Please enter user's last name",
                })}
              />
              {errors.last_name && (
                <p className="errorMsg" role="alert">
                  {errors.last_name.message}
                </p>
              )}
            </FormControl>
          </Form.Group>

          <Form.Group className="mb-3" controlId="super_user">
            <Form.Check label=" Super User" type="switch" {...register('super_user')} />
          </Form.Group>

          <Form.Group className="mb-3" controlId="organizations">
            <FormControl fullWidth margin="normal">
              <InputLabel>Organizations</InputLabel>
              <Select
                id="organizations"
                label="Organizations"
                multiple
                value={selectedOrganizationNames.map((name) => name.name)}
                onChange={(event) => {
                  const selectedOrgs = event.target.value as String[]
                  dispatch(updateOrganizations(organizationNames.filter((org) => selectedOrgs.includes(org.name))))
                }}
              >
                {organizationNames.map((org) => (
                  <MenuItem key={org.id} value={org.name}>
                    {org.name}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Form.Group>

          {selectedOrganizations.length > 0 && (
            <Form.Group className="mb-3" controlId="roles">
              <Typography fontSize="small">Roles</Typography>
              {selectedOrganizations.map((organization) => {
                let role = { role: organization.role }

                return (
                  <Form.Group className="mb-3" controlId={organization.id.toString()}>
                    <FormControl fullWidth margin="normal">
                      <InputLabel>{organization.name}</InputLabel>
                      <Select
                        id={organization.id.toString()}
                        label="Select Role"
                        value={role.role}
                        onChange={(event) => {
                          const selectedRole = event.target.value as string
                          dispatch(setSelectedRole({ ...organization, role: selectedRole }))
                        }}
                      >
                        {availableRoles.map((role) => (
                          <MenuItem key={role.role} value={role.role}>
                            {role.role}
                          </MenuItem>
                        ))}
                      </Select>
                    </FormControl>
                  </Form.Group>
                )
              })}
            </Form.Group>
          )}

          {selectedOrganizations.length === 0 && submitAttempt && (
            <ErrorMessageText role="alert">Must select at least one organization</ErrorMessageText>
          )}
        </Form>
      </DialogContent>
      <DialogActions sx={{ padding: '20px' }}>
        <Button
          onClick={() => {
            setOpen(false)
            navigate('/dashboard/admin/users')
          }}
          variant="outlined"
          color="info"
          style={{ position: 'absolute', bottom: 10, left: 10 }}
        >
          Close
        </Button>
        <Button
          form="add-user-form"
          type="submit"
          variant="contained"
          style={{ position: 'absolute', bottom: 10, right: 10 }}
        >
          Add User
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default AdminAddUser
