import React, { useEffect, useState } from 'react'
import { Form } from 'react-bootstrap'
import { useForm } from 'react-hook-form'
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
import { useSelector, useDispatch } from 'react-redux'

import '../adminRsuTab/Admin.css'
import '../../styles/fonts/museo-slab.css'
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
  Grid2,
  IconButton,
  InputLabel,
  MenuItem,
  OutlinedInput,
  Select,
} from '@mui/material'
import { ErrorMessageText } from '../../styles/components/Messages'

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
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()

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
          id="add-rsu-form"
          onSubmit={handleSubmit((data) => handleFormSubmit(data))}
          style={{ fontFamily: '"museo-slab", Arial, Helvetica, sans-serif' }}
        >
          <Form.Group className="mb-3" controlId="ip">
            <FormControl fullWidth margin="normal">
              <InputLabel htmlFor="rsu-ip">RSU IP</InputLabel>
              <OutlinedInput
                id="rsu-ip"
                type="text"
                placeholder="Enter RSU IP (Required)"
                label="RSU IP"
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
            </FormControl>
          </Form.Group>
          <Grid2 container spacing={1}>
            <Grid2 size={6}>
              <Form.Group className="mb-3" controlId="latitude">
                <FormControl fullWidth margin="normal">
                  <InputLabel htmlFor="rsu-lat">Latitude</InputLabel>
                  <OutlinedInput
                    id="rsu-lat"
                    type="text"
                    label="Latitude"
                    {...register('latitude', {
                      required: 'Please enter the RSU latitude',
                      pattern: {
                        value: /^(\+|-)?(?:90(?:(?:\.0{1,8})?)|(?:[0-9]|[1-8][0-9])(?:(?:\.[0-9]{1,8})?))$/,
                        message: 'Please enter a valid latitude',
                      },
                    })}
                  />
                  {errors.latitude && <p className="errorMsg">{errors.latitude.message}</p>}
                </FormControl>
              </Form.Group>
            </Grid2>
            <Grid2 size={6}>
              <Form.Group className="mb-3" controlId="longitude">
                <FormControl fullWidth margin="normal">
                  <InputLabel htmlFor="rsu-long">Longitude</InputLabel>
                  <OutlinedInput
                    id="rsu-long"
                    type="text"
                    label="Longitude"
                    {...register('longitude', {
                      required: 'Please enter the RSU longitude',
                      pattern: {
                        value:
                          /^(\+|-)?(?:180(?:(?:\.0{1,8})?)|(?:[0-9]|[1-9][0-9]|1[0-7][0-9])(?:(?:\.[0-9]{1,8})?))$/,
                        message: 'Please enter a valid longitude',
                      },
                    })}
                  />
                  {errors.longitude && <p className="errorMsg">{errors.longitude.message}</p>}
                </FormControl>
              </Form.Group>
            </Grid2>
            <Grid2 size={6}>
              <Form.Group className="mb-3" controlId="milepost">
                <FormControl fullWidth margin="normal">
                  <InputLabel htmlFor="rsu-milepost">Milepost</InputLabel>
                  <OutlinedInput
                    id="rsu-milepost"
                    type="text"
                    label="Milepost"
                    {...register('milepost', {
                      required: 'Please enter the RSU milepost',
                      pattern: {
                        value: /^\d*\.?\d*$/,
                        message: 'Please enter a valid number',
                      },
                    })}
                  />
                  {errors.milepost && <p className="errorMsg">{errors.milepost.message}</p>}
                </FormControl>
              </Form.Group>
            </Grid2>
            <Grid2 size={6}>
              <Form.Group className="mb-3" controlId="primary_route">
                <FormControl fullWidth margin="normal">
                  <InputLabel htmlFor="primary_route">Primary Route</InputLabel>
                  <Select
                    id="primary_route"
                    label="Primary Route"
                    value={selectedRoute}
                    defaultValue={selectedRoute}
                    onChange={(event) => {
                      const route = event.target.value as String
                      dispatch(updateSelectedRoute(route))
                    }}
                  >
                    <MenuItem value="Select Route (Required)">Select Route (Required)</MenuItem>
                    {primaryRoutes.map((route) => (
                      <MenuItem key={route.id} value={route.name}>
                        {route.name}
                      </MenuItem>
                    ))}
                  </Select>
                  {selectedRoute === 'Select Route (Required)' && submitAttempt && (
                    <ErrorMessageText role="alert">Must select a primary route</ErrorMessageText>
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
                </FormControl>
              </Form.Group>
            </Grid2>
            <Grid2 size={7}>
              <Form.Group className="mb-3" controlId="serial_number">
                <FormControl fullWidth margin="normal">
                  <InputLabel htmlFor="rsu-serial-number">Serial Number</InputLabel>
                  <OutlinedInput
                    id="rsu-serial-number"
                    type="text"
                    label="RSU Serial Number"
                    {...register('serial_number', {
                      required: 'Please enter the RSU serial number',
                    })}
                  />
                  {errors.serial_number && <p className="errorMsg">{errors.serial_number.message}</p>}
                </FormControl>
              </Form.Group>
            </Grid2>
            <Grid2 size={5}>
              <Form.Group className="mb-3" controlId="model">
                <FormControl fullWidth margin="normal">
                  <InputLabel htmlFor="model">RSU Model</InputLabel>
                  <Select
                    id="model"
                    label="RSU Model"
                    value={selectedModel}
                    defaultValue={selectedModel}
                    onChange={(event) => {
                      const selectedRSUModel = event.target.value as String
                      dispatch(updateSelectedModel(selectedRSUModel))
                    }}
                  >
                    <MenuItem value="Select RSU Model (Required)">Select RSU Model (Required)</MenuItem>
                    {rsuModels.map((model) => (
                      <MenuItem key={model.id} value={model.name}>
                        {model.name}
                      </MenuItem>
                    ))}
                  </Select>
                  {selectedModel === 'Select RSU Model (Required)' && submitAttempt && (
                    <ErrorMessageText role="alert">Must select a RSU model</ErrorMessageText>
                  )}
                </FormControl>
              </Form.Group>
            </Grid2>
          </Grid2>

          <Form.Group className="mb-3" controlId="scms_id">
            <FormControl fullWidth margin="normal">
              <InputLabel htmlFor="rsu-scms-id">SCMS ID</InputLabel>
              <OutlinedInput
                id="rsu-scms-id"
                type="text"
                label="SCMS ID"
                {...register('scms_id', {
                  required: 'Please enter the SCMS ID',
                })}
              />
              {errors.scms_id && <p className="errorMsg">{errors.scms_id.message}</p>}
            </FormControl>
          </Form.Group>

          <Form.Group className="mb-3" controlId="ssh_credential_group">
            <FormControl fullWidth margin="normal">
              <InputLabel htmlFor="ssh_credential_group">SSH Credential Group</InputLabel>
              <Select
                id="ssh_credential_group"
                label="SSH Credential Group"
                value={selectedSshGroup}
                defaultValue={selectedSshGroup}
                onChange={(event) => {
                  const selectedSSHGroup = event.target.value as String
                  dispatch(updateSelectedSshGroup(selectedSSHGroup))
                }}
              >
                <MenuItem value="Select SSH Group (Required)">Select SSH Credential Group (Required)</MenuItem>
                {sshCredentialGroups.map((group) => (
                  <MenuItem key={group.id} value={group.name}>
                    {group.name}
                  </MenuItem>
                ))}
              </Select>
              {selectedSshGroup === 'Select SSH Group (Required)' && submitAttempt && (
                <ErrorMessageText role="alert">Must select a SSH credential group</ErrorMessageText>
              )}
            </FormControl>
          </Form.Group>

          <Grid2 container spacing={1}>
            <Grid2 size={6}>
              <Form.Group className="mb-3" controlId="snmp_credential_group">
                <FormControl fullWidth margin="normal">
                  <InputLabel htmlFor="snmp_credential_group">SNMP Credential Group</InputLabel>
                  <Select
                    id="snmp_credential_group"
                    label="SNMP Credential Group"
                    value={selectedSnmpGroup}
                    defaultValue={selectedSnmpGroup}
                    onChange={(event) => {
                      const selectedGroup = event.target.value as String
                      dispatch(updateSelectedSnmpGroup(selectedGroup))
                    }}
                  >
                    <MenuItem value="Select SNMP Group (Required)">Select SNMP Credential Group (Required)</MenuItem>
                    {snmpCredentialGroups.map((group) => (
                      <MenuItem key={group.id} value={group.name}>
                        {group.name}
                      </MenuItem>
                    ))}
                  </Select>
                  {selectedSnmpGroup === 'Select SNMP Group (Required)' && submitAttempt && (
                    <ErrorMessageText role="alert">Must select a SNMP credential group</ErrorMessageText>
                  )}
                </FormControl>
              </Form.Group>
            </Grid2>
            <Grid2 size={6}>
              <Form.Group className="mb-3" controlId="snmp_version_group">
                <FormControl fullWidth margin="normal">
                  <InputLabel htmlFor="snmp_version_group">SNMP Protocol</InputLabel>
                  <Select
                    id="snmp_version_group"
                    label="SNMP Protocol"
                    value={selectedSnmpVersion}
                    defaultValue={selectedSnmpVersion}
                    onChange={(event) => {
                      const selectedVersion = event.target.value as String
                      dispatch(updateSelectedSnmpVersion(selectedVersion))
                    }}
                  >
                    <MenuItem value="Select SNMP Protocol (Required)">Select SNMP Protocol (Required)</MenuItem>
                    {snmpVersions.map((ver) => (
                      <MenuItem key={ver.id} value={ver.name}>
                        {ver.name}
                      </MenuItem>
                    ))}
                  </Select>
                  {selectedSnmpVersion === 'Select SNMP Protocol (Required)' && submitAttempt && (
                    <ErrorMessageText role="alert">Must select a SNMP protocol</ErrorMessageText>
                  )}
                </FormControl>
              </Form.Group>
            </Grid2>
          </Grid2>
          <Form.Group className="mb-3" controlId="organizations">
            <FormControl fullWidth margin="normal">
              <InputLabel htmlFor="organizations">Organizations</InputLabel>
              <Select
                id="organizations"
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
        </Form>
      </DialogContent>
      <DialogActions sx={{ padding: '20px', mt: 1 }}>
        <Button
          onClick={() => {
            setOpen(false)
            navigate('/dashboard/admin/rsus')
          }}
          variant="outlined"
          color="info"
          style={{ position: 'absolute', bottom: 10, left: 10 }}
        >
          Close
        </Button>
        <Button
          form="add-rsu-form"
          type="submit"
          variant="contained"
          style={{ position: 'absolute', bottom: 10, right: 10 }}
        >
          Add RSU
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default AdminAddRsu
