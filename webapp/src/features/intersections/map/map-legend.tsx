import { Paper, Box, IconButton, Typography, Divider, Fab, AccordionSummary, AccordionDetails } from '@mui/material'
import React, { useState } from 'react'
import MuiAccordion, { AccordionProps } from '@mui/material/Accordion'
import { styled, useTheme } from '@mui/material/styles'
import { selectMapLegendColors } from './map-layer-style-slice'
import { useSelector } from 'react-redux'
import { Close, ExpandMoreOutlined, VpnKeyOutlined } from '@mui/icons-material'

const Accordion = styled((props: AccordionProps) => <MuiAccordion disableGutters elevation={0} square {...props} />)(
  ({ theme }) => ({})
)

export const MapLegend = () => {
  const mapLegendColors = useSelector(selectMapLegendColors)
  const theme = useTheme()
  const [open, setOpen] = useState(false)

  const { bsmColors, travelConnectionColors, laneColors, signalHeadIcons } = mapLegendColors

  const bsmColorsList: JSX.Element[] = []
  for (const [key, value] of Object.entries(bsmColors)) {
    bsmColorsList.push(
      <React.Fragment key={key}>
        <div
          style={{
            display: 'flex',
            flexDirection: 'row',
            alignItems: 'center',
            margin: '5px',
          }}
        >
          <div style={{ height: 20, width: 20, backgroundColor: value }} />
          <Typography fontSize="small" sx={{ ml: 1 }}>
            {key}
          </Typography>
        </div>
      </React.Fragment>
    )
  }

  const travelConnectionColorsList: JSX.Element[] = []
  for (const [key, value] of Object.entries(travelConnectionColors)) {
    const barHeight = 20
    const numColorSets = 2
    const heightFactor = barHeight / numColorSets / value[1].reduce((partialSum, a) => partialSum + a, 0)
    const heightColored = value[1][0] * heightFactor
    const heightWhite = (value[1].length == 1 ? 0 : value[1][1]) * heightFactor
    travelConnectionColorsList.push(
      <React.Fragment key={key}>
        <div
          style={{
            display: 'flex',
            flexDirection: 'row',
            alignItems: 'center',
            margin: '5px',
          }}
        >
          <div style={{ display: 'flex', flexDirection: 'column' }}>
            <div style={{ height: heightColored, width: 10, backgroundColor: value[0] }} />
            <div style={{ height: heightWhite, width: 10, backgroundColor: '#ffffff' }} />
            <div style={{ height: heightColored, width: 10, backgroundColor: value[0] }} />
            <div style={{ height: heightWhite, width: 10, backgroundColor: '#ffffff' }} />
          </div>
          <Typography fontSize="small" sx={{ ml: 1 }}>
            {key}
          </Typography>
        </div>
      </React.Fragment>
    )
  }

  const laneColorsList: JSX.Element[] = []
  for (const [key, value] of Object.entries(laneColors)) {
    laneColorsList.push(
      <React.Fragment key={key}>
        <div
          style={{
            display: 'flex',
            flexDirection: 'row',
            alignItems: 'center',
            margin: '5px',
          }}
        >
          <div style={{ height: 20, width: 20, backgroundColor: value }} />
          <Typography fontSize="small" sx={{ ml: 1 }}>
            {key}
          </Typography>
        </div>
      </React.Fragment>
    )
  }

  const signalHeadIconsList: JSX.Element[] = []
  for (const [key, value] of Object.entries(signalHeadIcons)) {
    signalHeadIconsList.push(
      <React.Fragment key={key}>
        <div
          style={{
            display: 'flex',
            flexDirection: 'row',
            alignItems: 'center',
            margin: '5px',
          }}
        >
          <img src={value} style={{ height: 40, width: 30 }} />
          <Typography fontSize="small" sx={{ ml: 1 }}>
            {key}
          </Typography>
        </div>
      </React.Fragment>
    )
  }

  return (
    <>
      <Fab
        id="map-legend-button"
        style={{
          position: 'absolute',
          zIndex: 10,
          top: theme.spacing(3),
          right: theme.spacing(10),
          backgroundColor: theme.palette.background.paper,
        }}
        size="small"
        onClick={() => {
          setOpen(!open)
        }}
      >
        <VpnKeyOutlined />
      </Fab>
      <div
        style={{
          position: 'absolute',
          zIndex: 10,
          bottom: theme.spacing(3),
          maxHeight: 'calc(100vh - 240px)',
          right: 0,
          width: open ? 600 : 50,
          fontSize: '16px',
          overflow: 'auto',
          scrollBehavior: 'auto',
        }}
      >
        <Box style={{ position: 'relative', height: '100%', width: '100%' }}>
          <Paper sx={{ height: '100%', width: '100%' }} square>
            <Box>
              {!open ? null : (
                <>
                  <Box
                    sx={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      mb: 2,
                      px: 1,
                    }}
                  >
                    <Typography variant="h6">Legend</Typography>
                    <IconButton
                      onClick={() => {
                        setOpen(!open)
                      }}
                    >
                      <Close color="info" />
                    </IconButton>
                  </Box>
                  <Accordion disableGutters>
                    <AccordionSummary expandIcon={<ExpandMoreOutlined />}>
                      <Typography fontSize="small">Signal Heads</Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                      <div
                        style={{
                          display: 'flex',
                          flexDirection: 'column',
                          overflowY: 'auto',
                          justifyContent: 'flex-start',
                        }}
                      >
                        {signalHeadIconsList}
                      </div>
                    </AccordionDetails>
                  </Accordion>

                  <Divider sx={{ borderRadius: 1 }} />

                  <Accordion disableGutters>
                    <AccordionSummary expandIcon={<ExpandMoreOutlined />}>
                      <Typography fontSize="small">Lane Lines</Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                      <div
                        style={{
                          display: 'flex',
                          flexDirection: 'column',
                          overflowY: 'auto',
                          justifyContent: 'flex-start',
                        }}
                      >
                        {laneColorsList}
                      </div>
                    </AccordionDetails>
                  </Accordion>

                  <Divider sx={{ borderRadius: 1 }} />

                  <Accordion disableGutters>
                    <AccordionSummary expandIcon={<ExpandMoreOutlined />}>
                      <Typography fontSize="small">Lane Connections</Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                      <div
                        style={{
                          display: 'flex',
                          flexDirection: 'column',
                          overflowY: 'auto',
                          justifyContent: 'flex-start',
                        }}
                      >
                        {travelConnectionColorsList}
                      </div>
                    </AccordionDetails>
                  </Accordion>

                  <Accordion disableGutters>
                    <AccordionSummary expandIcon={<ExpandMoreOutlined />}>
                      <Typography fontSize="small">BSM Colors</Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                      <div
                        style={{
                          display: 'flex',
                          flexDirection: 'column',
                          overflowY: 'auto',
                          justifyContent: 'flex-start',
                        }}
                      >
                        {bsmColorsList}
                      </div>
                    </AccordionDetails>
                  </Accordion>

                  <Divider sx={{ borderRadius: 1 }} />
                </>
              )}
            </Box>
          </Paper>
        </Box>
      </div>
    </>
  )
}
