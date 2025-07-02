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
import { Button, Typography, useTheme } from '@mui/material'
import RefreshIcon from '@mui/icons-material/Refresh'

const RsuRebootMenu = () => {
  const theme = useTheme()
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
      {changeSuccess ? (
        <div>
          <Typography color={theme.palette.success.light} role="status">
            Successful reboot, the RSU will now be offline for a brief time
          </Typography>
        </div>
      ) : (
        <Typography>Warning: This action could result in taking the RSU offline</Typography>
      )}
      <Button
        className="museo-slab capital-case"
        variant="outlined"
        size="medium"
        color="info"
        startIcon={<RefreshIcon />}
        onClick={() => confirmAlert(options)}
        sx={{ mt: '10px', mb: '10px' }}
      >
        Reboot RSU
      </Button>
    </div>
  )
}

export default RsuRebootMenu
