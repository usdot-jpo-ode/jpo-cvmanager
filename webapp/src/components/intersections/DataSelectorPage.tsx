import React, { useEffect } from 'react'
import { Box, Container, Typography } from '@mui/material'
import EventsApi from '../../apis/intersections/events-api'
import AssessmentsApi from '../../apis/intersections/assessments-api'
import MessageMonitorApi from '../../apis/intersections/mm-api'
import GraphsApi from '../../apis/intersections/graphs-api'
import { DataSelectorEditForm } from '../../features/intersections/data-selector/data-selector-edit-form'
import { EventDataTable } from '../../features/intersections/data-selector/event-data-table'
import { AssessmentDataTable } from '../../features/intersections/data-selector/assessment-data-table'
import { DataVisualizer } from '../../features/intersections/data-selector/data-visualizer'
import toast from 'react-hot-toast'
import MapDialog from '../../features/intersections/intersection-selector/intersection-selector-dialog'
import JSZip from 'jszip'
import FileSaver from 'file-saver'
import { selectSelectedIntersectionId, selectSelectedRoadRegulatorId } from '../../generalSlices/intersectionSlice'
import { selectToken } from '../../generalSlices/userSlice'
import {
  selectType,
  selectEvents,
  selectAssessments,
  selectGraphData,
  selectOpenMapDialog,
  selectRoadRegulatorIntersectionIds,
  setType,
  setEvents,
  setAssessments,
  setGraphData,
  setOpenMapDialog,
  setRoadRegulatorIntersectionIds,
} from '../../features/intersections/data-selector/dataSelectorSlice'
import { useAppDispatch, useAppSelector } from '../../hooks'

// TODO: Support additional event types
// - "intersection_reference_alignment"
// - "map_minimum_data"
// - "spat_minimum_data"
// - "map_broadcast_rate"
// - "spat_broadcast_rate"
const valid_counts_event_types: string[] = [
  'connection_of_travel',
  'lane_direction_of_travel',
  'signal_group_alignment',
  'signal_state_conflict',
  'signal_state',
  'signal_state_stop',
  'time_change_details',
]

const DataSelectorPage = () => {
  const dispatch = useAppDispatch()

  const intersectionId = useAppSelector(selectSelectedIntersectionId)
  const roadRegulatorId = useAppSelector(selectSelectedRoadRegulatorId)
  const token = useAppSelector(selectToken)
  const type = useAppSelector(selectType)
  const events = useAppSelector(selectEvents)
  const assessments = useAppSelector(selectAssessments)
  const graphData = useAppSelector(selectGraphData)
  const openMapDialog = useAppSelector(selectOpenMapDialog)
  const roadRegulatorIntersectionIds = useAppSelector(selectRoadRegulatorIntersectionIds)

  const getPaddedTimestamp = () => {
    const date = new Date()
    // create padded timestamp like YYMMdd_HHMMSS
    return `${date.getFullYear().toString().slice(-2)}${(date.getMonth() + 1).toString().padStart(2, '0')}${date
      .getDate()
      .toString()
      .padStart(2, '0')}_${date.getHours().toString().padStart(2, '0')}${date
      .getMinutes()
      .toString()
      .padStart(2, '0')}${date.getSeconds().toString().padStart(2, '0')}`
  }

  const downloadFile = (contents: string, name: string, extension: string = 'txt') => {
    const element = document.createElement('a')
    const file = new Blob([contents], { type: 'text/plain' })
    element.href = URL.createObjectURL(file)
    element.download = `${name}_${getPaddedTimestamp()}.${extension}`
    document.body.appendChild(element) // Required for this to work in FireFox
    element.click()
  }

  useEffect(() => {
    MessageMonitorApi.getIntersections({ token: token }).then((intersections) => {
      const localRoadRegulatorIntersectionIds: { [roadRegulatorId: number | string]: number[] } = {}
      for (const intersection of intersections) {
        if (!localRoadRegulatorIntersectionIds[intersection.roadRegulatorID]) {
          localRoadRegulatorIntersectionIds[intersection.roadRegulatorID] = []
        }
        localRoadRegulatorIntersectionIds[intersection.roadRegulatorID].push(intersection.intersectionID)
      }
      dispatch(setRoadRegulatorIntersectionIds(localRoadRegulatorIntersectionIds))
    })
  }, [token])

  const query = async ({
    type,
    intersectionId,
    roadRegulatorId,
    startDate,
    endTime,
    eventTypes,
    assessmentTypes,
    bsmVehicleId,
  }) => {
    dispatch(setType(type))
    dispatch(setGraphData([]))
    switch (type) {
      case 'events':
        const events: MessageMonitor.Event[] = []
        // iterate through each event type in a for loop and add the events to events array
        const eventPromises: Promise<MessageMonitor.Event[]>[] = []
        for (let i = 0; i < eventTypes.length; i++) {
          const eventType = eventTypes[i]
          const promise = EventsApi.getEvents(token, eventType, intersectionId, roadRegulatorId, startDate, endTime)
          eventPromises.push(promise)
        }
        const allEventsPromise = Promise.all(eventPromises)
        toast.promise(allEventsPromise, {
          loading: `Loading event data`,
          success: `Successfully got event data`,
          error: `Failed to get event data. Please see console`,
        })

        try {
          const allEvents = await allEventsPromise
          allEvents.forEach((event) => {
            events.push(...event)
          })
        } catch (e) {
          console.error(`Failed to load event data because ${e}`)
        }

        events.sort((a, b) => a.eventGeneratedAt - b.eventGeneratedAt)
        dispatch(setEvents(events))
        return events
      case 'assessments':
        const assessments: Assessment[] = []
        const assessmentPromises: Promise<Assessment[]>[] = []
        // iterate through each event type in a for loop and add the events to events array
        for (let i = 0; i < assessmentTypes.length; i++) {
          const assessmentType = assessmentTypes[i]
          const promise = AssessmentsApi.getAssessments(
            token,
            assessmentType,
            intersectionId,
            roadRegulatorId,
            startDate,
            endTime
          )
          assessmentPromises.push(promise)
        }

        const allAssessmentsPromise = Promise.all(assessmentPromises)
        toast.promise(allAssessmentsPromise, {
          loading: `Loading assessment data`,
          success: `Successfully got assessment data`,
          error: `Failed to get assessment data`,
        })

        try {
          const allAssessments = await allAssessmentsPromise
          allAssessments.forEach((assessment) => {
            assessments.push(...assessment)
          })
        } catch (e) {
          console.error(`Failed to load assessment data because ${e}`)
        }
        assessments.sort((a, b) => a.assessmentGeneratedAt - b.assessmentGeneratedAt)
        dispatch(setAssessments(assessments))
        return assessments
    }
    return
  }

  const onVisualize = async ({
    intersectionId,
    roadRegulatorId,
    startDate,
    endTime,
    eventTypes,
  }: {
    intersectionId: number
    roadRegulatorId: number
    startDate: Date
    endTime: Date
    eventTypes: string[]
  }) => {
    dispatch(
      setGraphData(
        await GraphsApi.getGraphData({
          token: token,
          intersectionId: intersectionId,
          roadRegulatorId: roadRegulatorId,
          startTime: startDate,
          endTime: endTime,
          event_types: eventTypes.filter((e) => valid_counts_event_types.includes(e)),
        })
      )
    )
    dispatch(setType(undefined))
    dispatch(setEvents([]))
    dispatch(setAssessments([]))
  }

  function sanitizeCsvString(term) {
    try {
      if (term instanceof Object || term instanceof Array) {
        return `"${JSON.stringify(term).replaceAll('"', '""')}"`
      }
      if (term.match && term.match(/,|"/)) {
        return `"${term.replaceAll('"', '""')}"`
      } else {
        return term
      }
    } catch (e) {
      console.error(e)
      return term
    }
  }

  const convertToCsv = (data: any[]) => {
    const csvRows: string[] = []
    const headers = Object.keys(data[0])
    csvRows.push(headers.join(','))

    for (const row of data) {
      const values = headers.map((header) => row[header]?.toString() ?? 'undefined')
      csvRows.push(values.join(','))
    }
    return csvRows.join('\n')
  }

  const downloadEventCsvFiles = (data: MessageMonitor.Event[]) => {
    const csvRows: { [id: string]: string[] } = {}
    for (const event of data) {
      if (!csvRows[event.eventType]) {
        csvRows[event.eventType] = [Object.keys(event).join(',')]
      }
      csvRows[event.eventType].push(Object.values(event).map(sanitizeCsvString).join(','))
    }

    var zip = new JSZip()
    for (const eventType in csvRows) {
      zip.file(`cimms_events_${eventType}_export.csv`, csvRows[eventType].join('\n'))
    }
    zip.generateAsync({ type: 'blob' }).then(function (content) {
      FileSaver.saveAs(content, `cimms_events_export.zip`)
    })
  }

  const downloadAssessmentCsvFiles = (data: Assessment[]) => {
    const csvRows: { [id: string]: string[] } = {}
    for (const event of data) {
      if (!csvRows[event.assessmentType]) {
        csvRows[event.assessmentType] = [Object.keys(event).join(',')]
      }
      csvRows[event.assessmentType].push(Object.values(event).map(sanitizeCsvString).join(','))
    }

    var zip = new JSZip()
    for (const assessmentType in csvRows) {
      zip.file(`cimms_assessments_${assessmentType}_export.csv`, csvRows[assessmentType].join('\n'))
    }
    zip.generateAsync({ type: 'blob' }).then(function (content) {
      FileSaver.saveAs(content, `cimms_assessments_export.zip`)
    })
  }

  return (
    <>
      <Box
        component="main"
        sx={{
          backgroundColor: 'background.default',
          flexGrow: 1,
          py: 8,
        }}
      >
        <Container maxWidth={false}>
          <Box
            sx={{
              alignItems: 'center',
              display: 'flex',
              overflow: 'hidden',
            }}
          >
            <div>
              <Typography noWrap variant="h4" color="text.secondary">
                Query
              </Typography>
            </div>
          </Box>
          <Box mt={3}>
            <DataSelectorEditForm
              onQuery={query}
              onVisualize={onVisualize}
              roadRegulatorIntersectionIds={roadRegulatorIntersectionIds}
              dbIntersectionId={intersectionId}
            />
          </Box>
        </Container>
        <Container sx={{ mt: 5, alignItems: 'center', display: 'flex' }}>
          {type == 'events' && (
            <EventDataTable
              events={events}
              onDownload={() => downloadEventCsvFiles(events)}
              onDownloadJson={() =>
                downloadFile(events.map((e) => JSON.stringify(e)).join('\n'), 'cimms_events_export')
              }
            />
          )}
          {type == 'assessments' && (
            <AssessmentDataTable
              assessments={assessments}
              onDownload={() => downloadAssessmentCsvFiles(assessments)}
              onDownloadJson={() =>
                downloadFile(assessments.map((e) => JSON.stringify(e)).join('\n'), 'cimms_assessments_export')
              }
            />
          )}
          {graphData.length > 0 && (
            <DataVisualizer
              data={graphData}
              onDownload={() => {
                return downloadFile(convertToCsv(graphData), 'cimms_graph_data_export', 'csv')
              }}
            />
          )}
        </Container>
      </Box>
      <MapDialog
        open={openMapDialog}
        onClose={() => {
          dispatch(setOpenMapDialog(false))
        }}
      />
    </>
  )
}

export default DataSelectorPage
