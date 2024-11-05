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
  const dispatch = useAppDispatch()
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
      {view === 'buttons' && !selectedRsu && selectedRsuList?.length === 0 && (
        <div>
          <button id="toggle" onClick={() => dispatch(setDisplay({ view: 'tab', display: 'displayCounts' }))}>
            Display Counts
          </button>
        </div>
      )}
      {view === 'buttons' && !selectedRsu && selectedRsuList?.length === 0 && (
        <div>
          <button
            id="rsu-errors-toggle"
            onClick={() => dispatch(setDisplay({ view: 'tab', display: 'displayRsuErrors' }))}
          >
            Display RSU Status
          </button>
        </div>
      )}
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
