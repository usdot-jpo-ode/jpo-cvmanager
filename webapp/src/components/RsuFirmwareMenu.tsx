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

import './css/SnmpwalkMenu.css'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../store'
import { Button, Typography, useTheme } from '@mui/material'
import RefreshIcon from '@mui/icons-material/Refresh'

interface RsuFirmwareMenuProps {
  type: string
  rsuIpList: string[]
}

const RsuFirmwareMenu = (props: RsuFirmwareMenuProps) => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const theme = useTheme()
  const firmwareUpgradeAvailable = useSelector(selectFirmwareUpgradeAvailable)
  const firmwareUpgradeName = useSelector(selectFirmwareUpgradeName)
  const firmwareUpgradeMsg = useSelector(selectFirmwareUpgradeMsg)
  const firmwareUpgradeErr = useSelector(selectFirmwareUpgradeErr)

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
    <div>
      {props.type === 'single_rsu' && (
        <div>
          {firmwareUpgradeAvailable ? (
            <div>
              <div style={{ marginBottom: '15px' }}>
                <Typography color="primary">A firmware upgrade is available!</Typography>
                <Typography style={{ marginTop: '10px' }}>Version: {firmwareUpgradeName}</Typography>
              </div>

              <Button
                variant="contained"
                size="small"
                onClick={() => confirmAlert(options)}
                style={{
                  marginRight: '20px',
                }}
              >
                Run Firmware Upgrade
              </Button>
            </div>
          ) : (
            <div>
              <div id="firmwarediv" style={{ marginBottom: '15px' }}>
                <p id="firmwaretext">Check for the latest available RSU firmware upgrades and install them</p>
                {firmwareUpgradeMsg !== '' && (
                  <div
                    style={{
                      marginTop: '10px',
                      fontWeight: 500,
                    }}
                  >
                    {firmwareUpgradeErr ? (
                      <Typography color={theme.palette.error.main} role="alert" variant="subtitle1">
                        {firmwareUpgradeMsg}
                      </Typography>
                    ) : (
                      <Typography color="success" role="status">
                        {firmwareUpgradeMsg}
                      </Typography>
                    )}
                  </div>
                )}
              </div>
              <Button
                variant="outlined"
                startIcon={<RefreshIcon />}
                size="medium"
                color="info"
                onClick={() => dispatch(checkFirmwareUpgrade(props.rsuIpList))}
                style={{
                  marginRight: '20px',
                }}
              >
                Check For Upgrade Availability
              </Button>
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
          <Button
            variant="outlined"
            startIcon={<RefreshIcon />}
            size="medium"
            id="refreshbtn"
            color="info"
            onClick={() => confirmAlert(options)}
            style={{
              marginRight: '20px',
              marginTop: '10px',
            }}
          >
            Check For Upgrade Availability
          </Button>
        </div>
      )}
    </div>
  )
}

export default RsuFirmwareMenu
