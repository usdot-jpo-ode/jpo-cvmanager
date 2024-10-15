import React, { useEffect } from 'react'
import SnmpwalkItem from './SnmpwalkItem'
import { useAppDispatch, useAppSelector } from '../hooks'
import { confirmAlert } from 'react-confirm-alert'
import { Options } from './AdminDeletionOptions'
import { selectRsuManufacturer, selectRsuIpv4 } from '../generalSlices/rsuSlice'
import {
  selectMsgFwdConfig,

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
import { IconButton, ThemeProvider, StyledEngineProvider, Tooltip } from '@mui/material'
import toast from 'react-hot-toast'
import { snmpWalkMenuTheme } from '../styles'

const SnmpwalkMenu = () => {
  const dispatch = useAppDispatch()

  const msgFwdConfig = useAppSelector(selectMsgFwdConfig)

  const rsuIp = useAppSelector(selectRsuIpv4)
  const rsuManufacturer = useAppSelector(selectRsuManufacturer)

  useEffect(() => {
    // Refresh Data
    dispatch(refreshSnmpFwdConfig(rsuIp))
  }, [rsuIp, dispatch])

  const handleDelete = (countsMsgType: string, ip: string) => {
    const buttons = [
      {
        label: 'Yes',
        onClick: () => {
          dispatch(
            deleteSnmpSet({
              ipList: [rsuIp],
              snmpMsgType: countsMsgType,
              destIp: ip,
            })
          ).then((data: any) => {
            data.payload.changeSuccess
              ? toast.success('Successfully deleted SNMP forwarding')
              : toast.error('Failed to delete SNMP forwarding: ' + data.payload.errorState)
          })
        },
      },
      {
        label: 'No',
        onClick: () => {},
      },
    ]
    const alertOptions = Options(
      'Delete SNMP Configuration',
      `Are you sure you want to delete SNMP forwarding for ${countsMsgType} messages to the IP: ${ip}? `,
      buttons
    )

    confirmAlert(alertOptions)
  }

  return (
    <div id="snmpdiv">
      <StyledEngineProvider injectFirst>
        <ThemeProvider theme={snmpWalkMenuTheme}>
          <div id="msgfwddiv">
            <h2 id="snmpheader">Message Forwarding</h2>
            <Tooltip title="Refresh Message Forwarding">
              <IconButton
                onClick={() => {
                  dispatch(refreshSnmpFwdConfig(rsuIp))
                }}
                size="medium"
              >
                <RefreshIcon htmlColor="#b55e12" />
              </IconButton>
            </Tooltip>
          </div>

          <div>
            {Object.hasOwn(msgFwdConfig, 'rsuXmitMsgFwdingTable') &&
            Object.hasOwn(msgFwdConfig, 'rsuReceivedMsgTable') ? (
              <div>
                <h2 id="snmptxheader">TX Forward Table</h2>
                {Object.keys(msgFwdConfig.rsuXmitMsgFwdingTable).map((index) => (
                  <div key={'msgFwd-' + index}>
                    <Button
                      className="deletebutton"
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

                <h2 id="snmprxheader">RX Forward Table</h2>
                {Object.keys(msgFwdConfig.rsuReceivedMsgTable).map((index) => (
                  <div>
                    <Button
                      className="deletebutton"
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
            ) : (
              <div>
                {Object.keys(msgFwdConfig).map((index) => (
                  <div>
                    <Button
                      className="deletebutton"
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
          </div>
        </ThemeProvider>
      </StyledEngineProvider>
    </div>
  )
}

export default SnmpwalkMenu
