/// <reference path="Notification.d.ts" />
type SignalStateConflictNotification = MessageMonitor.Notification  & {
  event: SignalStateConflictEvent
}