import { number } from 'prop-types'
import { authApiHelper } from './api-helper-cviz'

interface Item {
  label: string
  value: string
}

const EVENT_TYPES: Item[] = [
  { label: 'ConnectionOfTravelEvent', value: 'connection-of-travel' },
  { label: 'IntersectionReferenceAlignmentEvent', value: 'intersection-reference-alignment' },
  { label: 'LaneDirectionOfTravelEvent', value: 'lane-direction-of-travel' },
  { label: 'SignalGroupAlignmentEvent', value: 'signal-group-alignment' },
  { label: 'SignalStateConflictEvent', value: 'signal-state-conflict' },
  { label: 'StopLinePassageEvent', value: 'stop-line-passage' },
  { label: 'StopLineStopEvent', value: 'stop-line-stop' },
  { label: 'TimeChangeDetailsEvent', value: 'time-change-details' },
  { label: 'MapMinimumDataEvent', value: 'map-minimum-data' },
  { label: 'SpatMinimumDataEvent', value: 'spat-minimum-data' },
  { label: 'MapBroadcastRateEvent', value: 'map-broadcast-rate' },
  { label: 'SpatBroadcastRateEvent', value: 'spat-broadcast-rate' },
]

class EventsApi {
  async getEvents(
    token: string,
    eventType: string,
    intersectionId: number,
    startTime: Date,
    endTime: Date,
    { latest = false, abortController }: { latest?: boolean; abortController?: AbortController } = {}
  ): Promise<MessageMonitor.Event[]> {
    const queryParams = {
      intersection_id: intersectionId.toString(),
      start_time_utc_millis: startTime.getTime().toString(),
      end_time_utc_millis: endTime.getTime().toString(),
      latest: latest.toString(),
    }

    const response: PagedResponse<MessageMonitor.Event> = await authApiHelper.invokeApi({
      path: `/data/cm-events/${eventType}`,
      token: token,
      queryParams: queryParams,
      abortController,
      failureMessage: `Failed to retrieve events of type ${eventType}`,
      tag: 'intersection',
    })
    return response?.content ?? ([] as MessageMonitor.Event[])
  }

  async getAllEvents(
    token: string,
    intersectionId: number,
    startTime: Date,
    endTime: Date,
    abortController?: AbortController
  ): Promise<MessageMonitor.Event[]> {
    const queryParams = {
      intersection_id: intersectionId.toString(),
      start_time_utc_millis: startTime.getTime().toString(),
      end_time_utc_millis: endTime.getTime().toString(),
    }

    const events: MessageMonitor.Event[] = []
    for (const eventTypeObj of EVENT_TYPES) {
      const response: MessageMonitor.Event[] =
        (
          (await authApiHelper.invokeApi({
            path: `/data/cm-events/${eventTypeObj.value}`,
            token: token,
            queryParams: queryParams,
            abortController,
            toastOnFailure: false,
            failureMessage: `Failed to retrieve events of type ${eventTypeObj.value}`,
            tag: 'intersection',
          })) as PagedResponse<MessageMonitor.Event>
        )?.content ?? []
      events.push(...response)
    }
    return events
  }

  async getBsmByMinuteEvents({
    token,
    intersectionId,
    startTime,
    endTime,
    test = false,
    abortController,
  }: {
    token: string
    intersectionId: number
    startTime: Date
    endTime: Date
    test?: boolean
    abortController?: AbortController
  }): Promise<MessageMonitor.MinuteCount[]> {
    const queryParams = {
      intersection_id: intersectionId.toString(),
      start_time_utc_millis: startTime.getTime().toString(),
      end_time_utc_millis: endTime.getTime().toString(),
      test: test.toString(),
    }

    const response: PagedResponse<MessageMonitor.MinuteCount> = await authApiHelper.invokeApi({
      path: '/data/cm-events/bsm-events-by-minute',
      token: token,
      queryParams: queryParams,
      abortController,
      failureMessage: `Failed to retrieve bsm events by minute`,
      tag: 'intersection',
    })
    return response?.content ?? ([] as MessageMonitor.MinuteCount[])
  }
}

export default new EventsApi()
