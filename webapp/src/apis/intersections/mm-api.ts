import { authApiHelper } from './api-helper-cviz'

class MessageMonitorApi {
  async getIntersections({ token }): Promise<IntersectionReferenceData[]> {
    var response = await authApiHelper.invokeApi({
      path: '/intersection/list',
      token: token,
      failureMessage: 'Failed to retrieve intersection list',
      tag: 'intersection',
    })
    return response ?? []
  }

  async getSpatMessages({
    token,
    intersectionId,
    roadRegulatorId,
    startTime,
    endTime,
    latest,
    compact,
  }: {
    token: string
    intersectionId: number
    roadRegulatorId: number
    startTime?: Date
    endTime?: Date
    latest?: boolean
    compact?: boolean
  }): Promise<ProcessedSpat[]> {
    const queryParams: Record<string, string> = {}
    queryParams['intersection_id'] = intersectionId.toString()
    queryParams['road_regulator_id'] = roadRegulatorId.toString()
    if (startTime) queryParams['start_time_utc_millis'] = startTime.getTime().toString()
    if (endTime) queryParams['end_time_utc_millis'] = endTime.getTime().toString()
    if (latest) queryParams['latest'] = latest.toString()
    if (compact) queryParams['compact'] = compact.toString()

    var response = await authApiHelper.invokeApi({
      path: '/spat/json',
      token: token,
      queryParams,
      failureMessage: 'Failed to retrieve SPAT messages',
      tag: 'intersection',
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
    roadRegulatorId?: number
    startTime?: Date
    endTime?: Date
    latest?: boolean
  }): Promise<ProcessedMap[]> {
    const queryParams: Record<string, string> = {}
    queryParams['intersection_id'] = intersectionId.toString()
    if (roadRegulatorId !== undefined) {
      queryParams['road_regulator_id'] = roadRegulatorId.toString()
    }
    if (startTime) queryParams['start_time_utc_millis'] = startTime.getTime().toString()
    if (endTime) queryParams['end_time_utc_millis'] = endTime.getTime().toString()
    if (latest !== undefined) queryParams['latest'] = latest.toString()

    var response = await authApiHelper.invokeApi({
      path: '/map/json',
      token: token,
      queryParams,
      failureMessage: 'Failed to retrieve MAP messages',
      tag: 'intersection',
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
      tag: 'intersection',
    })
    return response ?? ([] as OdeBsmData[])
  }

  async getMessageCount(
    token: string,
    messageType: string,
    intersectionId: number,
    startTime: Date,
    endTime: Date
  ): Promise<number> {
    var queryParams: Record<string, string> = {
      start_time_utc_millis: startTime.getTime().toString(),
      end_time_utc_millis: endTime.getTime().toString(),
      test: 'false',
    }

    if (messageType == 'bsm') {
      // Call getMapMessages to get the latitude and longitude
      const mapMessages = await this.getMapMessages({
        token: token,
        intersectionId: intersectionId,
        latest: true,
      })
      const latestMapMessage = mapMessages[0]
      const coordinates = latestMapMessage?.properties.refPoint
      if (latestMapMessage && coordinates.latitude && coordinates.longitude) {
        queryParams['latitude'] = coordinates.latitude.toString()
        queryParams['longitude'] = coordinates.longitude.toString()
        queryParams['distance'] = '500'
      }
    }

    if (intersectionId !== -1) {
      queryParams['intersection_id'] = intersectionId.toString()
    }

    const response = await authApiHelper.invokeApi({
      path: `/${messageType}/count`,
      token: token,
      queryParams: queryParams,
      failureMessage: `Failed to retrieve message count for type ${messageType}`,
      tag: 'intersection',
    })
    return response
  }
}

export default new MessageMonitorApi()
