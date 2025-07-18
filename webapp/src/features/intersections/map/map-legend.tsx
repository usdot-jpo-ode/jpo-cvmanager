import { Paper, Box, IconButton, Typography, Fab, AccordionSummary, AccordionDetails } from '@mui/material'
import React from 'react'
import MuiAccordion, { AccordionProps } from '@mui/material/Accordion'
import { styled, useTheme } from '@mui/material/styles'
import { selectMapLegendColors } from './map-layer-style-slice'
import { useSelector } from 'react-redux'
import { Close, ExpandMoreOutlined, FormatListBulleted } from '@mui/icons-material'

const Accordion = styled((props: AccordionProps) => <MuiAccordion disableGutters elevation={0} square {...props} />)(
  () => ({})
)

type MapLegendProps = {
  openPanel: string
  setOpenPanel: (panel: string) => void
}

export const MapLegend = (props: MapLegendProps) => {
  const mapLegendColors = useSelector(selectMapLegendColors)
  const theme = useTheme()

  const toggleOpen = () => {
    if (props.openPanel === 'map-legend') {
      props.setOpenPanel('')
    } else {
      props.setOpenPanel('map-legend')
    }
  }

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
          <div style={{ height: 20, width: 20, backgroundColor: value as string }} />
          <Typography fontSize="14px" sx={{ ml: 1, textTransform: 'capitalize' }}>
            {key.toLowerCase()}
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
          <Typography fontSize="14px" sx={{ ml: 1, textTransform: 'capitalize' }}>
            {key.toLowerCase()}
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
          <div style={{ height: 20, width: 20, backgroundColor: value as string }} />
          <Typography fontSize="14px" sx={{ ml: 1, textTransform: 'capitalize' }}>
            {key.toLowerCase()}
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
          <img src={value as string} style={{ height: 40, width: 30 }} />
          <Typography fontSize="14px" sx={{ ml: 1, textTransform: 'capitalize' }}>
            {key.toLowerCase()}
          </Typography>
        </div>
      </React.Fragment>
    )
  }

  return (
    <>
      <Fab
        id="map-legend-button"
        sx={{
          position: 'absolute',
          zIndex: 10,
          top: theme.spacing(3),
          right: theme.spacing(10),
          backgroundColor: theme.palette.background.paper,
          '&:hover': {
            backgroundColor: theme.palette.custom.intersectionMapButtonHover,
          },
        }}
        size="small"
        onClick={() => {
          toggleOpen()
        }}
      >
        <FormatListBulleted />
      </Fab>
      <div
        style={{
          position: 'absolute',
          zIndex: 10,
          bottom: theme.spacing(3),
          maxHeight: 'calc(100vh - 240px)',
          right: 0,
          width: props.openPanel === 'map-legend' ? 600 : 0,
          fontSize: '16px',
        }}
      >
        {props.openPanel !== 'map-legend' ? null : (
          <Box style={{ position: 'relative', height: '100%', width: '100%' }}>
            <Paper sx={{ height: '100%', width: '100%', borderRadius: '4px' }} id="legend-paper" square>
              <Box>
                <Box
                  sx={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    padding: '8px 16px',
                  }}
                >
                  <Typography fontSize="16px">Legend</Typography>
                  <IconButton
                    onClick={() => {
                      toggleOpen()
                    }}
                  >
                    <Close color="info" />
                  </IconButton>
                </Box>
                <Box
                  sx={{
                    maxHeight: '600px',
                    overflow: 'auto',
                    scrollbarColor: `${theme.palette.text.primary} ${theme.palette.background.paper}`,
                    borderRadius: '4px',
                  }}
                >
                  <Accordion
                    sx={{
                      '& .Mui-expanded': {
                        backgroundColor: theme.palette.custom.intersectionMapAccordionExpanded,
                      },
                    }}
                    disableGutters
                  >
                    <AccordionSummary expandIcon={<ExpandMoreOutlined />}>
                      <Typography fontSize="16px">Signal Heads</Typography>
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

                  <Accordion
                    sx={{
                      '& .Mui-expanded': {
                        backgroundColor: theme.palette.custom.intersectionMapAccordionExpanded,
                      },
                    }}
                    disableGutters
                  >
                    <AccordionSummary expandIcon={<ExpandMoreOutlined />}>
                      <Typography fontSize="16px">Lane Lines</Typography>
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

                  <Accordion
                    sx={{
                      '& .Mui-expanded': {
                        backgroundColor: theme.palette.custom.intersectionMapAccordionExpanded,
                      },
                    }}
                    disableGutters
                  >
                    <AccordionSummary expandIcon={<ExpandMoreOutlined />}>
                      <Typography fontSize="16px">Lane Connections</Typography>
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

                  <Accordion
                    sx={{
                      '& .Mui-expanded': {
                        backgroundColor: theme.palette.custom.intersectionMapAccordionExpanded,
                      },
                    }}
                    disableGutters
                  >
                    <AccordionSummary expandIcon={<ExpandMoreOutlined />}>
                      <Typography fontSize="16px">BSM Colors</Typography>
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
                </Box>
              </Box>
            </Paper>
          </Box>
        )}
      </div>
    </>
  )
}
