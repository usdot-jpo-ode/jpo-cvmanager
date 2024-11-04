import { authApiHelper } from './api-helper-cviz'

class AssessmentsApi {
  async getLatestAssessment(
    token: string,
    eventType: string,
    intersectionId: number,
    roadRegulatorId: number,
    startTime?: Date,
    endTime?: Date,
    abortController?: AbortController
  ): Promise<Assessment | undefined> {
    const queryParams: Record<string, string> = {}
    queryParams['intersection_id'] = intersectionId.toString()
    queryParams['road_regulator_id'] = roadRegulatorId.toString()
    queryParams['latest'] = 'true'
    if (startTime) queryParams['start_time_utc_millis'] = startTime.getTime().toString()
    if (endTime) queryParams['end_time_utc_millis'] = endTime.getTime().toString()

    var response =
      (await authApiHelper.invokeApi({
        path: `/assessments/${eventType}`,
        token: token,
        queryParams,
        abortController,
        failureMessage: `Failed to retrieve assessments of type ${eventType}`,
      })) ?? []
    return response.pop()
  }

  async getAssessments(
    token: string,
    eventType: string,
    intersectionId: number,
    roadRegulatorId: number,
    startTime?: Date,
    endTime?: Date,
    abortController?: AbortController
  ): Promise<Assessment[]> {
    const queryParams: Record<string, string> = {}
    // queryParams["road_regulator_id"] = road_regulator_id;
    queryParams['intersection_id'] = intersectionId.toString()
    queryParams['road_regulator_id'] = roadRegulatorId.toString()
    queryParams['latest'] = 'false'
    if (startTime) queryParams['start_time_utc_millis'] = startTime.getTime().toString()
    if (endTime) queryParams['end_time_utc_millis'] = endTime.getTime().toString()

    return (
      (await authApiHelper.invokeApi({
        path: `/assessments/${eventType}`,
        token: token,
        queryParams,
        abortController,
        failureMessage: `Failed to retrieve assessments of type ${eventType}`,
      })) ?? []
    )
  }
}

export default new AssessmentsApi()
