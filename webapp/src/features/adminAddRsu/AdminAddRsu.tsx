import React, { useEffect, useState } from 'react'
import { Form } from 'react-bootstrap'
import { useForm } from 'react-hook-form'
import { Multiselect, DropdownList } from 'react-widgets'
import {
  selectPrimaryRoutes,
  selectSelectedRoute,
  selectOtherRouteDisabled,
  selectRsuModels,
  selectSelectedModel,
  selectSshCredentialGroups,
  selectSelectedSshGroup,
  selectSnmpCredentialGroups,
  selectSelectedSnmpGroup,
  selectSnmpVersions,
  selectSelectedSnmpVersion,
  selectOrganizations,
  selectSelectedOrganizations,
  selectSubmitAttempt,

  // actions
  getRsuCreationData,
  submitForm,
  updateSelectedRoute,
  updateSelectedModel,
  updateSelectedSshGroup,
  updateSelectedSnmpGroup,
  updateSelectedSnmpVersion,
  updateSelectedOrganizations,
} from './adminAddRsuSlice'

import '../adminRsuTab/Admin.css'
import toast from 'react-hot-toast'
import { useNavigate } from 'react-router-dom'
import Dialog from '@mui/material/Dialog'
import { DialogActions, DialogContent, DialogTitle } from '@mui/material'
import { useAppDispatch, useAppSelector } from '../../hooks'

export type AdminAddRsuForm = {
  ip: string
  latitude: string
  longitude: string
  milepost: number
  primary_route: string
  serial_number: string
  model: string
  scms_id: string
  ssh_credential_group: string
  snmp_credential_group: string
  snmp_version_group: string
  organizations: string[]
}

const AdminAddRsu = () => {
  const dispatch = useAppDispatch()

  const primaryRoutes = useAppSelector(selectPrimaryRoutes)
  const selectedRoute = useAppSelector(selectSelectedRoute)
  const otherRouteDisabled = useAppSelector(selectOtherRouteDisabled)
  const rsuModels = useAppSelector(selectRsuModels)
  const selectedModel = useAppSelector(selectSelectedModel)
  const sshCredentialGroups = useAppSelector(selectSshCredentialGroups)
  const selectedSshGroup = useAppSelector(selectSelectedSshGroup)
  const snmpCredentialGroups = useAppSelector(selectSnmpCredentialGroups)
  const selectedSnmpGroup = useAppSelector(selectSelectedSnmpGroup)
  const snmpVersions = useAppSelector(selectSnmpVersions)
  const selectedSnmpVersion = useAppSelector(selectSelectedSnmpVersion)
  const organizations = useAppSelector(selectOrganizations)
  const selectedOrganizations = useAppSelector(selectSelectedOrganizations)
  const submitAttempt = useAppSelector(selectSubmitAttempt)

  const [open, setOpen] = useState(true)
  const navigate = useNavigate()

  const notifySuccess = (message: string) => toast.success(message)
  const notifyError = (message: string) => toast.error(message)

  const handleFormSubmit = (data: AdminAddRsuForm) => {
    dispatch(submitForm({ data, reset })).then((data: any) => {
      data.payload.success
        ? notifySuccess(data.payload.message)
        : notifyError('Failed to add RSU due to error: ' + data.payload.message)
    })
    setOpen(false)
    navigate('/dashboard/admin/rsus')
  }

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<AdminAddRsuForm>()

  useEffect(() => {
    dispatch(getRsuCreationData())
  }, [dispatch])

  return (
    <Dialog open={open}>
      <DialogTitle>Add RSU</DialogTitle>
      <DialogContent>
        <Form
          id="add-rsu-form"
          onSubmit={handleSubmit((data) => handleFormSubmit(data))}
          style={{ fontFamily: 'Arial, Helvetica, sans-serif' }}
        >
          <Form.Group className="mb-3" controlId="ip">
            <Form.Label>RSU IP</Form.Label>
            <Form.Control
              type="text"
              placeholder="Enter RSU IP (Required)"
              {...register('ip', {
                required: "Please enter the RSU's IP address",
                pattern: {
                  value:
                    /^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/,
                  message: 'Please enter a valid IP address',
                },
              })}
            />
            {errors.ip && <p className="errorMsg">{errors.ip.message}</p>}
          </Form.Group>

          <Form.Group className="mb-3" controlId="latitude">
            <Form.Label>Latitude</Form.Label>
            <Form.Control
              type="text"
              placeholder="Enter RSU Latitude (Required)"
              {...register('latitude', {
                required: 'Please enter the RSU latitude',
                pattern: {
                  value: /^(\+|-)?(?:90(?:(?:\.0{1,8})?)|(?:[0-9]|[1-8][0-9])(?:(?:\.[0-9]{1,8})?))$/,
                  message: 'Please enter a valid latitude',
                },
              })}
            />
            {errors.latitude && <p className="errorMsg">{errors.latitude.message}</p>}
          </Form.Group>

          <Form.Group className="mb-3" controlId="longitude">
            <Form.Label>Longitude</Form.Label>
            <Form.Control
              type="text"
              placeholder="Enter RSU Longitude (Required)"
              {...register('longitude', {
                required: 'Please enter the RSU longitude',
                pattern: {
                  value: /^(\+|-)?(?:180(?:(?:\.0{1,8})?)|(?:[0-9]|[1-9][0-9]|1[0-7][0-9])(?:(?:\.[0-9]{1,8})?))$/,
                  message: 'Please enter a valid longitude',
                },
              })}
            />
            {errors.longitude && <p className="errorMsg">{errors.longitude.message}</p>}
          </Form.Group>

          <Form.Group className="mb-3" controlId="milepost">
            <Form.Label>Milepost</Form.Label>
            <Form.Control
              type="text"
              placeholder="Enter RSU Milepost (Required)"
              {...register('milepost', {
                required: 'Please enter the RSU milepost',
                pattern: {
                  value: /^\d*\.?\d*$/,
                  message: 'Please enter a valid number',
                },
              })}
            />
            {errors.milepost && <p className="errorMsg">{errors.milepost.message}</p>}
          </Form.Group>

          <Form.Group className="mb-3" controlId="primary_route">
            <Form.Label>Primary Route</Form.Label>
            <DropdownList
              className="form-dropdown"
              dataKey="id"
              textField="name"
              defaultValue={primaryRoutes[0]}
              data={primaryRoutes}
              value={selectedRoute}
              onChange={(value) => {
                dispatch(updateSelectedRoute(value.name))
              }}
            />
            {selectedRoute === 'Select Route (Required)' && submitAttempt && (
              <p className="error-msg" role="alert">
                Must select a primary route
              </p>
            )}
            {(() => {
              if (selectedRoute === 'Other') {
                return (
                  <Form.Control
                    type="text"
                    placeholder="Enter Other Route"
                    disabled={otherRouteDisabled}
                    {...register('primary_route', {
                      required: 'Please enter the other route',
                    })}
                  />
                )
              }
            })()}
          </Form.Group>

          <Form.Group className="mb-3" controlId="serial_number">
            <Form.Label>Serial Number</Form.Label>
            <Form.Control
              type="text"
              placeholder="Enter RSU Serial Number (Required)"
              {...register('serial_number', {
                required: 'Please enter the RSU serial number',
              })}
            />
            {errors.serial_number && <p className="errorMsg">{errors.serial_number.message}</p>}
          </Form.Group>

          <Form.Group className="mb-3" controlId="model">
            <Form.Label>RSU Model</Form.Label>
            <DropdownList
              className="form-dropdown"
              dataKey="id"
              textField="name"
              data={rsuModels}
              value={selectedModel}
              onChange={(value) => {
                dispatch(updateSelectedModel(value.name))
              }}
            />
            {selectedModel === 'Select RSU Model (Required)' && submitAttempt && (
              <p className="error-msg" role="alert">
                Must select a RSU model
              </p>
            )}
          </Form.Group>

          <Form.Group className="mb-3" controlId="scms_id">
            <Form.Label>SCMS ID</Form.Label>
            <Form.Control
              type="text"
              placeholder="Enter SCMS ID (Required)"
              {...register('scms_id', {
                required: 'Please enter the SCMS ID',
              })}
            />
            {errors.scms_id && <p className="errorMsg">{errors.scms_id.message}</p>}
          </Form.Group>

          <Form.Group className="mb-3" controlId="ssh_credential_group">
            <Form.Label>SSH Credential Group</Form.Label>
            <DropdownList
              className="form-dropdown"
              dataKey="id"
              textField="name"
              data={sshCredentialGroups}
              value={selectedSshGroup}
              onChange={(value) => {
                dispatch(updateSelectedSshGroup(value.name))
              }}
            />
            {selectedSshGroup === 'Select SSH Group (Required)' && submitAttempt && (
              <p className="error-msg" role="alert">
                Must select a SSH credential group
              </p>
            )}
          </Form.Group>

          <Form.Group className="mb-3" controlId="snmp_credential_group">
            <Form.Label>SNMP Credential Group</Form.Label>
            <DropdownList
              className="form-dropdown"
              dataKey="id"
              textField="name"
              data={snmpCredentialGroups}
              value={selectedSnmpGroup}
              onChange={(value) => {
                dispatch(updateSelectedSnmpGroup(value.name))
              }}
            />
            {selectedSnmpGroup === 'Select SNMP Group (Required)' && submitAttempt && (
              <p className="error-msg" role="alert">
                Must select a SNMP credential group
              </p>
            )}
          </Form.Group>

          <Form.Group className="mb-3" controlId="snmp_version_group">
            <Form.Label>SNMP Protocol</Form.Label>
            <DropdownList
              className="form-dropdown"
              dataKey="id"
              textField="name"
              data={snmpVersions}
              value={selectedSnmpVersion}
              onChange={(value) => {
                dispatch(updateSelectedSnmpVersion(value.name))
              }}
            />
            {selectedSnmpVersion === 'Select SNMP Protocol (Required)' && submitAttempt && (
              <p className="error-msg" role="alert">
                Must select a SNMP protocol
              </p>
            )}
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
        </Form>
      </DialogContent>
      <DialogActions>
        <button
          onClick={() => {
            setOpen(false)
            navigate('/dashboard/admin/rsus')
          }}
          className="admin-button"
        >
          Close
        </button>
        <button form="add-rsu-form" type="submit" className="admin-button">
          Add RSU
        </button>
      </DialogActions>
    </Dialog>
  )
}

export default AdminAddRsu
