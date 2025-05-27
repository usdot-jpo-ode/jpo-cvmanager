import toast from 'react-hot-toast'
import { authApiHelper } from './api-helper-cviz'

const NOTIFICATION_TYPES: string[] = [
  'connection_of_travel',
  'intersection_reference_alignment',
  'lane_direction_of_travel',
  'signal_state_conflict_notification',
  'signal_group_alignment_notification',
  'map_broadcast_rate_notification',
  'spat_broadcast_rate_notification',
]

class NotificationApi {
  async getActiveNotifications({
    token,
    intersectionId,
    startTime,
    endTime,
    key,
    abortController,
  }: {
    token: string
    intersectionId: number
    startTime?: Date
    endTime?: Date
    key?: string
    abortController?: AbortController
  }): Promise<MessageMonitor.Notification[]> {
    const queryParams: Record<string, string> = {}
    queryParams['intersection_id'] = intersectionId.toString()
    if (startTime) queryParams['start_time_utc_millis'] = startTime.getTime().toString()
    if (endTime) queryParams['end_time_utc_millis'] = endTime.getTime().toString()
    if (key) queryParams['key'] = key

    const notifications = await authApiHelper.invokeApi({
      path: `/intersections/active-notifications`,
      token: token,
      queryParams,
      abortController,
      failureMessage: 'Failed to retrieve active notifications',
      tag: 'intersection',
    })

    return notifications?.content ?? []
  }

  async dismissNotifications({
    token,
    ids,
    abortController,
  }: {
    token: string
    ids: string[]
    abortController?: AbortController
  }): Promise<boolean> {
    let success = true
    for (const id of ids) {
      success =
        success &&
        (await authApiHelper.invokeApi({
          path: `/intersections/active-notifications`,
          method: 'DELETE',
          abortController,
          token: token,
          body: id.toString(),
          booleanResponse: true,
          tag: 'intersection',
        }))
    }
    if (success) {
      toast.success(`Successfully Dismissed ${ids.length} Notifications`)
    } else {
      toast.error(`Failed to Dismiss some Notifications`)
    }
    return true
  }

  async getAllNotifications({
    token,
    intersectionId,
    startTime,
    endTime,
    abortController,
  }: {
    token: string
    intersectionId: number
    startTime?: Date
    endTime?: Date
    abortController?: AbortController
  }): Promise<MessageMonitor.Notification[]> {
    const queryParams: Record<string, string> = {}
    queryParams['intersection_id'] = intersectionId.toString()
    if (startTime) queryParams['start_time_utc_millis'] = startTime.getTime().toString()
    if (endTime) queryParams['end_time_utc_millis'] = endTime.getTime().toString()

    const notifications: MessageMonitor.Notification[] = []
    for (const notificationType of NOTIFICATION_TYPES) {
      const resp: MessageMonitor.Notification[] =
        (
          await authApiHelper.invokeApi({
            path: `/data/cm-notifications/${notificationType}`,
            token: token,
            abortController,
            queryParams,
            failureMessage: `Failed to retrieve notifications of type ${notificationType}`,
            tag: 'intersection',
          })
        )?.content ?? []
      notifications.push(...resp)
    }

    return notifications
  }
}

export default new NotificationApi()
