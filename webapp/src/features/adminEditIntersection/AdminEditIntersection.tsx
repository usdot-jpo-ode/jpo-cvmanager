import React, { useEffect, useState } from 'react'
import { Form } from 'react-bootstrap'
import { useForm } from 'react-hook-form'
import { ErrorMessage } from '@hookform/error-message'
import {
  selectApiData,
  selectOrganizations,
  selectSelectedOrganizations,
  selectRsus,
  selectSelectedRsus,
  selectSubmitAttempt,

  // actions
  submitForm,
  setSelectedOrganizations,
  setSelectedRsus,
} from './adminEditIntersectionSlice'
import { useSelector, useDispatch } from 'react-redux'

import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../store'
import { AdminIntersection } from '../../models/Intersection'
import { useNavigate, useParams } from 'react-router-dom'
import { selectTableData, updateTableData } from '../adminIntersectionTab/adminIntersectionTabSlice'
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  TextField,
  Typography,
} from '@mui/material'
import toast from 'react-hot-toast'
import { AdminButton } from '../../styles/components/AdminButton'
import { ErrorMessageText } from '../../styles/components/Messages'
import '../../styles/fonts/museo-slab.css'
import { SideBarHeader } from '../../styles/components/SideBarHeader'

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
      {Object.keys(apiData ?? {}).length != 0 ? (
        <>
          <DialogContent sx={{ width: '600px', padding: '5px 10px' }}>
            <SideBarHeader
              onClick={() => {
                setOpen(false)
                navigate('..')
              }}
              title="Edit Intersection"
            />
            <Form
              id="edit-intersection-form"
              onSubmit={handleSubmit(onSubmit)}
              style={{ fontFamily: '"museo-slab", Arial, Helvetica, sans-serif' }}
            >
              <Form.Group controlId="intersection_id">
                <FormControl fullWidth margin="normal">
                  <TextField
                    label="Intersection ID"
                    placeholder="Enter Intersection ID"
                    color="info"
                    variant="outlined"
                    required
                    {...register('intersection_id', {
                      required: "Please enter the Intersection's numerical ID",
                      pattern: {
                        value: /^[0-9]+$/,
                        message: 'Please enter a valid number',
                      },
                    })}
                    slotProps={{
                      inputLabel: {
                        shrink: true,
                      },
                    }}
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
                </FormControl>
              </Form.Group>

              <Form.Group controlId="ref_pt.latitude">
                <FormControl fullWidth margin="normal">
                  <TextField
                    label="Latitude"
                    placeholder="Enter Reference Point Latitude"
                    color="info"
                    variant="outlined"
                    required
                    {...register('ref_pt.latitude', {
                      required: "Please enter the Intersection's reference point Latitude",
                      pattern: {
                        value: /^(\+|-)?(?:90(?:(?:\.0{1,8})?)|(?:[0-9]|[1-8][0-9])(?:(?:\.[0-9]{1,8})?))$/,
                        message: 'Please enter a valid latitude, in degrees',
                      },
                    })}
                    slotProps={{
                      inputLabel: {
                        shrink: true,
                      },
                    }}
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
                </FormControl>
              </Form.Group>

              <Form.Group controlId="ref_pt.longitude">
                <FormControl fullWidth margin="normal">
                  <TextField
                    label="Longitude"
                    placeholder="Enter Reference Point Longitude"
                    color="info"
                    variant="outlined"
                    required
                    {...register('ref_pt.longitude', {
                      required: "Please enter the Intersection's reference point Longitude",
                      pattern: {
                        value:
                          /^(\+|-)?(?:180(?:(?:\.0{1,8})?)|(?:[0-9]|[1-9][0-9]|1[0-7][0-9])(?:(?:\.[0-9]{1,8})?))$/,
                        message: 'Please enter a valid longitude, in degrees',
                      },
                    })}
                    slotProps={{
                      inputLabel: {
                        shrink: true,
                      },
                    }}
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
                </FormControl>
              </Form.Group>

              <Form.Group controlId="intersection_name">
                <FormControl fullWidth margin="normal">
                  <TextField
                    label="Name"
                    placeholder="Enter Intersection Name"
                    color="info"
                    variant="outlined"
                    required
                    {...register('intersection_name')}
                    slotProps={{
                      inputLabel: {
                        shrink: true,
                      },
                    }}
                  />
                  {errors.intersection_name && <p className="errorMsg">{errors.intersection_name.message}</p>}
                </FormControl>
              </Form.Group>

              <Form.Group controlId="origin_ip">
                <FormControl fullWidth margin="normal">
                  <TextField
                    label="Origin IP"
                    placeholder="Enter Origin IP"
                    color="info"
                    variant="outlined"
                    required
                    {...register('origin_ip', {
                      pattern: {
                        value:
                          /^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/,
                        message: 'Please enter a valid IP address',
                      },
                    })}
                    slotProps={{
                      inputLabel: {
                        shrink: true,
                      },
                    }}
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
                </FormControl>
              </Form.Group>

              <Form.Group controlId="organizations">
                <FormControl fullWidth margin="normal">
                  <InputLabel>Organizations</InputLabel>
                  <Select
                    id="organizations"
                    label="Organizations"
                    multiple
                    value={selectedOrganizations.map((org) => org.name)}
                    defaultValue={selectedOrganizations.map((org) => org.name)}
                    onChange={(event) => {
                      const selectedOrgs = event.target.value as string[]
                      dispatch(setSelectedOrganizations(organizations.filter((org) => selectedOrgs.includes(org.name))))
                    }}
                  >
                    {organizations.map((org) => (
                      <MenuItem key={org.name} value={org.name}>
                        {org.name}
                      </MenuItem>
                    ))}
                  </Select>
                  {selectedOrganizations.length === 0 && submitAttempt && (
                    <ErrorMessageText role="alert">Must select an organization</ErrorMessageText>
                  )}
                </FormControl>
              </Form.Group>

              <Form.Group controlId="rsus">
                <FormControl fullWidth margin="normal">
                  <InputLabel>RSUs</InputLabel>
                  <Select
                    id="rsus"
                    label="RSUs"
                    multiple
                    value={selectedRsus.map((rsu) => rsu.name)}
                    defaultValue={selectedRsus.map((rsu) => rsu.name)}
                    onChange={(event) => {
                      const selectedRsus = event.target.value as string[]
                      console.log('selectedRsus', selectedRsus)
                      const filteredRsus = rsus.filter((rsu) => selectedRsus.includes(rsu.name))
                      console.log('filteredRsus', filteredRsus)
                      dispatch(setSelectedRsus(rsus.filter((rsu) => selectedRsus.includes(rsu.name))))
                    }}
                  >
                    {rsus.map((rsu) => (
                      <MenuItem key={rsu.name} value={rsu.name}>
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
              className="museo-slab capital-case"
            >
              Cancel
            </Button>
            <Button
              form="edit-intersection-form"
              type="submit"
              variant="contained"
              style={{ position: 'absolute', bottom: 10, right: 10 }}
              className="museo-slab capital-case"
            >
              Apply Changes
            </Button>
          </DialogActions>
        </>
      ) : (
        <DialogContent>
          <Typography variant={'h4'}>
            Unknown Intersection ID. Either this Intersection does not exist, or you do not have access to it.
          </Typography>
          <AdminButton
            onClick={() => {
              setOpen(false)
              navigate('/dashboard/admin/intersections')
            }}
          >
            Close
          </AdminButton>
        </DialogContent>
      )}
    </Dialog>
  )
}

export default AdminEditIntersection
