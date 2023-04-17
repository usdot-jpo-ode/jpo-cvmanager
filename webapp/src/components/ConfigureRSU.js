import { useState } from 'react'
import SnmpwalkMenu from './SnmpwalkMenu'
import SnmpsetMenu from './SnmpsetMenu'
import RsuRebootMenu from './RsuRebootMenu'
import Accordion from '@mui/material/Accordion'
import AccordionDetails from '@mui/material/AccordionDetails'
import AccordionSummary from '@mui/material/AccordionSummary'
import { useSelector } from 'react-redux'
import { ThemeProvider, createTheme } from '@mui/material'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import Typography from '@mui/material/Typography'
import { selectSelectedRsu } from '../slices/rsuSlice'

import './css/SnmpwalkMenu.css'

const ConfigureRSU = () => {
    const [expanded, setExpanded] = useState(false)
    const handleChange = (panel) => (event, isExpanded) => {
        setExpanded(isExpanded ? panel : false)
    }
    const selectedRsu = useSelector(selectSelectedRsu)

    return (
        <div>
            {selectedRsu ? (
                <div>
                    <h2 class="snmpheader">RSU</h2>
                    <h2 class="snmpheader2">
                        Roadway: {selectedRsu.properties.primary_route}
                        <br />
                        Milepost: {String(selectedRsu.properties.milepost)}
                        <br />
                        IPv4: {selectedRsu.properties.ipv4_address}
                    </h2>
                </div>
            ) : (
                <h2 class="snmpheader2">
                    Select a RSU to configure on the Map tab
                </h2>
            )}
            <div id="sideBarBlock" className="accordion">
                <ThemeProvider theme={accordionTheme}>
                    <Accordion
                        className="accordion-content"
                        expanded={expanded === 'panel1'}
                        onChange={handleChange('panel1')}
                    >
                        <AccordionSummary
                            expandIcon={<ExpandMoreIcon className="expand" />}
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
                        expanded={expanded === 'panel2'}
                        onChange={handleChange('panel2')}
                    >
                        <AccordionSummary
                            expandIcon={<ExpandMoreIcon className="expand" />}
                            aria-controls="panel2bh-content"
                            id="panel2bh-header"
                            className="expand"
                        >
                            <Typography>Add Message Forwarding</Typography>
                        </AccordionSummary>
                        <ThemeProvider theme={innerAccordionTheme}>
                            <Accordion>
                                <AccordionDetails>
                                   
                                    <SnmpsetMenu />
                                   
                                </AccordionDetails>
                            </Accordion>
                        </ThemeProvider>
                    </Accordion>
                    <Accordion
                        expanded={expanded === 'panel3'}
                        onChange={handleChange('panel3')}
                    >
                        <AccordionSummary
                            className="expand"
                            expandIcon={<ExpandMoreIcon className="expand" />}
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

export default ConfigureRSU
