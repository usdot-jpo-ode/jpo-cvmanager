type ProcessedSrmFeature = {
  type: 'Feature'
  geometry: {
    type: 'Point'
    coordinates: [number, number]
  }
  properties: ProcessedSrmProperties
}

type ProcessedSrmFeatureWithStatus = {
  type: 'Feature'
  geometry: {
    type: 'Point'
    coordinates: [number, number]
  }
  properties: ProcessedSrmPropertiesWithStatus
}

type ProcessedSrmProperties = {
  schemaVersion: number
  messageType: 'SRM'
  odeReceivedAt: string
  odeReceivedAtEpochMillis: number
  originIp: string
  asn1: string
  timeStamp: string
  timeStampEpochMillis: number
  sequenceNumber?: number
  vehicleID: string
  role: ProcessedBasicVehicleRole
  subrole?: ProcessedRequestSubRole
  importanceLevel?: ProcessedRequestImportanceLevel
  iso3833VehicleType?: number
  hpmsType?: ProcessedVehicleType
  latitude: number
  longitude: number
  elevation?: number
  heading?: number
  transmission?: ProcessedTransmissionState
  speedMetersPerSecond?: number
  name?: string
  routeName?: string
  transitStatus?: ProcessedTransitVehicleStatus
  transitOccupancy?: ProcessedTransitVehicleOccupancy
  transitScheduleSeconds?: number // Duration in seconds
  requests?: ProcessedSignalRequest[]
  validationMessages: ProcessedValidationMessage[]
}

type ProcessedSrmPropertiesWithStatus = ProcessedSrmProperties & {
  ssms: ProcessedSsm[]
}

type ProcessedSignalRequest = {
  region?: number
  intersectionId: number
  requestID: number
  priorityRequestType: ProcessedPriorityRequestType
  inboundLaneID?: number
  inboundApproachID?: number // Will not be used in CV Manager
  inboundLaneConnectionID?: number
  outboundLaneID?: number
  outboundApproachID?: number // Will not be used in CV Manager
  outboundLaneConnectionID?: number
  estimatedTimeOfArrival?: string
  estimatedTimeOfArrivalDurationSeconds?: number // Duration in seconds
}

type SrmInfo = {
  vehicleInfo: SrmVehicleInfo
  timeStampEpochMillis: number
  sequenceNumber?: number
  requestID: number
  priorityRequestType: ProcessedPriorityRequestType
  inboundLaneID?: number
  inboundLaneConnectionID?: number
  outboundLaneID?: number
  outboundLaneConnectionID?: number
  estimatedTimeOfArrival?: string
  estimatedTimeOfArrivalDurationSeconds?: number // Duration in seconds
  responseInfo?: SsmInfo
}

type SrmVehicleInfo = {
  vehicleID: string
  role: ProcessedBasicVehicleRole
  subrole?: ProcessedRequestSubRole
  importanceLevel?: ProcessedRequestImportanceLevel
  iso3833VehicleType?: number
  hpmsType?: ProcessedVehicleType
}

// Enum-like types
type ProcessedBasicVehicleRole =
  | 'basicVehicle'
  | 'publicTransport'
  | 'specialTransport'
  | 'dangerousGoods'
  | 'roadWork'
  | 'roadRescue'
  | 'emergency'
  | 'safetyCar'
  | 'none-unknown'
  | 'truck'
  | 'motorcycle'
  | 'roadSideSource'
  | 'police'
  | 'fire'
  | 'ambulance'
  | 'dot'
  | 'transit'
  | 'slowMoving'
  | 'stopNgo'
  | 'cyclist'
  | 'pedestrian'
  | 'nonMotorized'
  | 'military'

type ProcessedRequestSubRole =
  | 'requestSubRoleUnKnown'
  | 'requestSubRole1'
  | 'requestSubRole2'
  | 'requestSubRole3'
  | 'requestSubRole4'
  | 'requestSubRole5'
  | 'requestSubRole6'
  | 'requestSubRole7'
  | 'requestSubRole8'
  | 'requestSubRole9'
  | 'requestSubRole10'
  | 'requestSubRole11'
  | 'requestSubRole12'
  | 'requestSubRole13'
  | 'requestSubRole14'
  | 'requestSubRoleReserved'

type ProcessedRequestImportanceLevel =
  | 'requestImportanceLevelUnKnown'
  | 'requestImportanceLevel1'
  | 'requestImportanceLevel2'
  | 'requestImportanceLevel3'
  | 'requestImportanceLevel4'
  | 'requestImportanceLevel5'
  | 'requestImportanceLevel6'
  | 'requestImportanceLevel7'
  | 'requestImportanceLevel8'
  | 'requestImportanceLevel9'
  | 'requestImportanceLevel10'
  | 'requestImportanceLevel11'
  | 'requestImportanceLevel12'
  | 'requestImportanceLevel13'
  | 'requestImportanceLevel14'
  | 'requestImportanceReserved'

type ProcessedVehicleType =
  | 'none'
  | 'unknown'
  | 'special'
  | 'moto'
  | 'car'
  | 'carOther'
  | 'bus'
  | 'axleCnt2'
  | 'axleCnt3'
  | 'axleCnt4'
  | 'axleCnt4Trailer'
  | 'axleCnt5Trailer'
  | 'axleCnt6Trailer'
  | 'axleCnt5MultiTrailer'
  | 'axleCnt6MultiTrailer'
  | 'axleCnt7MultiTrailer'

type ProcessedTransitVehicleStatus = 'loading' | 'anADAuse' | 'aBikeLoad' | 'doorOpen' | 'charging' | 'atStopLine'

type ProcessedTransitVehicleOccupancy =
  | 'occupancyUnknown'
  | 'occupancyEmpty'
  | 'occupancyVeryLow'
  | 'occupancyLow'
  | 'occupancyMed'
  | 'occupancyHigh'
  | 'occupancyNearlyFull'
  | 'occupancyFull'

type ProcessedPriorityRequestType =
  | 'priorityRequestTypeReserved'
  | 'priorityRequest'
  | 'priorityRequestUpdate'
  | 'priorityCancellation'
