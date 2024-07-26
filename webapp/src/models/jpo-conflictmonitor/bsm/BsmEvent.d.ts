
type BsmEvent = {
  startingBsm: OdeBsmData
  endingBsm: OdeBsmData
  startingBsmTimestamp: number
  endingBsmTimestamp: number
  wktPath: str
  wktMapBoundingBox: str
  inMapBoundingBox: booleanean
  wallClockTimestamp: number
}