import React from 'react'
import { useEffect, useState } from 'react'
import { useSelector, useDispatch } from "react-redux";
import { selectCountList, selectSelectedRsu, selectRsuCoordinates, updateRsuPoints, geoRsuQuery, selectRsuConfigList} from '../slices/rsuSlice'
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
    const dispatch = useDispatch();

    const userRole = useSelector(selectRole)
    const countList = useSelector(selectCountList)
    const selectedRsu = useSelector(selectSelectedRsu)
    const [displayCounts, setDisplayCounts] = useState(false)
    const [displayConfiguration, setDisplayConfiguration] = useState(false)
    const [sortedCountList, setSortedCountList] = useState(countList)
    const [view, setView] = useState('buttons')
    const rsuCoordinates = useSelector(selectRsuCoordinates)
    const rsuConfigList = useSelector(selectRsuConfigList)

    const tempGeo = [
        [
          -104.916848398416,
          39.61082224277507
        ],
        [
          -104.916848398416,
          39.48305364828414
        ],
        [
          -104.80317986888726,
          39.48305364828414
        ],
        [
          -104.80317986888726,
          39.61082224277507
        ],
        [
          -104.916848398416,
          39.61082224277507
        ]
      ]

    useEffect(() => {
        setSortedCountList(countList)
        dispatch(updateRsuPoints(tempGeo));
    }, [countList])

    useEffect(() => {
        console.log(rsuConfigList);
    }, [rsuConfigList])

    const displayCountsOnClick = () => {
        setView('tab')
        setDisplayCounts(true)
    }

    const testOnClick = () => {
        dispatch(geoRsuQuery());
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
                    <button id="test Button" onClick={testOnClick}>
                        Test Button
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
