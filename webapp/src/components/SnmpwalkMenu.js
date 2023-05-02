import React, { useEffect } from 'react'
import SnmpwalkItem from './SnmpwalkItem'
import { useSelector, useDispatch } from 'react-redux'
import { confirmAlert } from 'react-confirm-alert'
import { Options } from './AdminDeletionOptions'
import { selectRsuManufacturer, selectRsuIpv4 } from '../slices/rsuSlice'
import {
    selectMsgFwdConfig,
    selectErrorState,

    // Actions
    refreshSnmpFwdConfig,
} from '../slices/configSlice'
import Button from '@mui/material/Button'
import DeleteIcon from '@mui/icons-material/Delete'
import './css/SnmpwalkMenu.css'
import {
    // Actions
    deleteSnmpSet,
} from '../slices/configSlice'

const SnmpwalkMenu = (props) => {
    const dispatch = useDispatch()

    const msgFwdConfig = useSelector(selectMsgFwdConfig)
    const errorState = useSelector(selectErrorState)

    const rsuIp = useSelector(selectRsuIpv4)
    const rsuManufacturer = useSelector(selectRsuManufacturer)

    useEffect(() => {
        // Refresh Data
        dispatch(refreshSnmpFwdConfig([rsuIp]))
    }, [rsuIp, dispatch])

    const handleDelete = (msgType, ip) => {
        const buttons = [
            {
                label: 'Yes',
                onClick: () => deleteSnmpSet(ip, msgType),
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
            <h2 id="snmpheader">Current Message Forwarding</h2>

            {rsuManufacturer === 'Yunex' ? (
                <div>
                    {Object.hasOwn(msgFwdConfig, 'rsuXmitMsgFwdingTable') &&
                    Object.hasOwn(msgFwdConfig, 'rsuReceivedMsgTable') ? (
                        <div>
                            <h2 id="snmptxrxheader">TX Forward Table</h2>
                            {Object.keys(
                                msgFwdConfig.rsuXmitMsgFwdingTable
                            ).map((index) => (
                                <div key={'msgFwd-' + index}>
                                    <Button
                                        className="deletebutton"
                                        onClick={() =>
                                            handleDelete(
                                                msgFwdConfig
                                                    .rsuXmitMsgFwdingTable[
                                                    index
                                                ]['Message Type'],
                                                msgFwdConfig
                                                    .rsuXmitMsgFwdingTable[
                                                    index
                                                ]['IP'],
                                                index
                                            )
                                        }
                                        startIcon={<DeleteIcon />}
                                    >
                                        Delete
                                    </Button>
                                    <SnmpwalkItem
                                        key={'snmptxitem-' + index}
                                        content={
                                            msgFwdConfig.rsuXmitMsgFwdingTable[
                                                index
                                            ]
                                        }
                                        index={index}
                                    />
                                </div>
                            ))}
                            <h2 id="snmptxrxheader">RX Forward Table</h2>
                            {Object.keys(msgFwdConfig.rsuReceivedMsgTable).map(
                                (index) => (
                                    <div>
                                        <Button
                                            className="deletbutton"
                                            onClick={() =>
                                                handleDelete(
                                                    msgFwdConfig
                                                        .rsuReceivedMsgTable[
                                                        index
                                                    ]['Message Type'],
                                                    msgFwdConfig
                                                        .rsuReceivedMsgTable[
                                                        index
                                                    ]['IP'],
                                                    index
                                                )
                                            }
                                            startIcon={<DeleteIcon />}
                                        >
                                            Delete
                                        </Button>
                                        <SnmpwalkItem
                                            key={'snmprxitem-' + index}
                                            content={
                                                msgFwdConfig
                                                    .rsuReceivedMsgTable[index]
                                            }
                                            index={index}
                                        />
                                    </div>
                                )
                            )}
                        </div>
                    ) : null}
                </div>
            ) : (
                <div>
                    {Object.keys(msgFwdConfig).map((index) => (
                        <div>
                            <Button
                                className="deletbutton"
                                onClick={() =>
                                    handleDelete(
                                        msgFwdConfig[index]['Message Type'],
                                        msgFwdConfig[index]['IP'],
                                        index
                                    )
                                }
                                startIcon={<DeleteIcon />}
                            >
                                Delete
                            </Button>
                            <SnmpwalkItem
                                key={'snmpitem-' + index}
                                content={msgFwdConfig[index]}
                                index={index}
                            />
                        </div>
                    ))}
                </div>
            )}

            {errorState !== '' ? <p id="warningtext">{errorState}</p> : <div />}
        </div>
    )
}

export default SnmpwalkMenu
