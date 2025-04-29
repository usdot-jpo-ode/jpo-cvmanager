type Assessment = {
  assessmentGeneratedAt: number
  assessmentType: string
  intersectionID: number
  roadRegulatorID: number // This field is being phased out, but is left in because this field is still supplied by the ConflictMonitor
  source: string
}
