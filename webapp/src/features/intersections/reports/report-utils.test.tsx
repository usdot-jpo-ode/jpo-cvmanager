import {
  generateDateRange,
  moveMidnightToPreviousDay,
  processMissingElements,
  formatAxisTickNumber,
} from './report-utils'

describe('moveMidnightToPreviousDay', () => {
  it('should return the previous day if the date is at midnight UTC', () => {
    const date = new Date('2025-03-01T00:00:00.000Z') // Midnight UTC
    const result = moveMidnightToPreviousDay(date)
    expect(result.toISOString()).toBe('2025-02-28T00:00:00.000Z') // Previous day
  })

  it('should return the same date if the time is not midnight UTC', () => {
    const date = new Date('2025-03-01T12:00:00.000Z') // Noon UTC
    const result = moveMidnightToPreviousDay(date)
    expect(result.toISOString()).toBe('2025-03-01T12:00:00.000Z') // Same date
  })

  it('should handle dates near the start of the year correctly', () => {
    const date = new Date('2025-01-01T00:00:00.000Z') // Midnight UTC on New Year's Day
    const result = moveMidnightToPreviousDay(date)
    expect(result.toISOString()).toBe('2024-12-31T00:00:00.000Z') // Previous year
  })

  it('should handle dates near the end of the month correctly', () => {
    const date = new Date('2025-03-31T00:00:00.000Z') // Midnight UTC on the last day of March
    const result = moveMidnightToPreviousDay(date)
    expect(result.toISOString()).toBe('2025-03-30T00:00:00.000Z') // Previous day
  })

  it('should handle leap years correctly', () => {
    const date = new Date('2024-03-01T00:00:00.000Z') // Midnight UTC in a leap year
    const result = moveMidnightToPreviousDay(date)
    expect(result.toISOString()).toBe('2024-02-29T00:00:00.000Z') // Leap day
  })

  it('should handle non-leap years correctly', () => {
    const date = new Date('2025-03-01T00:00:00.000Z') // Midnight UTC in a non-leap year
    const result = moveMidnightToPreviousDay(date)
    expect(result.toISOString()).toBe('2025-02-28T00:00:00.000Z') // Previous day
  })
})

describe('generateDateRange', () => {
  it('should generate a range of dates between start and end dates', () => {
    const startDate = new Date('2025-03-01T01:00:00.000Z')
    const endDate = new Date('2025-03-05T23:00:00.000Z')
    const expected = ['2025-03-01', '2025-03-02', '2025-03-03', '2025-03-04', '2025-03-05']

    expect(generateDateRange(startDate, endDate)).toEqual(expected)
  })

  it('should exclude end date if it is exactly midnight', () => {
    // End date at midnight
    const startDate = new Date('2025-03-01T01:00:00.000Z')
    const endDate = new Date('2025-03-05T00:00:00.000Z')
    const expected = ['2025-03-01', '2025-03-02', '2025-03-03', '2025-03-04']

    expect(generateDateRange(startDate, endDate)).toEqual(expected)
  })

  it('should include end date if it is not exactly midnight', () => {
    const startDate = new Date('2025-03-01T01:00:00.000Z')
    const endDate = new Date('2025-03-05T12:30:45.000Z')
    const expected = ['2025-03-01', '2025-03-02', '2025-03-03', '2025-03-04', '2025-03-05']

    expect(generateDateRange(startDate, endDate)).toEqual(expected)
  })

  it('should return a single date when start and end are the same day', () => {
    const startDate = new Date('2025-03-01T01:00:00.000Z')
    const endDate = new Date('2025-03-01T23:00:00.000Z')
    const expected = ['2025-03-01']

    expect(generateDateRange(startDate, endDate)).toEqual(expected)
  })

  it('should return the previous day when same day is specified and both are midnight', () => {
    const sameDate = new Date('2025-03-01T00:00:00.000Z')
    const expected = ['2025-02-28']

    expect(generateDateRange(sameDate, sameDate)).toEqual(expected)
  })
})

describe('processMissingElements', () => {
  it('should process simple element paths', () => {
    const elements = ['header.timestamp', 'message.id']
    const expected = ['timestamp missing from header', 'id missing from message']

    expect(processMissingElements(elements)).toEqual(expected)
  })

  it('should remove $.prefix from element paths', () => {
    const elements = ['$.header.timestamp', '$.message.id']
    const expected = ['timestamp missing from header', 'id missing from message']

    expect(processMissingElements(elements)).toEqual(expected)
  })

  it('should remove payload.data prefix from element paths', () => {
    const elements = ['payload.data.header.timestamp', 'payload.data.message.id']
    const expected = ['timestamp missing from header', 'id missing from message']

    expect(processMissingElements(elements)).toEqual(expected)
  })

  it('should remove text after colons in element paths', () => {
    const elements = ['header.timestamp: missing value', 'message.id: null']
    const expected = ['timestamp missing from header', 'id missing from message']

    expect(processMissingElements(elements)).toEqual(expected)
  })

  it('should replace brackets appropriately in element paths', () => {
    const elements = ['messages[0].content', 'data[1].value']
    const expected = ['content missing from messages 0', 'value missing from data 1']

    expect(processMissingElements(elements)).toEqual(expected)
  })

  it('should filter out elements containing "metadata"', () => {
    const elements = ['header.timestamp', 'metadata.created', 'message.id', 'message.metadata.updated']
    const expected = ['timestamp missing from header', 'id missing from message']

    expect(processMissingElements(elements)).toEqual(expected)
  })

  it('should group elements by parent path', () => {
    const elements = ['message.id', 'message.content', 'message.timestamp']
    const expected = ['id, content, and timestamp missing from message']

    expect(processMissingElements(elements)).toEqual(expected)
  })

  it('should handle two missing elements correctly', () => {
    const elements = ['message.id', 'message.content']
    const expected = ['id and content missing from message']

    expect(processMissingElements(elements)).toEqual(expected)
  })

  it('should handle nested paths correctly', () => {
    const elements = ['header.meta.timestamp', 'header.meta.version']
    const expected = ['timestamp and version missing from meta, in header']

    expect(processMissingElements(elements)).toEqual(expected)
  })

  it('should handle multiple groupings', () => {
    const elements = ['header.timestamp', 'header.version', 'message.id', 'message.content']
    const expected = ['timestamp and version missing from header', 'id and content missing from message']

    expect(processMissingElements(elements)).toEqual(expected)
  })

  it('should return empty array for empty input', () => {
    expect(processMissingElements([])).toEqual([])
  })
})

describe('formatAxisTickNumber', () => {
  it('should format numbers less than 1000 as is', () => {
    expect(formatAxisTickNumber(0)).toBe('0')
    expect(formatAxisTickNumber(1)).toBe('1')
    expect(formatAxisTickNumber(999)).toBe('999')
  })

  it('should format thousands with K suffix', () => {
    expect(formatAxisTickNumber(1000)).toBe('1K')
    expect(formatAxisTickNumber(5000)).toBe('5K')
    expect(formatAxisTickNumber(999000)).toBe('999K')
  })

  it('should format millions with M suffix', () => {
    expect(formatAxisTickNumber(1000000)).toBe('1M')
    expect(formatAxisTickNumber(5000000)).toBe('5M')
    expect(formatAxisTickNumber(999000000)).toBe('999M')
  })

  it('should add decimal precision for non-round thousands', () => {
    expect(formatAxisTickNumber(1500)).toBe('1.5K')
    expect(formatAxisTickNumber(2750)).toBe('2.8K')
  })

  it('should add decimal precision for non-round millions', () => {
    expect(formatAxisTickNumber(1500000)).toBe('1.5M')
    expect(formatAxisTickNumber(2750000)).toBe('2.8M')
  })

  it('should handle negative numbers', () => {
    expect(formatAxisTickNumber(-1000)).toBe('-1K')
    expect(formatAxisTickNumber(-1500)).toBe('-1.5K')
    expect(formatAxisTickNumber(-1000000)).toBe('-1M')
  })
})
