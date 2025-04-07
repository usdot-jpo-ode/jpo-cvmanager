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
import { Button } from '@mui/material'

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
      <h2 id="snmpheader">Message Forwarding</h2>
      <form id="snmpform">
        <label id="snmplabel">
          <strong>Destination IP:</strong>
          <input id="snmpinput" type="text" value={destIp} onChange={(e) => dispatch(setDestIp(e.target.value))} />
        </label>
        <label id="snmplabel">
          <strong>Message Type:</strong>
          <select id="snmpdropdown" value={snmpMsgType} onChange={(e) => dispatch(setMsgType(e.target.value))}>
            <option value="bsm">BSM</option>
            <option value="map">Map</option>
            <option value="spat">SPaT</option>
            <option value="srm">SRM</option>
            <option value="ssm">SSM</option>
            <option value="tim">TIM</option>
          </select>
        </label>
        <label id="snmplabel">
          <strong>Security Header:</strong>
          <input
            id="securityHeaderCheckbox"
            type="checkbox"
            onChange={(e) => dispatch(setIncludeSecurityHeader(e.target.checked))}
          />
        </label>
      </form>

      <Button
        variant="contained"
        size="small"
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
      {type !== 'single_rsu' && (
        <button
          id="refreshbtn"
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
        </button>
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
    </div>
  )
}

export default SnmpsetMenu
