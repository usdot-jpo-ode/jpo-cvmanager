/// <reference path="Notification.d.ts" />
type TimeChangeDetailsNotification = MessageMonitor.Notification  & {
  event: TimeChangeDetailsEvent
}