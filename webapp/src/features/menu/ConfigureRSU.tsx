import React, { useState } from 'react'
import SnmpwalkMenu from '../../components/SnmpwalkMenu'
import SnmpsetMenu from '../../components/SnmpsetMenu'
import RsuRebootMenu from '../../components/RsuRebootMenu'
import RsuFirmwareMenu from '../../components/RsuFirmwareMenu'
import Accordion from '@mui/material/Accordion'
import AccordionDetails from '@mui/material/AccordionDetails'
import AccordionSummary from '@mui/material/AccordionSummary'
import { ThemeProvider, StyledEngineProvider } from '@mui/material'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import Typography from '@mui/material/Typography'
import { selectSelectedRsu, selectRsu } from '../../generalSlices/rsuSlice'
import { clearConfig, selectConfigList } from '../../generalSlices/configSlice'

import '../../components/css/SnmpwalkMenu.css'

import { accordionTheme } from '../../styles'
import { useAppDispatch, useAppSelector } from '../../hooks'

const ConfigureRSU = () => {
  const dispatch = useAppDispatch()

  const [expanded, setExpanded] = useState<string | undefined>(undefined)
  const handleChange = (panel: string) => (event: React.SyntheticEvent<Element, Event>, isExpanded: boolean) => {
    setExpanded(isExpanded ? panel : undefined)
  }
  const selectedRsu = useAppSelector(selectSelectedRsu)
  const selectedConfigList = useAppSelector(selectConfigList)

  return (
    <div>
      {selectedRsu && (
        <div>
          <h2 className="snmpheader">Selected RSU Config</h2>
          <button
            id="toggle"
            onClick={() => {
              dispatch(selectRsu(null))
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
          <StyledEngineProvider injectFirst>
            <ThemeProvider theme={accordionTheme}>
              <Accordion
                className="accordion-content"
                expanded={expanded === 'selected-rsu-current-config'}
                onChange={handleChange('selected-rsu-current-config')}
              >
                <AccordionSummary
                  expandIcon={<ExpandMoreIcon className="expand" />}
                  aria-controls="panel1bh-content"
                  id="panel1bh-header"
                >
                  <Typography>Current Configuration</Typography>
                </AccordionSummary>
                <StyledEngineProvider injectFirst>
                  <ThemeProvider theme={accordionTheme}>
                    <Accordion>
                      <AccordionDetails>
                        <SnmpwalkMenu />
                      </AccordionDetails>
                    </Accordion>
                  </ThemeProvider>
                </StyledEngineProvider>
              </Accordion>
              <Accordion
                className="accordion-content"
                expanded={expanded === 'selected-rsu-add-msg-forwarding'}
                onChange={handleChange('selected-rsu-add-msg-forwarding')}
              >
                <AccordionSummary
                  expandIcon={<ExpandMoreIcon className="expand" />}
                  aria-controls="panel2bh-content"
                  id="panel2bh-header"
                  className="expand"
                >
                  <Typography>Message Forwarding</Typography>
                </AccordionSummary>
                <StyledEngineProvider injectFirst>
                  <ThemeProvider theme={accordionTheme}>
                    <Accordion>
                      <AccordionDetails>
                        <SnmpsetMenu type="single_rsu" rsuIpList={[selectedRsu.properties.ipv4_address]} />
                      </AccordionDetails>
                    </Accordion>
                  </ThemeProvider>
                </StyledEngineProvider>
              </Accordion>
              <Accordion
                className="accordion-content"
                expanded={expanded === 'selected-rsu-firmware'}
                onChange={handleChange('selected-rsu-firmware')}
              >
                <AccordionSummary
                  className="expand"
                  expandIcon={<ExpandMoreIcon className="expand" />}
                  aria-controls="panel3bh-content"
                  id="panel3bh-header"
                >
                  <Typography>Firmware</Typography>
                </AccordionSummary>
                <StyledEngineProvider injectFirst>
                  <ThemeProvider theme={accordionTheme}>
                    <Accordion>
                      <AccordionDetails>
                        <RsuFirmwareMenu type="single_rsu" rsuIpList={[selectedRsu.properties.ipv4_address]} />
                      </AccordionDetails>
                    </Accordion>
                  </ThemeProvider>
                </StyledEngineProvider>
              </Accordion>
              <Accordion
                className="accordion-content"
                expanded={expanded === 'selected-rsu-reboot'}
                onChange={handleChange('selected-rsu-reboot')}
              >
                <AccordionSummary
                  className="expand"
                  expandIcon={<ExpandMoreIcon className="expand" />}
                  aria-controls="panel3bh-content"
                  id="panel3bh-header"
                >
                  <Typography>Reboot</Typography>
                </AccordionSummary>
                <StyledEngineProvider injectFirst>
                  <ThemeProvider theme={accordionTheme}>
                    <Accordion>
                      <AccordionDetails>
                        <RsuRebootMenu />
                      </AccordionDetails>
                    </Accordion>
                  </ThemeProvider>
                </StyledEngineProvider>
              </Accordion>
            </ThemeProvider>
          </StyledEngineProvider>
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
          <h2 className="snmpheader2">RSU IP List: {selectedConfigList.join(', ')}</h2>
        </div>
      )}
      {selectedConfigList.length > 0 && !selectedRsu && (
        <div id="sideBarBlock" className="accordion">
          <StyledEngineProvider injectFirst>
            <ThemeProvider theme={accordionTheme}>
              <Accordion
                className="accordion-content"
                expanded={expanded === 'multiple-rsu-add-msg-forwarding'}
                onChange={handleChange('multiple-rsu-add-msg-forwarding')}
              >
                <AccordionSummary
                  expandIcon={<ExpandMoreIcon className="expand" />}
                  aria-controls="panel2bh-content"
                  id="panel2bh-header"
                  className="expand"
                >
                  <Typography>Message Forwarding</Typography>
                </AccordionSummary>
                <StyledEngineProvider injectFirst>
                  <ThemeProvider theme={accordionTheme}>
                    <Accordion>
                      <AccordionDetails>
                        <SnmpsetMenu
                          type="multi_rsu"
                          rsuIpList={selectedConfigList.map((val: number) => val.toString())}
                        />
                      </AccordionDetails>
                    </Accordion>
                  </ThemeProvider>
                </StyledEngineProvider>
              </Accordion>
              <Accordion
                className="accordion-content"
                expanded={expanded === 'multiple-rsu-firmware'}
                onChange={handleChange('multiple-rsu-firmware')}
              >
                <AccordionSummary
                  className="expand"
                  expandIcon={<ExpandMoreIcon className="expand" />}
                  aria-controls="panel3bh-content"
                  id="panel3bh-header"
                >
                  <Typography>Firmware</Typography>
                </AccordionSummary>
                <StyledEngineProvider injectFirst>
                  <ThemeProvider theme={accordionTheme}>
                    <Accordion>
                      <AccordionDetails>
                        <RsuFirmwareMenu
                          type="multi_rsu"
                          rsuIpList={selectedConfigList.map((val: number) => val.toString())}
                        />
                      </AccordionDetails>
                    </Accordion>
                  </ThemeProvider>
                </StyledEngineProvider>
              </Accordion>
            </ThemeProvider>
          </StyledEngineProvider>
        </div>
      )}
    </div>
  )
}

export default ConfigureRSU
