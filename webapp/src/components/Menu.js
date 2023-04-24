import React from 'react'
import { useEffect, useState } from 'react'
import { useSelector } from "react-redux";
import { selectCountList, selectSelectedRsu} from '../slices/rsuSlice'
import { selectRole } from '../slices/userSlice'
import { selectRsuConfigList } from '../slices/configSlice'
import ConfigureRsu from './ConfigureRsu'
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
    const userRole = useSelector(selectRole);
    const countList = useSelector(selectCountList);
    const selectedRsu = useSelector(selectSelectedRsu);
    const selectedRsuList = useSelector(selectRsuConfigList);
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
            {view === 'buttons' && !selectedRsu && selectedRsuList.length === 0 && (
                <div>
                    <button id="toggle" onClick={displayCountsOnClick}>
                        Display Counts
                    </button>
                </div>
            )}
            {view === 'tab' && displayCounts === true && !selectedRsu && selectedRsuList.length === 0 && (
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
            {userRole === 'admin' && (selectedRsu || selectedRsuList.length > 0) && (
                <div
                    style={menuStyle}
                    id="sideBarBlock"
                    className="visibleProp"
                >
                    <ConfigureRsu/>
                </div>
            )}
        </div>
    )
}

export default Menu
