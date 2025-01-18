import { eachDayOfInterval, format, subDays } from 'date-fns'

export type LaneDirectionOfTravelReportData = {
  timestamp: number
  laneID: number
  segmentID: number
  headingDelta: number
  medianCenterlineDistance: number
}

export type StopLineStopReportData = {
  signalGroup: number
  numberOfEvents: number
  timeStoppedOnRed: number
  timeStoppedOnYellow: number
  timeStoppedOnGreen: number
  timeStoppedOnDark: number
}

export type StopLinePassageReportData = {
  signalGroup: number
  totalEvents: number
  redEvents: number
  yellowEvents: number
  greenEvents: number
  darkEvents: number
}

export const extractLaneIds = (data: LaneDirectionOfTravelReportData[]): number[] => {
  const laneIds = new Set<number>()
  data.forEach((assessment) => {
    laneIds.add(assessment.laneID)
  })
  return Array.from(laneIds).sort((a, b) => a - b)
}

export const generateDateRange = (startDate: Date, endDate: Date): string[] => {
  // If endDate is exactly midnight, do not include it in the range
  const isMidnight =
    endDate.getHours() === 0 &&
    endDate.getMinutes() === 0 &&
    endDate.getSeconds() === 0 &&
    endDate.getMilliseconds() === 0
  const adjustedEndDate = isMidnight ? subDays(endDate, 1) : endDate

  const dates = eachDayOfInterval({ start: startDate, end: adjustedEndDate })
  return dates.map((date) => format(date, 'yyyy-MM-dd'))
}

export const processMissingElements = (elements: string[]): string[] => {
  // Step 1: Process each element to remove prefixes and format the string
  const processedElements = elements
    .map((element) => {
      // Remove "$." prefix if it exists
      if (element.startsWith('$.')) {
        element = element.substring(2)
      }

      // Remove "payload.data." prefix if it exists
      if (element.startsWith('payload.data.')) {
        element = element.substring('payload.data.'.length)
      }

      // Remove the first colon and anything that comes after it
      const colonIndex = element.indexOf(':')
      if (colonIndex !== -1) {
        element = element.substring(0, colonIndex)
      }

      // Replace '[' with ' ' and ']' with ''
      element = element.replace(/\[/g, ' ').replace(/\]/g, '')

      return element
    })
    .filter((element) => !element.includes('metadata')) // Remove elements containing "metadata"

  // Step 2: Group elements by the last part of the string
  const groupedElements: { [key: string]: string[] } = {}
  processedElements.forEach((element) => {
    const parts = element.split('.')
    const lastPart = parts.pop()
    const key = parts.join('.')
    if (!groupedElements[key]) {
      groupedElements[key] = []
    }
    if (lastPart) {
      groupedElements[key].push(lastPart)
    }
  })

  // Step 3: Create readable strings from the grouped elements
  const readableStrings = Object.entries(groupedElements).map(([key, values]) => {
    const uniqueValues = Array.from(new Set(values))
    let readableString: string
    if (uniqueValues.length > 2) {
      const lastValue = uniqueValues.pop()
      readableString = `${uniqueValues.join(', ')}, and ${lastValue} missing from ${key
        .split('.')
        .reverse()
        .join(', in ')}`
    } else {
      readableString = `${uniqueValues.join(' and ')} missing from ${key.split('.').reverse().join(', in ')}`
    }
    return readableString
  })

  return readableStrings
}

export const formatAxisTickNumber = (num: number) => {
  if (num >= 1000000) {
    return num % 1000000 === 0 ? `${num / 1000000}M` : `${(num / 1000000).toFixed(1)}M`
  } else if (num >= 1000) {
    return num % 1000 === 0 ? `${num / 1000}K` : `${(num / 1000).toFixed(1)}K`
  } else {
    return num.toString()
  }
}
