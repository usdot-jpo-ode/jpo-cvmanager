import React, { useEffect, useState } from 'react'
import { Form } from 'react-bootstrap'
import { useForm } from 'react-hook-form'
import { Multiselect } from 'react-widgets'
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

import '../adminIntersectionTab/Admin.css'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../store'
import toast from 'react-hot-toast'
import { useNavigate } from 'react-router-dom'
import Dialog from '@mui/material/Dialog'
import { DialogActions, DialogContent, DialogTitle } from '@mui/material'

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
      <DialogContent>
        <Form
          id="add-intersection-form"
          onSubmit={handleSubmit((data) => handleFormSubmit(data))}
          style={{ fontFamily: 'Arial, Helvetica, sans-serif' }}
        >
          <Form.Group className="mb-3" controlId="ip">
            <Form.Label>Intersection ID</Form.Label>
            <Form.Control
              type="text"
              placeholder="Enter Intersection ID (Required)"
              {...register('intersection_id', {
                required: "Please enter the Intersection's numerical ID",
                pattern: {
                  value: /^[0-9]+$/,
                  message: 'Please enter a valid number',
                },
              })}
            />
            {errors.intersection_id && <p className="errorMsg">{errors.intersection_id.message}</p>}
          </Form.Group>

          <Form.Group className="mb-3" controlId="ip">
            <Form.Label>Reference Point Latitude</Form.Label>
            <Form.Control
              type="text"
              placeholder="Reference Point Latitude (Required)"
              {...register('ref_pt.latitude', {
                required: "Please enter the Intersection's reference point Latitude",
                pattern: {
                  value: /^(\+|-)?(?:90(?:(?:\.0{1,8})?)|(?:[0-9]|[1-8][0-9])(?:(?:\.[0-9]{1,8})?))$/,
                  message: 'Please enter a valid latitude, in degrees',
                },
              })}
            />
            {errors.ref_pt?.latitude && <p className="errorMsg">{errors.ref_pt.latitude.message}</p>}
          </Form.Group>

          <Form.Group className="mb-3" controlId="ip">
            <Form.Label>Reference Point Longitude</Form.Label>
            <Form.Control
              type="text"
              placeholder="Reference Point Longitude (Required)"
              {...register('ref_pt.longitude', {
                required: "Please enter the Intersection's IP address",
                pattern: {
                  value: /^(\+|-)?(?:180(?:(?:\.0{1,8})?)|(?:[0-9]|[1-9][0-9]|1[0-7][0-9])(?:(?:\.[0-9]{1,8})?))$/,
                  message: 'Please enter a valid longitude, in degrees',
                },
              })}
            />
            {errors.ref_pt?.longitude && <p className="errorMsg">{errors.ref_pt.longitude.message}</p>}
          </Form.Group>

          <Form.Group className="mb-3" controlId="serial_number">
            <Form.Label>Intersection Name</Form.Label>
            <Form.Control
              type="text"
              placeholder="Enter Intersection Name/Cross Streets (Optional)"
              {...register('intersection_name')}
            />
            {errors.intersection_name && <p className="errorMsg">{errors.intersection_name.message}</p>}
          </Form.Group>

          <Form.Group className="mb-3" controlId="ip">
            <Form.Label>Origin IP</Form.Label>
            <Form.Control
              type="text"
              placeholder="Enter Origin IP"
              {...register('origin_ip', {
                pattern: {
                  value:
                    /^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/,
                  message: 'Please enter a valid IP address',
                },
              })}
            />
            {errors.origin_ip && <p className="errorMsg">{errors.origin_ip.message}</p>}
          </Form.Group>

          <Form.Group className="mb-3" controlId="organizations">
            <Form.Label>Organization</Form.Label>
            <Multiselect
              className="form-dropdown"
              dataKey="id"
              textField="name"
              placeholder="Select Organizations (Required)"
              data={organizations}
              value={selectedOrganizations}
              onChange={(value) => {
                dispatch(updateSelectedOrganizations(value))
              }}
            />
            {selectedOrganizations.length === 0 && submitAttempt && (
              <p className="error-msg" role="alert">
                Must select an organization
              </p>
            )}
          </Form.Group>

          <Form.Group className="mb-3" controlId="organizations">
            <Form.Label>RSUs</Form.Label>
            <Multiselect
              className="form-dropdown"
              dataKey="id"
              textField="name"
              placeholder="Select RSUs (Optional)"
              data={rsus}
              value={selectedRsus}
              onChange={(value) => {
                dispatch(updateSelectedRsus(value))
              }}
            />
          </Form.Group>
        </Form>
      </DialogContent>
      <DialogActions>
        <button
          onClick={() => {
            setOpen(false)
            navigate('/dashboard/admin/intersections')
          }}
          className="admin-button"
        >
          Close
        </button>
        <button form="add-intersection-form" type="submit" className="admin-button">
          Add Intersection
        </button>
      </DialogActions>
    </Dialog>
  )
}

export default AdminAddIntersection
