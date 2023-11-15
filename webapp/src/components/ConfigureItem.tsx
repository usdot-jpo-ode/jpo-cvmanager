import React, { useState } from 'react'

import './css/ConfigureItem.css'

interface ConfigureItemProps {
  ip: string
  index: number
  indexList: number[]
  updateRsu: (index: number, selected: boolean) => void
}

const ConfigureItem = (props: ConfigureItemProps) => {
  const [selected, setSelected] = useState(false)

  const toggleSelect = () => {
    let localSelect = !selected
    props.updateRsu(props.index, localSelect)
    setSelected(localSelect)
  }

  return (
    <div
      id={props.indexList.includes(props.index) ? 'selectedconfigitemdiv' : 'configitemdiv'}
      onClick={() => toggleSelect()}
    >
      <p id="configitemtext">
        <strong>{props.ip}</strong>
      </p>
    </div>
  )
}

export default ConfigureItem
