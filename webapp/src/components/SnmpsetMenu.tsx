import { ThunkDispatch, AnyAction } from '@reduxjs/toolkit'
import React from 'react'
import { useDispatch, useSelector } from 'react-redux'

import {
  selectDestIp,
  selectSnmpMsgType,

  // Actions
  submitSnmpSet,
  deleteSnmpSet,
  filterSnmp,
  setDestIp,
  setMsgType,
} from '../generalSlices/configSlice'

import { selectRsuIpv4, selectRsuManufacturer } from '../generalSlices/rsuSlice'
import { RootState } from '../store'

import './css/SnmpwalkMenu.css'
import toast from 'react-hot-toast'
import { Box, Button, FormControl, Grid2, InputLabel, MenuItem, OutlinedInput, Select } from '@mui/material'
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

  const rsuIp = useSelector(selectRsuIpv4)
  const rsuManufacturer = useSelector(selectRsuManufacturer)

  return (
    <div>
      <form id="snmpform">
        <Grid2 container spacing={2}>
          <Grid2 size={6}>
            <FormControl fullWidth margin="normal">
              <InputLabel htmlFor="snmpinput">Destination IP</InputLabel>
              <OutlinedInput
                label="Destination IP"
                id="snmpinput"
                color="info"
                value={destIp}
                onChange={(event) => {
                  var ip = event.target.value as string
                  dispatch(setDestIp(ip))
                }}
              />
            </FormControl>
          </Grid2>
          <Grid2 size={6}>
            <FormControl fullWidth margin="normal">
              <InputLabel htmlFor="msg-type-select">Message Type</InputLabel>
              <Select
                label="Message Type"
                id="msg-type-select"
                value={snmpMsgType}
                onChange={(event) => {
                  var msgType = event.target.value as string
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
      </form>

      <Button
        className="museo-slab"
        variant="contained"
        size="medium"
        startIcon={<ControlPointOutlined />}
        onClick={() =>
          dispatch(submitSnmpSet(rsuIpList)).then((data: any) => {
            data.payload.changeSuccess
              ? toast.success('Forwarding Changes Applied Successfully')
              : toast.error('Failed to add forwarding: ', data.errorState)
          })
        }
      >
        Add Forwarding
      </Button>
      <Box />
      {type !== 'single_rsu' && (
        <Button
          className="museo-slab"
          variant="contained"
          size="medium"
          startIcon={<DeleteOutline />}
          sx={{
            marginTop: '10px',
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

      {type !== 'single_rsu' ? (
        <div>
          <p id="snmpfiltertext" style={{ marginTop: '40px' }}>
            By specifying a destination IP address along with a message type, you can add message forwarding to any of
            the selected RSUs or delete message forwarding from any RSUs that already have that configuration.
          </p>
        </div>
      ) : (
        <div />
      )}

      {rsuManufacturer === 'Yunex' ? (
        <div>
          <p id="snmpfiltertext" style={{ marginTop: '40px' }}>
            Yunex RSUs use different SNMP tables for message TX and RX forwarding. <br /> BSM and SSM are on the RX
            table. MAP, SPaT and SRM are on the TX table. <br /> Start over from the 1 index for each table.
          </p>
        </div>
      ) : (
        <div />
      )}

      {rsuManufacturer === 'Commsignia' ? (
        <div>
          <p id="snmpfiltertext" style={{ marginTop: '40px' }}>
            If you are configuring SPaT or MAP forwarding, apply the TX message <br /> filter after your configuration
            has been applied
          </p>
          <Button
            className="museo-slab"
            variant="contained"
            size="medium"
            startIcon={<ControlPointOutlined />}
            onClick={() =>
              dispatch(filterSnmp([rsuIp])).then((data: any) => {
                data.snmpFilterErr
                  ? toast.error('Failed to apply TX filter: ', data.snmpFilterMsg)
                  : toast.success('TX Filter Applied Successfully')
              })
            }
          >
            Apply TX Filter
          </Button>
        </div>
      ) : (
        <div />
      )}
    </div>
  )
}

export default SnmpsetMenu
