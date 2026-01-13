import React, { useMemo } from 'react'
import './Menu.css'
import { useSelector } from 'react-redux'
import { selectSelectedRsu } from '../../generalSlices/rsuSlice'
import { selectConfigList } from '../../generalSlices/configSlice'
import { selectDisplayCounts, selectDisplayRsuErrors } from './menuSlice'
import { SecureStorageManager } from '../../managers'
import DisplayCounts from './DisplayCounts'
import DisplayRsuErrors from './DisplayRsuErrors'
import ConfigureRSU from './ConfigureRSU'
import { headerTabHeight } from '../../styles/index'
import { useTheme } from '@mui/material'

const menuStyle: React.CSSProperties = {
  textAlign: 'left',
  position: 'absolute',
  zIndex: 90,
  height: 'fit-content',
  top: `${headerTabHeight + 91}px`,
  right: '25px',
  borderRadius: '4px',
}

const Menu = () => {
  const theme = useTheme()
  const selectedRsu = useSelector(selectSelectedRsu)
  const selectedRsuList = useSelector(selectConfigList)
  const displayCounts = useSelector(selectDisplayCounts)
  const displayRsuErrors = useSelector(selectDisplayRsuErrors)

  const isOperatorOrAbove = useMemo(() => {
    const allowedRoles = ['operator', 'admin']
    return allowedRoles.includes(SecureStorageManager.getUserRole())
  }, [])

  return (
    <div>
      {displayCounts === true && !selectedRsu && selectedRsuList?.length === 0 && (
        <div
          style={{
            ...menuStyle,
            backgroundColor: theme.palette.custom.mapLegendBackground,
            width: '400px',
            maxHeight: `calc(100vh - ${headerTabHeight + 185}px)`,
            overflowY: 'auto',
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
