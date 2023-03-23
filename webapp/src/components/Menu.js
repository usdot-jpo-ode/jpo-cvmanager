import React from 'react'
import { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { selectCountList, selectSelectedRsu } from '../slices/rsuSlice'
import { selectRole } from '../slices/userSlice'
import ConfigureRSU from './ConfigureRSU'
import DisplayCounts from './DisplayCounts'

import './css/Menu.css'

const menuStyle = {
    background: '#0e2052',
    textAlign: 'left',
    position: 'absolute',
    zIndex: '90',
    height: 'calc(100vh - 135px)', // : "calc(100vh - 100px)",
    width: '500px',
    top: '135px', // : "100px",
    right: '0%',
    overflow: 'auto',
}

const Menu = () => {
    const userRole = useSelector(selectRole)
    const countList = useSelector(selectCountList)
    const selectedRsu = useSelector(selectSelectedRsu)
    const [displayCounts, setDisplayCounts] = useState(false)
    const [displayConfiguration, setDisplayConfiguration] = useState(false)
    const [sortedCountList, setSortedCountList] = useState(countList)
    const [view, setView] = useState('buttons')

    useEffect(() => {
        setSortedCountList(countList)
    }, [countList])

    const displayCountsOnClick = () => {
        setView('tab')
        setDisplayCounts(true)
    }

    const exitCountsOnClick = () => {
        setDisplayCounts(false)
        setView('buttons')
    }

    return (
        <div>
            {view === 'buttons' && !selectedRsu && (
                <div>
                    <button id="toggle" onClick={displayCountsOnClick}>
                        Display Counts
                    </button>
                </div>
            )}
            {view === 'tab' && displayCounts === true && !selectedRsu && (
                <div
                    style={menuStyle}
                    id="sideBarBlock"
                    className="visibleProp"
                >
                    <button id="toggle" onClick={exitCountsOnClick}>
                        X
                    </button>
                    <DisplayCounts />
                </div>
            )}
            {userRole === 'admin' && selectedRsu && (
                <div
                    style={menuStyle}
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
