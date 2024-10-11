import React from 'react'
import './Menu.css'
import { useEffect } from 'react'
import { useSelector, useDispatch } from 'react-redux'
import { selectRole } from '../../generalSlices/userSlice'
import { selectCountList, selectSelectedRsu } from '../../generalSlices/rsuSlice'
import { selectConfigList } from '../../generalSlices/configSlice'
import { selectDisplayCounts, selectView, setDisplay, setSortedCountList, selectDisplayRsuErrors } from './menuSlice'
import { SecureStorageManager } from '../../managers'
import DisplayCounts from './DisplayCounts'
import DisplayRsuErrors from './DisplayRsuErrors'
import ConfigureRSU from './ConfigureRSU'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../store'

const menuStyle: React.CSSProperties = {
  background: '#0e2052',
  textAlign: 'left',
  position: 'absolute',
  zIndex: 90,
  height: 'calc(100vh - 135px)', // : "calc(100vh - 100px)",
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
  const displayRsuErrors = useSelector(selectDisplayRsuErrors)
  const view = useSelector(selectView)

  useEffect(() => {
    dispatch(setSortedCountList(countList))
  }, [countList, dispatch])

  return (
    <div>
      {view === 'tab' && displayCounts === true && !selectedRsu && selectedRsuList?.length === 0 && (
        <div style={menuStyle} id="sideBarBlock" className="visibleProp">
          <button id="toggle" onClick={() => dispatch(setDisplay({ view: 'buttons', display: 'displayCounts' }))}>
            X
          </button>
          <DisplayCounts />
        </div>
      )}
      {view === 'tab' && displayRsuErrors === true && !selectedRsu && selectedRsuList?.length === 0 && (
        <div style={menuStyle} id="sideBarBlock" className="visibleProp">
          <button id="toggle" onClick={() => dispatch(setDisplay({ view: 'buttons', display: 'displayRsuErrors' }))}>
            X
          </button>
          <DisplayRsuErrors />
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
