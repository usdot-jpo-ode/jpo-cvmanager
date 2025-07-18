import React, { useEffect } from 'react'
import SnmpwalkItem from './SnmpwalkItem'
import { useSelector, useDispatch } from 'react-redux'
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
import { Button, Tooltip } from '@mui/material'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../store'
import toast from 'react-hot-toast'

const SnmpwalkMenu = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()

  const msgFwdConfig = useSelector(selectMsgFwdConfig)

  const rsuIp = useSelector(selectRsuIpv4)

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
            if (data.payload.changeSuccess) {
              toast.success('Successfully deleted SNMP forwarding')
            } else {
              toast.error('Failed to delete SNMP forwarding: ' + data.payload.errorState)
            }
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
      <div id="msgfwddiv">
        <Tooltip title="Refresh Message Forwarding">
          <Button
            startIcon={<RefreshIcon />}
            variant="outlined"
            onClick={() => {
              dispatch(refreshSnmpFwdConfig(rsuIp))
            }}
            size="large"
            sx={{
              marginTop: '20px',
            }}
            color="info"
            className="museo-slab capital-case"
          >
            Refresh Message Forwarding
          </Button>
        </Tooltip>
      </div>
    </div>
  )
}

export default SnmpwalkMenu
