import React, { useEffect, useState, useRef } from 'react'
import {
  Box,
  Typography,
  Button,
  CircularProgress,
  Checkbox,
  FormControlLabel,
  LinearProgress,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
} from '@mui/material'
import { ReportMetadata } from '../../../apis/intersections/reports-api'
import { format } from 'date-fns'
import ValidConnectionOfTravelGraph from './graphs/valid-connection-of-travel-graph'
import InvalidConnectionOfTravelGraph from './graphs/invalid-connection-of-travel-graph'
import LaneDirectionDistanceGraph from './graphs/lane-direction-distance-graph'
import LaneDirectionHeadingGraph from './graphs/lane-direction-heading-graph'
import DistanceFromCenterlineGraphSet from './graphs/distance-from-centerline-graph-set'
import HeadingErrorGraphSet from './graphs/heading-error-graph-set'
import { generatePdf } from './pdf-generator'
import { generateDateRange, processMissingElements } from './report-utils'
import StopLineStackedGraph from './graphs/stop-line-stacked-graph'
import SignalGroupPassageGraph from './graphs/signal-group-passage-graph'
import SignalGroupStopGraph from './graphs/signal-group-stop-graph'
import reportColorPalette from './report-color-palette'
import BarChartComponent from './graphs/bar-chart-component'
import {
  LaneDirectionOfTravelReportDataByLaneId,
  StopLinePassageReportData,
  StopLineStopReportData,
} from '../../../models/ReportData'

interface ReportDetailsModalProps {
  open: boolean
  onClose: () => void
  report: ReportMetadata | null
}

const ReportDetailsModal = ({ open, onClose, report }: ReportDetailsModalProps) => {
  const [mapBroadcastRateEventCount, setMapBroadcastRateEventCount] = useState<{ name: string; value: number }[]>([])
  const [mapMinimumDataEventCount, setMapMinimumDataEventCount] = useState<{ name: string; value: number }[]>([])
  const [timeChangeDetailsEventCount, setTimeChangeDetailsEventCount] = useState<{ name: string; value: number }[]>([])
  const [spatMinimumDataEventCount, setSpatMinimumDataEventCount] = useState<{ name: string; value: number }[]>([])
  const [spatBroadcastRateEventCount, setSpatBroadcastRateEventCount] = useState<{ name: string; value: number }[]>([])
  const [signalStateConflictEventCount, setSignalStateConflictEventCount] = useState<{ name: string; value: number }[]>(
    []
  )
  const [signalStateEventCounts, setSignalStateEventCounts] = useState<{ name: string; value: number }[]>([])
  const [stopLineStopEventCounts, setStopLineStopEventCounts] = useState<{ name: string; value: number }[]>([])
  const [signalGroupStopLineData, setSignalGroupStopLineData] = useState<StopLineStopReportData[]>([])
  const [signalGroupPassageData, setSignalGroupPassageData] = useState<StopLinePassageReportData[]>([])
  const [connectionOfTravelEventCounts, setConnectionOfTravelEventCounts] = useState<{ name: string; value: number }[]>(
    []
  )
  const [laneDirectionOfTravelEventCounts, setLaneDirectionOfTravelEventCounts] = useState<
    { name: string; value: number }[]
  >([])
  const [laneDirectionDistanceDistribution, setLaneDirectionDistanceDistribution] = useState<
    { name: string; value: number }[]
  >([])
  const [laneDirectionHeadingDistribution, setLaneDirectionHeadingDistribution] = useState<
    { name: string; value: number }[]
  >([])
  const [intersectionReferenceAlignmentEventCounts, setIntersectionReferenceAlignmentEventCounts] = useState<
    { name: string; value: number }[]
  >([])
  const [laneDirectionOfTravelReportDataByLaneId, setLaneDirectionOfTravelReportDataByLaneId] =
    useState<LaneDirectionOfTravelReportDataByLaneId>({})
  const [mapMissingElements, setMapMissingElements] = useState<string[]>([])
  const [spatMissingElements, setSpatMissingElements] = useState<string[]>([])
  const [loading, setLoading] = useState(false)
  const [includeLaneSpecificCharts, setIncludeLaneSpecificCharts] = useState(false)
  const [progress, setProgress] = useState(0)
  const [isWindowWideEnough, setIsWindowWideEnough] = useState(window.innerWidth >= 820)
  const abortControllerRef = useRef<AbortController | null>(null)

  const generateMergedData = (eventCounts: { id: string; count: number }[], dateRange: string[]) => {
    const eventCountMap = new Map(eventCounts.map((item: any) => [item.id, item.count]))
    return dateRange.map((date) => ({
      name: date,
      value: eventCountMap.get(date) || 0,
    }))
  }

  useEffect(() => {
    if (report) {
      const dateRange = generateDateRange(new Date(report.reportStartTime), new Date(report.reportStopTime))

      const eventCounts = [
        { data: report.mapBroadcastRateEventCount, setter: setMapBroadcastRateEventCount },
        { data: report.mapMinimumDataEventCount, setter: setMapMinimumDataEventCount },
        { data: report.timeChangeDetailsEventCount, setter: setTimeChangeDetailsEventCount },
        { data: report.spatMinimumDataEventCount, setter: setSpatMinimumDataEventCount },
        { data: report.spatBroadcastRateEventCount, setter: setSpatBroadcastRateEventCount },
        { data: report.signalStateConflictEventCount, setter: setSignalStateConflictEventCount },
        { data: report.signalStateEventCounts, setter: setSignalStateEventCounts },
        { data: report.signalStateStopEventCounts, setter: setStopLineStopEventCounts },
        { data: report.connectionOfTravelEventCounts, setter: setConnectionOfTravelEventCounts },
        { data: report.laneDirectionOfTravelEventCounts, setter: setLaneDirectionOfTravelEventCounts },
        {
          data: report.intersectionReferenceAlignmentEventCounts,
          setter: setIntersectionReferenceAlignmentEventCounts,
        },
      ]

      eventCounts.forEach(({ data, setter }) => {
        if (data) {
          setter(generateMergedData(data, dateRange))
        }
      })

      // Set state for lane direction distance and heading distributions directly
      if (report.laneDirectionOfTravelMedianDistanceDistribution) {
        setLaneDirectionDistanceDistribution(
          report.laneDirectionOfTravelMedianDistanceDistribution.map((item) => ({
            name: item.id,
            value: item.count,
          }))
        )
      }

      if (report.laneDirectionOfTravelMedianHeadingDistribution) {
        setLaneDirectionHeadingDistribution(
          report.laneDirectionOfTravelMedianHeadingDistribution.map((item) => ({
            name: item.id,
            value: item.count,
          }))
        )
      }

      // Set state for lane direction of travel report data
      if (report.laneDirectionOfTravelReportData) {
        setLaneDirectionOfTravelReportDataByLaneId(
          report.laneDirectionOfTravelReportData.reduce((acc, item) => {
            if (!acc[item.laneID]) {
              acc[item.laneID] = []
            }
            acc[item.laneID].push(item)
            return acc
          }, {})
        )
      }

      // Set state for signal group stop line data
      if (report.stopLineStopReportData) {
        setSignalGroupStopLineData(report.stopLineStopReportData)
      }

      // Set state for signal group passage data
      if (report.stopLinePassageReportData) {
        setSignalGroupPassageData(report.stopLinePassageReportData)
      }

      // Process and set missing elements
      if (report.latestMapMinimumDataEventMissingElements) {
        setMapMissingElements(processMissingElements(report.latestMapMinimumDataEventMissingElements))
      }

      if (report.latestSpatMinimumDataEventMissingElements) {
        setSpatMissingElements(processMissingElements(report.latestSpatMinimumDataEventMissingElements))
      }
    }
  }, [report])

  useEffect(() => {
    if (open) {
      setIncludeLaneSpecificCharts(false) // Reset checkbox state when modal opens
    }
  }, [open])

  useEffect(() => {
    const handleResize = () => {
      setIsWindowWideEnough(window.innerWidth >= 810)
    }

    window.addEventListener('resize', handleResize)
    return () => {
      window.removeEventListener('resize', handleResize)
    }
  }, [])

  const getInterval = (dataLength: number) => {
    return dataLength <= 15 ? 0 : Math.ceil(dataLength / 30)
  }

  const renderList = (title: string, data: string[]) =>
    data.length > 0 && (
      <>
        <Typography variant="h6" align="center" sx={{ mt: 4 }}>
          {title}
        </Typography>
        <Box sx={{ mt: 2 }}>
          {data.map((element, index) => {
            const missingIndex = element.indexOf('missing')
            let firstWords = ''
            let restOfWords = element

            if (missingIndex !== -1) {
              firstWords = element.substring(0, missingIndex).trim()
              restOfWords = element.substring(missingIndex)
            }

            return (
              <Typography key={index} variant="body1" sx={{ mb: 1 }}>
                {firstWords && <span style={{ fontWeight: 'bold', color: reportColorPalette.blue }}>{firstWords}</span>}{' '}
                {restOfWords}
              </Typography>
            )
          })}
        </Box>
      </>
    )

  const handleGeneratePdf = async () => {
    if (report) {
      setLoading(true)
      setProgress(0) // Reset progress
      const abortController = new AbortController()
      abortControllerRef.current = abortController
      await generatePdf(report, setLoading, includeLaneSpecificCharts, () => open, setProgress, abortController.signal)
      setLoading(false)
    }
  }

  const handleClose = () => {
    if (abortControllerRef.current) {
      abortControllerRef.current.abort()
    }
    onClose()
  }

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="lg">
      {/* Dialog Title */}
      <DialogTitle>
        <Typography variant="h6">Report Details</Typography>
      </DialogTitle>

      {/* Dialog Content */}
      <DialogContent dividers>
        <Box sx={{ mb: 2 }}>
          {/* Scrollable Content */}
          <Box
            sx={{
              overflowY: 'auto', // Enable vertical scrolling
              flex: 1, // Allow the content to take up remaining space
            }}
          >
            {loading && <LinearProgress variant="determinate" value={progress} sx={{ mb: 2 }} />} {/* Progress bar */}
            {!report ? (
              <Typography>No report found</Typography>
            ) : (
              <>
                <Typography variant="h2" align="center">
                  Conflict Monitor Report
                </Typography>
                <Typography variant="h6" align="center">
                  {`Intersection ${report.intersectionID}`}
                </Typography>
                <Typography variant="body1" align="center">
                  {`${format(new Date(report.reportStartTime), "yyyy-MM-dd' T'HH:mm:ss'Z'")} - ${format(
                    new Date(report.reportStopTime),
                    "yyyy-MM-dd' T'HH:mm:ss'Z'"
                  )}`}
                </Typography>

                <Typography variant="h4" align="center" sx={{ mt: 4 }}>
                  Lane Direction of Travel
                </Typography>
                <Box id="lane-direction-of-travel-graph" sx={{ display: 'flex', justifyContent: 'center' }}>
                  <BarChartComponent
                    title="Lane Direction of Travel Events Per Day"
                    data={laneDirectionOfTravelEventCounts}
                    getInterval={getInterval}
                    barColor={reportColorPalette.cyan}
                  />
                </Box>
                <Typography variant="body2" align="center" sx={{ mt: 0.5, mb: 6, fontStyle: 'italic' }}>
                  The number of events triggered when vehicles passed a lane segment.
                </Typography>

                <Box id="lane-direction-distance-graph" sx={{ display: 'flex', justifyContent: 'center' }}>
                  <LaneDirectionDistanceGraph
                    data={laneDirectionDistanceDistribution}
                    getInterval={getInterval}
                    distanceTolerance={report.distanceTolerance}
                  />
                </Box>
                <Typography variant="body2" align="center" sx={{ mt: 0.5, mb: 6, fontStyle: 'italic' }}>
                  The median deviation in distance between vehicles and the center of the lane as defined by the MAP.
                </Typography>

                <Box id="lane-direction-heading-graph" sx={{ display: 'flex', justifyContent: 'center' }}>
                  <LaneDirectionHeadingGraph
                    data={laneDirectionHeadingDistribution}
                    headingTolerance={report.headingTolerance}
                  />
                </Box>
                <Typography variant="body2" align="center" sx={{ mt: 0.5, mb: 6, fontStyle: 'italic' }}>
                  The median deviation in heading between vehicles and the lanes as defined by the MAP.
                </Typography>

                {includeLaneSpecificCharts && (
                  <>
                    <Typography variant="h4" align="center" sx={{ mt: 4 }}>
                      Distance From Centerline Over Time
                    </Typography>
                    <Typography variant="body2" align="center" sx={{ mt: 0.5, mb: 6, fontStyle: 'italic' }}>
                      The average of median distances between vehicles and the centerline of each lane as it changed
                      over time.
                    </Typography>
                    <Box
                      id="distance-from-centerline-over-time-graphs"
                      sx={{ display: 'flex', justifyContent: 'center', flexDirection: 'column', alignItems: 'center' }}
                    >
                      <DistanceFromCenterlineGraphSet
                        data={laneDirectionOfTravelReportDataByLaneId}
                        distanceTolerance={report.distanceTolerance}
                      />
                    </Box>

                    <Typography variant="h4" align="center" sx={{ mt: 4 }}>
                      Vehicle Heading Error Delta
                    </Typography>
                    <Typography variant="body2" align="center" sx={{ mt: 0.5, mb: 6, fontStyle: 'italic' }}>
                      The median deviation in heading between vehicles and the expected heading as defined by the MAP.
                    </Typography>
                    <Box
                      id="heading-error-over-time-graphs"
                      sx={{ display: 'flex', justifyContent: 'center', flexDirection: 'column', alignItems: 'center' }}
                    >
                      <HeadingErrorGraphSet
                        data={laneDirectionOfTravelReportDataByLaneId}
                        headingTolerance={report.headingTolerance}
                      />
                    </Box>
                  </>
                )}

                <Typography variant="h4" align="center" sx={{ mt: 4 }}>
                  Connection of Travel
                </Typography>

                <Box id="connection-of-travel-graph" sx={{ display: 'flex', justifyContent: 'center' }}>
                  <BarChartComponent
                    title="Connection of Travel Events Per Day"
                    data={connectionOfTravelEventCounts}
                    getInterval={getInterval}
                    barColor={reportColorPalette.yellow}
                  />
                </Box>
                <Typography variant="body2" align="center" sx={{ mt: 0.5, mb: 6, fontStyle: 'italic' }}>
                  The number of events triggered when a vehicle entered and exited the intersection.
                </Typography>

                <Box id="valid-connection-of-travel-graph" sx={{ display: 'flex', justifyContent: 'center' }}>
                  <ValidConnectionOfTravelGraph data={report.validConnectionOfTravelData} />
                </Box>
                <Typography variant="body2" align="center" sx={{ mt: 0.5, mb: 6, fontStyle: 'italic' }}>
                  The number of vehicles that followed the defined ingress-egress lane pairings for each lane at the
                  intersection.
                </Typography>

                <Box id="invalid-connection-of-travel-graph" sx={{ display: 'flex', justifyContent: 'center' }}>
                  <InvalidConnectionOfTravelGraph data={report.invalidConnectionOfTravelData} />
                </Box>
                <Typography variant="body2" align="center" sx={{ mt: 0.5, mb: 6, fontStyle: 'italic' }}>
                  The number of vehicles that did not follow the defined ingress-egress lane pairings for each lane at
                  the intersection.
                </Typography>

                <Typography variant="h4" align="center" sx={{ mt: 4 }}>
                  Signal State Events
                </Typography>

                <Box id="signal-group-stop-line-graph" sx={{ display: 'flex', justifyContent: 'center' }}>
                  <SignalGroupStopGraph data={signalGroupStopLineData} />
                </Box>
                <Typography variant="body2" align="center" sx={{ mt: 0.5, mb: 6, fontStyle: 'italic' }}>
                  The percentage of time vehicles spent stopped at a light depending on the color of the light.
                </Typography>

                <Box id="signal-group-passage-line-graph" sx={{ display: 'flex', justifyContent: 'center' }}>
                  <SignalGroupPassageGraph data={signalGroupPassageData} />
                </Box>
                <Typography variant="body2" align="center" sx={{ mt: 0.5, mb: 6, fontStyle: 'italic' }}>
                  The percentage of vehicles that passed through a light depending on the color of the signal light.
                </Typography>

                <Box id="stop-line-stacked-graph" sx={{ display: 'flex', justifyContent: 'center' }}>
                  <StopLineStackedGraph
                    stopData={stopLineStopEventCounts}
                    passageData={signalStateEventCounts}
                    getInterval={getInterval}
                  />
                </Box>
                <Typography variant="body2" align="center" sx={{ mt: 0.5, mb: 6, fontStyle: 'italic' }}>
                  A composite view comparing vehicles that stopped before passing through the intersection versus those
                  that did not.
                </Typography>

                <Box id="signal-state-conflict-graph" sx={{ display: 'flex', justifyContent: 'center' }}>
                  <BarChartComponent
                    title="Signal State Conflict Events per Day"
                    data={signalStateConflictEventCount}
                    getInterval={getInterval}
                    barColor={reportColorPalette.blueGrey}
                  />
                </Box>
                <Typography variant="body2" align="center" sx={{ mt: 0.5, mb: 6, fontStyle: 'italic' }}>
                  The number of times the system detected contradictory signal states, such as conflicting green lights.
                  <br />
                  Lower numbers indicate better performance.
                </Typography>

                <Box id="time-change-details-graph" sx={{ display: 'flex', justifyContent: 'center' }}>
                  <BarChartComponent
                    title="Time Change Details Events per Day"
                    data={timeChangeDetailsEventCount}
                    getInterval={getInterval}
                    barColor={reportColorPalette.blueGrey}
                  />
                </Box>
                <Typography variant="body2" align="center" sx={{ mt: 0.5, mb: 6, fontStyle: 'italic' }}>
                  The number of times the system detected differences in timing between expected and actual signal state
                  changes.
                  <br />
                  Lower numbers indicate better performance.
                </Typography>

                <Typography variant="h4" align="center" sx={{ mt: 4 }}>
                  Intersection Reference Alignments Per Day
                </Typography>

                <Box id="intersection-reference-alignment-graph" sx={{ display: 'flex', justifyContent: 'center' }}>
                  <BarChartComponent
                    title="Intersection Reference Alignments Per Day"
                    data={intersectionReferenceAlignmentEventCounts}
                    getInterval={getInterval}
                    barColor={reportColorPalette.pink}
                  />
                </Box>
                <Typography variant="body2" align="center" sx={{ mt: 0.5, mb: 6, fontStyle: 'italic' }}>
                  The number of events flagging a mismatch between intersection ID and road regulator ID.
                  <br />
                  Lower numbers indicate better performance.
                </Typography>

                <Typography variant="h4" align="center" sx={{ mt: 4 }}>
                  MAP
                </Typography>

                <Box id="map-broadcast-rate-graph" sx={{ display: 'flex', justifyContent: 'center' }}>
                  <BarChartComponent
                    title="MAP Broadcast Rate Events per Day"
                    data={mapBroadcastRateEventCount}
                    getInterval={getInterval}
                    barColor={reportColorPalette.teal}
                  />
                </Box>
                <Typography variant="body2" align="center" sx={{ mt: 0.5, mb: 6, fontStyle: 'italic' }}>
                  The number of broadcast windows in which the system flagged more or less frequent MAP broadcasts than
                  the expected rate of 1 Hz. Each day has a total of 8,640 broadcast windows. Lower numbers indicate
                  better performance.
                </Typography>

                <Box id="map-minimum-data-graph" sx={{ display: 'flex', justifyContent: 'center' }}>
                  <BarChartComponent
                    title="MAP Minimum Data Events per Day"
                    data={mapMinimumDataEventCount}
                    getInterval={getInterval}
                    barColor={reportColorPalette.teal}
                  />
                </Box>
                <Typography variant="body2" align="center" sx={{ mt: 0.5, mb: 6, fontStyle: 'italic' }}>
                  The number of times the system flagged MAP messages with missing or incomplete data.
                  <br />
                  Lower numbers indicate better performance.
                </Typography>

                {mapMissingElements.length > 0 && renderList('MAP Missing Data Elements', mapMissingElements)}

                <Typography variant="h4" align="center" sx={{ mt: 4 }}>
                  SPaT
                </Typography>

                <Box id="spat-broadcast-rate-graph" sx={{ display: 'flex', justifyContent: 'center' }}>
                  <BarChartComponent
                    title="SPaT Broadcast Rate Events per Day"
                    data={spatBroadcastRateEventCount}
                    getInterval={getInterval}
                    barColor={reportColorPalette.purple}
                  />
                </Box>
                <Typography variant="body2" align="center" sx={{ mt: 0.5, mb: 6, fontStyle: 'italic' }}>
                  The number of broadcast windows in which the system flagged more or less frequent SPaT broadcasts than
                  the expected rate of 10 Hz. Each day has a total of 8,640 broadcast windows. Lower numbers indicate
                  better performance.
                </Typography>

                <Box id="spat-minimum-data-graph" sx={{ display: 'flex', justifyContent: 'center' }}>
                  <BarChartComponent
                    title="SPaT Minimum Data Events per Day"
                    data={spatMinimumDataEventCount}
                    getInterval={getInterval}
                    barColor={reportColorPalette.purple}
                  />
                </Box>
                <Typography variant="body2" align="center" sx={{ mt: 0.5, mb: 6, fontStyle: 'italic' }}>
                  The number of times the system flagged SPaT messages with missing or incomplete data.
                  <br />
                  Lower numbers indicate better performance.
                </Typography>

                {spatMissingElements.length > 0 && renderList('SPaT Missing Data Elements', spatMissingElements)}
              </>
            )}
          </Box>
        </Box>
      </DialogContent>

      {/* Dialog Actions */}
      <DialogActions sx={{ justifyContent: 'space-between' }}>
        {/* Left Section: Download and Include Lane-Specific Charts */}
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <Button
            onClick={handleGeneratePdf}
            variant="contained"
            color="primary"
            disabled={loading || !isWindowWideEnough}
            sx={{ mr: 2 }}
            className="capital-case"
          >
            {loading ? <CircularProgress size={24} /> : 'Download PDF'}
          </Button>
          <FormControlLabel
            control={
              <Checkbox
                checked={includeLaneSpecificCharts}
                onChange={(e) => setIncludeLaneSpecificCharts(e.target.checked)}
                color="primary"
                disabled={loading} // Disable the checkbox while loading
              />
            }
            label="Include lane-specific charts"
          />
        </Box>

        {/* Right Section: Close Button */}
        <Button onClick={handleClose} color="primary" className="capital-case">
          Close
        </Button>
      </DialogActions>
    </Dialog>
  )
}
export default ReportDetailsModal
