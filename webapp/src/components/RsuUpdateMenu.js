import React, { Component } from 'react'
import EnvironmentVars from './../EnvironmentVars'
import { confirmAlert } from 'react-confirm-alert'
import RsuUpdateItem from './RsuUpdateItem'
import 'react-confirm-alert/src/react-confirm-alert.css'

import './css/SnmpwalkMenu.css'

class RsuUpdateMenu extends Component {
    constructor(props) {
        super(props)
        this.state = {
            checked: false,
            osUpdateAvailable: [],
            fwUpdateAvailable: [],
        }
    }

    verifyOS = {
        title: 'RSU OS Update',
        message: "Are you sure you want to update the RSU's operating system?",
        buttons: [
            {
                label: 'Yes',
                onClick: () => this.performOSUpdate(),
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

    verifyFW = {
        title: 'RSU Firmware Update',
        message: "Are you sure you want to update the RSU's firmware?",
        buttons: [
            {
                label: 'Yes',
                onClick: () => this.performFWUpdate(),
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

    performCheck = async () => {
        if (this.props.isLoginActive()) {
            this.props.setLoading(true)
            let ipList = Object.entries(this.props.ipList).map((rsu) => {
                return rsu[1]
            })
            for (let i = 0; i < this.props.ipList.length; i++) {
                try {
                    const res = await fetch(EnvironmentVars.rsuRestAuth, {
                        method: 'POST',
                        body: JSON.stringify({
                            command: 'checkforupdates',
                            rsu_ip: ipList[i]['properties']['Ipv4Address'],
                            args: {},
                        }),
                        headers: {
                            'Content-Type': 'application/json',
                            Authorization: this.props.token,
                        },
                    })

                    const data = await res.json()
                    if (data.os === true) {
                        this.setState(
                            {
                                osUpdateAvailable: [
                                    ...this.state.osUpdateAvailable,
                                    ipList[i].properties.Ipv4Address,
                                ],
                            },
                            () => console.log(this.state.osUpdateAvailable)
                        )
                    }
                    if (data.firmware === true) {
                        this.setState(
                            {
                                fwUpdateAvailable: [
                                    ...this.state.fwUpdateAvailable,
                                    ipList[i].properties.Ipv4Address,
                                ],
                            },
                            () => console.log(this.state.osUpdateAvailable)
                        )
                    }
                    this.setState({ checked: true })
                    this.props.setLoading(false)
                } catch (exception_var) {
                    this.props.setLoading(false)
                    console.error(exception_var)
                }
            }
        }
    }

    performOSUpdate = async (ip) => {
        if (this.props.isLoginActive()) {
            try {
                const res = await fetch(EnvironmentVars.rsuRestAuth, {
                    method: 'POST',
                    body: JSON.stringify({
                        command: 'osupdate',
                        rsu_ip: ip,
                        args: {},
                    }),
                    headers: {
                        'Content-Type': 'application/json',
                        Authorization: this.props.token,
                    },
                })

                const data = await res.json()
                console.log(data)
            } catch (exception_var) {
                console.error(exception_var)
            }
        }
    }

    performFWUpdate = async (ip) => {
        if (this.props.isLoginActive()) {
            try {
                const res = await fetch(EnvironmentVars.rsuRestAuth, {
                    method: 'POST',
                    body: JSON.stringify({
                        command: 'fwupdate',
                        rsu_ip: ip,
                        args: {},
                    }),
                    headers: {
                        'Content-Type': 'application/json',
                        Authorization: this.props.token,
                    },
                })

                const data = await res.json()
                console.log(data)
            } catch (exception_var) {
                console.error(exception_var)
            }
        }
    }

    handleUpdateOS = () => {
        confirmAlert(this.verifyOS)
    }

    handleUpdateFW = () => {
        confirmAlert(this.verifyFW)
    }

    render() {
        return (
            <div id="snmpdiv">
                <div id="updatediv">
                    <h2 id="snmpheader">Administrator Update</h2>

                    <button id="updatebtn" onClick={this.performCheck}>
                        Check For Updates
                    </button>

                    {Object.entries(this.props.ipList).map((rsu) => (
                        <RsuUpdateItem
                            key={rsu[0]}
                            ip={rsu[1]['properties']['Ipv4Address']}
                            osUpdateAvailable={this.state.osUpdateAvailable}
                            fwUpdateAvailable={this.state.fwUpdateAvailable}
                            handleUpdateOS={this.handleUpdateOS}
                            handleUpdateFW={this.handleUpdateFW}
                        />
                    ))}
                </div>
            </div>
        )
    }
}

export default RsuUpdateMenu
