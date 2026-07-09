import React from 'react'
import { Popup } from 'react-map-gl'

import { Box, Typography } from '@mui/material'
import { CustomTable } from './custom-table'
import { format, parseISO } from 'date-fns'
import { getSsmInfoList } from './utilities/message-utils'

const getSrmImportanceLevel = (level?: ProcessedRequestImportanceLevel): string => {
  if (!level || level === 'requestImportanceLevelUnKnown') return 'Unknown'
  if (level.startsWith('requestImportanceLevel')) return level.replace('requestImportanceLevel', '')
  return level
}

export const getSelectedLayerPopupContent = (feature: any) => {
  // Feature object has top level structure, but each sub-object is JSON serialized to a string
  switch (feature?.layer?.id) {
    case 'bsm': {
      const bsm = feature.properties
      return (
        <Box>
          <Typography sx={{ paddingLeft: 1 }}>BSM</Typography>
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
    case 'srm': {
      const srm = feature.properties as ProcessedSrmPropertiesWithStatus

      const rows: any[] = [
        ['Id', srm.vehicleID],
        ['Time', format(srm.timeStampEpochMillis, 'yyyy-MM-dd HH:mm:ss.SSS')],
        ['Importance Level', srm.importanceLevel],
        ['Role', srm.role],
      ]
      // Pre-process SSMs into a dictionary keyed by SRM vehicleID + requestID
      const ssms = (JSON.parse((srm.ssms as unknown as string) ?? '[]') as ProcessedSsm[]).flatMap(getSsmInfoList)
      const ssmResponseDict: Record<string, SsmInfo[]> = {}
      ssms.forEach((ssm) => {
        const key = ssm.requestInfo.vehicleID + '_' + ssm.requestID
        if (key in ssmResponseDict) {
          ssmResponseDict[key] = [...ssmResponseDict[key], ssm]
        } else if (key) {
          ssmResponseDict[key] = [ssm]
        }
      })
      JSON.parse((srm.requests as unknown as string) ?? '[]').forEach((request: ProcessedSignalRequest) => {
        rows.push([`Request ID`, request.requestID])
        rows.push([`  Seq Num`, srm.sequenceNumber])
        rows.push([`  Request Type`, request.priorityRequestType])
        if (request.estimatedTimeOfArrival) {
          rows.push([
            `  Estimated Arrival`,
            format(parseISO(request.estimatedTimeOfArrival), 'yyyy-MM-dd HH:mm:ss.SSS'),
          ])
          rows.push(['  Inbound Lane', request.inboundLaneID])
          rows.push(['  Outbound Lane', request.outboundLaneID])
        }
        if (request.inboundApproachID || request.outboundApproachID) {
          rows.push(['  Inbound Approach', request.inboundApproachID])
          rows.push(['  Outbound Approach', request.outboundApproachID])
        }
        if (request.inboundLaneConnectionID || request.outboundLaneConnectionID) {
          rows.push(['  Inbound Lane Connection', request.inboundLaneConnectionID])
          rows.push(['  Outbound Lane Connection', request.outboundLaneConnectionID])
        }
        const ssmKey = srm.vehicleID + '_' + request.requestID
        const ssms: SsmInfo[] = ssmResponseDict[ssmKey]
        if (ssms) {
          // Find matching SSM by requesterSequenceNumber, or use latest if none match
          const sortedSsms = ssms.toSorted((a, b) => (b.sequenceNumber ?? 0) - (a.sequenceNumber ?? 0))
          const matchingSsm = sortedSsms[0]
          rows.push([`  SSM Status (seq ${matchingSsm.requestInfo.requesterSequenceNumber})`, matchingSsm.status])
        }
      })

      return (
        <Box>
          <Typography sx={{ paddingLeft: 1 }}>SRM</Typography>
          <CustomTable headers={['Field', 'Value']} data={rows} />
        </Box>
      )
    }
    case 'srm-requested-lanes':
    case 'map-message': {
      const map = feature.properties
      const rows: any[] = []
      JSON.parse(map?.connectsTo ?? '[]')?.forEach((connectsTo) => {
        rows.push(['Connected Lane', connectsTo.connectingLane.lane])
        rows.push(['Signal Group', connectsTo.signalGroup])
        rows.push(['Connection ID', connectsTo.connectionID])
      })
      const ssmResponses = JSON.parse(map?.signalStatuses ?? '[]') as SsmInfo[]
      const ssmResponseDict: Record<string, SsmInfo[]> = {}
      ssmResponses.forEach((ssm) => {
        const key = ssm.requestInfo.vehicleID + '_' + ssm.requestID
        if (key in ssmResponseDict) {
          ssmResponseDict[key] = [...ssmResponseDict[key], ssm]
        } else if (key) {
          ssmResponseDict[key] = [ssm]
        }
      })
      JSON.parse(map?.signalRequests ?? '[]').forEach((srm: SrmInfo) => {
        rows.push([`SRM ID`, srm.requestID])
        rows.push(['  Status', srm.priorityRequestType])
        if (srm.estimatedTimeOfArrival)
          rows.push([`  Estimated Arrival`, format(parseISO(srm.estimatedTimeOfArrival), 'yyyy-MM-dd HH:mm:ss.SSS')])
        rows.push(['  Sequence Number', srm.sequenceNumber])
        if (srm.inboundLaneID || srm.outboundLaneID) {
          rows.push(['  Inbound Lane', srm.inboundLaneID])
          rows.push(['  Outbound Lane', srm.outboundLaneID])
        }
        if (srm.inboundLaneConnectionID || srm.outboundLaneConnectionID) {
          rows.push(['  Inbound Lane Connection', srm.inboundLaneConnectionID])
          rows.push(['  Outbound Lane Connection', srm.outboundLaneConnectionID])
        }
        const ssmKey = srm.vehicleInfo.vehicleID + '_' + srm.requestID
        const ssms: SsmInfo[] = ssmResponseDict[ssmKey]
        if (ssms) {
          // Find matching SSM by requesterSequenceNumber, or use latest if none match
          const sortedSsms = ssms.toSorted((a, b) => (b.sequenceNumber ?? 0) - (a.sequenceNumber ?? 0))
          const matchingSsm = sortedSsms[0]
          rows.push([`  SSM Status (seq ${matchingSsm.requestInfo.requesterSequenceNumber})`, matchingSsm.status])
        }
      })
      return (
        <Box>
          <Typography sx={{ paddingLeft: 1 }}>MAP Lane</Typography>
          <CustomTable headers={['Field', 'Value']} data={[['Lane Id', map.laneId], ...rows]} />
        </Box>
      )
    }
    case 'ssm-connection-status':
    case 'ssm-connection-highlight':
    case 'connecting-lanes': {
      const map = feature.properties
      const rows: any[] = [
        ['State', feature.properties.signalState],
        ['Ingress Lane', feature.properties.ingressLaneId],
        ['Egress Lane', feature.properties.egressLaneId],
        ['Signal Group', feature.properties.signalGroupId],
      ]
      // Get latest SSMs, one per vehicleID
      const signalStatuses: Record<string, SsmInfo> = {}
      JSON.parse(map?.signalStatuses ?? '[]').forEach((ssm: SsmInfo) => {
        const vehicleId = ssm.requestInfo?.vehicleID
        if (!vehicleId) return
        const existing = signalStatuses[vehicleId]
        if (!existing || (ssm.sequenceNumber ?? 0) > (existing.sequenceNumber ?? 0)) {
          signalStatuses[vehicleId] = ssm
        }
      })
      // Add SSM info to table
      Object.values(signalStatuses).forEach((ssm: SsmInfo) => {
        rows.push([`SSM ID`, ssm.requestID])
        rows.push([`  Status`, ssm.status])
        if (ssm.inboundLaneID || ssm.outboundLaneID) {
          rows.push(['  Inbound Lane', ssm.inboundLaneID])
          rows.push(['  Outbound Lane', ssm.outboundLaneID])
        }
        if (ssm.inboundLaneConnectionID || ssm.outboundLaneConnectionID) {
          rows.push(['  Inbound Lane Connection', ssm.inboundLaneConnectionID])
          rows.push(['  Outbound Lane Connection', ssm.outboundLaneConnectionID])
        }
        if (ssm.requestInfo) {
          rows.push(['  SRM Veh. ID', ssm.requestInfo.vehicleID])
          rows.push(['  SRM Veh. Role', ssm.requestInfo.role])
          rows.push(['  SRM Req. Level', getSrmImportanceLevel(ssm.requestInfo.importanceLevel)])
        }
      })
      return (
        <Box>
          <Typography sx={{ paddingLeft: 1 }}>Connecting Lane</Typography>
          <CustomTable headers={['Field', 'Value']} data={rows} />
        </Box>
      )
    }

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
      return <Typography sx={{ paddingLeft: 1 }}>{JSON.stringify(feature)}</Typography>
    }
  }
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
