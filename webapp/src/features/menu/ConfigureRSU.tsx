import React, { useState } from 'react'
import SnmpwalkMenu from '../../components/SnmpwalkMenu'
import SnmpsetMenu from '../../components/SnmpsetMenu'
import RsuRebootMenu from '../../components/RsuRebootMenu'
import RsuFirmwareMenu from '../../components/RsuFirmwareMenu'
import Accordion from '@mui/material/Accordion'
import AccordionDetails from '@mui/material/AccordionDetails'
import AccordionSummary from '@mui/material/AccordionSummary'
import { useDispatch, useSelector } from 'react-redux'
import { Box, useTheme, Paper, Grid2, IconButton, Divider } from '@mui/material'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import Typography from '@mui/material/Typography'
import { selectSelectedRsu, selectRsu } from '../../generalSlices/rsuSlice'
import { clearConfig, selectConfigList } from '../../generalSlices/configSlice'

import '../../components/css/SnmpwalkMenu.css'
import { RootState } from '../../store'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'

import CloseIcon from '@mui/icons-material/Close'
import { RoomOutlined } from '@mui/icons-material'
import { headerTabHeight } from '../../styles'
import { SideBarHeader } from '../../styles/components/SideBarHeader'

const ConfigMenu = ({ children }) => {
  return <Box>{children}</Box>
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
    <Paper sx={{ lineHeight: 1.1, backgroundColor: theme.palette.background.paper }}>
      {selectedRsu && (
        <div>
          <IconButton
            sx={{
              position: 'absolute',
              zIndex: 100,
              top: '10px',
              right: '10px',
            }}
            onClick={() => {
              dispatch(selectRsu(null))
            }}
          >
            <CloseIcon />
          </IconButton>
          <Grid2
            container
            columnSpacing={0.5}
            rowSpacing={0.5}
            sx={{
              color: theme.palette.text.primary,
              backgroundColor: theme.palette.background.paper,
              width: '100%',
              marginTop: '10px',
            }}
          >
            <Grid2 size={1} justifyContent="flex-start" sx={{ ml: '16px' }}>
              <RoomOutlined color="info" fontSize="medium" />
            </Grid2>
            <Grid2 size={8} justifyContent="flex-start">
              <Typography fontSize="large" className="museo-slab">
                {selectedRsu.properties.primary_route} Milepost {selectedRsu.properties.milepost}
              </Typography>
            </Grid2>
            <Grid2 size={4} justifyContent="flex-start" sx={{ ml: '16px' }}>
              <Typography fontSize="medium">IPv4: {selectedRsu.properties.ipv4_address}</Typography>
            </Grid2>
          </Grid2>
        </div>
      )}

      {selectedRsu && (
        <Box
          sx={{
            maxHeight: `calc(100vh - ${headerTabHeight + 185}px)`,
            overflowY: 'auto',
            height: 'fit-content',
            scrollbarColor: `${theme.palette.text.primary} ${theme.palette.background.paper}`,
            color: theme.palette.text.secondary,
          }}
        >
          <Accordion
            expanded={expanded === 'selected-rsu-current-config'}
            onChange={handleChange('selected-rsu-current-config')}
            elevation={0}
          >
            <AccordionSummary expandIcon={<ExpandMoreIcon />} aria-controls="panel1bh-content" id="panel1bh-header">
              <Typography>Current Configuration</Typography>
            </AccordionSummary>
            <AccordionDetails>
              <ConfigMenu>
                <SnmpwalkMenu />
              </ConfigMenu>
            </AccordionDetails>
          </Accordion>
          <Divider />
          <Accordion
            elevation={0}
            expanded={expanded === 'selected-rsu-add-msg-forwarding'}
            onChange={handleChange('selected-rsu-add-msg-forwarding')}
          >
            <AccordionSummary expandIcon={<ExpandMoreIcon />} aria-controls="panel2bh-content" id="panel2bh-header">
              <Typography>Message Forwarding</Typography>
            </AccordionSummary>
            <AccordionDetails>
              <ConfigMenu>
                <SnmpsetMenu type="single_rsu" rsuIpList={[selectedRsu.properties.ipv4_address]} />
              </ConfigMenu>
            </AccordionDetails>
          </Accordion>
          <Divider />
          <Accordion
            elevation={0}
            expanded={expanded === 'selected-rsu-firmware'}
            onChange={handleChange('selected-rsu-firmware')}
          >
            <AccordionSummary expandIcon={<ExpandMoreIcon />} aria-controls="panel3bh-content" id="panel3bh-header">
              <Typography>Firmware</Typography>
            </AccordionSummary>
            <AccordionDetails>
              <ConfigMenu>
                <RsuFirmwareMenu type="single_rsu" rsuIpList={[selectedRsu.properties.ipv4_address]} />
              </ConfigMenu>
            </AccordionDetails>
          </Accordion>
          <Divider />
          <Accordion
            elevation={0}
            expanded={expanded === 'selected-rsu-reboot'}
            onChange={handleChange('selected-rsu-reboot')}
          >
            <AccordionSummary expandIcon={<ExpandMoreIcon />} aria-controls="panel3bh-content" id="panel3bh-header">
              <Typography>Reboot</Typography>
            </AccordionSummary>
            <AccordionDetails>
              <ConfigMenu>
                <RsuRebootMenu />
              </ConfigMenu>
            </AccordionDetails>
          </Accordion>
        </Box>
      )}
      {selectedConfigList.length > 0 && !selectedRsu && (
        <Box sx={{ pl: 1, pr: 1 }}>
          <SideBarHeader onClick={() => dispatch(clearConfig())} title={'RSUs: ' + selectedConfigList.join(', ')} />
          <Box
            sx={{
              maxHeight: `calc(100vh - ${headerTabHeight + 185}px)`,
              overflowY: 'auto',
              height: 'fit-content',
              color: theme.palette.text.secondary,
            }}
          >
            <Accordion
              elevation={0}
              expanded={expanded === 'multiple-rsu-add-msg-forwarding'}
              onChange={handleChange('multiple-rsu-add-msg-forwarding')}
            >
              <AccordionSummary expandIcon={<ExpandMoreIcon />} aria-controls="panel2bh-content" id="panel2bh-header">
                <Typography>Message Forwarding</Typography>
              </AccordionSummary>
              <AccordionDetails>
                <ConfigMenu>
                  <SnmpsetMenu type="multi_rsu" rsuIpList={selectedConfigList.map((val: number) => val.toString())} />
                </ConfigMenu>
              </AccordionDetails>
            </Accordion>
            <Divider />
            <Accordion
              elevation={0}
              expanded={expanded === 'multiple-rsu-firmware'}
              onChange={handleChange('multiple-rsu-firmware')}
            >
              <AccordionSummary expandIcon={<ExpandMoreIcon />} aria-controls="panel3bh-content" id="panel3bh-header">
                <Typography>Firmware</Typography>
              </AccordionSummary>
              <AccordionDetails>
                <ConfigMenu>
                  <RsuFirmwareMenu
                    type="multi_rsu"
                    rsuIpList={selectedConfigList.map((val: number) => val.toString())}
                  />
                </ConfigMenu>
              </AccordionDetails>
            </Accordion>
          </Box>
        </Box>
      )}
    </Paper>
  )
}

export default ConfigureRSU
