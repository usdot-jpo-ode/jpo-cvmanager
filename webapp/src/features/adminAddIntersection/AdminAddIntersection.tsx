import React, { useEffect, useState } from 'react'
import { Form } from 'react-bootstrap'
import { useForm } from 'react-hook-form'
import {
  selectOrganizations,
  selectSelectedOrganizations,
  selectRsus,
  selectSelectedRsus,
  selectSubmitAttempt,

  // actions
  getIntersectionCreationData,
  submitForm,
  updateSelectedOrganizations,
  updateSelectedRsus,
} from './adminAddIntersectionSlice'
import { useSelector, useDispatch } from 'react-redux'

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
} from '@mui/material'
import { ErrorMessageText } from '../../styles/components/Messages'

export type AdminAddIntersectionForm = {
  intersection_id: string
  ref_pt: {
    latitude: string
    longitude: string
  }
  bbox?: {
    latitude1: string
    longitude1: string
    latitude2: string
    longitude2: string
  }
  intersection_name?: string
  origin_ip?: string
  organizations: string[]
  rsus: string[]
}

const AdminAddIntersection = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()

  const organizations = useSelector(selectOrganizations)
  const selectedOrganizations = useSelector(selectSelectedOrganizations)
  const rsus = useSelector(selectRsus)
  const selectedRsus = useSelector(selectSelectedRsus)
  const submitAttempt = useSelector(selectSubmitAttempt)

  const [open, setOpen] = useState(true)
  const navigate = useNavigate()

  const notifySuccess = (message: string) => toast.success(message)
  const notifyError = (message: string) => toast.error(message)

  const handleFormSubmit = (data: AdminAddIntersectionForm) => {
    dispatch(submitForm({ data, reset })).then((data: any) => {
      data.payload.success
        ? notifySuccess(data.payload.message)
        : notifyError('Failed to add Intersection due to error: ' + data.payload.message)
    })
    setOpen(false)
    navigate('/dashboard/admin/intersections')
  }

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<AdminAddIntersectionForm>()

  useEffect(() => {
    dispatch(getIntersectionCreationData())
  }, [dispatch])

  return (
    <Dialog open={open}>
      <DialogTitle>Add Intersection</DialogTitle>
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
          id="add-intersection-form"
          onSubmit={handleSubmit((data) => handleFormSubmit(data))}
          style={{ fontFamily: 'Arial, Helvetica, sans-serif' }}
        >
          <Form.Group className="mb-3" controlId="ip">
            <FormControl fullWidth margin="normal">
              <InputLabel htmlFor="int-id">Intersection ID</InputLabel>
              <OutlinedInput
                id="int-id"
                type="text"
                label="Intersection ID"
                {...register('intersection_id', {
                  required: "Please enter the Intersection's numerical ID",
                  pattern: {
                    value: /^[0-9]+$/,
                    message: 'Please enter a valid number',
                  },
                })}
              />
              {errors.intersection_id && <p className="errorMsg">{errors.intersection_id.message}</p>}
            </FormControl>
          </Form.Group>

          <Form.Group className="mb-3" controlId="ip">
            <FormControl fullWidth margin="normal">
              <InputLabel htmlFor="latitude-id">Reference Point Latitude</InputLabel>
              <OutlinedInput
                id="latitude-id"
                type="text"
                label="Reference Point Latitude"
                {...register('ref_pt.latitude', {
                  required: "Please enter the Intersection's reference point Latitude",
                  pattern: {
                    value: /^(\+|-)?(?:90(?:(?:\.0{1,8})?)|(?:[0-9]|[1-8][0-9])(?:(?:\.[0-9]{1,8})?))$/,
                    message: 'Please enter a valid latitude, in degrees',
                  },
                })}
              />
              {errors.ref_pt?.latitude && <p className="errorMsg">{errors.ref_pt.latitude.message}</p>}
            </FormControl>
          </Form.Group>

          <Form.Group className="mb-3" controlId="ip">
            <FormControl fullWidth margin="normal">
              <InputLabel htmlFor="longitude-id">Reference Point Longitude</InputLabel>
              <OutlinedInput
                id="longitude-id"
                type="text"
                label="Reference Point Longitude"
                {...register('ref_pt.longitude', {
                  required: "Please enter the Intersection's IP address",
                  pattern: {
                    value: /^(\+|-)?(?:180(?:(?:\.0{1,8})?)|(?:[0-9]|[1-9][0-9]|1[0-7][0-9])(?:(?:\.[0-9]{1,8})?))$/,
                    message: 'Please enter a valid longitude, in degrees',
                  },
                })}
              />
              {errors.ref_pt?.longitude && <p className="errorMsg">{errors.ref_pt.longitude.message}</p>}
            </FormControl>
          </Form.Group>

          <Form.Group className="mb-3" controlId="serial_number">
            <FormControl fullWidth margin="normal">
              <InputLabel htmlFor="int-name">Intersection Name</InputLabel>
              <OutlinedInput id="int-name" type="text" label="Intersection Name" {...register('intersection_name')} />
              {errors.intersection_name && <p className="errorMsg">{errors.intersection_name.message}</p>}
            </FormControl>
          </Form.Group>

          <Form.Group className="mb-3" controlId="ip">
            <FormControl fullWidth margin="normal">
              <InputLabel htmlFor="int-origin-ip">Origin IP</InputLabel>
              <OutlinedInput
                id="int-origin-ip"
                type="text"
                label="Origin IP"
                {...register('origin_ip', {
                  pattern: {
                    value:
                      /^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/,
                    message: 'Please enter a valid IP address',
                  },
                })}
              />
              {errors.origin_ip && <p className="errorMsg">{errors.origin_ip.message}</p>}
            </FormControl>
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
                onChange={(event) => {
                  const selectedOrgs = event.target.value as String[]
                  dispatch(updateSelectedOrganizations(organizations.filter((org) => selectedOrgs.includes(org.name))))
                }}
              >
                {organizations.map((org) => (
                  <MenuItem key={org.id} value={org.name}>
                    {org.name}
                  </MenuItem>
                ))}
              </Select>
              {selectedOrganizations.length === 0 && submitAttempt && (
                <ErrorMessageText role="alert">Must select an organization</ErrorMessageText>
              )}
            </FormControl>
          </Form.Group>

          <Form.Group className="mb-3" controlId="rsus">
            <FormControl fullWidth margin="normal">
              <InputLabel>RSUs</InputLabel>
              <Select
                id="organizations"
                className="form-dropdown"
                label="RSUs"
                multiple
                value={selectedRsus.map((rsu) => rsu.name)}
                onChange={(event) => {
                  const selectedRsus = event.target.value as String[]
                  console.log('selectedRsus', selectedRsus)
                  var filteredRsus = rsus.filter((rsu) => selectedRsus.includes(rsu.name))
                  console.log('filteredRsus', filteredRsus)
                  dispatch(updateSelectedRsus(rsus.filter((rsu) => selectedRsus.includes(rsu.name))))
                }}
              >
                {rsus.map((rsu) => (
                  <MenuItem key={rsu.id} value={rsu.name}>
                    {rsu.name}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Form.Group>
        </Form>
      </DialogContent>
      <DialogActions sx={{ padding: '20px' }}>
        <Button
          onClick={() => {
            setOpen(false)
            navigate('/dashboard/admin/intersections')
          }}
          variant="outlined"
          color="info"
          style={{ position: 'absolute', bottom: 10, left: 10 }}
        >
          Close
        </Button>
        <Button
          form="add-intersection-form"
          type="submit"
          variant="contained"
          style={{ position: 'absolute', bottom: 10, right: 10 }}
        >
          Add Intersection
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default AdminAddIntersection
