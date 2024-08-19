import React, { useEffect } from 'react'
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
import { Typography } from '@mui/material'
import { theme } from '../../styles'

const AdminEditOrganization = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()

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
  }

  useEffect(() => {
    if (successMsg) navigate('..')
    dispatch(setSuccessMsg(''))
  }, [successMsg])

  return (
    <div>
      {Object.keys(selectedOrg ?? {}).length != 0 ? (
        <Form onSubmit={handleSubmit((data) => onSubmit(data))}>
          <Form.Group className="mb-3" controlId="name">
            <Form.Label>Organization Name</Form.Label>
            <Form.Control
              type="text"
              placeholder="Enter organization name"
              {...register('name', {
                required: 'Please enter the organization name',
              })}
            />
            <Form.Label>Organization Email</Form.Label>
            <Form.Control
              type="text"
              placeholder="Enter organization email"
              {...register('email', {
                required: 'Please enter the organization email',
              })}
            />
            {errors.name && (
              <p className="errorMsg" role="alert">
                {errors.name.message}
              </p>
            )}
          </Form.Group>

          <div className="form-control">
            <label></label>
            <button type="submit" className="admin-button">
              Apply Changes
            </button>
          </div>
        </Form>
      ) : (
        <Typography variant={'h4'} style={{ color: '#fff' }}>
          Unknown organization. Either this organization does not exist, or you do not have access to it.{' '}
          <Link to="../">Organizations</Link>
        </Typography>
      )}
    </div>
  )
}

export default AdminEditOrganization
