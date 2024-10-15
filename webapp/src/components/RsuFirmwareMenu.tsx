import React from 'react'
import { confirmAlert } from 'react-confirm-alert'
import 'react-confirm-alert/src/react-confirm-alert.css'

import {
  selectFirmwareUpgradeAvailable,
  selectFirmwareUpgradeName,
  selectFirmwareUpgradeMsg,
  selectFirmwareUpgradeErr,

  // Actions
  checkFirmwareUpgrade,
  startFirmwareUpgrade,
} from '../generalSlices/configSlice'

import './css/SnmpwalkMenu.css'
import { useAppDispatch, useAppSelector } from '../hooks'

interface RsuFirmwareMenuProps {
  type: string
  rsuIpList: string[]
}

const RsuFirmwareMenu = (props: RsuFirmwareMenuProps) => {
  const dispatch = useAppDispatch()
  const firmwareUpgradeAvailable = useAppSelector(selectFirmwareUpgradeAvailable)
  const firmwareUpgradeName = useAppSelector(selectFirmwareUpgradeName)
  const firmwareUpgradeMsg = useAppSelector(selectFirmwareUpgradeMsg)
  const firmwareUpgradeErr = useAppSelector(selectFirmwareUpgradeErr)

  const options = {
    title: 'RSU Firmware Upgrade',
    message:
      'Are you sure you would like to run a firmware upgrade? This can take up to an extended period of time to complete: 5 - 60 minutes. Once an upgrade has started, you may continue to use or close the CV Manager web application without interfering with the upgrade.',
    buttons: [
      {
        label: 'Yes',
        onClick: () => dispatch(startFirmwareUpgrade(props.rsuIpList)),
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
      <h2 className="firmwareHeader">Firmware Upgrade</h2>

      {props.type === 'single_rsu' && (
        <div>
          {firmwareUpgradeAvailable ? (
            <div>
              <div id="firmwarediv">
                <p id="firmwarenoticetext" role="status">
                  A firmware upgrade is available!
                </p>
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
                      <p id="warningtext" role="alert">
                        {firmwareUpgradeMsg}
                      </p>
                    ) : (
                      <p id="successtext" role="status">
                        {firmwareUpgradeMsg}
                      </p>
                    )}
                  </div>
                )}
              </div>

              <button id="refreshbtn" onClick={() => dispatch(checkFirmwareUpgrade(props.rsuIpList))}>
                Check For Upgrade Availability
              </button>
            </div>
          )}
        </div>
      )}

      {props.type === 'multi_rsu' && (
        <div>
          <div id="firmwarediv">
            <p id="firmwaretext">
              Submit all selected RSUs to have their firmware upgraded if available. RSUs that are already up to date
              will be skipped. If the RSU is offline, it will be marked for an upgrade and will be upgraded when it
              comes back online.
            </p>
          </div>

          <button id="refreshbtn" onClick={() => confirmAlert(options)}>
            Run Firmware Upgrades
          </button>
        </div>
      )}
    </div>
  )
}

export default RsuFirmwareMenu
