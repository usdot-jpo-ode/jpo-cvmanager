import React from 'react'
import './Menu.css'
import { useSelector } from 'react-redux'
import { selectSelectedRsu } from '../../generalSlices/rsuSlice'
import { selectConfigList } from '../../generalSlices/configSlice'
import { selectDisplayCounts, selectDisplayRsuErrors } from './menuSlice'
import DisplayCounts from './DisplayCounts'
import DisplayRsuErrors from './DisplayRsuErrors'
import ConfigureRSU from './ConfigureRSU'
import { headerTabHeight } from '../../styles/index'
import { useTheme } from '@mui/material'
import { selectIsOperatorOrAbove } from '../../generalSlices/userSlice'

const menuTopOffset = headerTabHeight + 91
const menuRightOffset = 25

const menuStyle: React.CSSProperties = {
  textAlign: 'left',
  position: 'absolute',
  zIndex: 90,
  height: 'auto',
  top: `${menuTopOffset}px`,
  right: `${menuRightOffset}px`,
  maxHeight: `calc(100dvh - ${menuTopOffset + menuRightOffset}px)`,
  overflowY: 'auto',
  borderRadius: '4px',
}

const Menu = () => {
  const theme = useTheme()
  const selectedRsu = useSelector(selectSelectedRsu)
  const selectedRsuList = useSelector(selectConfigList)
  const displayCounts = useSelector(selectDisplayCounts)
  const displayRsuErrors = useSelector(selectDisplayRsuErrors)
  const isOperatorOrAbove = useSelector(selectIsOperatorOrAbove)

  return (
    <div>
      {displayCounts === true && !selectedRsu && selectedRsuList?.length === 0 && (
        <div
          style={{
            ...menuStyle,
            backgroundColor: theme.palette.custom.mapLegendBackground,
            width: '400px',
            scrollbarColor: `${theme.palette.text.primary} ${theme.palette.background.paper}`,
          }}
          className="visibleProp map-control-container"
        >
          <DisplayCounts />
        </div>
      )}
      {displayRsuErrors === true && !selectedRsu && selectedRsuList?.length === 0 && (
        <div style={{ ...menuStyle, width: '570px' }} className="visibleProp map-control-container">
          <DisplayRsuErrors />
        </div>
      )}
      {isOperatorOrAbove && (selectedRsu || selectedRsuList?.length > 0) && (
        <div
          style={{ ...menuStyle, backgroundColor: theme.palette.custom.mapLegendBackground, width: '400px' }}
          className="visibleProp map-control-container"
        >
          <ConfigureRSU />
        </div>
      )}
    </div>
  )
}

export default Menu
