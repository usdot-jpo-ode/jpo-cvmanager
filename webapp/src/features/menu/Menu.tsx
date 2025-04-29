import React from 'react'
import './Menu.css'
import { useEffect } from 'react'
import { useSelector, useDispatch } from 'react-redux'
import { selectCountList, selectSelectedRsu } from '../../generalSlices/rsuSlice'
import { selectConfigList } from '../../generalSlices/configSlice'
import { selectDisplayCounts, setSortedCountList, selectDisplayRsuErrors, toggleMapMenuSelection } from './menuSlice'
import { SecureStorageManager } from '../../managers'
import DisplayCounts from './DisplayCounts'
import DisplayRsuErrors from './DisplayRsuErrors'
import ConfigureRSU from './ConfigureRSU'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../store'
import { headerTabHeight } from '../../styles/index'
import { useTheme } from '@mui/material'

const menuStyle: React.CSSProperties = {
  textAlign: 'left',
  position: 'absolute',
  zIndex: 90,
  maxHeight: `calc(100vh - ${headerTabHeight + 80}px)`,
  height: 'fit-content',
  top: `${headerTabHeight + 91}px`,
  right: '25px',
  borderRadius: '4px',
}

const Menu = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const theme = useTheme()
  const countList = useSelector(selectCountList)
  const selectedRsu = useSelector(selectSelectedRsu)
  const selectedRsuList = useSelector(selectConfigList)
  const displayCounts = useSelector(selectDisplayCounts)
  const displayRsuErrors = useSelector(selectDisplayRsuErrors)

  useEffect(() => {
    dispatch(setSortedCountList(countList))
  }, [countList, dispatch])

  return (
    <div>
      {displayCounts === true && !selectedRsu && selectedRsuList?.length === 0 && (
        <div
          style={{ ...menuStyle, backgroundColor: theme.palette.custom.mapLegendBackground, width: '400px' }}
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
      {SecureStorageManager.getUserRole() === 'admin' && (selectedRsu || selectedRsuList?.length > 0) && (
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
