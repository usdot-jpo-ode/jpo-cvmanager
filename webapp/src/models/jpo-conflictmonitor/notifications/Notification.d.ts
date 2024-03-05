declare namespace MessageMonitor {
type Notification = {
  logger: Logger
  id: str
  key: str
  notificationGeneratedAt: number
  notificationType: str
  notificationText: str
  notificationHeading: str
  intersectionID: number
  roadRegulatorID: number
  notificationExpiresAt: Date
  getUniqueId: str
}
}