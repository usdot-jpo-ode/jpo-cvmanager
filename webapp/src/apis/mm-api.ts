import { authApiHelper } from './api-helper-cviz'

class MessageMonitorApi {
  async getIntersections({ token }): Promise<IntersectionReferenceData[]> {
    var response = await authApiHelper.invokeApi({
      path: '/intersection/list',
      token: token,
      failureMessage: 'Failed to retrieve intersection list',
    })
    return response ?? []
  }

  async getSpatMessages({
    token,
    intersectionId,
    roadRegulatorId,
    startTime,
    endTime,
  }: {
    token: string
    intersectionId: number
    roadRegulatorId: number
    startTime?: Date
    endTime?: Date
  }): Promise<ProcessedSpat[]> {
    const queryParams: Record<string, string> = {}
    queryParams['intersection_id'] = intersectionId.toString()
    queryParams['road_regulator_id'] = roadRegulatorId.toString()
    if (startTime) queryParams['start_time_utc_millis'] = startTime.getTime().toString()
    if (endTime) queryParams['end_time_utc_millis'] = endTime.getTime().toString()

    var response = await authApiHelper.invokeApi({
      path: '/spat/json',
      token: token,
      queryParams,
      failureMessage: 'Failed to retrieve SPAT messages',
    })
    return response ?? ([] as ProcessedSpat[])
  }

  async getMapMessages({
    token,
    intersectionId,
    roadRegulatorId,
    startTime,
    endTime,
    latest,
  }: {
    token: string
    intersectionId: number
    roadRegulatorId: number
    startTime?: Date
    endTime?: Date
    latest?: boolean
  }): Promise<ProcessedMap[]> {
    const queryParams: Record<string, string> = {}
    queryParams['intersection_id'] = intersectionId.toString()
    queryParams['road_regulator_id'] = roadRegulatorId.toString()
    if (startTime) queryParams['start_time_utc_millis'] = startTime.getTime().toString()
    if (endTime) queryParams['end_time_utc_millis'] = endTime.getTime().toString()
    if (latest !== undefined) queryParams['latest'] = latest.toString()

    var response = await authApiHelper.invokeApi({
      path: '/map/json',
      token: token,
      queryParams,
      failureMessage: 'Failed to retrieve MAP messages',
    })
    return response ?? ([] as ProcessedMap[])
  }

  async getBsmMessages({
    token,
    vehicleId,
    startTime,
    endTime,
    long,
    lat,
    distance,
  }: {
    token: string
    vehicleId?: string
    startTime?: Date
    endTime?: Date
    long?: number
    lat?: number
    distance?: number
  }): Promise<OdeBsmData[]> {
    const queryParams: Record<string, string> = {}
    if (vehicleId) queryParams['origin_ip'] = vehicleId
    if (startTime) queryParams['start_time_utc_millis'] = startTime.getTime().toString()
    if (endTime) queryParams['end_time_utc_millis'] = endTime.getTime().toString()
    if (long) queryParams['longitude'] = long.toString()
    if (lat) queryParams['latitude'] = lat.toString()
    if (distance) queryParams['distance'] = distance.toString()

    var response = await authApiHelper.invokeApi({
      path: '/bsm/json',
      token: token,
      queryParams,
      failureMessage: 'Failed to retrieve BSM messages',
    })
    return response ?? ([] as OdeBsmData[])
  }
}

export default new MessageMonitorApi()
