import React from 'react'
import './Menu.css'
import { useEffect } from 'react'
import { useSelector, useDispatch } from 'react-redux'
import { selectRole } from '../../generalSlices/userSlice'
import { selectCountList, selectSelectedRsu } from '../../generalSlices/rsuSlice'
import { selectConfigList } from '../../generalSlices/configSlice'
import {
  selectDisplayCounts,
  selectView,
  setDisplay,
  setSortedCountList,
  selectDisplayRsuErrors,
  toggleMapMenuSelection,
} from './menuSlice'
import { SecureStorageManager } from '../../managers'
import DisplayCounts from './DisplayCounts'
import DisplayRsuErrors from './DisplayRsuErrors'
import ConfigureRSU from './ConfigureRSU'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../store'
import { headerTabHeight } from '../../styles/index'
import { PositionedToggleButton, PositionedToggleIconButton } from '../../styles/components/PositionedToggleButton'
import CloseIcon from '@mui/icons-material/Close'
import { useTheme } from '@mui/material'

const menuStyle: React.CSSProperties = {
  textAlign: 'left',
  position: 'absolute',
  zIndex: 90,
  height: `calc(100vh - ${headerTabHeight}px)`,
  width: '420px',
  top: `${headerTabHeight}px`,
  right: '0%',
  overflow: 'auto',
}

const Menu = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const theme = useTheme()
  const userRole = useSelector(selectRole)
  const countList = useSelector(selectCountList)
  const selectedRsu = useSelector(selectSelectedRsu)
  const selectedRsuList = useSelector(selectConfigList)
  const displayCounts = useSelector(selectDisplayCounts)
  const displayRsuErrors = useSelector(selectDisplayRsuErrors)
  const view = useSelector(selectView)

  useEffect(() => {
    dispatch(setSortedCountList(countList))
  }, [countList, dispatch])

  return (
    <div>
      {view === 'tab' && displayCounts === true && !selectedRsu && selectedRsuList?.length === 0 && (
        <div
          style={{ ...menuStyle, backgroundColor: theme.palette.custom.mapLegendBackground }}
          id="sideBarBlock"
          className="visibleProp"
        >
          <PositionedToggleIconButton onClick={() => dispatch(toggleMapMenuSelection('Display Message Counts'))}>
            <CloseIcon />
          </PositionedToggleIconButton>
          <DisplayCounts />
        </div>
      )}
      {view === 'tab' && displayRsuErrors === true && !selectedRsu && selectedRsuList?.length === 0 && (
        <div style={menuStyle} id="sideBarBlock" className="visibleProp">
          <PositionedToggleIconButton onClick={() => dispatch(toggleMapMenuSelection('Display RSU Status'))}>
            <CloseIcon />
          </PositionedToggleIconButton>
          <DisplayRsuErrors />
        </div>
      )}
      {SecureStorageManager.getUserRole() === 'admin' && (selectedRsu || selectedRsuList?.length > 0) && (
        <div
          style={{ ...menuStyle, backgroundColor: theme.palette.custom.mapLegendBackground }}
          id="sideBarBlock"
          className="visibleProp"
        >
          <ConfigureRSU />
        </div>
      )}
    </div>
  )
}

export default Menu
