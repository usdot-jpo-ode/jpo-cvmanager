import React from 'react'
import { Popup } from 'react-map-gl'

import { Box, Typography } from '@mui/material'
import { CustomTable } from './custom-table'

export const getSelectedLayerPopupContent = (feature: any) => {
  switch (feature?.layer?.id) {
    case 'bsm': {
      const bsm = feature.properties
      return (
        <Box>
          <Typography>BSM</Typography>
          <CustomTable
            headers={['Field', 'Value']}
            data={[
              ['Id', bsm.id],
              ['Message Count', bsm.msgCnt],
              ['Time', bsm.secMark / 1000],
              ['Speed', bsm.speed],
              ['Heading', bsm.heading],
            ]}
          />
        </Box>
      )
    }
    case 'map-message': {
      const map = feature.properties
      const connectedObjs: any[] = []
      JSON.parse(map?.connectsTo ?? '[]')?.forEach((connectsTo) => {
        connectedObjs.push(['Connected Lane', connectsTo.connectingLane.lane])
        connectedObjs.push(['Signal Group', connectsTo.signalGroup])
        connectedObjs.push(['Connection ID', connectsTo.connectionID])
      })
      return (
        <Box>
          <Typography>MAP Lane</Typography>
          <CustomTable headers={['Field', 'Value']} data={[['Lane Id', map.laneId], ...connectedObjs]} />
        </Box>
      )
    }
    case 'connecting-lanes':
      return (
        <Box>
          <Typography>Connecting Lane</Typography>
          <CustomTable
            headers={['Field', 'Value']}
            data={[
              ['State', feature.properties.signalState],
              ['Ingress Lane', feature.properties.ingressLaneId],
              ['Egress Lane', feature.properties.egressLaneId],
              ['Signal Group', feature.properties.signalGroupId],
            ]}
          />
        </Box>
      )

    case 'signal-states':
      return (
        <Box>
          <Typography>Signal State</Typography>
          <CustomTable
            headers={['Field', 'Value']}
            data={[
              ['Signal State', feature.properties.signalState],
              ['Signal Group', feature.properties.signalGroup],
            ]}
          />
        </Box>
      )
    default: {
      return <Typography>{JSON.stringify(feature)}</Typography>
    }
  }
  return <Typography>No Data</Typography>
}

export const CustomPopup = (props) => {
  return (
    <Popup
      longitude={props.selectedFeature.clickedLocation.lng}
      latitude={props.selectedFeature.clickedLocation.lat}
      anchor="bottom"
      onClose={props.onClose}
      onOpen={() => {}}
      maxWidth={'500px'}
      closeOnClick={false}
    >
      {getSelectedLayerPopupContent(props.selectedFeature.feature)}
    </Popup>
  )
}
