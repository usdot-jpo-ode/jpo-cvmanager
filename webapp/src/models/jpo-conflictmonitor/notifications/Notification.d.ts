declare namespace MessageMonitor {
  type Notification = {
    logger: any
    id: string
    key: string
    notificationGeneratedAt: number
    notificationType: string
    notificationText: string
    notificationHeading: string
    intersectionID: number
    roadRegulatorID: number
    notificationExpiresAt: Date
    getUniqueId: string
  }
}
