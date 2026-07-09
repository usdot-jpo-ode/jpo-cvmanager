import {
  filterSsms,
  filterSrms,
  getTimeWindowFromQueryParams,
  getTimeWindowFromRenderInterval,
} from './intersectionMapApiSlice'
import { ProcessedSsm, ProcessedSrmFeature } from '../../models/intersections/processed_data'

describe('intersectionMapApiSlice utility functions', () => {
  describe('getTimeWindowFromQueryParams', () => {
    it('should convert query params to time window', () => {
      const queryParams = {
        startDate: new Date('2024-01-01T00:00:00Z'),
        endDate: new Date('2024-01-01T01:00:00Z'),
      } as any

      const result = getTimeWindowFromQueryParams(queryParams)

      expect(result).toEqual({
        startMillis: new Date('2024-01-01T00:00:00Z').getTime(),
        endMillis: new Date('2024-01-01T01:00:00Z').getTime(),
      })
    })
  })

  describe('getTimeWindowFromRenderInterval', () => {
    it('should convert render interval to time window', () => {
      const renderInterval = [1000, 2000] // seconds

      const result = getTimeWindowFromRenderInterval(renderInterval)

      expect(result).toEqual({
        startMillis: 1000000,
        endMillis: 2000000,
      })
    })
  })

  describe('filterSsms', () => {
    const createMockSsm = (timestampMillis: number): ProcessedSsm =>
      ({
        timeStampEpochMillis: timestampMillis,
        // Add other required fields as needed
      } as ProcessedSsm)

    it('should filter SSMs within time window', () => {
      const ssms = [
        createMockSsm(1000),
        createMockSsm(2000),
        createMockSsm(3000),
        createMockSsm(4000),
        createMockSsm(5000),
      ]

      const timeWindow = { startMillis: 2000, endMillis: 4000 }
      const result = filterSsms(ssms, timeWindow)

      expect(result).toHaveLength(3)
      expect(result[0].timeStampEpochMillis).toBe(2000)
      expect(result[2].timeStampEpochMillis).toBe(4000)
    })

    it('should return empty array when no SSMs in time window', () => {
      const ssms = [createMockSsm(1000), createMockSsm(2000)]

      const timeWindow = { startMillis: 3000, endMillis: 4000 }
      const result = filterSsms(ssms, timeWindow)

      expect(result).toHaveLength(0)
    })

    it('should handle empty SSM array', () => {
      const ssms: ProcessedSsm[] = []
      const timeWindow = { startMillis: 1000, endMillis: 2000 }
      const result = filterSsms(ssms, timeWindow)

      expect(result).toHaveLength(0)
    })

    it('should include SSMs at exact start time', () => {
      const ssms = [createMockSsm(1000), createMockSsm(2000), createMockSsm(3000)]

      const timeWindow = { startMillis: 2000, endMillis: 3000 }
      const result = filterSsms(ssms, timeWindow)

      expect(result).toHaveLength(2)
      expect(result[0].timeStampEpochMillis).toBe(2000)
      expect(result[1].timeStampEpochMillis).toBe(3000)
    })

    it('should exclude SSMs at exact end time', () => {
      const ssms = [createMockSsm(1000), createMockSsm(2000), createMockSsm(3000)]

      const timeWindow = { startMillis: 1000, endMillis: 2000 }
      const result = filterSsms(ssms, timeWindow)

      expect(result).toHaveLength(2)
      expect(result[0].timeStampEpochMillis).toBe(1000)
      expect(result[1].timeStampEpochMillis).toBe(2000)
    })

    it('should handle duplicate timestamps', () => {
      const ssms = [
        createMockSsm(1000),
        createMockSsm(2000),
        createMockSsm(2000),
        createMockSsm(2000),
        createMockSsm(3000),
      ]

      const timeWindow = { startMillis: 2000, endMillis: 2900 }
      const result = filterSsms(ssms, timeWindow)

      expect(result).toHaveLength(3)
      expect(result.every((ssm) => ssm.timeStampEpochMillis === 2000)).toBe(true)
    })

    it('should handle time window before all SSMs', () => {
      const ssms = [createMockSsm(3000), createMockSsm(4000), createMockSsm(5000)]

      const timeWindow = { startMillis: 1000, endMillis: 2000 }
      const result = filterSsms(ssms, timeWindow)

      expect(result).toHaveLength(0)
    })

    it('should handle time window after all SSMs', () => {
      const ssms = [createMockSsm(1000), createMockSsm(2000), createMockSsm(3000)]

      const timeWindow = { startMillis: 4000, endMillis: 5000 }
      const result = filterSsms(ssms, timeWindow)

      expect(result).toHaveLength(0)
    })

    it('should handle single SSM in time window', () => {
      const ssms = [createMockSsm(2000)]
      const timeWindow = { startMillis: 1000, endMillis: 3000 }
      const result = filterSsms(ssms, timeWindow)

      expect(result).toHaveLength(1)
      expect(result[0].timeStampEpochMillis).toBe(2000)
    })
  })

  describe('filterSrms', () => {
    const createMockSrm = (timestampMillis: number): ProcessedSrmFeature =>
      ({
        type: 'Feature',
        geometry: { type: 'Point', coordinates: [0, 0] },
        properties: {
          timeStampEpochMillis: timestampMillis,
        } as any,
      } as ProcessedSrmFeature)

    it('should filter SRMs within time window', () => {
      const srms = [
        createMockSrm(1000),
        createMockSrm(2000),
        createMockSrm(3000),
        createMockSrm(4000),
        createMockSrm(5000),
      ]

      const timeWindow = { startMillis: 2000, endMillis: 4000 }
      const result = filterSrms(srms, timeWindow)

      expect(result).toHaveLength(3)
      expect(result[0].properties.timeStampEpochMillis).toBe(2000)
      expect(result[2].properties.timeStampEpochMillis).toBe(4000)
    })

    it('should return empty array when no SRMs in time window', () => {
      const srms = [createMockSrm(1000), createMockSrm(2000)]

      const timeWindow = { startMillis: 3000, endMillis: 4000 }
      const result = filterSrms(srms, timeWindow)

      expect(result).toHaveLength(0)
    })

    it('should handle empty SRM array', () => {
      const srms: ProcessedSrmFeature[] = []
      const timeWindow = { startMillis: 1000, endMillis: 2000 }
      const result = filterSrms(srms, timeWindow)

      expect(result).toHaveLength(0)
    })

    it('should include SRMs at exact start time', () => {
      const srms = [createMockSrm(1000), createMockSrm(2000), createMockSrm(3000)]

      const timeWindow = { startMillis: 2000, endMillis: 3000 }
      const result = filterSrms(srms, timeWindow)

      expect(result).toHaveLength(2)
      expect(result[0].properties.timeStampEpochMillis).toBe(2000)
      expect(result[1].properties.timeStampEpochMillis).toBe(3000)
    })

    it('should exclude SRMs at exact end time', () => {
      const srms = [createMockSrm(1000), createMockSrm(2000), createMockSrm(3000)]

      const timeWindow = { startMillis: 1000, endMillis: 2000 }
      const result = filterSrms(srms, timeWindow)

      expect(result).toHaveLength(2)
      expect(result[0].properties.timeStampEpochMillis).toBe(1000)
    })

    it('should handle duplicate timestamps', () => {
      const srms = [
        createMockSrm(1000),
        createMockSrm(2000),
        createMockSrm(2000),
        createMockSrm(2000),
        createMockSrm(3000),
      ]

      const timeWindow = { startMillis: 2000, endMillis: 2900 }
      const result = filterSrms(srms, timeWindow)

      expect(result).toHaveLength(3)
      expect(result.every((srm) => srm.properties.timeStampEpochMillis === 2000)).toBe(true)
    })

    it('should handle large dataset efficiently', () => {
      // Create 10000 SRMs
      const srms = Array.from({ length: 10000 }, (_, i) => createMockSrm(i * 1000))

      const timeWindow = { startMillis: 5000000, endMillis: 5010000 }
      const result = filterSrms(srms, timeWindow)

      expect(result).toHaveLength(11)
      expect(result[0].properties.timeStampEpochMillis).toBe(5000000)
      expect(result[10].properties.timeStampEpochMillis).toBe(5010000)
    })
  })

  describe('edge cases for binary search', () => {
    const createMockSsm = (timestampMillis: number): ProcessedSsm =>
      ({
        timeStampEpochMillis: timestampMillis,
      } as ProcessedSsm)

    it('should handle time window containing all SSMs', () => {
      const ssms = [createMockSsm(2000), createMockSsm(3000), createMockSsm(4000)]

      const timeWindow = { startMillis: 1000, endMillis: 5000 }
      const result = filterSsms(ssms, timeWindow)

      expect(result).toHaveLength(3)
    })

    it('should handle time window with same start and end', () => {
      const ssms = [createMockSsm(1000), createMockSsm(2000), createMockSsm(3000)]

      const timeWindow = { startMillis: 2000, endMillis: 2000 }
      const result = filterSsms(ssms, timeWindow)

      expect(result).toHaveLength(1)
    })

    it('should handle very large timestamps', () => {
      const ssms = [
        createMockSsm(Number.MAX_SAFE_INTEGER - 2),
        createMockSsm(Number.MAX_SAFE_INTEGER - 1),
        createMockSsm(Number.MAX_SAFE_INTEGER),
      ]

      const timeWindow = {
        startMillis: Number.MAX_SAFE_INTEGER - 1,
        endMillis: Number.MAX_SAFE_INTEGER,
      }
      const result = filterSsms(ssms, timeWindow)

      expect(result).toHaveLength(2)
      expect(result[0].timeStampEpochMillis).toBe(Number.MAX_SAFE_INTEGER - 1)
    })
  })
})
