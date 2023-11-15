import React, { Component } from 'react'
import './css/RsuUpdateItem.css'

export type RsuUpdateItemProps = {
  ip: string
  osUpdateAvailable: string[]
  fwUpdateAvailable: string[]
  handleUpdateOS: (ip: string) => void
  handleUpdateFW: (ip: string) => void
}

const RsuUpdateItem = (props: RsuUpdateItemProps) => {
  const handleOSClick = () => {
    props.handleUpdateOS(props.ip)
  }

  const handleFWClick = () => {
    props.handleUpdateFW(props.ip)
  }

  return (
    <div id="updateitem">
      <div id="item">
        {<h3 id="ipaddr">{props.ip}</h3>}

        {props.osUpdateAvailable.includes(props.ip) ? (
          <button id="updatebtn" onClick={handleOSClick}>
            Update OS
          </button>
        ) : (
          <button id="disabledbtn" disabled={true}>
            Update OS
          </button>
        )}

        {!props.osUpdateAvailable.includes(props.ip) && props.fwUpdateAvailable.includes(props.ip) ? (
          <button id="updatebtn" onClick={handleFWClick}>
            Update Firmware
          </button>
        ) : (
          <button id="disabledbtn" disabled={true}>
            Update Firmware
          </button>
        )}

        {props.osUpdateAvailable.includes(props.ip) || props.fwUpdateAvailable.includes(props.ip) ? (
          <p id="infotext">Allow for 10 minutes for update to occur once started</p>
        ) : null}
      </div>
    </div>
  )
}

export default RsuUpdateItem
