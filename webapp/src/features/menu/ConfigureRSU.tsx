import React, { useState } from 'react'
import SnmpwalkMenu from '../../components/SnmpwalkMenu'
import SnmpsetMenu from '../../components/SnmpsetMenu'
import RsuRebootMenu from '../../components/RsuRebootMenu'
import RsuFirmwareMenu from '../../components/RsuFirmwareMenu'
import Accordion from '@mui/material/Accordion'
import AccordionDetails from '@mui/material/AccordionDetails'
import AccordionSummary from '@mui/material/AccordionSummary'
import { useDispatch, useSelector } from 'react-redux'
import { Box, useTheme, Paper } from '@mui/material'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import Typography from '@mui/material/Typography'
import { selectSelectedRsu, selectRsu } from '../../generalSlices/rsuSlice'
import { clearConfig, selectConfigList } from '../../generalSlices/configSlice'
import '../../components/css/SnmpwalkMenu.css'
import { RootState } from '../../store'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { PositionedToggleIconButton } from '../../styles/components/PositionedToggleButton'
import CloseIcon from '@mui/icons-material/Close'

const ConfigMenu = ({ children }) => {
  const theme = useTheme()
  return (
    <Box
      sx={{
        backgroundColor: theme.palette.custom.mapLegendBackground,
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
  const theme = useTheme()

  const [expanded, setExpanded] = useState<string | undefined>(undefined)
  const handleChange = (panel: string) => (event: React.SyntheticEvent<Element, Event>, isExpanded: boolean) => {
    setExpanded(isExpanded ? panel : undefined)
  }
  const selectedRsu = useSelector(selectSelectedRsu)
  const selectedConfigList = useSelector(selectConfigList)

  return (
    <Paper sx={{ lineHeight: 1.1, backgroundColor: theme.palette.custom.mapLegendBackground }}>
      {selectedRsu && (
        <div>
          <h3 className="snmpheader">Selected RSU Config</h3>
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
          <Accordion
            className="accordion-content"
            expanded={expanded === 'selected-rsu-current-config'}
            onChange={handleChange('selected-rsu-current-config')}
          >
            <AccordionSummary expandIcon={<ExpandMoreIcon />} aria-controls="panel1bh-content" id="panel1bh-header">
              <Typography>Current Configuration</Typography>
            </AccordionSummary>
            <Accordion>
              <AccordionDetails>
                <ConfigMenu>
                  <SnmpwalkMenu />
                </ConfigMenu>
              </AccordionDetails>
            </Accordion>
          </Accordion>
          <Accordion
            className="accordion-content"
            expanded={expanded === 'selected-rsu-add-msg-forwarding'}
            onChange={handleChange('selected-rsu-add-msg-forwarding')}
          >
            <AccordionSummary expandIcon={<ExpandMoreIcon />} aria-controls="panel2bh-content" id="panel2bh-header">
              <Typography>Message Forwarding</Typography>
            </AccordionSummary>
            <Accordion>
              <AccordionDetails>
                <ConfigMenu>
                  <SnmpsetMenu type="single_rsu" rsuIpList={[selectedRsu.properties.ipv4_address]} />
                </ConfigMenu>
              </AccordionDetails>
            </Accordion>
          </Accordion>
          <Accordion
            className="accordion-content"
            expanded={expanded === 'selected-rsu-firmware'}
            onChange={handleChange('selected-rsu-firmware')}
          >
            <AccordionSummary expandIcon={<ExpandMoreIcon />} aria-controls="panel3bh-content" id="panel3bh-header">
              <Typography>Firmware</Typography>
            </AccordionSummary>
            <Accordion>
              <AccordionDetails>
                <ConfigMenu>
                  <RsuFirmwareMenu type="single_rsu" rsuIpList={[selectedRsu.properties.ipv4_address]} />
                </ConfigMenu>
              </AccordionDetails>
            </Accordion>
          </Accordion>
          <Accordion
            className="accordion-content"
            expanded={expanded === 'selected-rsu-reboot'}
            onChange={handleChange('selected-rsu-reboot')}
          >
            <AccordionSummary expandIcon={<ExpandMoreIcon />} aria-controls="panel3bh-content" id="panel3bh-header">
              <Typography>Reboot</Typography>
            </AccordionSummary>
            <Accordion>
              <AccordionDetails>
                <ConfigMenu>
                  <RsuRebootMenu />
                </ConfigMenu>
              </AccordionDetails>
            </Accordion>
          </Accordion>
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
          <Accordion
            className="accordion-content"
            expanded={expanded === 'multiple-rsu-add-msg-forwarding'}
            onChange={handleChange('multiple-rsu-add-msg-forwarding')}
          >
            <AccordionSummary expandIcon={<ExpandMoreIcon />} aria-controls="panel2bh-content" id="panel2bh-header">
              <Typography>Message Forwarding</Typography>
            </AccordionSummary>
            <Accordion>
              <AccordionDetails>
                <ConfigMenu>
                  <SnmpsetMenu type="multi_rsu" rsuIpList={selectedConfigList.map((val: number) => val.toString())} />
                </ConfigMenu>
              </AccordionDetails>
            </Accordion>
          </Accordion>
          <Accordion
            className="accordion-content"
            expanded={expanded === 'multiple-rsu-firmware'}
            onChange={handleChange('multiple-rsu-firmware')}
          >
            <AccordionSummary expandIcon={<ExpandMoreIcon />} aria-controls="panel3bh-content" id="panel3bh-header">
              <Typography>Firmware</Typography>
            </AccordionSummary>
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
          </Accordion>
        </div>
      )}
    </Paper>
  )
}

export default ConfigureRSU
