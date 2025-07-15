import React, { useEffect, useState } from 'react'
import { Form } from 'react-bootstrap'
import { useForm } from 'react-hook-form'
import { ErrorMessage } from '@hookform/error-message'
import {
  selectApiData,
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
  submitForm,
  updateSelectedRoute,
  setSelectedModel,
  setSelectedSshGroup,
  setSelectedSnmpGroup,
  setSelectedSnmpVersion,
  setSelectedOrganizations,
} from './adminEditRsuSlice'
import { useSelector, useDispatch } from 'react-redux'

import '../adminRsuTab/Admin.css'
import '../../styles/fonts/museo-slab.css'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../store'
import { AdminRsu } from '../../models/Rsu'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { selectTableData, updateTableData } from '../adminRsuTab/adminRsuTabSlice'
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  FormControl,
  Grid2,
  InputLabel,
  MenuItem,
  Select,
  TextField,
  Typography,
} from '@mui/material'
import toast from 'react-hot-toast'
import { ErrorMessageText } from '../../styles/components/Messages'
import { SideBarHeader } from '../../styles/components/SideBarHeader'

export type AdminEditRsuFormType = {
  orig_ip: string
  ip: string
  geo_position: {
    latitude: string
    longitude: string
  }
  milepost: string | number
  primary_route: string
  serial_number: string
  model: string
  scms_id: string
  ssh_credential_group: string
  snmp_credential_group: string
  snmp_version_group: string
  organizations: string[]
  organizations_to_add: string[]
  organizations_to_remove: string[]
}

const AdminEditRsu = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const apiData = useSelector(selectApiData)
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
  const rsuTableData = useSelector(selectTableData)

  const [open, setOpen] = useState(true)
  const [unknownRsu, setUnknownRsu] = useState(false)

  const navigate = useNavigate()

  const {
    register,
    handleSubmit,
    formState: { errors },
    setValue,
  } = useForm<AdminEditRsuFormType>({
    defaultValues: {
      orig_ip: '',
      ip: '',
      geo_position: {
        latitude: '',
        longitude: '',
      },
      milepost: '',
      primary_route: '',
      serial_number: '',
      model: '',
      scms_id: '',
      ssh_credential_group: '',
      snmp_credential_group: '',
      snmp_version_group: '',
      organizations_to_add: [],
      organizations_to_remove: [],
    },
  })

  const { rsuIp } = useParams<{ rsuIp: string }>()

  useEffect(() => {
    const currRsu = (rsuTableData ?? []).find((rsu: AdminRsu) => rsu.ip === rsuIp)
    if (currRsu) {
      setValue('orig_ip', currRsu.ip)
      setValue('ip', currRsu.ip)
      setValue('geo_position.latitude', currRsu.geo_position.latitude.toString())
      setValue('geo_position.longitude', currRsu.geo_position.longitude.toString())
      setValue('milepost', String(currRsu.milepost))
      setValue('serial_number', currRsu.serial_number)
      setValue('scms_id', currRsu.scms_id)
      if (unknownRsu) setUnknownRsu(false)
    } else {
      setUnknownRsu(true)
      console.error('Unknown RSU IP: ', rsuIp)
    }
  }, [apiData, rsuIp, rsuTableData, setValue, unknownRsu])

  useEffect(() => {
    dispatch(updateSelectedRoute(selectedRoute))
  }, [selectedRoute, dispatch])

  useEffect(() => {
    dispatch(updateTableData())
  }, [dispatch])

  const onSubmit = (data: AdminEditRsuFormType) => {
    dispatch(submitForm(data)).then((data: any) => {
      if (data.payload.success) {
        toast.success('RSU updated successfully')
      } else {
        toast.error('Failed to update RSU: ' + data.payload.message)
      }
    })
    setOpen(false)
    navigate('/dashboard/admin/rsus')
  }

  return (
    <>
      {Object.keys(apiData ?? {}).length !== 0 && !unknownRsu ? (
        <Dialog open={open}>
          <DialogContent sx={{ width: '600px', padding: '5px 10px' }}>
            <SideBarHeader
              onClick={() => {
                setOpen(false)
                navigate('..')
              }}
              title="Edit RSU"
            />
            <Form
              id="edit-rsu-form"
              onSubmit={handleSubmit(onSubmit)}
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
                  <ErrorMessage
                    errors={errors}
                    name="ip"
                    render={({ message }) => (
                      <p className="errorMsg" role="alert">
                        {' '}
                        {message}{' '}
                      </p>
                    )}
                  />
                </FormControl>
              </Form.Group>
              <Grid2 container spacing={1}>
                <Grid2 size={6}>
                  <Form.Group controlId="geo_position.latitude">
                    <FormControl fullWidth margin="normal">
                      <TextField
                        label="Latitude"
                        placeholder="Enter RSU Latitude"
                        color="info"
                        variant="outlined"
                        required
                        {...register('geo_position.latitude', {
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
                      <ErrorMessage
                        errors={errors}
                        name="geo_position.latitude"
                        render={({ message }) => (
                          <p className="errorMsg" role="alert">
                            {' '}
                            {message}{' '}
                          </p>
                        )}
                      />
                    </FormControl>
                  </Form.Group>
                </Grid2>
                <Grid2 size={6}>
                  <Form.Group controlId="geo_position.longitude">
                    <FormControl fullWidth margin="normal">
                      <TextField
                        label="Longitude"
                        placeholder="Enter RSU Longitude"
                        color="info"
                        variant="outlined"
                        required
                        {...register('geo_position.longitude', {
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
                      <ErrorMessage
                        errors={errors}
                        name="geo_position.longitude"
                        render={({ message }) => (
                          <p className="errorMsg" role="alert">
                            {' '}
                            {message}{' '}
                          </p>
                        )}
                      />
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
                      <ErrorMessage
                        errors={errors}
                        name="milepost"
                        render={({ message }) => (
                          <p className="errorMsg" role="alert">
                            {' '}
                            {message}{' '}
                          </p>
                        )}
                      />
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
                        defaultValue={selectedRoute}
                        required
                        onChange={(event) => {
                          const route = event.target.value as String
                          dispatch(updateSelectedRoute(route))
                        }}
                      >
                        {primaryRoutes.map((route) => (
                          <MenuItem key={route.name} value={route.name}>
                            {route.name}
                          </MenuItem>
                        ))}
                      </Select>
                      {selectedRoute === '' && submitAttempt && (
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
                      {errors.serial_number && (
                        <p className="errorMsg" role="alert">
                          {errors.serial_number.message}
                        </p>
                      )}
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
                        defaultValue={selectedModel}
                        required
                        onChange={(event) => {
                          const selectedRSUModel = event.target.value as String
                          dispatch(setSelectedModel(selectedRSUModel))
                        }}
                      >
                        <MenuItem value="Select RSU Model (Required)">Select RSU Model (Required)</MenuItem>
                        {rsuModels.map((model) => (
                          <MenuItem key={model.name} value={model.name}>
                            {model.name}
                          </MenuItem>
                        ))}
                      </Select>
                      {selectedModel === '' && submitAttempt && (
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
                  {errors.scms_id && (
                    <p className="errorMsg" role="alert">
                      {errors.scms_id.message}
                    </p>
                  )}
                </FormControl>
              </Form.Group>

              <Form.Group controlId="ssh_credential_group">
                <FormControl fullWidth margin="normal">
                  <InputLabel htmlFor="ssh_credential_group">SSH Credential Group</InputLabel>
                  <Select
                    id="ssh_credential_group"
                    label="SSH Credential Group"
                    value={selectedSshGroup}
                    defaultValue={selectedSshGroup}
                    required
                    onChange={(event) => {
                      const selectedSSHGroup = event.target.value as String
                      dispatch(setSelectedSshGroup(selectedSSHGroup))
                    }}
                  >
                    <MenuItem value="Select SSH Group (Required)">Select SSH Credential Group (Required)</MenuItem>
                    {sshCredentialGroups.map((group) => (
                      <MenuItem key={group.name} value={group.name}>
                        {group.name}
                      </MenuItem>
                    ))}
                  </Select>
                  {selectedSshGroup === '' && submitAttempt && (
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
                        defaultValue={selectedSnmpGroup}
                        required
                        onChange={(event) => {
                          const selectedGroup = event.target.value as String
                          dispatch(setSelectedSnmpGroup(selectedGroup))
                        }}
                      >
                        <MenuItem value="Select SNMP Group (Required)">
                          Select SNMP Credential Group (Required)
                        </MenuItem>
                        {snmpCredentialGroups.map((group) => (
                          <MenuItem key={group.name} value={group.name}>
                            {group.name}
                          </MenuItem>
                        ))}
                      </Select>
                      {selectedSnmpGroup === '' && submitAttempt && (
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
                        defaultValue={selectedSnmpVersion}
                        required
                        onChange={(event) => {
                          const selectedVersion = event.target.value as String
                          dispatch(setSelectedSnmpVersion(selectedVersion))
                        }}
                      >
                        <MenuItem value="Select SNMP Protocol (Required)">Select SNMP Protocol (Required)</MenuItem>
                        {snmpVersions.map((ver) => (
                          <MenuItem key={ver.name} value={ver.name}>
                            {ver.name}
                          </MenuItem>
                        ))}
                      </Select>
                      {selectedSnmpVersion === '' && submitAttempt && (
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
                    value={selectedOrganizations.map((org) => org.name)}
                    defaultValue={selectedOrganizations.map((org) => org.name)}
                    onChange={(event) => {
                      const selectedOrgs = event.target.value as String[]
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
              className="museo-slab capital-case"
            >
              Cancel
            </Button>
            <Button
              form="edit-rsu-form"
              type="submit"
              variant="contained"
              style={{ position: 'absolute', bottom: 10, right: 10 }}
              className="museo-slab capital-case"
            >
              Apply Changes
            </Button>
          </DialogActions>
        </Dialog>
      ) : (
        unknownRsu && (
          <Dialog open={open}>
            <DialogContent sx={{ width: '600px', padding: '5px 10px' }}>
              <Typography variant={'h4'}>
                Unknown RSU IP address. Either this RSU does not exist, or you do not have access to it.{' '}
                <Link to="../">RSUs</Link>
              </Typography>
            </DialogContent>
          </Dialog>
        )
      )}
    </>
  )
}

export default AdminEditRsu
