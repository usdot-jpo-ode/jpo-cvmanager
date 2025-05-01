import { authApiHelper } from './api-helper-cviz'

class AssessmentsApi {
  async getLatestAssessment(
    token: string,
    eventType: string,
    intersectionId: number,
    startTime?: Date,
    endTime?: Date,
    abortController?: AbortController
  ): Promise<Assessment | undefined> {
    const queryParams: Record<string, string> = {}
    queryParams['intersection_id'] = intersectionId.toString()
    queryParams['latest'] = 'true'
    if (startTime) queryParams['start_time_utc_millis'] = startTime.getTime().toString()
    if (endTime) queryParams['end_time_utc_millis'] = endTime.getTime().toString()

    var response =
      (
        (await authApiHelper.invokeApi({
          path: `/assessments/${eventType}`,
          token: token,
          queryParams,
          abortController,
          failureMessage: `Failed to retrieve assessments of type ${eventType}`,
          tag: 'intersection',
        })) as PagedResponse<Assessment>
      )?.content ?? []
    return response.pop()
  }

  async getAssessments(
    token: string,
    eventType: string,
    intersectionId: number,
    startTime?: Date,
    endTime?: Date,
    abortController?: AbortController
  ): Promise<Assessment[]> {
    const queryParams: Record<string, string> = {}
    queryParams['intersection_id'] = intersectionId.toString()
    queryParams['latest'] = 'false'
    if (startTime) queryParams['start_time_utc_millis'] = startTime.getTime().toString()
    if (endTime) queryParams['end_time_utc_millis'] = endTime.getTime().toString()

    return (
      (
        (await authApiHelper.invokeApi({
          path: `/assessments/${eventType}`,
          token: token,
          queryParams,
          abortController,
          failureMessage: `Failed to retrieve assessments of type ${eventType}`,
          tag: 'intersection',
        })) as PagedResponse<Assessment>
      )?.content ?? []
    )
  }
}

export default new AssessmentsApi()
