import { useState } from 'react'
import SnmpwalkMenu from './SnmpwalkMenu'
import SnmpsetMenu from './SnmpsetMenu'
import RsuRebootMenu from './RsuRebootMenu'
import Accordion from '@mui/material/Accordion'
import AccordionDetails from '@mui/material/AccordionDetails'
import AccordionSummary from '@mui/material/AccordionSummary'
import { useDispatch, useSelector } from 'react-redux'
import { ThemeProvider, createTheme } from '@mui/material'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import Typography from '@mui/material/Typography'
import { selectSelectedRsu, selectRsu } from '../slices/rsuSlice'
import { clearConfig, selectConfigList } from '../slices/configSlice'

import './css/SnmpwalkMenu.css'

const ConfigureRsu = () => {
    const dispatch = useDispatch()

    const [expanded, setExpanded] = useState(false)
    const handleChange = (panel) => (event, isExpanded) => {
        setExpanded(isExpanded ? panel : false)
    }
    const selectedRsu = useSelector(selectSelectedRsu)
    const selectedConfigList = useSelector(selectConfigList)

    return (
        <div>
            {selectedRsu && (
                <div>
                    <h2 className="snmpheader">Selected RSU Config</h2>
                    <button
                        id="toggle"
                        onClick={() => {
                            dispatch(selectRsu(''))
                        }}
                    >
                        X
                    </button>
                    <h2 className="snmpheader2">
                        Roadway: {selectedRsu.properties.primary_route}
                        <br />
                        Milepost: {String(selectedRsu.properties.milepost)}
                        <br />
                        IPv4: {selectedRsu.properties.ipv4_address}
                    </h2>
                </div>
            )}

            {selectedRsu && (
                <div id="sideBarBlock" className="accordion">
                    <ThemeProvider theme={accordionTheme}>
                        <Accordion
                            className="accordion-content"
                            expanded={
                                expanded === 'selected-rsu-current-config'
                            }
                            onChange={handleChange(
                                'selected-rsu-current-config'
                            )}
                        >
                            <AccordionSummary
                                expandIcon={
                                    <ExpandMoreIcon className="expand" />
                                }
                                aria-controls="panel1bh-content"
                                id="panel1bh-header"
                            >
                                <Typography>Current Configuration</Typography>
                            </AccordionSummary>
                            <ThemeProvider theme={innerAccordionTheme}>
                                <Accordion>
                                    <AccordionDetails>
                                        <SnmpwalkMenu />
                                    </AccordionDetails>
                                </Accordion>
                            </ThemeProvider>
                        </Accordion>
                        <Accordion
                            className="accordion-content"
                            expanded={
                                expanded === 'selected-rsu-add-msg-forwarding'
                            }
                            onChange={handleChange(
                                'selected-rsu-add-msg-forwarding'
                            )}
                        >
                            <AccordionSummary
                                expandIcon={
                                    <ExpandMoreIcon className="expand" />
                                }
                                aria-controls="panel2bh-content"
                                id="panel2bh-header"
                                className="expand"
                            >
                                <Typography>Message Forwarding</Typography>
                            </AccordionSummary>
                            <ThemeProvider theme={innerAccordionTheme}>
                                <Accordion>
                                    <AccordionDetails>
                                        <SnmpsetMenu
                                            type="single_rsu"
                                            rsuIpList={[
                                                selectedRsu.properties
                                                    .ipv4_address,
                                            ]}
                                        />
                                    </AccordionDetails>
                                </Accordion>
                            </ThemeProvider>
                        </Accordion>
                        <Accordion
                            className="accordion-content"
                            expanded={expanded === 'selected-rsu-reboot'}
                            onChange={handleChange('selected-rsu-reboot')}
                        >
                            <AccordionSummary
                                className="expand"
                                expandIcon={
                                    <ExpandMoreIcon className="expand" />
                                }
                                aria-controls="panel3bh-content"
                                id="panel3bh-header"
                            >
                                <Typography>Reboot</Typography>
                            </AccordionSummary>
                            <ThemeProvider theme={innerAccordionTheme}>
                                <Accordion>
                                    <AccordionDetails>
                                        <RsuRebootMenu />
                                    </AccordionDetails>
                                </Accordion>
                            </ThemeProvider>
                        </Accordion>
                    </ThemeProvider>
                </div>
            )}
            {selectedConfigList.length > 0 && !selectedRsu && (
                <div>
                    <div className="header-container">
                        <h2 className="snmpheader">Multiple RSU Config</h2>
                        <button
                            id="toggle"
                            onClick={() => {
                                dispatch(clearConfig())
                            }}
                        >
                            X
                        </button>
                    </div>
                    <h2 className="snmpheader2">
                        RSU IP List: {selectedConfigList.join(', ')}
                    </h2>
                </div>
            )}
            {selectedConfigList.length > 0 && !selectedRsu && (
                <div id="sideBarBlock" className="accordion">
                    <ThemeProvider theme={accordionTheme}>
                        <Accordion
                            className="accordion-content"
                            expanded={
                                expanded === 'multiple-rsu-add-msg-forwarding'
                            }
                            onChange={handleChange(
                                'multiple-rsu-add-msg-forwarding'
                            )}
                        >
                            <AccordionSummary
                                expandIcon={
                                    <ExpandMoreIcon className="expand" />
                                }
                                aria-controls="panel2bh-content"
                                id="panel2bh-header"
                                className="expand"
                            >
                                <Typography>Message Forwarding</Typography>
                            </AccordionSummary>
                            <ThemeProvider theme={innerAccordionTheme}>
                                <Accordion>
                                    <AccordionDetails>
                                        <SnmpsetMenu
                                            type="multi_rsu"
                                            rsuIpList={selectedConfigList}
                                        />
                                    </AccordionDetails>
                                </Accordion>
                            </ThemeProvider>
                        </Accordion>
                    </ThemeProvider>
                </div>
            )}
        </div>
    )
}

const accordionTheme = createTheme({
    palette: {
        text: {
            primary: '#ffffff',
            secondary: '#ffffff',
            disabled: '#ffffff',
            hint: '#ffffff',
        },
        divider: '#333',
        background: {
            paper: '#333',
        },
    },
})
const innerAccordionTheme = createTheme({
    palette: {
        text: {
            primary: '#fff',
            secondary: '#fff',
            disabled: '#fff',
            hint: '#fff',
        },
        divider: '#333',
        background: {
            paper: '#333',
        },
    },
})

export default ConfigureRsu
