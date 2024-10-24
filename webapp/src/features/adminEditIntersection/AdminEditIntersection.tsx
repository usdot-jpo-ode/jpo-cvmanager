import React, { useEffect, useState } from 'react'
import { Form } from 'react-bootstrap'
import { useForm } from 'react-hook-form'
import { ErrorMessage } from '@hookform/error-message'
import { Multiselect } from 'react-widgets'
import {
  selectApiData,
  selectOrganizations,
  selectSelectedOrganizations,
  selectRsus,
  selectSelectedRsus,
  selectSubmitAttempt,

  // actions
  getIntersectionInfo,
  submitForm,
  setSelectedOrganizations,
  setSelectedRsus,
} from './adminEditIntersectionSlice'
import { useSelector, useDispatch } from 'react-redux'

import '../adminIntersectionTab/Admin.css'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../store'
import { AdminIntersection } from '../../models/Intersection'
import { useNavigate, useParams } from 'react-router-dom'
import { selectTableData, updateTableData } from '../adminIntersectionTab/adminIntersectionTabSlice'
import { Dialog, DialogActions, DialogContent, DialogTitle, Typography } from '@mui/material'
import toast from 'react-hot-toast'

export type AdminEditIntersectionFormType = AdminIntersection & {
  orig_intersection_id: string
  organizations_to_add: string[]
  organizations_to_remove: string[]
  rsus: string[]
  rsus_to_add: string[]
  rsus_to_remove: string[]
}

const AdminEditIntersection = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const apiData = useSelector(selectApiData)
  const organizations = useSelector(selectOrganizations)
  const selectedOrganizations = useSelector(selectSelectedOrganizations)
  const rsus = useSelector(selectRsus)
  const selectedRsus = useSelector(selectSelectedRsus)
  const submitAttempt = useSelector(selectSubmitAttempt)
  const intersectionTableData = useSelector(selectTableData)

  const [open, setOpen] = useState(true)
  const navigate = useNavigate()

  const {
    register,
    handleSubmit,
    formState: { errors },
    setValue,
  } = useForm<AdminEditIntersectionFormType>({
    defaultValues: {
      orig_intersection_id: '',
      intersection_id: '',
      ref_pt: {
        latitude: '',
        longitude: '',
      },
      bbox: {
        latitude1: '',
        longitude1: '',
        latitude2: '',
        longitude2: '',
      },
      intersection_name: '',
      origin_ip: '',
      organizations_to_add: [],
      organizations_to_remove: [],
      rsus_to_add: [],
      rsus_to_remove: [],
    },
  })

  const { intersectionId } = useParams<{ intersectionId: string }>()

  useEffect(() => {
    if (
      (intersectionTableData ?? []).find(
        (intersection: AdminIntersection) => intersection.intersection_id === intersectionId
      )
    ) {
      dispatch(getIntersectionInfo(intersectionId))
    }
  }, [dispatch, intersectionId, intersectionTableData])

  useEffect(() => {
    const currIntersection = (intersectionTableData ?? []).find(
      (intersection: AdminIntersection) => intersection.intersection_id === intersectionId
    )
    if (currIntersection) {
      setValue('orig_intersection_id', currIntersection.intersection_id)
      setValue('intersection_id', currIntersection.intersection_id)
      setValue('ref_pt.latitude', currIntersection.ref_pt?.latitude?.toString())
      setValue('ref_pt.longitude', currIntersection.ref_pt?.longitude?.toString())
      setValue('bbox.latitude1', currIntersection.bbox?.latitude1?.toString())
      setValue('bbox.longitude1', currIntersection.bbox?.longitude1?.toString())
      setValue('bbox.latitude2', currIntersection.bbox?.latitude2?.toString())
      setValue('bbox.longitude2', currIntersection.bbox?.longitude2?.toString())
      setValue('intersection_name', currIntersection.intersection_name)
      setValue('origin_ip', currIntersection.origin_ip)
    }
  }, [apiData, setValue])

  useEffect(() => {
    dispatch(updateTableData())
  }, [dispatch])

  const onSubmit = (data: AdminEditIntersectionFormType) => {
    dispatch(submitForm(data)).then((data: any) => {
      if (data.payload.success) {
        toast.success('Intersection updated successfully')
      } else {
        toast.error('Failed to update Intersection: ' + data.payload.message)
      }
    })
    setOpen(false)
    navigate('/dashboard/admin/intersections')
  }

  return (
    <Dialog open={open}>
      <DialogTitle>Edit Intersection</DialogTitle>
      {Object.keys(apiData ?? {}).length != 0 ? (
        <>
          <DialogContent>
            <Form
              id="edit-intersection-form"
              onSubmit={handleSubmit(onSubmit)}
              style={{ fontFamily: 'Arial, Helvetica, sans-serif' }}
            >
              <Form.Group className="mb-3" controlId="intersection_id">
                <Form.Label>Intersection ID</Form.Label>
                <Form.Control
                  type="text"
                  placeholder="Enter Intersection ID"
                  {...register('intersection_id', {
                    required: "Please enter the Intersection's numerical ID",
                    pattern: {
                      value: /^[0-9]+$/,
                      message: 'Please enter a valid number',
                    },
                  })}
                />
                <ErrorMessage
                  errors={errors}
                  name="intersection_id"
                  render={({ message }) => (
                    <p className="errorMsg" role="alert">
                      {' '}
                      {message}{' '}
                    </p>
                  )}
                />
              </Form.Group>

              <Form.Group className="mb-3" controlId="ref_pt.latitude">
                <Form.Label>Reference Point Latitude</Form.Label>
                <Form.Control
                  type="text"
                  placeholder="Enter Reference Point Latitude"
                  {...register('ref_pt.latitude', {
                    required: 'Please enter the Intersection Reference Point latitude',
                    pattern: {
                      value: /^(\+|-)?(?:90(?:(?:\.0{1,8})?)|(?:[0-9]|[1-8][0-9])(?:(?:\.[0-9]{1,8})?))$/,
                      message: 'Please enter a valid latitude',
                    },
                  })}
                />
                <ErrorMessage
                  errors={errors}
                  name="ref_pt.latitude"
                  render={({ message }) => (
                    <p className="errorMsg" role="alert">
                      {' '}
                      {message}{' '}
                    </p>
                  )}
                />
              </Form.Group>

              <Form.Group className="mb-3" controlId="ref_pt.longitude">
                <Form.Label>Reference Point Longitude</Form.Label>
                <Form.Control
                  type="text"
                  placeholder="Enter Reference Point Longitude"
                  {...register('ref_pt.longitude', {
                    required: 'Please enter the Intersection Reference Point longitude',
                    pattern: {
                      value: /^(\+|-)?(?:180(?:(?:\.0{1,8})?)|(?:[0-9]|[1-9][0-9]|1[0-7][0-9])(?:(?:\.[0-9]{1,8})?))$/,
                      message: 'Please enter a valid longitude',
                    },
                  })}
                />
                <ErrorMessage
                  errors={errors}
                  name="ref_pt.longitude"
                  render={({ message }) => (
                    <p className="errorMsg" role="alert">
                      {' '}
                      {message}{' '}
                    </p>
                  )}
                />
              </Form.Group>

              <Form.Group className="mb-3" controlId="intersection_name">
                <Form.Label>Intersection Name</Form.Label>
                <Form.Control
                  type="text"
                  placeholder="Enter Intersection Name/Cross Streets (Optional)"
                  {...register('intersection_name')}
                />
                {errors.intersection_name && (
                  <p className="errorMsg" role="alert">
                    {errors.intersection_name.message}
                  </p>
                )}
              </Form.Group>

              <Form.Group className="mb-3" controlId="origin_ip">
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
                <ErrorMessage
                  errors={errors}
                  name="origin_ip"
                  render={({ message }) => (
                    <p className="errorMsg" role="alert">
                      {' '}
                      {message}{' '}
                    </p>
                  )}
                />
              </Form.Group>

              <Form.Group className="mb-3" controlId="organizations">
                <Form.Label>Organization</Form.Label>
                <Multiselect
                  className="form-dropdown"
                  dataKey="name"
                  textField="name"
                  data={organizations}
                  placeholder="Select organizations"
                  value={selectedOrganizations}
                  onChange={(value) => {
                    dispatch(setSelectedOrganizations(value))
                  }}
                />
                {selectedOrganizations.length === 0 && submitAttempt && (
                  <p className="error-msg" role="alert">
                    Must select an organization
                  </p>
                )}
              </Form.Group>

              <Form.Group className="mb-3" controlId="rsus">
                <Form.Label>RSUs</Form.Label>
                <Multiselect
                  className="form-dropdown"
                  dataKey="name"
                  textField="name"
                  data={rsus}
                  placeholder="Select rsus"
                  value={selectedRsus}
                  onChange={(value) => {
                    dispatch(setSelectedRsus(value))
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
            <button form="edit-intersection-form" type="submit" className="admin-button">
              Apply Changes
            </button>
          </DialogActions>
        </>
      ) : (
        <DialogContent>
          <Typography variant={'h4'} style={{ color: '#fff' }}>
            Unknown Intersection ID. Either this Intersection does not exist, or you do not have access to it.
          </Typography>
          <button
            onClick={() => {
              setOpen(false)
              navigate('/dashboard/admin/intersections')
            }}
            className="admin-button"
          >
            Close
          </button>
        </DialogContent>
      )}
    </Dialog>
  )
}

export default AdminEditIntersection
