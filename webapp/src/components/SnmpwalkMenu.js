import React, { useEffect } from 'react'
import SnmpwalkItem from './SnmpwalkItem'
import { useSelector, useDispatch } from 'react-redux'
import { selectRsuManufacturer, selectRsuIpv4 } from '../slices/rsuSlice'
import {
    selectMsgFwdConfig,
    selectErrorState,

    // Actions
    refreshSnmpFwdConfig,
} from '../slices/configSlice'

import './css/SnmpwalkMenu.css'

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
                                <SnmpwalkItem
                                    key={'snmptxitem-' + index}
                                    content={
                                        msgFwdConfig.rsuXmitMsgFwdingTable[
                                            index
                                        ]
                                    }
                                    index={index}
                                />
                            ))}
                            <h2 id="snmptxrxheader">RX Forward Table</h2>
                            {Object.keys(msgFwdConfig.rsuReceivedMsgTable).map(
                                (index) => (
                                    <SnmpwalkItem
                                        key={'snmprxitem-' + index}
                                        content={
                                            msgFwdConfig.rsuReceivedMsgTable[
                                                index
                                            ]
                                        }
                                        index={index}
                                    />
                                )
                            )}
                        </div>
                    ) : null}
                </div>
            ) : (
                <div>
                    {Object.keys(msgFwdConfig).map((index) => (
                        <SnmpwalkItem
                            key={'snmpitem-' + index}
                            content={msgFwdConfig[index]}
                            index={index}
                        />
                    ))}
                </div>
            )}

            {errorState !== '' ? <p id="warningtext">{errorState}</p> : <div />}
        </div>
    )
}

export default SnmpwalkMenu
