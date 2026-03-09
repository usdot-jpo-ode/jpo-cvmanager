import React, { useEffect, useState } from 'react'
import { Form } from 'react-bootstrap'
import { useForm } from 'react-hook-form'
import toast from 'react-hot-toast'
import { useNavigate } from 'react-router-dom'
import Dialog from '@mui/material/Dialog'
import {
  Button,
  Checkbox,
  DialogActions,
  DialogContent,
  FormControl,
  FormControlLabel,
  Grid2,
  InputLabel,
  MenuItem,
  Select,
  TextField,
  CircularProgress,
  Box,
} from '@mui/material'
import { ErrorMessageText } from '../../styles/components/Messages'
import { SideBarHeader } from '../../styles/components/SideBarHeader'
import { useGetRsuAllowedSelectionsQuery, useCreateRsuMutation } from '../api/rsuApiSlice'

import '../adminRsuTab/Admin.css'
import '../../styles/fonts/museo-slab.css'

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
  tim_deposit: boolean
  snmp_monitoring: boolean
}

export type AdminRsuCreationBody = {
  ip: string
  milepost: number
  serial_number: string
  scms_id: string
  geo_position: {
    latitude: number
    longitude: number
  }
  primary_route: string
  model: string
  ssh_credential_group: string
  snmp_credential_group: string
  snmp_version_group: string
  tim_deposit: boolean
  snmp_monitoring: boolean
  organizations: string[]
}

const AdminAddRsu = () => {
  const navigate = useNavigate()
  const [open, setOpen] = useState(true)

  // Local state for form selections
  const [selectedRoute, setSelectedRoute] = useState('Select Route (Required)')
  const [otherRouteDisabled, setOtherRouteDisabled] = useState(true)
  const [selectedModel, setSelectedModel] = useState('Select RSU Model (Required)')
  const [selectedSshGroup, setSelectedSshGroup] = useState('Select SSH Group (Required)')
  const [selectedSnmpGroup, setSelectedSnmpGroup] = useState('Select SNMP Group (Required)')
  const [selectedSnmpVersion, setSelectedSnmpVersion] = useState('Select SNMP Protocol (Required)')
  const [selectedOrganizations, setSelectedOrganizations] = useState<string[]>([])
  const [submitAttempt, setSubmitAttempt] = useState(false)

  // RTK Query hooks
  const { data: allowedSelections, isLoading: isLoadingData } = useGetRsuAllowedSelectionsQuery()
  const [createRsu, { isLoading: isCreating }] = useCreateRsuMutation()

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
    watch,
  } = useForm<AdminAddRsuForm>()

  const handleClose = () => {
    setOpen(false)
    navigate('/dashboard/admin/rsus')
  }

  const checkForm = (): boolean => {
    if (selectedRoute === 'Select Route (Required)') return false
    if (selectedModel === 'Select RSU Model (Required)') return false
    if (selectedSshGroup === 'Select SSH Group (Required)') return false
    if (selectedSnmpGroup === 'Select SNMP Group (Required)') return false
    if (selectedSnmpVersion === 'Select SNMP Protocol (Required)') return false
    if (selectedOrganizations.length === 0) return false
    return true
  }

  const buildRequestBody = (data: AdminAddRsuForm): AdminRsuCreationBody => {
    return {
      ip: data.ip,
      milepost: Number(data.milepost),
      serial_number: data.serial_number,
      scms_id: data.scms_id,
      geo_position: {
        latitude: Number(data.latitude),
        longitude: Number(data.longitude),
      },
      primary_route: selectedRoute === 'Other' ? data.primary_route : selectedRoute,
      model: selectedModel,
      ssh_credential_group: selectedSshGroup,
      snmp_credential_group: selectedSnmpGroup,
      snmp_version_group: selectedSnmpVersion,
      tim_deposit: data.tim_deposit ?? false,
      snmp_monitoring: data.snmp_monitoring ?? false,
      organizations: selectedOrganizations,
    }
  }

  const handleFormSubmit = async (data: AdminAddRsuForm) => {
    setSubmitAttempt(true)

    if (!checkForm()) {
      toast.error('Please fill out all required fields')
      return
    }

    const requestBody = buildRequestBody(data)

    try {
      await createRsu(requestBody).unwrap()
      toast.success('RSU Created Successfully')

      // Reset form
      reset()
      setSelectedRoute('Select Route (Required)')
      setOtherRouteDisabled(true)
      setSelectedModel('Select RSU Model (Required)')
      setSelectedSshGroup('Select SSH Group (Required)')
      setSelectedSnmpGroup('Select SNMP Group (Required)')
      setSelectedSnmpVersion('Select SNMP Protocol (Required)')
      setSelectedOrganizations([])
      setSubmitAttempt(false)

      handleClose()
    } catch (error: any) {
      console.log('Error creating RSU:', error)
      toast.error(
        'Failed to add RSU due to error: ' +
          (error?.data?.message || error?.data?.detail || error?.message || 'Unknown error')
      )
    }
  }

  const handleRouteChange = (route: string) => {
    setSelectedRoute(route)
    setOtherRouteDisabled(route !== 'Other')
  }

  if (isLoadingData) {
    return (
      <Dialog open={open}>
        <DialogContent sx={{ width: '600px', padding: '40px' }}>
          <Box display="flex" justifyContent="center" alignItems="center">
            <CircularProgress />
          </Box>
        </DialogContent>
      </Dialog>
    )
  }

  const primaryRoutes = allowedSelections?.primary_routes || []
  const rsuModels = allowedSelections?.rsu_models || []
  const sshCredentialGroups = allowedSelections?.ssh_credential_groups || []
  const snmpCredentialGroups = allowedSelections?.snmp_credential_groups || []
  const snmpVersions = allowedSelections?.snmp_version_groups || []
  const organizations = allowedSelections?.organizations || []

  return (
    <Dialog open={open}>
      <DialogContent sx={{ width: '600px', padding: '5px 10px' }}>
        <SideBarHeader onClick={handleClose} title="Add RSU" />
        <Form
          id="add-rsu-form"
          onSubmit={handleSubmit(handleFormSubmit)}
          style={{ fontFamily: '"museo-slab", Arial, Helvetica, sans-serif' }}
        >
          <Form.Group controlId="ip">
            <FormControl fullWidth margin="normal">
              <TextField
                label="RSU IP"
                placeholder="Enter RSU IP"
                color="info"
                variant="outlined"
                required
                {...register('ip', {
                  required: "Please enter the RSU's IP address",
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
              {errors.ip && <p className="errorMsg">{errors.ip.message}</p>}
            </FormControl>
          </Form.Group>

          <Grid2 container spacing={1}>
            <Grid2 size={6}>
              <Form.Group controlId="latitude">
                <FormControl fullWidth margin="normal">
                  <TextField
                    label="Latitude"
                    placeholder="Enter RSU Latitude"
                    color="info"
                    variant="outlined"
                    required
                    {...register('latitude', {
                      required: 'Please enter the RSU latitude',
                      pattern: {
                        value: /^(\+|-)?(?:90(?:(?:\.0{1,8})?)|(?:[0-9]|[1-8][0-9])(?:(?:\.[0-9]{1,8})?))$/,
                        message: 'Please enter a valid latitude',
                      },
                    })}
                    slotProps={{
                      inputLabel: {
                        shrink: true,
                      },
                    }}
                  />
                  {errors.latitude && <p className="errorMsg">{errors.latitude.message}</p>}
                </FormControl>
              </Form.Group>
            </Grid2>

            <Grid2 size={6}>
              <Form.Group controlId="longitude">
                <FormControl fullWidth margin="normal">
                  <TextField
                    label="Longitude"
                    placeholder="Enter RSU Longitude"
                    color="info"
                    variant="outlined"
                    required
                    {...register('longitude', {
                      required: 'Please enter the RSU longitude',
                      pattern: {
                        value:
                          /^(\+|-)?(?:180(?:(?:\.0{1,8})?)|(?:[0-9]|[1-9][0-9]|1[0-7][0-9])(?:(?:\.[0-9]{1,8})?))$/,
                        message: 'Please enter a valid longitude',
                      },
                    })}
                    slotProps={{
                      inputLabel: {
                        shrink: true,
                      },
                    }}
                  />
                  {errors.longitude && <p className="errorMsg">{errors.longitude.message}</p>}
                </FormControl>
              </Form.Group>
            </Grid2>

            <Grid2 size={6}>
              <Form.Group controlId="milepost">
                <FormControl fullWidth margin="normal">
                  <TextField
                    label="Milepost"
                    placeholder="Enter RSU Milepost"
                    color="info"
                    variant="outlined"
                    required
                    {...register('milepost', {
                      required: 'Please enter the RSU milepost',
                      pattern: {
                        value: /^\d*\.?\d*$/,
                        message: 'Please enter a valid number',
                      },
                    })}
                    slotProps={{
                      inputLabel: {
                        shrink: true,
                      },
                    }}
                  />
                  {errors.milepost && <p className="errorMsg">{errors.milepost.message}</p>}
                </FormControl>
              </Form.Group>
            </Grid2>

            <Grid2 size={6}>
              <Form.Group controlId="primary_route">
                <FormControl fullWidth margin="normal">
                  <InputLabel htmlFor="primary_route">Primary Route</InputLabel>
                  <Select
                    id="primary_route"
                    label="Primary Route"
                    value={selectedRoute}
                    required
                    onChange={(event) => handleRouteChange(event.target.value)}
                  >
                    <MenuItem value="Select Route (Required)">Select Route (Required)</MenuItem>
                    {primaryRoutes.map((route, index) => (
                      <MenuItem key={index} value={route}>
                        {route}
                      </MenuItem>
                    ))}
                    <MenuItem value="Other">Other</MenuItem>
                  </Select>
                  {selectedRoute === 'Select Route (Required)' && submitAttempt && (
                    <ErrorMessageText role="alert">Must select a primary route</ErrorMessageText>
                  )}
                  {selectedRoute === 'Other' && (
                    <TextField
                      placeholder="Enter Other Route"
                      disabled={otherRouteDisabled}
                      fullWidth
                      margin="normal"
                      {...register('primary_route', {
                        required: 'Please enter the other route',
                      })}
                    />
                  )}
                </FormControl>
              </Form.Group>
            </Grid2>

            <Grid2 size={7}>
              <Form.Group controlId="serial_number">
                <FormControl fullWidth margin="normal">
                  <TextField
                    label="Serial Number"
                    placeholder="Enter RSU Serial Number"
                    color="info"
                    variant="outlined"
                    required
                    {...register('serial_number', {
                      required: 'Please enter the RSU serial number',
                    })}
                    slotProps={{
                      inputLabel: {
                        shrink: true,
                      },
                    }}
                  />
                  {errors.serial_number && <p className="errorMsg">{errors.serial_number.message}</p>}
                </FormControl>
              </Form.Group>
            </Grid2>

            <Grid2 size={5}>
              <Form.Group controlId="model">
                <FormControl fullWidth margin="normal">
                  <InputLabel htmlFor="model">RSU Model</InputLabel>
                  <Select
                    id="model"
                    label="RSU Model"
                    value={selectedModel}
                    required
                    onChange={(event) => setSelectedModel(event.target.value)}
                  >
                    <MenuItem value="Select RSU Model (Required)">Select RSU Model (Required)</MenuItem>
                    {rsuModels.map((model, index) => (
                      <MenuItem key={index} value={model}>
                        {model}
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

          <Form.Group controlId="scms_id">
            <FormControl fullWidth margin="normal">
              <TextField
                label="SCMS ID"
                placeholder="Enter RSU SCMS ID"
                color="info"
                variant="outlined"
                required
                {...register('scms_id', {
                  required: 'Please enter the SCMS ID',
                })}
                slotProps={{
                  inputLabel: {
                    shrink: true,
                  },
                }}
              />
              {errors.scms_id && <p className="errorMsg">{errors.scms_id.message}</p>}
            </FormControl>
          </Form.Group>

          <Grid2 container spacing={1}>
            <Grid2 size={6}>
              <Form.Group controlId="tim_deposit">
                <FormControlLabel
                  control={<Checkbox {...register('tim_deposit')} checked={watch('tim_deposit')} color="primary" />}
                  label="TIM Deposit"
                />
              </Form.Group>
            </Grid2>
            <Grid2 size={6}>
              <Form.Group controlId="snmp_monitoring">
                <FormControlLabel
                  control={
                    <Checkbox {...register('snmp_monitoring')} checked={watch('snmp_monitoring')} color="primary" />
                  }
                  label="SNMP Monitoring"
                />
              </Form.Group>
            </Grid2>
          </Grid2>

          <Form.Group controlId="ssh_credential_group">
            <FormControl fullWidth margin="normal">
              <InputLabel htmlFor="ssh_credential_group">SSH Credential Group</InputLabel>
              <Select
                id="ssh_credential_group"
                label="SSH Credential Group"
                value={selectedSshGroup}
                required
                onChange={(event) => setSelectedSshGroup(event.target.value)}
              >
                <MenuItem value="Select SSH Group (Required)">Select SSH Group (Required)</MenuItem>
                {sshCredentialGroups.map((group, index) => (
                  <MenuItem key={index} value={group}>
                    {group}
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
              <Form.Group controlId="snmp_credential_group">
                <FormControl fullWidth margin="normal">
                  <InputLabel htmlFor="snmp_credential_group">SNMP Credential Group</InputLabel>
                  <Select
                    id="snmp_credential_group"
                    label="SNMP Credential Group"
                    value={selectedSnmpGroup}
                    onChange={(event) => setSelectedSnmpGroup(event.target.value)}
                  >
                    <MenuItem value="Select SNMP Group (Required)">Select SNMP Credential Group (Required)</MenuItem>
                    {snmpCredentialGroups.map((group, index) => (
                      <MenuItem key={index} value={group}>
                        {group}
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
              <Form.Group controlId="snmp_version_group">
                <FormControl fullWidth margin="normal">
                  <InputLabel htmlFor="snmp_version_group">SNMP Protocol</InputLabel>
                  <Select
                    id="snmp_version_group"
                    label="SNMP Protocol"
                    value={selectedSnmpVersion}
                    required
                    onChange={(event) => setSelectedSnmpVersion(event.target.value)}
                  >
                    <MenuItem value="Select SNMP Protocol (Required)">Select SNMP Protocol (Required)</MenuItem>
                    {snmpVersions.map((ver, index) => (
                      <MenuItem key={index} value={ver}>
                        {ver}
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

          <Form.Group controlId="organizations">
            <FormControl fullWidth margin="normal">
              <InputLabel htmlFor="organizations">Organizations</InputLabel>
              <Select
                id="organizations"
                label="Organizations"
                multiple
                required
                value={selectedOrganizations}
                onChange={(event) => setSelectedOrganizations(event.target.value as string[])}
              >
                {organizations.map((org, index) => (
                  <MenuItem key={index} value={org}>
                    {org}
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
          onClick={handleClose}
          variant="outlined"
          color="info"
          style={{ position: 'absolute', bottom: 10, left: 10 }}
          className="museo-slab capital-case"
          disabled={isCreating}
        >
          Cancel
        </Button>
        <Button
          form="add-rsu-form"
          type="submit"
          variant="contained"
          style={{ position: 'absolute', bottom: 10, right: 10 }}
          className="museo-slab capital-case"
          disabled={isCreating}
        >
          {isCreating ? 'Adding...' : 'Add RSU'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default AdminAddRsu
