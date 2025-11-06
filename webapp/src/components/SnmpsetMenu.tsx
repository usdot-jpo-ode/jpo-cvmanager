import { ThunkDispatch, AnyAction } from '@reduxjs/toolkit'
import React from 'react'
import { useDispatch, useSelector } from 'react-redux'

import {
  selectDestIp,
  selectSnmpMsgType,

  // Actions
  submitSnmpSet,
  deleteSnmpSet,
  setDestIp,
  setMsgType,
  setIncludeSecurityHeader,
} from '../generalSlices/configSlice'

import { RootState } from '../store'

import './css/SnmpwalkMenu.css'
import toast from 'react-hot-toast'
import {
  Button,
  Checkbox,
  FormControl,
  FormControlLabel,
  Grid2,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { ControlPointOutlined, DeleteOutline } from '@mui/icons-material'

export type SnmpsetMenuProps = {
  type: string
  rsuIpList: string[]
}

const SnmpsetMenu = (props: SnmpsetMenuProps) => {
  const { type, rsuIpList } = props
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()

  const destIp = useSelector(selectDestIp)
  const snmpMsgType = useSelector(selectSnmpMsgType)

  return (
    <div>
      <form id="snmpform">
        <Grid2 container spacing={2}>
          <Grid2 size={8}>
            <FormControl fullWidth margin="normal">
              <TextField
                label="Destination IP"
                placeholder="Enter Destination IP"
                id="snmpinput"
                color="info"
                variant="outlined"
                value={destIp}
                onChange={(event) => {
                  const ip = event.target.value as string
                  dispatch(setDestIp(ip))
                }}
                slotProps={{
                  inputLabel: {
                    shrink: true,
                  },
                }}
              />
            </FormControl>
          </Grid2>
          <Grid2 size={4}>
            <FormControl fullWidth margin="normal">
              <InputLabel htmlFor="msg-type-select">Message Type</InputLabel>
              <Select
                label="Message Type"
                id="msg-type-select"
                value={snmpMsgType}
                onChange={(event) => {
                  const msgType = event.target.value as string
                  dispatch(setMsgType(msgType))
                }}
              >
                {['bsm', 'spat', 'map', 'srm', 'ssm', 'tim'].map((msgType) => (
                  <MenuItem key={msgType} value={msgType}>
                    {msgType === 'spat' ? 'SPaT' : msgType.toUpperCase()}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid2>
        </Grid2>
        <FormControlLabel
          control={
            <Checkbox
              onChange={(e) => {
                dispatch(setIncludeSecurityHeader(e.target.checked))
              }}
            />
          }
          label="Security Header"
        />
      </form>
      <Stack spacing={2} direction="column">
        <Button
          className="museo-slab capital-case"
          variant="contained"
          size="medium"
          startIcon={<ControlPointOutlined />}
          onClick={() =>
            dispatch(submitSnmpSet(rsuIpList)).then((data: any) => {
              if (data.payload.changeSuccess) {
                toast.success('Forwarding Changes Applied Successfully')
              } else {
                toast.error('Failed to add forwarding: ' + data.errorState)
              }
            })
          }
          sx={{
            width: 'fit-content',
          }}
        >
          Add Forwarding
        </Button>

        {type !== 'single_rsu' && (
          <Button
            className="museo-slab capital-case"
            variant="contained"
            size="medium"
            startIcon={<DeleteOutline />}
            sx={{
              marginTop: '10px',
              width: 'fit-content',
            }}
            onClick={() =>
              dispatch(
                deleteSnmpSet({
                  ipList: rsuIpList,
                  snmpMsgType: snmpMsgType,
                  destIp: destIp,
                })
              )
            }
          >
            Delete Forwarding
          </Button>
        )}
      </Stack>

      {type !== 'single_rsu' ? (
        <div style={{ marginTop: '16px' }}>
          <Typography variant="body1">
            By specifying a destination IP address along with a message type, you can add message forwarding to any of
            the selected RSUs or delete message forwarding from any RSUs that already have that configuration.
          </Typography>
        </div>
      ) : null}
    </div>
  )
}

export default SnmpsetMenu
