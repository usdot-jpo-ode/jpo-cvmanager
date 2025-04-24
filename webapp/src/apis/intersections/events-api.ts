import { number } from 'prop-types'
import { authApiHelper } from './api-helper-cviz'

interface Item {
  label: string
  value: string
}

const EVENT_TYPES: Item[] = [
  { label: 'ConnectionOfTravelEvent', value: 'connection_of_travel' },
  { label: 'IntersectionReferenceAlignmentEvent', value: 'intersection_reference_alignment' },
  { label: 'LaneDirectionOfTravelEvent', value: 'lane_direction_of_travel' },
  { label: 'SignalGroupAlignmentEvent', value: 'signal_group_alignment' },
  { label: 'SignalStateConflictEvent', value: 'signal_state_conflict' },
  { label: 'StopLinePassageEvent', value: 'stop_line_passage' },
  { label: 'StopLineStopEvent', value: 'stop_line_stop' },
  { label: 'TimeChangeDetailsEvent', value: 'time_change_details' },
  { label: 'MapMinimumDataEvent', value: 'map_minimum_data' },
  { label: 'SpatMinimumDataEvent', value: 'spat_minimum_data' },
  { label: 'MapBroadcastRateEvent', value: 'map_broadcast_rate' },
  { label: 'SpatBroadcastRateEvent', value: 'spat_broadcast_rate' },
]

class EventsApi {
  async getEvents(
    token: string,
    eventType: string,
    intersectionId: number,
    roadRegulatorId: number,
    startTime: Date,
    endTime: Date,
    { latest = false, abortController }: { latest?: boolean; abortController?: AbortController } = {}
  ): Promise<MessageMonitor.Event[]> {
    const queryParams = {
      intersection_id: intersectionId.toString(),
      road_regulator_id: roadRegulatorId.toString(),
      start_time_utc_millis: startTime.getTime().toString(),
      end_time_utc_millis: endTime.getTime().toString(),
      latest: latest.toString(),
    }

    const response = await authApiHelper.invokeApi({
      path: `/events/${eventType}`,
      token: token,
      queryParams: queryParams,
      abortController,
      failureMessage: `Failed to retrieve events of type ${eventType}`,
      tag: 'intersection',
    })
    return response ?? ([] as MessageMonitor.Event[])
  }

  async getAllEvents(
    token: string,
    intersectionId: number,
    roadRegulatorId: number,
    startTime: Date,
    endTime: Date,
    abortController?: AbortController
  ): Promise<MessageMonitor.Event[]> {
    const queryParams = {
      intersection_id: intersectionId.toString(),
      road_regulator_id: roadRegulatorId.toString(),
      start_time_utc_millis: startTime.getTime().toString(),
      end_time_utc_millis: endTime.getTime().toString(),
    }

    const events: MessageMonitor.Event[] = []
    for (const eventTypeObj of EVENT_TYPES) {
      const response: MessageMonitor.Event[] =
        (await authApiHelper.invokeApi({
          path: `/events/${eventTypeObj.value}`,
          token: token,
          queryParams: queryParams,
          abortController,
          toastOnFailure: false,
          failureMessage: `Failed to retrieve events of type ${eventTypeObj.value}`,
          tag: 'intersection',
        })) ?? []
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

    const response = await authApiHelper.invokeApi({
      path: `/events/bsm_events_by_minute`,
      token: token,
      queryParams: queryParams,
      abortController,
      failureMessage: `Failed to retrieve bsm events by minute`,
      tag: 'intersection',
    })
    return response ?? ([] as MessageMonitor.MinuteCount[])
  }
}

export default new EventsApi()
