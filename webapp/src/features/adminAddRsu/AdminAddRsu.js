import React, { useEffect } from 'react'
import { Form } from 'react-bootstrap'
import { useForm } from 'react-hook-form'
import { Multiselect, DropdownList } from 'react-widgets'
import {
  selectSuccessMsg,
  selectErrorState,
  selectErrorMsg,
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
import { useSelector, useDispatch } from 'react-redux'

import '../adminRsuTab/Admin.css'

const AdminAddRsu = (props) => {
  const dispatch = useDispatch()

  const successMsg = useSelector(selectSuccessMsg)
  const errorState = useSelector(selectErrorState)
  const errorMsg = useSelector(selectErrorMsg)
  const primaryRoutes = useSelector(selectPrimaryRoutes)
  const selectedRoute = useSelector(selectSelectedRoute)
  const otherRouteDisabled = useSelector(selectOtherRouteDisabled)
  const rsuModels = useSelector(selectRsuModels)
  const selectedModel = useSelector(selectSelectedModel)
  const sshCredentialGroups = useSelector(selectSshCredentialGroups)
  const selectedSshGroup = useSelector(selectSelectedSshGroup)
  const snmpCredentialGroups = useSelector(selectSnmpCredentialGroups)
  const selectedSnmpGroup = useSelector(selectSelectedSnmpGroup)
  const snmpVersions = useSelector(selectSnmpVersions)
  const selectedSnmpVersion = useSelector(selectSelectedSnmpVersion)
  const organizations = useSelector(selectOrganizations)
  const selectedOrganizations = useSelector(selectSelectedOrganizations)
  const submitAttempt = useSelector(selectSubmitAttempt)

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm()

  useEffect(() => {
    dispatch(getRsuCreationData())
  }, [dispatch])

  return (
    <div>
      <Form onSubmit={handleSubmit((data) => dispatch(submitForm({ data, reset })))}>
        <Form.Group className="mb-3" controlId="ip">
          <Form.Label>RSU IP</Form.Label>
          <Form.Control
            type="text"
            placeholder="Enter RSU IP"
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
            placeholder="Enter RSU Latitude"
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
            placeholder="Enter RSU Longitude"
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
            placeholder="Enter RSU Milepost"
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
          {selectedRoute === 'Select Route' && submitAttempt && (
            <p className="error-msg">Must select a primary route</p>
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
            placeholder="Enter RSU Serial Number"
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
          {selectedModel === 'Select RSU Model' && submitAttempt && (
            <p className="error-msg">Must select a RSU model</p>
          )}
        </Form.Group>

        <Form.Group className="mb-3" controlId="scms_id">
          <Form.Label>SCMS ID</Form.Label>
          <Form.Control
            type="text"
            placeholder="Enter SCMS ID"
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
          {selectedSshGroup === 'Select SSH Group' && submitAttempt && (
            <p className="error-msg">Must select a SSH credential group</p>
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
          {selectedSnmpGroup === 'Select SNMP Group' && submitAttempt && (
            <p className="error-msg">Must select a SNMP credential group</p>
          )}
        </Form.Group>

        <Form.Group className="mb-3" controlId="snmp_version_group">
          <Form.Label>SNMP Version</Form.Label>
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
          {selectedSnmpVersion === 'Select SNMP Version' && submitAttempt && (
            <p className="error-msg">Must select a SNMP version</p>
          )}
        </Form.Group>

        <Form.Group className="mb-3" controlId="organizations">
          <Form.Label>Organization</Form.Label>
          <Multiselect
            className="form-dropdown"
            dataKey="id"
            textField="name"
            placeholder="Select organizations"
            data={organizations}
            value={selectedOrganizations}
            onChange={(value) => {
              dispatch(updateSelectedOrganizations(value))
            }}
          />
          {selectedOrganizations.length === 0 && submitAttempt && (
            <p className="error-msg">Must select an organization</p>
          )}
        </Form.Group>

        {<p className="success-msg">{successMsg}</p>}

        {errorState && <p className="error-msg">Failed to add rsu due to error: {errorMsg}</p>}

        <div className="form-control">
          <label></label>
          <button type="submit" className="admin-button">
            Add RSU
          </button>
        </div>
      </Form>
    </div>
  )
}

export default AdminAddRsu
