type J2735BsmCoreData = {
  msgCnt: number
  id: string
  secMark: number

  position: OdePosition3D
  accelSet: J2735AccelerationSet4Way
  accuracy: J2735PositionalAccuracy

  transmission: J2735TransmissionState
  speed: number
  heading: number
  angle: number
  brakes: J2735BrakeSystemStatus
  size: J2735VehicleSize
}

type J2735AccelerationSet4Way = {
  accelLat: number
  accelLong: number
  accelVert: number
  accelYaw: number
}

type J2735PositionalAccuracy = {
  semiMajor: number
  semiMinor: number
  orientation: number
}

type J2735TransmissionState =
  | 'NEUTRAL'
  | 'PARK'
  | 'FORWARDGEARS'
  | 'REVERSEGEARS'
  | 'RESERVED1'
  | 'RESERVED2'
  | 'RESERVED3'
  | 'UNAVAILABLE'

type J2735BrakeSystemStatus = {
  wheelBrakes: J2735WheelBrakes
  traction: string
  abs: string
  scs: string
  brakeBoost: string
  auxBrakes: string
}

type J2735WheelBrakes = {
  unavailable: Boolean
  leftFront: Boolean
  leftRear: Boolean
  rightFront: Boolean
  rightRear: Boolean
}

type J2735VehicleSize = {
  width: number
  length: number
}
