import React, { Component } from 'react';
import './css/RsuUpdateItem.css'

class RsuUpdateItem extends Component {
  handleOSClick = () => {
    this.props.handleUpdateOS(this.props.ip);
  }

  handleFWClick = () => {
    this.props.handleUpdateFW(this.props.ip);
  }

  render() {
    return (
      <div id="updateitem">
        <div id="item">
          {
            <h3 id="ipaddr">{this.props.ip}</h3>
          }

          {
            this.props.osUpdateAvailable.includes(this.props.ip) ? (
              <button id="updatebtn" onClick={this.handleOSClick}>Update OS</button>
            ) : <button id="disabledbtn" disabled={true}>Update OS</button>
          }

          {
            !this.props.osUpdateAvailable.includes(this.props.ip) && this.props.fwUpdateAvailable.includes(this.props.ip) ? (
              <button id="updatebtn" onClick={this.handleFWClick}>Update Firmware</button>
            ) : <button id="disabledbtn" disabled={true}>Update Firmware</button>
          }

          {
          this.props.osUpdateAvailable.includes(this.props.ip) || this.props.fwUpdateAvailable.includes(this.props.ip) ? (
            <p id="infotext">Allow for 10 minutes for update to occur once started</p>
          ) : (null)
          }
        </div>
      </div>
    )
  }
}

export default RsuUpdateItem;