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
    roadRegulatorID: number // This field is being phased out, but is left in because this field is still supplied by the ConflictMonitor
    notificationExpiresAt: Date
    getUniqueId: string
  }
}
