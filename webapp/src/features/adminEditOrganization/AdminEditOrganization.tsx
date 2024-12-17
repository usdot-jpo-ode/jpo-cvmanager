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
import toast from 'react-hot-toast'

import '../adminRsuTab/Admin.css'
import 'react-widgets/styles.css'
import {
  AdminOrgSummary,
  adminOrgPatch,
  getOrgData,
  selectOrgData,
  selectSelectedOrg,
  setSelectedOrg,
} from '../adminOrganizationTab/adminOrganizationTabSlice'
import { Link, useParams, useNavigate } from 'react-router-dom'
import { DialogActions, DialogContent, DialogTitle, Typography } from '@mui/material'
import Dialog from '@mui/material/Dialog'
import { useAppDispatch, useAppSelector } from '../../hooks'
import { AdminButton } from '../../styles/components/AdminButton'

const AdminEditOrganization = () => {
  const dispatch = useAppDispatch()

  const [open, setOpen] = useState(true)
  const successMsg = useAppSelector(selectSuccessMsg)
  const selectedOrg = useAppSelector(selectSelectedOrg)
  const orgData = useAppSelector(selectOrgData)
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
      <DialogContent>
        {Object.keys(selectedOrg ?? {}).length != 0 ? (
          <Form
            id="admin-edit-org"
            onSubmit={handleSubmit((data) => onSubmit(data))}
            style={{ fontFamily: 'Arial, Helvetica, sans-serif' }}
          >
            <Form.Group className="mb-3" controlId="name">
              <Form.Label>Organization Name *</Form.Label>
              <Form.Control
                type="text"
                placeholder="Enter organization name"
                {...register('name', {
                  required: 'Please enter the organization name',
                })}
              />
              <Form.Label>Organization Email</Form.Label>
              <Form.Control type="text" placeholder="Enter organization email" {...register('email')} />
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
      <DialogActions>
        <AdminButton
          onClick={() => {
            setOpen(false)
            navigate('..')
          }}
        >
          Close
        </AdminButton>
        <AdminButton form="admin-edit-org" type="submit">
          Apply Changes
        </AdminButton>
      </DialogActions>
    </Dialog>
  )
}

export default AdminEditOrganization
