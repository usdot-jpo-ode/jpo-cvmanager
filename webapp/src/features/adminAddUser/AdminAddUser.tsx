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
import '../../styles/fonts/museo-slab.css'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../store'
import toast from 'react-hot-toast'
import { useNavigate } from 'react-router-dom'
import Dialog from '@mui/material/Dialog'
import {
  Button,
  DialogActions,
  DialogContent,
  FormControl,
  InputLabel,
  MenuItem,
  TextField,
  Select,
  Typography,
} from '@mui/material'
import { ErrorMessageText } from '../../styles/components/Messages'
import { SideBarHeader } from '../../styles/components/SideBarHeader'

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
      <DialogContent sx={{ width: '600px', padding: '5px 10px' }}>
        <SideBarHeader
          onClick={() => {
            setOpen(false)
            navigate('..')
          }}
          title="Add User"
        />
        <Form id="add-user-form" onSubmit={handleSubmit(onSubmit)}>
          <Form.Group controlId="email">
            <FormControl fullWidth margin="normal">
              <TextField
                label="Email"
                placeholder="Enter User Email"
                color="info"
                variant="outlined"
                required
                {...register('email', {
                  required: 'Please enter user email',
                  pattern: {
                    value: /^[^@ ]+@[^@ ]+\.[^@ .]{2,}$/,
                    message: 'Please enter a valid email',
                  },
                })}
                slotProps={{
                  inputLabel: {
                    shrink: true,
                  },
                }}
              />
              {errors.email && (
                <p className="errorMsg" role="alert">
                  {errors.email.message}
                </p>
              )}
            </FormControl>
          </Form.Group>

          <Form.Group controlId="first_name">
            <FormControl fullWidth margin="normal">
              <TextField
                label="First Name"
                placeholder="Enter First Name"
                color="info"
                variant="outlined"
                required
                {...register('first_name', {
                  required: "Please enter user's first name",
                })}
                slotProps={{
                  inputLabel: {
                    shrink: true,
                  },
                }}
              />
              {errors.first_name && (
                <p className="errorMsg" role="alert">
                  {errors.first_name.message}
                </p>
              )}
            </FormControl>
          </Form.Group>

          <Form.Group controlId="last_name">
            <FormControl fullWidth margin="normal">
              <TextField
                label="Last Name"
                placeholder="Enter Last Name"
                color="info"
                variant="outlined"
                required
                {...register('last_name', {
                  required: "Please enter user's last name",
                })}
                slotProps={{
                  inputLabel: {
                    shrink: true,
                  },
                }}
              />
              {errors.last_name && (
                <p className="errorMsg" role="alert">
                  {errors.last_name.message}
                </p>
              )}
            </FormControl>
          </Form.Group>

          <Form.Group controlId="super_user">
            <Form.Check label=" Super User" type="switch" {...register('super_user')} />
          </Form.Group>

          <Form.Group controlId="organizations">
            <FormControl fullWidth margin="normal">
              <InputLabel>Organizations</InputLabel>
              <Select
                id="organizations"
                label="Organizations"
                multiple
                value={selectedOrganizationNames.map((name) => name.name)}
                onChange={(event) => {
                  const selectedOrgs = event.target.value as string[]
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
            <Form.Group controlId="roles">
              <Typography fontSize="small">Roles</Typography>
              {selectedOrganizations.map((organization) => {
                const role = { role: organization.role }

                return (
                  <Form.Group controlId={organization.id.toString()}>
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
          className="museo-slab capital-case"
        >
          Cancel
        </Button>
        <Button
          form="add-user-form"
          type="submit"
          variant="contained"
          style={{ position: 'absolute', bottom: 10, right: 10 }}
          className="museo-slab capital-case"
        >
          Add User
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default AdminAddUser
