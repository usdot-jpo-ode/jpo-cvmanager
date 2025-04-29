import React, { useEffect, useState } from 'react'
import { Form } from 'react-bootstrap'
import { useForm } from 'react-hook-form'
import {
  selectSelectedOrganizationNames,
  selectSelectedOrganizations,
  selectOrganizationNames,
  selectAvailableRoles,
  selectSubmitAttempt,
  selectApiData,
  setSelectedRole,

  // actions
  submitForm,
  updateOrganizations,
  UserApiDataOrgs,
} from './adminEditUserSlice'
import { useSelector, useDispatch } from 'react-redux'

import '../adminRsuTab/Admin.css'
import 'react-widgets/styles.css'
import '../../styles/fonts/museo-slab.css'
import { ThunkDispatch, AnyAction } from '@reduxjs/toolkit'
import { RootState } from '../../store'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { getAvailableUsers, selectTableData } from '../adminUserTab/adminUserTabSlice'
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
import Dialog from '@mui/material/Dialog'
import toast from 'react-hot-toast'
import CloseIcon from '@mui/icons-material/Close'
import { ErrorMessageText } from '../../styles/components/Messages'

const AdminEditUser = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const selectedOrganizationNames = useSelector(selectSelectedOrganizationNames)
  const selectedOrganizations = useSelector(selectSelectedOrganizations)
  const organizationNames = useSelector(selectOrganizationNames)
  const availableRoles = useSelector(selectAvailableRoles)
  const apiData = useSelector(selectApiData)
  const submitAttempt = useSelector(selectSubmitAttempt)
  const userTableData = useSelector(selectTableData)
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
      super_user: false,
      organizations_to_add: [],
      organizations_to_modify: [],
      organizations_to_remove: [],
    },
  })

  const { email } = useParams<{ email: string }>()

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
      setValue('super_user', currUser.super_user)
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
        {Object.keys(apiData ?? {}).length != 0 ? (
          <Form
            id="edit-user-form"
            onSubmit={handleSubmit(onSubmit)}
            style={{ fontFamily: '"museo-slab", Arial, Helvetica, sans-serif' }}
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
                  className="form-dropdown"
                  label="Organizations"
                  multiple
                  value={selectedOrganizations.map((org) => org.name)}
                  defaultValue={selectedOrganizations.map((org) => org.name)}
                  onChange={(event) => {
                    const selectedOrgs = event.target.value as String[]
                    dispatch(updateOrganizations(organizationNames.filter((org) => selectedOrgs.includes(org.name))))
                  }}
                >
                  {organizationNames.map((org) => (
                    <MenuItem key={org.name} value={org.name}>
                      {org.name}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Form.Group>

            {selectedOrganizations.length > 0 && (
              <Form.Group className="mb-3" controlId="roles">
                <Form.Label>Roles</Form.Label>
                <p className="spacer" />
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
                          defaultValue={role.role}
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
        ) : (
          <Typography variant={'h4'}>
            Unknown email address. Either this user does not exist, or you do not have permissions to view them.{' '}
            <Link to="../">Users</Link>
          </Typography>
        )}
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
          form="edit-user-form"
          type="submit"
          variant="contained"
          style={{ position: 'absolute', bottom: 10, right: 10 }}
        >
          Apply Changes
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default AdminEditUser
