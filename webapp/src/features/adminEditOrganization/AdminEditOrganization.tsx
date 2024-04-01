import React, { useEffect } from 'react'
import { Form } from 'react-bootstrap'
import { useForm } from 'react-hook-form'
import {
  selectSuccessMsg,
  selectErrorState,
  selectErrorMsg,

  // actions
  updateStates,
  editOrganization,
} from './adminEditOrganizationSlice'
import { useSelector, useDispatch } from 'react-redux'

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
import { Link, useParams } from 'react-router-dom'
import { ThemeProvider, Typography } from '@mui/material'
import { theme } from '../../styles'

const AdminEditOrganization = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()

  const successMsg = useSelector(selectSuccessMsg)
  const errorState = useSelector(selectErrorState)
  const errorMsg = useSelector(selectErrorMsg)
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
    },
  })

  const { orgName } = useParams<{ orgName: string }>()

  useEffect(() => {
    if (
      (orgData ?? []).find((organization: AdminOrgSummary) => organization?.name === orgName) &&
      Object.keys(selectedOrg).length == 0
    ) {
      dispatch(getOrgData({ orgName }))
    } else {
      dispatch(setSelectedOrg(null))
    }
  }, [orgData, orgName, dispatch])

  useEffect(() => {
    dispatch(getOrgData({ orgName: 'all', all: true, specifiedOrg: undefined }))
  }, [dispatch])

  useEffect(() => {
    updateStates(setValue, selectedOrg?.name)
  }, [setValue, selectedOrg?.name])

  const onSubmit = (data: adminOrgPatch) => {
    dispatch(editOrganization({ json: data, setValue, selectedOrg: selectedOrg?.name }))
  }

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
            {errors.name && (
              <p className="errorMsg" role="alert">
                {errors.name.message}
              </p>
            )}
          </Form.Group>

          {successMsg && (
            <p className="success-msg" role="status">
              {successMsg}
            </p>
          )}
          {errorState && (
            <p className="error-msg" role="alert">
              Failed to apply changes to organization due to error: {errorMsg}
            </p>
          )}
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
