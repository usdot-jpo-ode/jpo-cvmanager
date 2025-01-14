import React from 'react'
import './Menu.css'
import { useEffect } from 'react'
import { selectCountList, selectSelectedRsu } from '../../generalSlices/rsuSlice'
import { selectConfigList } from '../../generalSlices/configSlice'
import { selectDisplayCounts, selectView, setDisplay, setSortedCountList, selectDisplayRsuErrors } from './menuSlice'
import { SecureStorageManager } from '../../managers'
import DisplayCounts from './DisplayCounts'
import DisplayRsuErrors from './DisplayRsuErrors'
import ConfigureRSU from './ConfigureRSU'
import { useAppDispatch, useAppSelector } from '../../hooks'
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
  const dispatch = useAppDispatch()
  const theme = useTheme()
  const countList = useAppSelector(selectCountList)
  const selectedRsu = useAppSelector(selectSelectedRsu)
  const selectedRsuList = useAppSelector(selectConfigList)
  const displayCounts = useAppSelector(selectDisplayCounts)
  const displayRsuErrors = useAppSelector(selectDisplayRsuErrors)
  const view = useAppSelector(selectView)

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
          <PositionedToggleIconButton
            onClick={() => dispatch(setDisplay({ view: 'buttons', display: 'displayCounts' }))}
          >
            <CloseIcon />
          </PositionedToggleIconButton>
          <DisplayCounts />
        </div>
      )}
      {view === 'tab' && displayRsuErrors === true && !selectedRsu && selectedRsuList?.length === 0 && (
        <div style={menuStyle} id="sideBarBlock" className="visibleProp">
          <PositionedToggleIconButton
            onClick={() => dispatch(setDisplay({ view: 'buttons', display: 'displayRsuErrors' }))}
          >
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
