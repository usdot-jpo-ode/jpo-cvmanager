import { ThunkDispatch, AnyAction } from '@reduxjs/toolkit'
import React from 'react'
import { confirmAlert } from 'react-confirm-alert'
import 'react-confirm-alert/src/react-confirm-alert.css'
import { useDispatch, useSelector } from 'react-redux'
import { RootState } from '../store'

import {
  selectRebootChangeSuccess,

  // Actions
  rebootRsu,
} from '../generalSlices/configSlice'

import { selectRsuIpv4 } from '../generalSlices/rsuSlice'

import './css/SnmpwalkMenu.css'
import { Button, Typography } from '@mui/material'

const RsuRebootMenu = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const changeSuccess = useSelector(selectRebootChangeSuccess)

  const rsuIp = useSelector(selectRsuIpv4)

  const options = {
    title: 'RSU Reboot',
    message: 'Are you sure you want to perform a reboot?',
    buttons: [
      {
        label: 'Yes',
        onClick: () => dispatch(rebootRsu([rsuIp])),
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
      <h2 id="snmpheader">RSU Reboot</h2>

      <Button variant="contained" size="small" onClick={() => confirmAlert(options)} sx={{ mt: '10px', mb: '10px' }}>
        Reboot
      </Button>

      {changeSuccess ? (
        <div>
          <Typography color="success" role="status">
            Successful reboot, the RSU will now be offline for a brief time
          </Typography>
        </div>
      ) : (
        <Typography>Warning: This action could result in taking the RSU offline</Typography>
      )}
    </div>
  )
}

export default RsuRebootMenu
