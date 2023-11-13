import React from 'react'

import './css/SnmpItem.css'

const SnmpwalkItem = (props) => {
  return (
    <div id="snmpitemdiv">
      <h3 id="snmpitemheader">{props.index}</h3>
      <p id="snmpitemtext">
        <strong>Message Type:</strong> {props.content['Message Type']}
      </p>
      <p id="snmpitemtext">
        <strong>Destination IP:</strong> {props.content['IP']}
      </p>
      <p id="snmpitemtext">
        <strong>Port:</strong> {props.content['Port']}
      </p>
      <p id="snmpitemtext">
        <strong>Protocol:</strong> {props.content['Protocol']}
      </p>
      <p id="snmpitemtext">
        <strong>Start:</strong> {props.content['Start DateTime']}
      </p>
      <p id="snmpitemtext">
        <strong>End:</strong> {props.content['End DateTime']}
      </p>
      <p id="snmpitemtext">
        <strong>Active:</strong> {props.content['Config Active']}
      </p>
    </div>
  )
}

export default SnmpwalkItem
