import { authApiHelper } from './api-helper-cviz'

class GraphsApi {
  createGraphDataVal = (id: number, event_type: string, count: number): GraphArrayDataType => {
    const val = {
      id,
      ConnectionOfTravelEventCount: 0,
      IntersectionReferenceAlignmentEventCount: 0,
      LaneDirectionOfTravelEventCount: 0,
      ProcessingTimePeriodCount: 0,
      SignalGroupAlignmentEventCount: 0,
      SignalStateConflictEventCount: 0,
      SignalStateEventCount: 0,
      SignalStateStopEventCount: 0,
      TimeChangeDetailsEventCount: 0,
      MapMinimumDataEventCount: 0,
      SpatMinimumDataEventCount: 0,
      MapBroadcastRateEventCount: 0,
      SpatBroadcastRateEventCount: 0,
    }
    switch (event_type) {
      case 'connection_of_travel':
        val.ConnectionOfTravelEventCount = count
        break
      case 'intersection_reference_alignment':
        val.IntersectionReferenceAlignmentEventCount = count
        break
      case 'lane_direction_of_travel':
        val.LaneDirectionOfTravelEventCount = count
        break
      case 'processing_time_period':
        val.ProcessingTimePeriodCount = count
        break
      case 'signal_group_alignment':
        val.SignalGroupAlignmentEventCount = count
        break
      case 'signal_state_conflict':
        val.SignalStateConflictEventCount = count
        break
      case 'signal_state':
        val.SignalStateEventCount = count
        break
      case 'signal_state_stop':
        val.SignalStateStopEventCount = count
        break
      case 'time_change_details':
        val.TimeChangeDetailsEventCount = count
        break
      case 'map_minimum_data':
        val.MapMinimumDataEventCount = count
        break
      case 'spat_minimum_data':
        val.SpatMinimumDataEventCount = count
        break
      case 'map_broadcast_rate':
        val.MapBroadcastRateEventCount = count
        break
      case 'spat_broadcast_rate':
        val.SpatBroadcastRateEventCount = count
        break
    }
    return val
  }

  combineGraphDataVals = (val1: GraphArrayDataType, val2: GraphArrayDataType): GraphArrayDataType => ({
    id: val1.id,
    ConnectionOfTravelEventCount: val1.ConnectionOfTravelEventCount + val2.ConnectionOfTravelEventCount,
    IntersectionReferenceAlignmentEventCount:
      val1.IntersectionReferenceAlignmentEventCount + val2.IntersectionReferenceAlignmentEventCount,
    LaneDirectionOfTravelEventCount: val1.LaneDirectionOfTravelEventCount + val2.LaneDirectionOfTravelEventCount,
    ProcessingTimePeriodCount: val1.ProcessingTimePeriodCount + val2.ProcessingTimePeriodCount,
    SignalGroupAlignmentEventCount: val1.SignalGroupAlignmentEventCount + val2.SignalGroupAlignmentEventCount,
    SignalStateConflictEventCount: val1.SignalStateConflictEventCount + val2.SignalStateConflictEventCount,
    SignalStateEventCount: val1.SignalStateEventCount + val2.SignalStateEventCount,
    SignalStateStopEventCount: val1.SignalStateStopEventCount + val2.SignalStateStopEventCount,
    TimeChangeDetailsEventCount: val1.TimeChangeDetailsEventCount + val2.TimeChangeDetailsEventCount,
    MapMinimumDataEventCount: val1.MapMinimumDataEventCount + val2.MapMinimumDataEventCount,
    SpatMinimumDataEventCount: val1.SpatMinimumDataEventCount + val2.SpatMinimumDataEventCount,
    MapBroadcastRateEventCount: val1.MapBroadcastRateEventCount + val2.MapBroadcastRateEventCount,
    SpatBroadcastRateEventCount: val1.SpatBroadcastRateEventCount + val2.SpatBroadcastRateEventCount,
  })

  async getGraphData({
    token,
    intersectionId,
    roadRegulatorId,
    event_types,
    startTime,
    endTime,
  }: {
    token: string
    intersectionId: number
    roadRegulatorId: number
    event_types: string[]
    startTime: Date
    endTime: Date
  }): Promise<Array<GraphArrayDataType>> {
    const queryParams: Record<string, string> = {}
    queryParams['intersection_id'] = intersectionId.toString()
    queryParams['road_regulator_id'] = roadRegulatorId.toString()
    queryParams['start_time_utc_millis'] = startTime.getTime().toString()
    queryParams['end_time_utc_millis'] = endTime.getTime().toString()

    const results: { [id: string]: GraphArrayDataType } = {}

    for (const event_type of event_types) {
      try {
        const graphData: Array<{ id: string; count: number }> = await authApiHelper.invokeApi({
          path: `/events/${event_type}/daily_counts`,
          token: token,
          queryParams,
          failureMessage: 'Failed to generate graph data',
          tag: 'intersection',
        })
        graphData?.forEach((data) => {
          const val = this.createGraphDataVal(new Date(data.id).getTime(), event_type, data.count)
          if (results[data.id] === undefined) {
            results[data.id] = val
          } else {
            results[data.id] = this.combineGraphDataVals(results[data.id], val)
          }
        })
      } catch (e) {
        console.error('Failed to generate graph data for event type ' + event_type, e)
      }
    }
    return Object.values(results)
  }
}

export default new GraphsApi()
