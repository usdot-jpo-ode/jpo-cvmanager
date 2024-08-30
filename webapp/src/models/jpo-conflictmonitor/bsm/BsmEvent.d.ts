type BsmEvent = {
  startingBsm: OdeBsmData
  endingBsm: OdeBsmData
  startingBsmTimestamp: number
  endingBsmTimestamp: number
  wktPath: string
  wktMapBoundingBox: string
  inMapBoundingBox: boolean
  wallClockTimestamp: number
}
