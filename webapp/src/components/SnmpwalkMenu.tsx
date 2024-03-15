import React, { useEffect } from 'react'
import SnmpwalkItem from './SnmpwalkItem'
import { useSelector, useDispatch } from 'react-redux'
import { confirmAlert } from 'react-confirm-alert'
import { Options } from './AdminDeletionOptions'
import { selectRsuManufacturer, selectRsuIpv4 } from '../generalSlices/rsuSlice'
import {
  selectMsgFwdConfig,
  selectErrorState,

  // Actions
  refreshSnmpFwdConfig,
} from '../generalSlices/configSlice'
import Button from '@mui/material/Button'
import DeleteIcon from '@mui/icons-material/Delete'
import RefreshIcon from '@mui/icons-material/Refresh'
import './css/SnmpwalkMenu.css'
import {
  // Actions
  deleteSnmpSet,
} from '../generalSlices/configSlice'
import { IconButton, ThemeProvider, Tooltip, createTheme } from '@mui/material'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../store'

const SnmpwalkMenu = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()

  const msgFwdConfig = useSelector(selectMsgFwdConfig)
  const errorState = useSelector(selectErrorState)

  const rsuIp = useSelector(selectRsuIpv4)
  const rsuManufacturer = useSelector(selectRsuManufacturer)

  useEffect(() => {
    // Refresh Data
    dispatch(refreshSnmpFwdConfig([rsuIp]))
  }, [rsuIp, dispatch])

  const handleDelete = (msgType: string, ip: string) => {
    const buttons = [
      {
        label: 'Yes',
        onClick: () => {
          dispatch(
            deleteSnmpSet({
              ipList: [rsuIp],
              snmpMsgType: msgType,
              destIp: ip,
            })
          )
        },
      },
      {
        label: 'No',
        onClick: () => {},
      },
    ]
    const alertOptions = Options(
      'Delete SNMP Configuration',
      `Are you sure you want to delete SNMP forwarding for ${msgType} messages to the IP: ${ip}? `,
      buttons
    )

    confirmAlert(alertOptions)
  }

  return (
    <div id="snmpdiv">
      <ThemeProvider theme={theme}>
        <div id="msgfwddiv">
          <h2 id="snmpheader">Message Forwarding</h2>
          <Tooltip title="Refresh Message Forwarding">
            <IconButton
              onClick={() => {
                dispatch(refreshSnmpFwdConfig([rsuIp]))
              }}
            >
              <RefreshIcon htmlColor="#b55e12" />
            </IconButton>
          </Tooltip>
        </div>
        {rsuManufacturer === 'Yunex' ? (
          <div>
            {Object.hasOwn(msgFwdConfig, 'rsuXmitMsgFwdingTable') &&
            Object.hasOwn(msgFwdConfig, 'rsuReceivedMsgTable') ? (
              <div>
                <h2 id="snmptxrxheader">TX Forward Table</h2>
                {Object.keys(msgFwdConfig.rsuXmitMsgFwdingTable).map((index) => (
                  <div key={'msgFwd-' + index}>
                    <Button
                      onClick={() =>
                        handleDelete(
                          msgFwdConfig.rsuXmitMsgFwdingTable[index]['Message Type'],
                          msgFwdConfig.rsuXmitMsgFwdingTable[index]['IP']
                        )
                      }
                      startIcon={<DeleteIcon />}
                    >
                      Delete
                    </Button>
                    <SnmpwalkItem
                      key={'snmptxitem-' + index}
                      content={msgFwdConfig.rsuXmitMsgFwdingTable[index]}
                      index={index}
                    />
                  </div>
                ))}
                <h2 id="snmptxrxheader">RX Forward Table</h2>
                {Object.keys(msgFwdConfig.rsuReceivedMsgTable).map((index) => (
                  <div>
                    <Button
                      className="deletbutton"
                      onClick={() =>
                        handleDelete(
                          msgFwdConfig.rsuReceivedMsgTable[index]['Message Type'],
                          msgFwdConfig.rsuReceivedMsgTable[index]['IP']
                        )
                      }
                      startIcon={<DeleteIcon />}
                    >
                      Delete
                    </Button>
                    <SnmpwalkItem
                      key={'snmprxitem-' + index}
                      content={msgFwdConfig.rsuReceivedMsgTable[index]}
                      index={index}
                    />
                  </div>
                ))}
              </div>
            ) : null}
          </div>
        ) : (
          <div>
            {Object.keys(msgFwdConfig).map((index) => (
              <div>
                <Button
                  className="deletbutton"
                  onClick={() => handleDelete(msgFwdConfig[index]['Message Type'], msgFwdConfig[index]['IP'])}
                  startIcon={<DeleteIcon />}
                >
                  Delete
                </Button>
                <SnmpwalkItem key={'snmpitem-' + index} content={msgFwdConfig[index]} index={index} />
              </div>
            ))}
          </div>
        )}
        {errorState !== '' ? <p id="warningtext">{errorState}</p> : <div />}
      </ThemeProvider>
    </div>
  )
}

const theme = createTheme({
  palette: {
    primary: {
      main: '#d16d15',
      light: '#0e2052',
      contrastTextColor: '#0e2052',
    },
    secondary: {
      main: '#d16d15',
      light: '#0e2052',
      contrastTextColor: '#0e2052',
    },
    text: {
      primary: '#ffffff',
      secondary: '#ffffff',
      disabled: '#ffffff',
      hint: '#ffffff',
    },
    action: {
      disabledBackground: 'rgba(209, 109, 21, 0.2)',
      disabled: '#ffffff',
    },
  },
  components: {
    MuiSvgIcon: {
      styleOverrides: {
        root: {
          color: '#d16d15',
        },
      },
    },
  },
  typography: {
    allVariants: {
      color: '#ffffff',
    },
    button: {
      textTransform: 'none',
    },
  },
})

export default SnmpwalkMenu
