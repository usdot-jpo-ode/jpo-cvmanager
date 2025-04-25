import { jsPDF } from 'jspdf'
import { toPng } from 'html-to-image'
import { generatePdf } from './pdf-generator'
import { ReportMetadata } from '../../../apis/intersections/reports-api'

// Create a helper function to record constructor calls
const mockJsPDFConstructor = jest.fn()

jest.mock('jspdf', () => {
  const addPageMock = jest.fn()
  const setFontSizeMock = jest.fn()
  const setFontMock = jest.fn()
  const textMock = jest.fn()
  const addImageMock = jest.fn()
  const saveMock = jest.fn()
  return {
    __esModule: true,
    jsPDF: class MockJsPDF {
      addPage = addPageMock
      setFontSize = setFontSizeMock
      setFont = setFontMock
      text = textMock
      addImage = addImageMock
      getImageProperties = jest.fn(() => ({ height: 100, width: 200 }))
      internal = {
        pageSize: {
          getHeight: jest.fn(() => 297), // mock A4 height in mm
          getWidth: jest.fn(() => 210), // mock A4 width in mm
        },
      }
      save = saveMock
      constructor(...args: any[]) {
        // Record the arguments with which the constructor is called
        mockJsPDFConstructor(...args)
      }
    },
  }
})

jest.mock('html-to-image', () => ({
  toPng: jest.fn(),
}))

describe('generatePdf', () => {
  let mockSetLoading: jest.Mock
  let mockSetProgress: jest.Mock
  let mockIsModalOpen: jest.Mock
  let mockSignal: AbortSignal
  let mockReport: ReportMetadata

  beforeEach(() => {
    // Initialize mock functions
    mockSetLoading = jest.fn()
    mockSetProgress = jest.fn()
    mockIsModalOpen = jest.fn().mockReturnValue(true)
    mockSignal = { aborted: false } as AbortSignal
    jest.spyOn(console, 'error').mockImplementation(() => {})

    mockReport = {
      reportName: 'Test Report',
      intersectionID: 123,
      reportGeneratedAt: new Date('2023-01-01T00:00:00Z'),
      reportStartTime: new Date('2023-01-01T00:00:00Z'),
      reportStopTime: new Date('2023-01-01T01:00:00Z'),
      reportContents: ['Lane Direction of Travel', 'Signal State Events'],
      laneDirectionOfTravelEventCounts: [
        { id: '1', count: 10 },
        { id: '2', count: 15 },
      ],
      laneDirectionOfTravelMedianDistanceDistribution: [
        { id: '1', count: 5 },
        { id: '2', count: 8 },
      ],
      laneDirectionOfTravelMedianHeadingDistribution: [
        { id: '1', count: 3 },
        { id: '2', count: 7 },
      ],
      laneDirectionOfTravelReportData: [
        { timestamp: 1743434775000, laneID: 1, segmentID: 1, headingDelta: 0.5, medianCenterlineDistance: 1.2 },
        { timestamp: 1743434776000, laneID: 2, segmentID: 2, headingDelta: 0.7, medianCenterlineDistance: 1.5 },
      ],
      connectionOfTravelEventCounts: [
        { id: '1', count: 20 },
        { id: '2', count: 25 },
      ],
      signalStateConflictEventCount: [{ id: '1', count: 5 }],
      signalStateEventCounts: [
        { id: '1', count: 30 },
        { id: '2', count: 35 },
      ],
      signalStateStopEventCounts: [
        { id: '1', count: 12 },
        { id: '2', count: 18 },
      ],
      timeChangeDetailsEventCount: [{ id: '1', count: 4 }],
      intersectionReferenceAlignmentEventCounts: [
        { id: '1', count: 6 },
        { id: '2', count: 9 },
      ],
      mapBroadcastRateEventCount: [{ id: '1', count: 40 }],
      mapMinimumDataEventCount: [{ id: '1', count: 50 }],
      spatMinimumDataEventCount: [{ id: '1', count: 60 }],
      spatBroadcastRateEventCount: [{ id: '1', count: 70 }],
      latestMapMinimumDataEventMissingElements: ['Element1', 'Element2'],
      latestSpatMinimumDataEventMissingElements: ['Element3', 'Element4'],
      validConnectionOfTravelData: [
        { connectionID: 1, ingressLaneID: 101, egressLaneID: 201, eventCount: 5 },
        { connectionID: 2, ingressLaneID: 102, egressLaneID: 202, eventCount: 10 },
      ],
      invalidConnectionOfTravelData: [
        { connectionID: 3, ingressLaneID: 103, egressLaneID: 203, eventCount: 2 },
        { connectionID: 4, ingressLaneID: 104, egressLaneID: 204, eventCount: 4 },
      ],
      headingTolerance: 5,
      distanceTolerance: 10,
      stopLineStopReportData: [
        {
          signalGroup: 1,
          numberOfEvents: 5,
          timeStoppedOnRed: 10,
          timeStoppedOnYellow: 15,
          timeStoppedOnGreen: 20,
          timeStoppedOnDark: 25,
        },
        {
          signalGroup: 2,
          numberOfEvents: 8,
          timeStoppedOnRed: 12,
          timeStoppedOnYellow: 18,
          timeStoppedOnGreen: 22,
          timeStoppedOnDark: 28,
        },
      ],
      stopLinePassageReportData: [
        {
          signalGroup: 1,
          totalEvents: 5,
          redEvents: 10,
          yellowEvents: 15,
          greenEvents: 20,
          darkEvents: 25,
        },
        {
          signalGroup: 2,
          totalEvents: 8,
          redEvents: 12,
          yellowEvents: 18,
          greenEvents: 22,
          darkEvents: 28,
        },
      ],
    }
    jest.spyOn(document, 'getElementById').mockImplementation((id) => {
      const mockElement = document.createElement('div')
      mockElement.id = id
      return mockElement
    })
  })
  afterEach(() => {
    jest.restoreAllMocks()
  })

  it('should initialize the PDF and set loading state', async () => {
    const pdf = new jsPDF()

    await generatePdf(mockReport, mockSetLoading, false, mockIsModalOpen, mockSetProgress, mockSignal)

    console.log(pdf.setFontSize)
    expect(pdf.internal.pageSize.getHeight()).toBe(297)
    expect(pdf.internal.pageSize.getWidth()).toBe(210)

    expect(mockSetLoading).toHaveBeenCalledWith(true)
    expect(mockJsPDFConstructor).toHaveBeenCalledWith('p', 'mm', 'a4')
    expect(pdf.setFontSize).toHaveBeenCalledWith(36)
    expect(pdf.text).toHaveBeenCalledWith(
      'Conflict Monitor Report',
      105, // Centered on A4 width (210mm)
      98.5, // Centered vertically
      { align: 'center' }
    )
    expect(mockSetLoading).toHaveBeenCalledWith(false)
  })

  it('should calculate unique lane IDs and total graphs', async () => {
    const pdf = new jsPDF()
    console.log('pdf-generator-test 220', pdf.getImageProperties, pdf.getImageProperties(''))
    await generatePdf(mockReport, mockSetLoading, true, mockIsModalOpen, mockSetProgress, mockSignal)

    expect(pdf.text).toHaveBeenCalledWith('Lane Direction of Travel', 105, 20, { align: 'center' })
    expect(mockSetProgress).toHaveBeenCalledWith(expect.any(Number))
  })

  it('should add lane-specific charts if includeLaneSpecificCharts is true', async () => {
    const pdf = new jsPDF()
    await generatePdf(mockReport, mockSetLoading, true, mockIsModalOpen, mockSetProgress, mockSignal)

    expect(pdf.text).toHaveBeenCalledWith('Distance From Centerline Over Time', 105, 25, { align: 'center' })
    expect(pdf.text).toHaveBeenCalledWith('Vehicle Heading Error Delta Over Time', 105, 25, { align: 'center' })
  })

  it('should skip lane-specific charts if includeLaneSpecificCharts is false', async () => {
    const pdf = new jsPDF()
    await generatePdf(mockReport, mockSetLoading, false, mockIsModalOpen, mockSetProgress, mockSignal)

    expect(pdf.text).not.toHaveBeenCalledWith('Distance From Centerline Over Time', 105, 25, { align: 'center' })
    expect(pdf.text).not.toHaveBeenCalledWith('Vehicle Heading Error Delta Over Time', 105, 25, { align: 'center' })
  })

  it('should save the PDF if the modal is still open', async () => {
    const pdf = new jsPDF()
    await generatePdf(mockReport, mockSetLoading, false, mockIsModalOpen, mockSetProgress, mockSignal)

    expect(mockIsModalOpen).toHaveBeenCalled()
    expect(pdf.save).toHaveBeenCalledWith('Test Report.pdf')
  })

  it('should not save the PDF if the modal is closed', async () => {
    mockIsModalOpen.mockReturnValue(false)
    const pdf = new jsPDF()
    await generatePdf(mockReport, mockSetLoading, false, mockIsModalOpen, mockSetProgress, mockSignal)

    expect(mockIsModalOpen).toHaveBeenCalled()
    expect(pdf.save).not.toHaveBeenCalled()
  })

  it('should handle errors during graph capture gracefully', async () => {
    ;(toPng as jest.Mock).mockRejectedValue(new Error('Graph capture failed'))
    const pdf = new jsPDF()

    await generatePdf(mockReport, mockSetLoading, false, mockIsModalOpen, mockSetProgress, mockSignal)

    expect(console.error).toHaveBeenCalledWith('Error capturing graph:', expect.any(Error))
  })

  it('should handle an aborted signal gracefully', async () => {
    const abortController = new AbortController()
    abortController.abort()
    mockSignal = abortController.signal
    const pdf = new jsPDF()

    await generatePdf(mockReport, mockSetLoading, false, mockIsModalOpen, mockSetProgress, mockSignal)

    expect(console.error).not.toHaveBeenCalled()
    expect(mockSetProgress).not.toHaveBeenCalled()
  })
})
