export type LaneDirectionOfTravelReportData = {
  timestamp: number
  laneID: number
  segmentID: number
  headingDelta: number
  medianCenterlineDistance: number
}

export type LaneDirectionOfTravelReportDataByLaneId = {
  [laneID: number]: LaneDirectionOfTravelReportData[]
}

export type StopLineStopReportData = {
  signalGroup: number
  numberOfEvents: number
  timeStoppedOnRed: number
  timeStoppedOnYellow: number
  timeStoppedOnGreen: number
  timeStoppedOnDark: number
}

export type StopLinePassageReportData = {
  signalGroup: number
  totalEvents: number
  redEvents: number
  yellowEvents: number
  greenEvents: number
  darkEvents: number
}
