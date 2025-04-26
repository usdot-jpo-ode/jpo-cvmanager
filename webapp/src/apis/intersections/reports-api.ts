import {
  LaneDirectionOfTravelReportData,
  StopLinePassageReportData,
  StopLineStopReportData,
} from '../../models/ReportData'
import { authApiHelper } from './api-helper-cviz'

export type ReportMetadata = {
  reportName: string
  intersectionID: number
  roadRegulatorID: string
  reportGeneratedAt: Date
  reportStartTime: Date
  reportStopTime: Date
  reportContents: string[]
  laneDirectionOfTravelEventCounts: CountWithId[]
  laneDirectionOfTravelMedianDistanceDistribution: CountWithId[]
  laneDirectionOfTravelMedianHeadingDistribution: CountWithId[]
  laneDirectionOfTravelReportData: LaneDirectionOfTravelReportData[]
  connectionOfTravelEventCounts: CountWithId[]
  signalStateConflictEventCount: CountWithId[]
  signalStateEventCounts: CountWithId[]
  signalStateStopEventCounts: CountWithId[]
  timeChangeDetailsEventCount: CountWithId[]
  intersectionReferenceAlignmentEventCounts: CountWithId[]
  mapBroadcastRateEventCount: CountWithId[]
  mapMinimumDataEventCount: CountWithId[]
  spatMinimumDataEventCount: CountWithId[]
  spatBroadcastRateEventCount: CountWithId[]
  latestMapMinimumDataEventMissingElements: string[]
  latestSpatMinimumDataEventMissingElements: string[]
  validConnectionOfTravelData: {
    connectionID: number
    ingressLaneID: number
    egressLaneID: number
    eventCount: number
  }[]
  invalidConnectionOfTravelData: {
    connectionID: number
    ingressLaneID: number
    egressLaneID: number
    eventCount: number
  }[]
  headingTolerance: number
  distanceTolerance: number
  stopLineStopReportData: StopLineStopReportData[]
  stopLinePassageReportData: StopLinePassageReportData[]
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

    if (startTime) {
      const startTimeUTC = new Date(startTime.getTime() - startTime.getTimezoneOffset() * 60000)
      startTimeUTC.setSeconds(0, 0)
      queryParams['start_time_utc_millis'] = startTimeUTC.getTime().toString()
    }
    if (endTime) {
      const endTimeUTC = new Date(endTime.getTime() - endTime.getTimezoneOffset() * 60000)
      endTimeUTC.setSeconds(0, 0)
      queryParams['end_time_utc_millis'] = endTimeUTC.getTime().toString()
    }

    const pdfReport = await authApiHelper.invokeApi({
      path: `/reports/generate`,
      token: token,
      responseType: 'blob',
      queryParams,
      abortController,
      failureMessage: 'Failed to generate PDF report',
      tag: 'intersection',
    })

    return pdfReport?.content?.[0]
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
      tag: 'intersection',
    })

    const reportList = pdfReport?.content?.[0]

    if (reportList) {
      reportList.forEach((report: ReportMetadata) => {
        report.reportStartTime = new Date(
          new Date(report.reportStartTime).getTime() + new Date(report.reportStartTime).getTimezoneOffset() * 60000
        )
        report.reportStopTime = new Date(
          new Date(report.reportStopTime).getTime() + new Date(report.reportStopTime).getTimezoneOffset() * 60000
        )
      })
    }

    return reportList
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
      tag: 'intersection',
    })

    return pdfReport?.content?.[0]
  }
}

export default new ReportsApi()
