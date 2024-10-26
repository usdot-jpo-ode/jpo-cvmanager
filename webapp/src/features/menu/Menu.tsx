import React from 'react'
import './Menu.css'
import { useEffect } from 'react'
import { useSelector, useDispatch } from 'react-redux'
import { selectRole } from '../../generalSlices/userSlice'
import { selectCountList, selectSelectedRsu } from '../../generalSlices/rsuSlice'
import { selectConfigList } from '../../generalSlices/configSlice'
import { selectDisplayCounts, selectView, setDisplay, setSortedCountList } from './menuSlice'
import { SecureStorageManager } from '../../managers'
import DisplayCounts from './DisplayCounts'
import ConfigureRSU from './ConfigureRSU'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../store'
import { headerTabHeight } from '../../styles/index'
import { PositionedToggleButton, PositionedToggleIconButton } from '../../styles/components/PositionedToggleButton'
import CloseIcon from '@mui/icons-material/Close'

const menuStyle: React.CSSProperties = {
  background: '#0e2052',
  textAlign: 'left',
  position: 'absolute',
  zIndex: 90,
  height: `calc(100vh - ${headerTabHeight}px)`,
  width: '420px',
  top: '135px', // : "100px",
  right: '0%',
  overflow: 'auto',
}

const Menu = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const userRole = useSelector(selectRole)
  const countList = useSelector(selectCountList)
  const selectedRsu = useSelector(selectSelectedRsu)
  const selectedRsuList = useSelector(selectConfigList)
  const displayCounts = useSelector(selectDisplayCounts)
  const view = useSelector(selectView)

  useEffect(() => {
    dispatch(setSortedCountList(countList))
  }, [countList, dispatch])

  return (
    <div>
      {view === 'buttons' && !selectedRsu && selectedRsuList?.length === 0 && (
        <div>
          <PositionedToggleButton
            onClick={() => {
              dispatch(setDisplay('tab'))
            }}
          >
            Display Counts
          </PositionedToggleButton>
        </div>
      )}
      {view === 'tab' && displayCounts === true && !selectedRsu && selectedRsuList?.length === 0 && (
        <div style={menuStyle} id="sideBarBlock" className="visibleProp">
          <PositionedToggleIconButton onClick={() => dispatch(setDisplay('buttons'))}>
            <CloseIcon />
          </PositionedToggleIconButton>
          <DisplayCounts />
        </div>
      )}
      {SecureStorageManager.getUserRole() === 'admin' && (selectedRsu || selectedRsuList?.length > 0) && (
        <div style={menuStyle} id="sideBarBlock" className="visibleProp">
          <ConfigureRSU />
        </div>
      )}
    </div>
  )
}

export default Menu
