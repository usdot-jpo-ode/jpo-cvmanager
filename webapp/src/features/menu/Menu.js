import React from 'react'
import './Menu.css'
import { useEffect } from 'react'
import { useSelector, useDispatch } from 'react-redux'
import { selectRole } from '../../generalSlices/userSlice'
import { selectCountList, selectSelectedRsu } from '../../generalSlices/rsuSlice'
import { selectConfigList } from '../../generalSlices/configSlice'
import { selectDisplayCounts, selectView, setDisplay, setSortedCountList } from './menuSlice'
import DisplayCounts from './DisplayCounts'
import ConfigureRSU from './ConfigureRSU'

const menuStyle = {
  background: '#0e2052',
  textAlign: 'left',
  position: 'absolute',
  zIndex: '90',
  height: 'calc(100vh - 135px)', // : "calc(100vh - 100px)",
  width: '350px',
  top: '135px', // : "100px",
  right: '0%',
  overflow: 'auto',
}

const Menu = () => {
  const dispatch = useDispatch()
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
          <button id="toggle" onClick={() => dispatch(setDisplay('tab'))}>
            Display Counts
          </button>
        </div>
      )}
      {view === 'tab' && displayCounts === true && !selectedRsu && selectedRsuList?.length === 0 && (
        <div style={menuStyle} id="sideBarBlock" className="visibleProp">
          <button id="toggle" onClick={() => dispatch(setDisplay('buttons'))}>
            X
          </button>
          <DisplayCounts />
        </div>
      )}
      {userRole === 'admin' && (selectedRsu || selectedRsuList?.length > 0) && (
        <div style={menuStyle} id="sideBarBlock" className="visibleProp">
          <ConfigureRSU />
        </div>
      )}
    </div>
  )
}

export default Menu
