import {
  LaneDirectionOfTravelReportData,
  StopLineStopReportData,
  StopLinePassageReportData,
} from '../../features/intersections/reports/report-utils'
import { authApiHelper } from './api-helper-cviz'

export type ReportMetadata = {
  reportName: string
  intersectionID: number
  roadRegulatorID: string
  reportGeneratedAt: Date
  reportStartTime: Date
  reportStopTime: Date
  reportContents: string[]
  laneDirectionOfTravelEventCounts: { id: string; count: number }[]
  laneDirectionOfTravelMedianDistanceDistribution: { id: string; count: number }[]
  laneDirectionOfTravelMedianHeadingDistribution: { id: string; count: number }[]
  laneDirectionOfTravelReportData: LaneDirectionOfTravelReportData[]
  connectionOfTravelEventCounts: { id: string; count: number }[]
  signalStateConflictEventCount: { id: string; count: number }[]
  signalStateEventCounts: { id: string; count: number }[]
  signalStateStopEventCounts: { id: string; count: number }[]
  timeChangeDetailsEventCount: { id: string; count: number }[]
  intersectionReferenceAlignmentEventCounts: { id: string; count: number }[]
  mapBroadcastRateEventCount: { id: string; count: number }[]
  mapMinimumDataEventCount: { id: string; count: number }[]
  spatMinimumDataEventCount: { id: string; count: number }[]
  spatBroadcastRateEventCount: { id: string; count: number }[]
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
      tag: 'intersection',
    })

    if (pdfReport) {
      pdfReport.forEach((report: ReportMetadata) => {
        report.reportStartTime = new Date(
          new Date(report.reportStartTime).getTime() + new Date(report.reportStartTime).getTimezoneOffset() * 60000
        )
        report.reportStopTime = new Date(
          new Date(report.reportStopTime).getTime() + new Date(report.reportStopTime).getTimezoneOffset() * 60000
        )
      })
    }

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
      tag: 'intersection',
    })

    return pdfReport
  }
}

export default new ReportsApi()
