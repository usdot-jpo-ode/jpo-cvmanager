import { useEffect, useState } from 'react'
import { Form } from 'react-bootstrap'
import { useForm } from 'react-hook-form'
import { ErrorMessage } from '@hookform/error-message'

import '../adminRsuTab/Admin.css'
import '../../styles/fonts/museo-slab.css'
import { AdminRsu } from '../../models/Rsu'
import { Link, useNavigate, useParams } from 'react-router-dom'
import {
  Checkbox,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  FormControl,
  FormControlLabel,
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
import { useGetRsuAllowedSelectionsQuery, useGetRsuQuery, usePatchRsuMutation } from '../api/rsuApiSlice'

export type AdminEditRsuFormType = {
  orig_ip: string
  ip: string
  geo_position: {
    latitude: string
    longitude: string
  }
  milepost: string | number
  primary_route: string
  other_route: string
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

const AdminEditRsu = () => {
  const navigate = useNavigate()
  const { rsuIp } = useParams<{ rsuIp: string }>()

  const { data: rsuInfo, isLoading: isLoadingRsu } = useGetRsuQuery(rsuIp!)
  const { data: rsuAllowedSelections, isLoading: isLoadingAllowedSelections } = useGetRsuAllowedSelectionsQuery()
  const [patchRsu, { isLoading: isPatchingRsu }] = usePatchRsuMutation()

  const [open, setOpen] = useState(true)
  const [submitAttempt, setSubmitAttempt] = useState(false)

  const {
    register,
    handleSubmit,
    formState: { errors },
    setValue,
    watch,
    reset,
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
      other_route: '',
      serial_number: '',
      model: '',
      scms_id: '',
      ssh_credential_group: '',
      snmp_credential_group: '',
      snmp_version_group: '',
      organizations: [],
      tim_deposit: false,
      snmp_monitoring: false,
    },
  })

  // Watch form values
  const watchedPrimaryRoute = watch('primary_route')
  const watchedModel = watch('model')
  const watchedSshGroup = watch('ssh_credential_group')
  const watchedSnmpGroup = watch('snmp_credential_group')
  const watchedSnmpVersion = watch('snmp_version_group')
  const watchedOrganizations = watch('organizations')

  // Initialize form when RSU data loads
  useEffect(() => {
    if (rsuInfo) {
      reset({
        orig_ip: rsuInfo.ip,
        ip: rsuInfo.ip,
        geo_position: {
          latitude: rsuInfo.geo_position.latitude.toString(),
          longitude: rsuInfo.geo_position.longitude.toString(),
        },
        milepost: String(rsuInfo.milepost),
        primary_route: rsuInfo.primary_route,
        other_route: '',
        serial_number: rsuInfo.serial_number,
        model: rsuInfo.model,
        scms_id: rsuInfo.scms_id,
        ssh_credential_group: rsuInfo.ssh_credential_group,
        snmp_credential_group: rsuInfo.snmp_credential_group,
        snmp_version_group: rsuInfo.snmp_version_group,
        organizations: rsuInfo.organizations,
        tim_deposit: rsuInfo.tim_deposit ?? false,
        snmp_monitoring: rsuInfo.snmp_monitoring ?? false,
      })
    }
  }, [rsuInfo, reset])

  const onSubmit = async (data: AdminEditRsuFormType) => {
    setSubmitAttempt(true)

    // Validate dropdowns
    if (
      !data.primary_route ||
      !data.model ||
      !data.ssh_credential_group ||
      !data.snmp_credential_group ||
      !data.snmp_version_group ||
      data.organizations.length === 0
    ) {
      toast.error('Please fill in all required fields')
      return
    }

    const loadingToast = toast.loading('Updating RSU...')

    try {
      // Build patch object with only changed fields
      const patch: Partial<AdminRsu> = {}

      if (data.ip !== rsuInfo?.ip) patch.ip = data.ip
      if (
        data.geo_position.latitude !== rsuInfo?.geo_position.latitude.toString() ||
        data.geo_position.longitude !== rsuInfo?.geo_position.longitude.toString()
      ) {
        patch.geo_position = {
          latitude: data.geo_position.latitude,
          longitude: data.geo_position.longitude,
        }
      }
      const formMilepost = Number(data.milepost)
      if (formMilepost !== rsuInfo?.milepost) patch.milepost = formMilepost
      if (data.primary_route !== rsuInfo?.primary_route) {
        patch.primary_route = data.primary_route === 'Other' ? data.other_route : data.primary_route
      }
      if (data.serial_number !== rsuInfo?.serial_number) patch.serial_number = data.serial_number
      if (data.model !== rsuInfo?.model) patch.model = data.model
      if (data.scms_id !== rsuInfo?.scms_id) patch.scms_id = data.scms_id
      if (data.ssh_credential_group !== rsuInfo?.ssh_credential_group) {
        patch.ssh_credential_group = data.ssh_credential_group
      }
      if (data.snmp_credential_group !== rsuInfo?.snmp_credential_group) {
        patch.snmp_credential_group = data.snmp_credential_group
      }
      if (data.snmp_version_group !== rsuInfo?.snmp_version_group) {
        patch.snmp_version_group = data.snmp_version_group
      }

      // Check if organizations changed
      const orgsChanged =
        data.organizations.length !== rsuInfo?.organizations.length ||
        data.organizations.some((org) => !rsuInfo?.organizations.includes(org))

      if (orgsChanged) {
        patch.organizations = data.organizations
      }

      // Check if tim_deposit changed
      if (data.tim_deposit !== (rsuInfo?.tim_deposit ?? false)) {
        patch.tim_deposit = data.tim_deposit
      }

      // Check if snmp_monitoring changed
      if (data.snmp_monitoring !== (rsuInfo?.snmp_monitoring ?? false)) {
        patch.snmp_monitoring = data.snmp_monitoring
      }

      await patchRsu({ rsuIp: data.orig_ip, patch }).unwrap()
      toast.success('RSU updated successfully', { id: loadingToast })
      // Add a small delay to allow backend to finalize changes
      await new Promise((resolve) => setTimeout(resolve, 500))
      setOpen(false)
      navigate('/dashboard/admin/rsus')
    } catch (error: any) {
      toast.error(
        'Failed to update RSU: ' + (error?.data?.message || error?.message || error?.data?.detail || 'Unknown error'),
        {
          id: loadingToast,
        }
      )
    }
  }

  const isLoading = isLoadingRsu || isLoadingAllowedSelections

  return (
    <Dialog open={open}>
      {!isLoading && rsuInfo && rsuAllowedSelections ? (
        <>
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
                        {message}
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
                            {message}
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
                            {message}
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
                            {message}
                          </p>
                        )}
                      />
                    </FormControl>
                  </Form.Group>
                </Grid2>
                <Grid2 size={6}>
                  <Form.Group controlId="primary_route">
                    <FormControl fullWidth margin="normal">
                      <InputLabel htmlFor="primary_route" required>
                        Primary Route
                      </InputLabel>
                      <Select
                        id="primary_route"
                        label="Primary Route"
                        value={watchedPrimaryRoute || ''}
                        required
                        {...register('primary_route', { required: true })}
                        onChange={(event) => {
                          setValue('primary_route', event.target.value as string)
                        }}
                      >
                        {rsuAllowedSelections.primary_routes?.map((route) => (
                          <MenuItem key={route} value={route}>
                            {route}
                          </MenuItem>
                        ))}
                      </Select>
                      {!watchedPrimaryRoute && submitAttempt && (
                        <ErrorMessageText role="alert">Must select a primary route</ErrorMessageText>
                      )}
                    </FormControl>
                  </Form.Group>
                  {watchedPrimaryRoute === 'Other' && (
                    <FormControl fullWidth margin="normal">
                      <TextField
                        label="Other Route"
                        placeholder="Enter Other Route"
                        color="info"
                        variant="outlined"
                        required
                        {...register('other_route', {
                          required: watchedPrimaryRoute === 'Other' ? 'Please enter the other route' : false,
                        })}
                        slotProps={{
                          inputLabel: {
                            shrink: true,
                          },
                        }}
                      />
                      <ErrorMessage
                        errors={errors}
                        name="other_route"
                        render={({ message }) => (
                          <p className="errorMsg" role="alert">
                            {message}
                          </p>
                        )}
                      />
                    </FormControl>
                  )}
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
                      <InputLabel htmlFor="model" required>
                        RSU Model
                      </InputLabel>
                      <Select
                        id="model"
                        label="RSU Model"
                        value={watchedModel || ''}
                        required
                        {...register('model', { required: true })}
                        onChange={(event) => {
                          setValue('model', event.target.value as string)
                        }}
                      >
                        {rsuAllowedSelections.rsu_models?.map((model) => (
                          <MenuItem key={model} value={model}>
                            {model}
                          </MenuItem>
                        ))}
                      </Select>
                      {!watchedModel && submitAttempt && (
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

              <Grid2 container spacing={1}>
                <Grid2 size={6}>
                  <Form.Group controlId="tim_deposit">
                    <FormControlLabel
                      control={
                        <Checkbox
                          {...register('tim_deposit')}
                          checked={watch('tim_deposit')}
                          color="primary"
                          onChange={(event) => {
                            setValue('tim_deposit', event.target.checked)
                          }}
                        />
                      }
                      label="TIM Deposit"
                    />
                  </Form.Group>
                </Grid2>
                <Grid2 size={6}>
                  <Form.Group controlId="snmp_monitoring">
                    <FormControlLabel
                      control={
                        <Checkbox
                          {...register('snmp_monitoring')}
                          checked={watch('snmp_monitoring')}
                          color="primary"
                          onChange={(event) => {
                            setValue('snmp_monitoring', event.target.checked)
                          }}
                        />
                      }
                      label="SNMP Monitoring"
                    />
                  </Form.Group>
                </Grid2>
              </Grid2>

              <Form.Group controlId="ssh_credential_group">
                <FormControl fullWidth margin="normal">
                  <InputLabel htmlFor="ssh_credential_group" required>
                    SSH Credential Group
                  </InputLabel>
                  <Select
                    id="ssh_credential_group"
                    label="SSH Credential Group"
                    value={watchedSshGroup || ''}
                    required
                    {...register('ssh_credential_group', { required: true })}
                    onChange={(event) => {
                      setValue('ssh_credential_group', event.target.value as string)
                    }}
                  >
                    {rsuAllowedSelections.ssh_credential_groups?.map((group) => (
                      <MenuItem key={group} value={group}>
                        {group}
                      </MenuItem>
                    ))}
                  </Select>
                  {!watchedSshGroup && submitAttempt && (
                    <ErrorMessageText role="alert">Must select a SSH credential group</ErrorMessageText>
                  )}
                </FormControl>
              </Form.Group>

              <Grid2 container spacing={1}>
                <Grid2 size={6}>
                  <Form.Group controlId="snmp_credential_group">
                    <FormControl fullWidth margin="normal">
                      <InputLabel htmlFor="snmp_credential_group" required>
                        SNMP Credential Group
                      </InputLabel>
                      <Select
                        id="snmp_credential_group"
                        label="SNMP Credential Group"
                        value={watchedSnmpGroup || ''}
                        required
                        {...register('snmp_credential_group', { required: true })}
                        onChange={(event) => {
                          setValue('snmp_credential_group', event.target.value as string)
                        }}
                      >
                        {rsuAllowedSelections.snmp_credential_groups?.map((group) => (
                          <MenuItem key={group} value={group}>
                            {group}
                          </MenuItem>
                        ))}
                      </Select>
                      {!watchedSnmpGroup && submitAttempt && (
                        <ErrorMessageText role="alert">Must select a SNMP credential group</ErrorMessageText>
                      )}
                    </FormControl>
                  </Form.Group>
                </Grid2>
                <Grid2 size={6}>
                  <Form.Group controlId="snmp_version_group">
                    <FormControl fullWidth margin="normal">
                      <InputLabel htmlFor="snmp_version_group" required>
                        SNMP Protocol
                      </InputLabel>
                      <Select
                        id="snmp_version_group"
                        label="SNMP Protocol"
                        value={watchedSnmpVersion || ''}
                        required
                        {...register('snmp_version_group', { required: true })}
                        onChange={(event) => {
                          setValue('snmp_version_group', event.target.value as string)
                        }}
                      >
                        {rsuAllowedSelections.snmp_version_groups?.map((ver) => (
                          <MenuItem key={ver} value={ver}>
                            {ver}
                          </MenuItem>
                        ))}
                      </Select>
                      {!watchedSnmpVersion && submitAttempt && (
                        <ErrorMessageText role="alert">Must select a SNMP protocol</ErrorMessageText>
                      )}
                    </FormControl>
                  </Form.Group>
                </Grid2>
              </Grid2>

              <Form.Group controlId="organizations">
                <FormControl fullWidth margin="normal">
                  <InputLabel htmlFor="organizations" required>
                    Organizations
                  </InputLabel>
                  <Select
                    id="organizations"
                    label="Organizations"
                    multiple
                    required
                    value={watchedOrganizations || []}
                    {...register('organizations', { required: true })}
                    onChange={(event) => {
                      const value = event.target.value as string[]
                      setValue('organizations', value)
                    }}
                  >
                    {rsuAllowedSelections.organizations?.map((org) => (
                      <MenuItem key={org} value={org}>
                        {org}
                      </MenuItem>
                    ))}
                  </Select>
                  {watchedOrganizations?.length === 0 && submitAttempt && (
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
              disabled={isPatchingRsu}
              style={{ position: 'absolute', bottom: 10, right: 10 }}
              className="museo-slab capital-case"
            >
              {isPatchingRsu ? 'Saving...' : 'Apply Changes'}
            </Button>
          </DialogActions>
        </>
      ) : isLoading ? (
        <DialogContent sx={{ width: '600px', padding: '5px 10px' }}>
          <Typography variant={'h4'}>Loading...</Typography>
        </DialogContent>
      ) : (
        <DialogContent sx={{ width: '600px', padding: '5px 10px' }}>
          <Typography variant={'h4'}>
            Unknown RSU IP address. Either this RSU does not exist, or you do not have access to it.{' '}
            <Link to="../">RSUs</Link>
          </Typography>
        </DialogContent>
      )}
    </Dialog>
  )
}

export default AdminEditRsu
