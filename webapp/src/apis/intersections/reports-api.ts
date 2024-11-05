import { authApiHelper } from './api-helper-cviz'

export type ReportMetadata = {
  reportName: string
  intersectionID: number
  roadRegulatorID: string
  reportGeneratedAt: Date
  reportStartTime: Date
  reportStopTime: Date
  reportContents: string[]
}

class ReportsApi {
  async generateReport({
    token,
    intersectionId,
    roadRegulatorId,
    startTime,
    endTime,
    abortController,
  }: {
    token: string
    intersectionId: number
    roadRegulatorId: number
    startTime: Date
    endTime: Date
    abortController?: AbortController
  }): Promise<Blob | undefined> {
    const queryParams: Record<string, string> = {}
    queryParams['intersection_id'] = intersectionId.toString()
    queryParams['road_regulator_id'] = roadRegulatorId.toString()
    if (startTime) queryParams['start_time_utc_millis'] = startTime.getTime().toString()
    if (endTime) queryParams['end_time_utc_millis'] = endTime.getTime().toString()

    const pdfReport = await authApiHelper.invokeApi({
      path: `/reports/generate`,
      token: token,
      responseType: 'blob',
      queryParams,
      abortController,
      failureMessage: 'Failed to generate PDF report',
    })

    return pdfReport
  }

  async listReports({
    token,
    intersectionId,
    roadRegulatorId,
    startTime,
    endTime,
    abortController,
  }: {
    token: string
    intersectionId: number
    roadRegulatorId: number
    startTime: Date
    endTime: Date
    abortController?: AbortController
  }): Promise<ReportMetadata[] | undefined> {
    const queryParams: Record<string, string> = {}
    queryParams['intersection_id'] = intersectionId.toString()
    queryParams['road_regulator_id'] = roadRegulatorId.toString()
    queryParams['start_time_utc_millis'] = startTime.getTime().toString()
    queryParams['end_time_utc_millis'] = endTime.getTime().toString()
    queryParams['latest'] = 'false'

    const pdfReport = await authApiHelper.invokeApi({
      path: `/reports/list`,
      token: token,
      queryParams,
      abortController,
      failureMessage: 'Failed to list PDF reports',
    })

    return pdfReport
  }

  async downloadReport({
    token,
    reportName,
    abortController,
  }: {
    token: string
    reportName: string
    abortController?: AbortController
  }): Promise<Blob | undefined> {
    const queryParams: Record<string, string> = {}
    queryParams['report_name'] = reportName

    const pdfReport = await authApiHelper.invokeApi({
      path: `/reports/download`,
      token: token,
      responseType: 'blob',
      queryParams,
      abortController,
      failureMessage: `Failed to download PDF report ${reportName}`,
    })

    return pdfReport
  }
}

export default new ReportsApi()
