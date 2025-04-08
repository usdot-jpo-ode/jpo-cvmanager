import React, { useEffect, useState } from 'react'
import { Form } from 'react-bootstrap'
import { useForm } from 'react-hook-form'
import {
  selectSuccessMsg,

  // actions
  updateStates,
  editOrganization,
  setSuccessMsg,
} from './adminEditOrganizationSlice'
import { useSelector, useDispatch } from 'react-redux'
import toast from 'react-hot-toast'

import '../adminRsuTab/Admin.css'
import 'react-widgets/styles.css'
import '../../styles/fonts/museo-slab.css'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../store'
import {
  AdminOrgSummary,
  adminOrgPatch,
  getOrgData,
  selectOrgData,
  selectSelectedOrg,
  setSelectedOrg,
} from '../adminOrganizationTab/adminOrganizationTabSlice'
import { Link, useParams, useNavigate } from 'react-router-dom'
import {
  Button,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  IconButton,
  InputLabel,
  OutlinedInput,
  Typography,
} from '@mui/material'
import CloseIcon from '@mui/icons-material/Close'
import Dialog from '@mui/material/Dialog'

const AdminEditOrganization = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()

  const [open, setOpen] = useState(true)
  const successMsg = useSelector(selectSuccessMsg)
  const selectedOrg = useSelector(selectSelectedOrg)
  const orgData = useSelector(selectOrgData)
  const {
    register,
    handleSubmit,
    formState: { errors },
    setValue,
  } = useForm<adminOrgPatch>({
    defaultValues: {
      name: '',
      email: '',
    },
  })

  const { orgName } = useParams<{ orgName: string }>()
  const navigate = useNavigate()

  useEffect(() => {
    dispatch(getOrgData({ orgName }))
  }, [orgName])

  useEffect(() => {
    const selectedOrg = (orgData ?? []).find((organization: AdminOrgSummary) => organization?.name === orgName)
    dispatch(setSelectedOrg(selectedOrg))
  }, [orgData])

  useEffect(() => {
    dispatch(getOrgData({ orgName: 'all', all: true, specifiedOrg: undefined }))
  }, [dispatch])

  useEffect(() => {
    updateStates(setValue, selectedOrg?.name, selectedOrg?.email)
  }, [setValue, selectedOrg?.name])

  const onSubmit = (data: adminOrgPatch) => {
    dispatch(editOrganization({ json: data, setValue, selectedOrg: selectedOrg?.name })).then((data: any) => {
      data.payload.success
        ? toast.success(data.payload.message)
        : toast.error('Failed to apply changes to organization due to error: ' + data.payload.message)
    })
    setOpen(false)
    navigate('..')
  }

  useEffect(() => {
    if (successMsg) navigate('..')
    dispatch(setSuccessMsg(''))
  }, [successMsg])

  return (
    <Dialog open={open}>
      <DialogTitle>Edit Organization</DialogTitle>
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
      <DialogContent>
        {Object.keys(selectedOrg ?? {}).length != 0 ? (
          <Form
            id="admin-edit-org"
            onSubmit={handleSubmit((data) => onSubmit(data))}
            style={{ fontFamily: '"museo-slab", Arial, Helvetica, sans-serif' }}
          >
            <Form.Group className="mb-3" controlId="name">
              <FormControl fullWidth margin="normal">
                <InputLabel htmlFor="org-name">Organization Name</InputLabel>
                <OutlinedInput
                  id="org-name"
                  type="text"
                  {...register('name', {
                    required: 'Please enter the organization name',
                  })}
                  label="Organization Name"
                />
              </FormControl>
              <FormControl fullWidth margin="normal">
                <InputLabel htmlFor="org-email">Organization Email</InputLabel>
                <OutlinedInput id="org-email" type="text" label="Organization Email" {...register('email')} />
              </FormControl>
              {errors.name && (
                <p className="errorMsg" role="alert">
                  {errors.name.message}
                </p>
              )}
            </Form.Group>
          </Form>
        ) : (
          <Typography variant={'h4'}>
            Unknown organization. Either this organization does not exist, or you do not have access to it.{' '}
            <Link to="../">Organizations</Link>
          </Typography>
        )}
      </DialogContent>
      <DialogActions sx={{ padding: '20px' }}>
        <Button
          onClick={() => {
            setOpen(false)
            navigate('/dashboard/admin/organizations')
          }}
          variant="outlined"
          color="info"
          style={{ position: 'absolute', bottom: 10, left: 10 }}
        >
          Cancel
        </Button>
        <Button
          form="admin-edit-org"
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

export default AdminEditOrganization
