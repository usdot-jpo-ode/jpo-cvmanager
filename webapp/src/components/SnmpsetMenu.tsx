import { ThunkDispatch, AnyAction } from '@reduxjs/toolkit'
import React from 'react'
import { useDispatch, useSelector } from 'react-redux'

import {
  selectChangeSuccess,
  selectErrorState,
  selectDestIp,
  selectSnmpMsgType,
  selectSnmpFilterMsg,
  selectSnmpFilterErr,

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

export type SnmpsetMenuProps = {
  type: string
  rsuIpList: string[]
}

const SnmpsetMenu = (props: SnmpsetMenuProps) => {
  const { type, rsuIpList } = props
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()

  const changeSuccess = useSelector(selectChangeSuccess)
  const errorState = useSelector(selectErrorState)
  const destIp = useSelector(selectDestIp)
  const snmpMsgType = useSelector(selectSnmpMsgType)
  const snmpFilterMsg = useSelector(selectSnmpFilterMsg)
  const snmpFilterErr = useSelector(selectSnmpFilterErr)

  const rsuIp = useSelector(selectRsuIpv4)
  const rsuManufacturer = useSelector(selectRsuManufacturer)

  return (
    <div id="snmpdiv">
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
            <option value="spat">SPaT</option>
            <option value="map">MAP</option>
            <option value="srm">SRM</option>
            <option value="ssm">SSM</option>
            <option value="tim">TIM</option>
          </select>
        </label>
      </form>

      <button id="refreshbtn" onClick={() => dispatch(submitSnmpSet(rsuIpList))}>
        Add Forwarding
      </button>
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

      {changeSuccess ? (
        <div>
          <p id="successtext">Successful write to RSU</p>
        </div>
      ) : (
        <p id="infotext"></p>
      )}
      {errorState !== '' ? <p id="warningtext" role="alert">{errorState}</p> : <div />}

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
          <button id="refreshbtn" onClick={() => dispatch(filterSnmp([rsuIp]))}>
            Apply TX Filter
          </button>
          {snmpFilterMsg !== '' ? (
            <div>
              {snmpFilterErr === true ? (
                <p id="warningtext" role="alert">{snmpFilterMsg}</p>
              ) : (
                <p id="successtext" role="status">{snmpFilterMsg}</p>
              )}
            </div>
          ) : (
            <div />
          )}
        </div>
      ) : (
        <div />
      )}
    </div>
  )
}

export default SnmpsetMenu
