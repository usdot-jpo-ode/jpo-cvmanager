import React, { useState } from 'react'
import SnmpwalkMenu from '../../components/SnmpwalkMenu'
import SnmpsetMenu from '../../components/SnmpsetMenu'
import RsuRebootMenu from '../../components/RsuRebootMenu'
import RsuFirmwareMenu from '../../components/RsuFirmwareMenu'
import Accordion from '@mui/material/Accordion'
import AccordionDetails from '@mui/material/AccordionDetails'
import AccordionSummary from '@mui/material/AccordionSummary'
import { useDispatch, useSelector } from 'react-redux'
import { ThemeProvider, StyledEngineProvider, Box, useTheme } from '@mui/material'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import Typography from '@mui/material/Typography'
import { selectSelectedRsu, selectRsu } from '../../generalSlices/rsuSlice'
import { clearConfig, selectConfigList } from '../../generalSlices/configSlice'

import '../../components/css/SnmpwalkMenu.css'
import { RootState } from '../../store'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'

import { accordionTheme } from '../../styles'
import { PositionedToggleIconButton } from '../../styles/components/PositionedToggleButton'
import CloseIcon from '@mui/icons-material/Close'

const ConfigMenu = ({ children }) => {
  const theme = useTheme()
  return (
    <Box
      sx={{
        backgroundColor: theme.palette.secondary.main,
        border: `1px solid ${theme.palette.text.primary}`,
        borderRadius: 3,
        pt: '25px',
        pb: '30px',
        pl: '30px',
        pr: '35px',
        verticalAlign: 'top',
      }}
    >
      {children}
    </Box>
  )
}

const ConfigureRSU = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()

  const [expanded, setExpanded] = useState<string | undefined>(undefined)
  const handleChange = (panel: string) => (event: React.SyntheticEvent<Element, Event>, isExpanded: boolean) => {
    setExpanded(isExpanded ? panel : undefined)
  }
  const selectedRsu = useSelector(selectSelectedRsu)
  const selectedConfigList = useSelector(selectConfigList)

  return (
    <div style={{ lineHeight: 1.1 }}>
      {selectedRsu && (
        <div>
          <h2 className="snmpheader">Selected RSU Config</h2>
          <PositionedToggleIconButton
            onClick={() => {
              dispatch(selectRsu(null))
            }}
          >
            <CloseIcon />
          </PositionedToggleIconButton>
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
                  <Typography color="textSecondary">Current Configuration</Typography>
                </AccordionSummary>
                <StyledEngineProvider injectFirst>
                  <ThemeProvider theme={accordionTheme}>
                    <Accordion>
                      <AccordionDetails>
                        <ConfigMenu>
                          <SnmpwalkMenu />
                        </ConfigMenu>
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
                        <ConfigMenu>
                          <SnmpsetMenu type="single_rsu" rsuIpList={[selectedRsu.properties.ipv4_address]} />
                        </ConfigMenu>
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
                        <ConfigMenu>
                          <RsuFirmwareMenu type="single_rsu" rsuIpList={[selectedRsu.properties.ipv4_address]} />
                        </ConfigMenu>
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
                        <ConfigMenu>
                          <RsuRebootMenu />
                        </ConfigMenu>
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
            <PositionedToggleIconButton
              onClick={() => {
                dispatch(clearConfig())
              }}
            >
              <CloseIcon />
            </PositionedToggleIconButton>
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
                        <ConfigMenu>
                          <SnmpsetMenu
                            type="multi_rsu"
                            rsuIpList={selectedConfigList.map((val: number) => val.toString())}
                          />
                        </ConfigMenu>
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
                        <ConfigMenu>
                          <RsuFirmwareMenu
                            type="multi_rsu"
                            rsuIpList={selectedConfigList.map((val: number) => val.toString())}
                          />
                        </ConfigMenu>
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
