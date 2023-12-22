import React from 'react'
import { confirmAlert } from 'react-confirm-alert'
import 'react-confirm-alert/src/react-confirm-alert.css'
import { useDispatch, useSelector } from 'react-redux'

import {
  selectFirmwareUpgradeAvailable,
  selectFirmwareUpgradeName,
  selectFirmwareUpgradeMsg,
  selectFirmwareUpgradeErr,

  // Actions
  checkFirmwareUpgrade,
  startFirmwareUpgrade,
} from '../generalSlices/configSlice'

import { selectRsuIpv4 } from '../generalSlices/rsuSlice'

import './css/SnmpwalkMenu.css'

const RsuFirmwareMenu = () => {
  const dispatch = useDispatch()
  const firmwareUpgradeAvailable = useSelector(selectFirmwareUpgradeAvailable)
  const firmwareUpgradeName = useSelector(selectFirmwareUpgradeName)
  const firmwareUpgradeMsg = useSelector(selectFirmwareUpgradeMsg)
  const firmwareUpgradeErr = useSelector(selectFirmwareUpgradeErr)

  const rsuIp = useSelector(selectRsuIpv4)

  const options = {
    title: 'RSU Firmware Upgrade',
    message:
      'Are you sure you would like to run this firmware upgrade? This can take up to an extended period of time to complete: 5 - 60 minutes. Once an upgrade has started, you may continue to use or close the CV Manager web application without interfering with the upgrade.',
    buttons: [
      {
        label: 'Yes',
        onClick: () => dispatch(startFirmwareUpgrade([rsuIp])),
      },
      {
        label: 'No',
        onClick: () => {},
      },
    ],
    childrenElement: () => <div />,
    closeOnEscape: true,
    closeOnClickOutside: true,
    keyCodeForClose: [8, 32],
    willUnmount: () => {},
    afterClose: () => {},
    onClickOutside: () => {},
    onKeypressEscape: () => {},
  }

  return (
    <div id="snmpdiv">
      <h2 class="firmwareHeader">Firmware Upgrade</h2>

      {firmwareUpgradeAvailable ? (
        <div>
          <div id="firmwarediv">
            <p id="firmwarenoticetext">A firmware upgrade is available!</p>
            <p id="firmwaresecondarytext">
              <b>Version: {firmwareUpgradeName}</b>
            </p>
          </div>

          <button id="refreshbtn" onClick={() => confirmAlert(options)}>
            Run Firmware Upgrade
          </button>
        </div>
      ) : (
        <div>
          <div id="firmwarediv">
            <p id="firmwaretext">Check for the latest available RSU firmware upgrades and install them</p>
            {firmwareUpgradeMsg !== '' && (
              <div>
                {firmwareUpgradeErr ? (
                  <p id="warningtext">{firmwareUpgradeMsg}</p>
                ) : (
                  <p id="successtext">{firmwareUpgradeMsg}</p>
                )}
              </div>
            )}
          </div>

          <button id="refreshbtn" onClick={() => dispatch(checkFirmwareUpgrade([rsuIp]))}>
            Check For Upgrade Availability
          </button>
        </div>
      )}
    </div>
  )
}

export default RsuFirmwareMenu
