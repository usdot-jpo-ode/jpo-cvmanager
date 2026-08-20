type EmailSendResponse = {
  statusCode: number
  message: string
}

type EmailApiResponse = {
  responses: EmailSendResponse[]
  successCount: number
  failureCount: number
}

type SupportRequestEmailContents = {
  email: string
  subject: string
  message: string
}

type RsuErrorSummaryEmailContents = {
  subject: string
  message: string
}
