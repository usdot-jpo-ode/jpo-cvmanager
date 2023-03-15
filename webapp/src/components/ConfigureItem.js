import React, { Component } from 'react';

import './css/ConfigureItem.css';

class ConfigureItem extends Component {
  constructor(props) {
    super(props);
    this.state = {
      selected: false,
    }
  }

  toggleSelect = () => {
    let select = !(this.state.selected)
    this.props.updateRsu(this.props.index, select)
    this.setState({'selected': select})
  }

  render() {
    return (
      <div id={this.props.indexList.includes(this.props.index) ? "selectedconfigitemdiv" : "configitemdiv"} 
            onClick={() => this.toggleSelect()}>
        <p id="configitemtext">
          <strong>{this.props.ip}</strong>
        </p>
      </div>
    )
  }
}

export default ConfigureItem;