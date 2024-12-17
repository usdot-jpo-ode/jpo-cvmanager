import React, { useEffect } from 'react'
import SnmpwalkItem from './SnmpwalkItem'
import { useAppDispatch, useAppSelector } from '../hooks'
import { confirmAlert } from 'react-confirm-alert'
import { Options } from './AdminDeletionOptions'
import { selectRsuIpv4 } from '../generalSlices/rsuSlice'
import {
  selectMsgFwdConfig,

  // Actions
  refreshSnmpFwdConfig,
} from '../generalSlices/configSlice'
import RefreshIcon from '@mui/icons-material/Refresh'
import './css/SnmpwalkMenu.css'
import {
  // Actions
  deleteSnmpSet,
} from '../generalSlices/configSlice'
import { IconButton, Tooltip } from '@mui/material'
import toast from 'react-hot-toast'

const SnmpwalkMenu = () => {
  const dispatch = useAppDispatch()

  const msgFwdConfig = useAppSelector(selectMsgFwdConfig)

  const rsuIp = useAppSelector(selectRsuIpv4)

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
    <div>
      <div id="msgfwddiv">
        <h2 id="snmpheader">Message Forwarding</h2>
        <Tooltip title="Refresh Message Forwarding">
          <IconButton
            onClick={() => {
              dispatch(refreshSnmpFwdConfig(rsuIp))
            }}
            size="medium"
          >
            <RefreshIcon />
          </IconButton>
        </Tooltip>
      </div>

      <div>
        {Object.hasOwn(msgFwdConfig, 'rsuXmitMsgFwdingTable') && Object.hasOwn(msgFwdConfig, 'rsuReceivedMsgTable') ? (
          <div>
            <h2 id="snmptxheader">TX Forward Table</h2>
            {Object.keys(msgFwdConfig.rsuXmitMsgFwdingTable).map((index) => (
              <div key={'msgFwd-' + index}>
                <SnmpwalkItem
                  key={'snmptxitem-' + index}
                  content={msgFwdConfig.rsuXmitMsgFwdingTable[index]}
                  handleDelete={handleDelete}
                  index={index}
                />
              </div>
            ))}

            <h2 id="snmprxheader">RX Forward Table</h2>
            {Object.keys(msgFwdConfig.rsuReceivedMsgTable).map((index) => (
              <div>
                <SnmpwalkItem
                  key={'snmprxitem-' + index}
                  content={msgFwdConfig.rsuReceivedMsgTable[index]}
                  handleDelete={handleDelete}
                  index={index}
                />
              </div>
            ))}
          </div>
        ) : (
          <div>
            {Object.keys(msgFwdConfig).map((index) => (
              <div>
                <SnmpwalkItem
                  key={'snmpitem-' + index}
                  content={msgFwdConfig[index]}
                  handleDelete={handleDelete}
                  index={index}
                />
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

export default SnmpwalkMenu
