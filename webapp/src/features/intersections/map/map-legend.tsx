import { Box, Typography, AccordionSummary, AccordionDetails } from '@mui/material'
import React from 'react'
import MuiAccordion, { AccordionProps } from '@mui/material/Accordion'
import { styled, useTheme } from '@mui/material/styles'
import { selectMapLegendColors } from './map-layer-style-slice'
import { useSelector } from 'react-redux'
import MapFabTab from './map-fab-tab'
import { ExpandMoreOutlined, FormatListBulleted } from '@mui/icons-material'

const Accordion = styled((props: AccordionProps) => <MuiAccordion disableGutters elevation={0} square {...props} />)(
  () => ({})
)

type MapLegendProps = {
  openPanel: string
  setOpenPanel: (panel: string) => void
}

const hexToFilter = (hex: string): string => {
  // Simulate mapbox sdf recoloring using CSS filters
  // Remove # if present
  hex = hex.replace('#', '')

  // Convert hex to RGB (0-255)
  const r = Number.parseInt(hex.substring(0, 2), 16)
  const g = Number.parseInt(hex.substring(2, 4), 16)
  const b = Number.parseInt(hex.substring(4, 6), 16)

  // Check if it's a grey color (R ≈ G ≈ B)
  const isGrey = Math.abs(r - g) < 5 && Math.abs(g - b) < 5 && Math.abs(r - b) < 5

  if (isGrey) {
    // For grey colors, just use brightness
    const brightness = r / 255
    return `brightness(0) saturate(0%) invert(${brightness * 100}%)`
  }

  // For colored icons
  const rNorm = r / 255
  const gNorm = g / 255
  const bNorm = b / 255

  const max = Math.max(rNorm, gNorm, bNorm)
  const min = Math.min(rNorm, gNorm, bNorm)
  const delta = max - min

  // Calculate hue
  let hue = 0
  if (delta !== 0) {
    if (max === rNorm) {
      hue = 60 * (((gNorm - bNorm) / delta) % 6)
    } else if (max === gNorm) {
      hue = 60 * ((bNorm - rNorm) / delta + 2)
    } else {
      hue = 60 * ((rNorm - gNorm) / delta + 4)
    }
  }
  if (hue < 0) hue += 360

  // Calculate saturation
  const saturation = max === 0 ? 0 : delta / max

  // Calculate lightness (for better color accuracy)
  const lightness = (max + min) / 2

  // Adjust saturation multiplier based on the color
  // Pure colors (high saturation) need less boost
  const saturationMultiplier = saturation > 0.9 ? 5000 : 10000

  // For very bright, saturated colors (like pure red), reduce brightness boost
  const brightnessMultiplier = saturation > 0.9 && max > 0.9 ? 0.9 : 1.0

  return `brightness(0) saturate(100%) invert(${lightness * 100}%) sepia(100%) saturate(${
    saturation * saturationMultiplier
  }%) hue-rotate(${hue}deg) brightness(${brightnessMultiplier * 100}%)`
}

const LegendContainer = (props: { key: string; label: string; content: React.ReactNode }) => {
  return (
    <React.Fragment key={props.key}>
      <div
        style={{
          display: 'flex',
          flexDirection: 'row',
          alignItems: 'center',
          margin: '5px',
        }}
      >
        {props.content}
        <Typography fontSize="14px" sx={{ ml: 1, textTransform: 'capitalize' }}>
          {props.label}
        </Typography>
      </div>
    </React.Fragment>
  )
}

export const MapLegend = (props: MapLegendProps) => {
  const mapLegendColors = useSelector(selectMapLegendColors)
  const theme = useTheme()

  const { bsmColors, travelConnectionColors, laneColors, signalHeadIcons, ssmStatusIcons, srmColors } = mapLegendColors

  const bsmColorsList: JSX.Element[] = []
  for (const [key, value] of Object.entries(bsmColors)) {
    bsmColorsList.push(
      <LegendContainer
        key={key}
        label={key.toLowerCase()}
        content={<div style={{ height: 20, width: 20, backgroundColor: value as string }} />}
      />
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
      <LegendContainer
        key={key}
        label={key.toLowerCase()}
        content={
          <div style={{ display: 'flex', flexDirection: 'column' }}>
            <div style={{ height: heightColored, width: 10, backgroundColor: value[0] }} />
            <div style={{ height: heightWhite, width: 10, backgroundColor: '#ffffff' }} />
            <div style={{ height: heightColored, width: 10, backgroundColor: value[0] }} />
            <div style={{ height: heightWhite, width: 10, backgroundColor: '#ffffff' }} />
          </div>
        }
      />
    )
  }

  const laneColorsList: JSX.Element[] = []
  for (const [key, value] of Object.entries(laneColors)) {
    laneColorsList.push(
      <LegendContainer
        key={key}
        label={key.toLowerCase()}
        content={<div style={{ height: 20, width: 20, backgroundColor: value as string }} />}
      />
    )
  }

  const signalHeadIconsList: JSX.Element[] = []
  for (const [key, value] of Object.entries(signalHeadIcons)) {
    signalHeadIconsList.push(
      <LegendContainer
        key={key}
        label={key.toLowerCase()}
        content={<img src={value as string} style={{ height: 40, width: 30 }} />}
      />
    )
  }

  const ssmStatusIconList: JSX.Element[] = []
  for (const [key, value] of Object.entries(ssmStatusIcons)) {
    ssmStatusIconList.push(
      <LegendContainer
        key={key}
        label={key.toLowerCase()}
        content={
          <div
            style={{
              display: 'flex',
              flexDirection: 'row',
              alignItems: 'center',
              margin: '5px',
            }}
          >
            <div
              style={{
                backgroundColor: '#ffffff',
                padding: '2px',
                borderRadius: '2px',
                display: 'inline-flex', // ⚡ Add this to shrink-wrap
                alignItems: 'center', // ⚡ Add this to center the icon
                justifyContent: 'center', // ⚡ Add this to center the icon
              }}
            >
              <img
                src={value[0] as string}
                style={{
                  width: 20,
                  height: 20,
                  filter: hexToFilter(value[1]),
                }}
              />
            </div>
          </div>
        }
      />
    )
  }
  const srmColorsList: JSX.Element[] = []
  for (const [key, value] of Object.entries(srmColors)) {
    srmColorsList.push(
      <LegendContainer
        key={key}
        label={key.toLowerCase()}
        content={<div style={{ height: 20, width: 20, backgroundColor: value as string }} />}
      />
    )
  }

  const content = (
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
          <Typography fontSize="16px">SSM Status Icons</Typography>
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
            {ssmStatusIconList}
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

      <Accordion
        sx={{
          '& .Mui-expanded': {
            backgroundColor: theme.palette.custom.intersectionMapAccordionExpanded,
          },
        }}
        disableGutters
      >
        <AccordionSummary expandIcon={<ExpandMoreOutlined />}>
          <Typography fontSize="16px">SRM Vehicles</Typography>
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
            {srmColorsList}
          </div>
        </AccordionDetails>
      </Accordion>
    </Box>
  )

  return (
    <MapFabTab
      title="Legend"
      width="auto"
      right={theme.spacing(17)}
      panelId="map-legend"
      openPanel={props.openPanel}
      setOpenPanel={props.setOpenPanel}
      icon={<FormatListBulleted />}
      content={content}
    />
  )
}
