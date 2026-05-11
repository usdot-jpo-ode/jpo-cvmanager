import { useEffect, useState } from 'react'
import { Form } from 'react-bootstrap'
import { useForm } from 'react-hook-form'
import { useSelector } from 'react-redux'

import '../adminRsuTab/Admin.css'
import 'react-widgets/styles.css'
import '../../styles/fonts/museo-slab.css'
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
  Box,
  IconButton,
  Card,
  CircularProgress,
} from '@mui/material'
import { ErrorMessageText } from '../../styles/components/Messages'
import { SideBarHeader } from '../../styles/components/SideBarHeader'
import { selectSuperUser } from '../../generalSlices/userSlice'
import DeleteIcon from '@mui/icons-material/Delete'
import AddIcon from '@mui/icons-material/Add'
import { useCreateUserMutation, useGetUserAllowedSelectionsQuery } from '../api/userApiSlice'

const AdminAddUser = () => {
  const navigate = useNavigate()
  const isSuperUser = useSelector(selectSuperUser)
  const [selectedOrganizations, setSelectedOrganizations] = useState<UserOrganization[]>([])
  const [submitAttempt, setSubmitAttempt] = useState(false)
  const [open, setOpen] = useState(true)

  // RTK Query hooks
  const { data: allowedSelections, isLoading: isLoadingData } = useGetUserAllowedSelectionsQuery()
  const [createUser, { isLoading: isCreating }] = useCreateUserMutation()

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<AdminUserCreationBody>({
    defaultValues: {
      super_user: false,
    },
  })

  // Initialize with one empty organization on mount
  useEffect(() => {
    if (selectedOrganizations.length === 0 && !isLoadingData) {
      setSelectedOrganizations([{ organization: '', role: 'USER' }])
    }
  }, [isLoadingData])

  const handleClose = () => {
    setOpen(false)
    navigate('/dashboard/admin/users')
  }

  const handleAddOrganization = () => {
    setSelectedOrganizations([...selectedOrganizations, { organization: '', role: 'USER' }])
  }

  const handleRemoveOrganization = (index: number) => {
    // Prevent removing if it's the last one
    if (selectedOrganizations.length === 1) {
      toast.error('At least one organization is required')
      return
    }
    setSelectedOrganizations(selectedOrganizations.filter((_, i) => i !== index))
  }

  const handleOrganizationChange = (index: number, field: keyof UserOrganization, value: string) => {
    const updated = [...selectedOrganizations]

    // If changing organization, check for duplicates
    if (field === 'organization') {
      const isDuplicate = updated.some((org, i) => i !== index && org.organization === value)
      if (isDuplicate) {
        toast.error('This organization has already been added')
        return
      }
      updated[index] = { ...updated[index], organization: value }
    } else {
      updated[index] = { ...updated[index], role: value as UserOrganization['role'] }
    }

    setSelectedOrganizations(updated)
  }

  // Get available organizations excluding already selected ones
  const getAvailableOrganizations = (currentIndex: number) => {
    const selectedOrgNames = selectedOrganizations
      .map((org, index) => (index !== currentIndex ? org.organization : null))
      .filter((org) => org !== null && org !== '')

    return allowedSelections?.organizations?.filter((org) => !selectedOrgNames.includes(org)) || []
  }

  const checkForm = (): boolean => {
    if (selectedOrganizations.length === 0) return false

    // Check that all organizations have both org and role selected
    const allFieldsFilled = selectedOrganizations.every((org) => org.organization !== '' && org.role)

    // Check for duplicate organizations
    const orgNames = selectedOrganizations.map((org) => org.organization).filter((name) => name !== '')
    const hasDuplicates = new Set(orgNames).size !== orgNames.length

    return allFieldsFilled && !hasDuplicates
  }

  const handleFormSubmit = async (data: AdminUserCreationBody) => {
    setSubmitAttempt(true)

    if (!checkForm()) {
      const orgNames = selectedOrganizations.map((org) => org.organization).filter((name) => name !== '')
      const hasDuplicates = new Set(orgNames).size !== orgNames.length

      if (hasDuplicates) {
        toast.error('Cannot add the same organization multiple times')
      } else {
        toast.error('Please fill out all required fields')
      }
      return
    }

    try {
      const requestBody = {
        ...data,
        super_user: isSuperUser ? Boolean(data.super_user) : false,
        organizations: selectedOrganizations,
      }

      await createUser(requestBody).unwrap()
      toast.success('User Created Successfully')

      // Reset form
      reset()
      setSelectedOrganizations([{ organization: '', role: 'USER' }])
      setSubmitAttempt(false)

      handleClose()
    } catch (error: any) {
      console.log('Error creating User:', error)
      toast.error(
        'Failed to add User due to error: ' +
          (error?.data?.message || error?.data?.detail || error?.message || 'Unknown error')
      )
    }
  }

  if (isLoadingData) {
    return (
      <Dialog open={open}>
        <DialogContent sx={{ width: '600px', padding: '40px' }}>
          <Box display="flex" justifyContent="center" alignItems="center">
            <CircularProgress />
          </Box>
        </DialogContent>
      </Dialog>
    )
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
        <Form id="add-user-form" onSubmit={handleSubmit(handleFormSubmit)}>
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

          {isSuperUser && (
            <Form.Group controlId="super_user">
              <Form.Check label=" Super User" type="switch" {...register('super_user')} />
            </Form.Group>
          )}

          <Box sx={{ mt: 3, mb: 2 }}>
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
              <Typography variant="subtitle1" fontWeight="bold">
                Organizations & Roles
              </Typography>
              <Button
                variant="outlined"
                size="small"
                startIcon={<AddIcon />}
                onClick={handleAddOrganization}
                className="museo-slab capital-case"
                disabled={
                  allowedSelections?.organizations &&
                  selectedOrganizations.filter((org) => org.organization !== '').length >=
                    allowedSelections.organizations.length
                }
              >
                Add Organization
              </Button>
            </Box>

            {selectedOrganizations.map((orgRole, index) => (
              <Card key={index} sx={{ mb: 2, p: 2, position: 'relative' }}>
                <Box display="flex" gap={2} flexDirection={{ xs: 'column', sm: 'row' }} sx={{ pr: 5 }}>
                  <FormControl fullWidth>
                    <InputLabel>Organization</InputLabel>
                    <Select
                      value={orgRole.organization}
                      label="Organization"
                      onChange={(e) => handleOrganizationChange(index, 'organization', e.target.value)}
                      required
                    >
                      {getAvailableOrganizations(index).map((org) => (
                        <MenuItem key={org} value={org}>
                          {org}
                        </MenuItem>
                      ))}
                      {/* Show currently selected org even if it would be filtered out */}
                      {orgRole.organization && !getAvailableOrganizations(index).includes(orgRole.organization) && (
                        <MenuItem key={orgRole.organization} value={orgRole.organization}>
                          {orgRole.organization}
                        </MenuItem>
                      )}
                    </Select>
                  </FormControl>

                  <FormControl fullWidth>
                    <InputLabel>Role</InputLabel>
                    <Select
                      value={orgRole.role}
                      label="Role"
                      onChange={(e) => handleOrganizationChange(index, 'role', e.target.value)}
                      required
                      disabled={!orgRole.organization}
                    >
                      {allowedSelections?.roles?.map((role) => (
                        <MenuItem key={role.toUpperCase()} value={role.toUpperCase()}>
                          {role}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                </Box>

                {selectedOrganizations.length > 1 && (
                  <IconButton
                    aria-label="delete"
                    size="small"
                    onClick={() => handleRemoveOrganization(index)}
                    sx={{
                      position: 'absolute',
                      top: 8,
                      right: 8,
                      color: 'error.main',
                      '&:hover': {
                        backgroundColor: 'error.lighter',
                      },
                    }}
                  >
                    <DeleteIcon fontSize="small" />
                  </IconButton>
                )}
              </Card>
            ))}

            {selectedOrganizations.length > 0 && submitAttempt && !checkForm() && (
              <ErrorMessageText role="alert">
                Please complete all organization and role selections. Each organization can only be added once.
              </ErrorMessageText>
            )}
          </Box>
        </Form>
      </DialogContent>
      <DialogActions sx={{ padding: '20px', mt: 1 }}>
        <Button
          onClick={handleClose}
          variant="outlined"
          color="info"
          style={{ position: 'absolute', bottom: 10, left: 10 }}
          className="museo-slab capital-case"
          disabled={isCreating}
        >
          Cancel
        </Button>
        <Button
          form="add-user-form"
          type="submit"
          variant="contained"
          style={{ position: 'absolute', bottom: 10, right: 10 }}
          className="museo-slab capital-case"
          disabled={isCreating}
        >
          {isCreating ? 'Adding...' : 'Add User'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default AdminAddUser
