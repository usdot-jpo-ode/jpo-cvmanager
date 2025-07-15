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
import { Button, DialogActions, DialogContent, FormControl, TextField, Typography } from '@mui/material'
import Dialog from '@mui/material/Dialog'
import { SideBarHeader } from '../../styles/components/SideBarHeader'

const AdminEditOrganization = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()

  const [open, setOpen] = useState(true)
  const [unknownOrg, setUnknownOrg] = useState(false)
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
    if (selectedOrg) {
      updateStates(setValue, selectedOrg?.name, selectedOrg?.email)
      if (unknownOrg) setUnknownOrg(false)
    } else setUnknownOrg(true)
  }, [setValue, selectedOrg?.name, selectedOrg?.email, selectedOrg, unknownOrg])

  const onSubmit = (data: adminOrgPatch) => {
    dispatch(editOrganization({ json: data, setValue, selectedOrg: selectedOrg?.name })).then((data: any) => {
      if (data.payload.success) {
        toast.success(data.payload.message)
      } else {
        toast.error('Failed to apply changes to organization due to error: ' + data.payload.message)
      }
    })
    setOpen(false)
    navigate('..')
  }

  useEffect(() => {
    if (successMsg) navigate('..')
    dispatch(setSuccessMsg(''))
  }, [successMsg])

  return (
    <>
      {Object.keys(selectedOrg ?? {}).length !== 0 && !unknownOrg ? (
        <Dialog open={open}>
          <DialogContent sx={{ width: '600px', padding: '5px 10px' }}>
            <SideBarHeader
              onClick={() => {
                setOpen(false)
                navigate('..')
              }}
              title="Edit Organization"
            />
            <Form id="admin-edit-org" onSubmit={handleSubmit((data) => onSubmit(data))}>
              <Form.Group controlId="name">
                <FormControl fullWidth margin="normal">
                  <TextField
                    label="Organization Name"
                    placeholder="Enter Organization Name"
                    color="info"
                    variant="outlined"
                    required
                    {...register('name', {
                      required: 'Please enter the organization name',
                    })}
                    slotProps={{
                      inputLabel: {
                        shrink: true,
                      },
                    }}
                  />
                </FormControl>
                <FormControl fullWidth margin="normal">
                  <TextField
                    label="Organization Email"
                    placeholder="Enter Organization Email"
                    color="info"
                    variant="outlined"
                    required
                    {...register('email')}
                    slotProps={{
                      inputLabel: {
                        shrink: true,
                      },
                    }}
                  />
                </FormControl>
                {errors.name && (
                  <p className="errorMsg" role="alert">
                    {errors.name.message}
                  </p>
                )}
              </Form.Group>
            </Form>
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
              className="museo-slab capital-case"
            >
              Cancel
            </Button>
            <Button
              form="admin-edit-org"
              type="submit"
              variant="contained"
              style={{ position: 'absolute', bottom: 10, right: 10 }}
              className="museo-slab capital-case"
            >
              Apply Changes
            </Button>
          </DialogActions>
        </Dialog>
      ) : (
        unknownOrg && (
          <Dialog open={open}>
            <DialogContent>
              <Typography variant={'h4'}>
                Unknown organization. Either this organization does not exist, or you do not have access to it.{' '}
                <Link to="../">Organizations</Link>
              </Typography>
            </DialogContent>
          </Dialog>
        )
      )}
    </>
  )
}

export default AdminEditOrganization
