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
  /**
   * Retrieves SPAT (Signal Phase and Timing) messages for a specific intersection,
   * including the latest SPAT message before the specified start time and all SPAT messages
   * within the specified time range
   *
   * @param {Object} params - The parameters for the API request.
   * @param {string} params.token - The authentication token for the API request.
   * @param {number} params.intersectionId - The ID of the intersection to filter SPAT messages.
   * @param {number} params.roadRegulatorId - The ID of the road regulator associated with the intersection.
   * @param {Date} [params.startTime] - The start time of the time range (optional).
   * @param {Date} [params.endTime] - The end time of the time range (optional).
   * @param {boolean} [params.compact] - Whether to request a compact version of the SPAT messages (optional).
   * @param {AbortController} [params.abortController] - Optional AbortController to cancel the API request.
   * @returns {Promise<ProcessedSpat[]>} - A promise that resolves to an array of processed SPAT messages.
   *
   * @throws {Error} - Throws an error if the API request fails.
   *
   * @description
   * This function retrieves SPAT messages for a specific intersection, including the latest SPAT message
   * before the specified start time and all SPAT messages within the specified time range.
   * This is intended to account for querying de-duplicated data, in which data within a specified time range may be sparse.
   * This function queries for data within the time range, as well as retrieving the latest SPaT message before the time range.
   * This ensures that there is data available for the start of the time range.
   */
  async getSpatMessagesWithLatest({
    token,
    intersectionId,
    roadRegulatorId,
    startTime,
    endTime,
    compact,
    abortController,
  }: {
    token: string
    intersectionId: number
    roadRegulatorId: number
    startTime?: Date
    endTime?: Date
    compact?: boolean
    abortController?: AbortController
  }): Promise<ProcessedSpat[]> {
    // Retrieve latest data before time interval
    const latestSpats = await this.getSpatMessages({
      token,
      intersectionId,
      roadRegulatorId,
      endTime: startTime,
      latest: true,
      compact,
      abortController,
    })
    // Retrieve data within time interval
    const allSpats = await this.getSpatMessages({
      token,
      intersectionId,
      roadRegulatorId,
      startTime,
      endTime,
      compact,
      abortController,
    })
    return [...allSpats, ...latestSpats].filter((spat) => spat != null)
  }

  async getSpatMessages({
    token,
    intersectionId,
    roadRegulatorId,
    startTime,
    endTime,
    latest,
    compact,
    abortController,
  }: {
    token: string
    intersectionId: number
    roadRegulatorId: number
    startTime?: Date
    endTime?: Date
    latest?: boolean
    compact?: boolean
    abortController?: AbortController
  }): Promise<ProcessedSpat[]> {
    const queryParams: Record<string, string> = {}
    queryParams['intersection_id'] = intersectionId.toString()
    queryParams['road_regulator_id'] = roadRegulatorId.toString()
    if (startTime) queryParams['start_time_utc_millis'] = startTime.getTime().toString()
    if (endTime) queryParams['end_time_utc_millis'] = endTime.getTime().toString()
    if (latest) queryParams['latest'] = latest.toString()
    if (compact) queryParams['compact'] = compact.toString()

    var response: PagedResponse<ProcessedSpat> = await authApiHelper.invokeApi({
      path: '/spat/json',
      token: token,
      queryParams,
      abortController,
      failureMessage: 'Failed to retrieve SPAT messages',
      tag: 'intersection',
    })
    return response?.content ?? ([] as ProcessedSpat[])
  }

  async getMapMessages({
    token,
    intersectionId,
    roadRegulatorId,
    startTime,
    endTime,
    latest,
    abortController,
  }: {
    token: string
    intersectionId: number
    roadRegulatorId?: number
    startTime?: Date
    endTime?: Date
    latest?: boolean
    abortController?: AbortController
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
      abortController,
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
    abortController,
  }: {
    token: string
    vehicleId?: string
    startTime?: Date
    endTime?: Date
    long?: number
    lat?: number
    distance?: number
    abortController?: AbortController
  }): Promise<OdeBsmData[]> {
    const queryParams: Record<string, string> = {}
    if (vehicleId) queryParams['origin_ip'] = vehicleId
    if (startTime) queryParams['start_time_utc_millis'] = startTime.getTime().toString()
    if (endTime) queryParams['end_time_utc_millis'] = endTime.getTime().toString()
    if (long) queryParams['longitude'] = long.toString()
    if (lat) queryParams['latitude'] = lat.toString()
    if (distance) queryParams['distance'] = distance.toString()

    var response: PagedResponse<OdeBsmData> = await authApiHelper.invokeApi({
      path: '/bsm/json',
      token: token,
      queryParams,
      abortController,
      failureMessage: 'Failed to retrieve BSM messages',
      tag: 'intersection',
    })
    return response?.content ?? ([] as OdeBsmData[])
  }

  async getMessageCount(
    token: string,
    messageType: string,
    intersectionId: number,
    startTime: Date,
    endTime: Date,
    abortController?: AbortController
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

    const response: PagedResponse<number> = await authApiHelper.invokeApi({
      path: `/${messageType}/count`,
      token: token,
      queryParams: queryParams,
      abortController,
      failureMessage: `Failed to retrieve message count for type ${messageType}`,
      tag: 'intersection',
    })
    return response?.content?.[0]
  }
}

export default new MessageMonitorApi()
