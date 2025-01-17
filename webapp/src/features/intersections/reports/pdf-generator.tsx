import jsPDF from 'jspdf'
import { toPng } from 'html-to-image'
import { format } from 'date-fns'
import { ReportMetadata } from '../../../apis/intersections/reports-api'
import { processMissingElements } from './report-utils'

const setPdfSectionTitleFormatting = (pdf: jsPDF) => {
  pdf.setFontSize(24)
  pdf.setFont('helvetica', 'bold')
}

const setPdfDescriptionFormatting = (pdf: jsPDF) => {
  pdf.setFontSize(9)
  pdf.setFont('helvetica', 'italic')
}

const setPdfBodyFormatting = (pdf: jsPDF) => {
  pdf.setFontSize(9)
  pdf.setFont('helvetica', 'normal')
}

const setPdfItemTitleFormatting = (pdf: jsPDF) => {
  pdf.setFontSize(16)
  pdf.setFont('helvetica', 'bold')
}

const captureGraph = async (
  pdf: jsPDF,
  elementId: string,
  position: { x: number; y: number },
  setProgress: (progress: number) => void,
  totalGraphs: number,
  currentGraph: number,
  signal: AbortSignal
) => {
  const input = document.getElementById(elementId)
  if (input) {
    try {
      const imgData = await toPng(input, { quality: 1 })
      const imgProps = pdf.getImageProperties(imgData)
      const pdfWidth = pdf.internal.pageSize.getWidth()
      const imgWidth = pdfWidth - 30
      const imgHeight = (imgProps.height * imgWidth) / imgProps.width
      pdf.addImage(imgData, 'PNG', position.x + 15, position.y, imgWidth, imgHeight, undefined, 'FAST')
      setProgress((currentGraph / totalGraphs) * 100)
    } catch (error) {
      if (!signal.aborted) {
        console.error('Error capturing graph:', error)
      }
    }
  } else {
    console.error(`Element with id ${elementId} not found`)
  }
}

const addPageWithNumber = (pdf: jsPDF, currentPage: number) => {
  pdf.addPage()
  pdf.setFontSize(10)
  pdf.setFont('helvetica', 'normal')
  pdf.text(`Page ${currentPage}`, pdf.internal.pageSize.getWidth() - 20, pdf.internal.pageSize.getHeight() - 10)
  return currentPage + 1
}

const addDistanceFromCenterlineGraphs = async (
  pdf: jsPDF,
  laneIds: number[],
  pdfHeight: number,
  setProgress: (progress: number) => void,
  totalGraphs: number,
  currentGraph: number,
  signal: AbortSignal,
  currentPage: number
) => {
  if (laneIds.length === 0) {
    pdf.setFont('helvetica', 'normal')
    pdf.text('No Data', pdf.internal.pageSize.getWidth() / 2, pdfHeight / 2, { align: 'center' })
    return currentPage
  }
  for (let i = 0; i < laneIds.length; i++) {
    if (i % 2 === 0 && i !== 0) {
      currentPage = addPageWithNumber(pdf, currentPage)
    }
    const position = i % 2 === 0 ? { x: 0, y: 35 } : { x: 0, y: pdfHeight / 2 + 10 }
    await captureGraph(
      pdf,
      `distance-from-centerline-graph-${laneIds[i]}`,
      position,
      setProgress,
      totalGraphs,
      currentGraph + i + 1,
      signal
    )
  }
  return currentPage
}

const addHeadingErrorGraphs = async (
  pdf: jsPDF,
  laneIds: number[],
  pdfHeight: number,
  setProgress: (progress: number) => void,
  totalGraphs: number,
  currentGraph: number,
  signal: AbortSignal,
  currentPage: number
) => {
  if (laneIds.length === 0) {
    pdf.setFont('helvetica', 'normal')
    pdf.text('No Data', pdf.internal.pageSize.getWidth() / 2, pdfHeight / 2, { align: 'center' })
    return currentPage
  }
  for (let i = 0; i < laneIds.length; i++) {
    if (i % 2 === 0 && i !== 0) {
      currentPage = addPageWithNumber(pdf, currentPage)
    }
    const position = i % 2 === 0 ? { x: 0, y: 35 } : { x: 0, y: pdfHeight / 2 + 10 }
    await captureGraph(
      pdf,
      `heading-error-graph-${laneIds[i]}`,
      position,
      setProgress,
      totalGraphs,
      currentGraph + i + 1,
      signal
    )
  }
  return currentPage
}

export const generatePdf = async (
  report: ReportMetadata,
  setLoading: (loading: boolean) => void,
  includeLaneSpecificCharts: boolean,
  isModalOpen: () => boolean,
  setProgress: (progress: number) => void,
  signal: AbortSignal
) => {
  setLoading(true)
  const pdf = new jsPDF('p', 'mm', 'a4')
  const pdfHeight = pdf.internal.pageSize.getHeight()
  const pdfWidth = pdf.internal.pageSize.getWidth()

  // Extract unique lane IDs from laneDirectionOfTravelReportData
  const laneIds = Array.from(new Set(report.laneDirectionOfTravelReportData.map((item) => item.laneID)))
  const totalGraphs = 14 + (includeLaneSpecificCharts ? 2 * laneIds.length : 0)

  let currentGraph = 0
  let currentPage = 1

  pdf.setFontSize(36)
  pdf.text('Conflict Monitor Report', pdfWidth / 2, pdfHeight / 2 - 50, { align: 'center' })
  pdf.setFontSize(18)
  pdf.text(`Intersection ${report?.intersectionID}`, pdfWidth / 2, pdfHeight / 2 - 38, { align: 'center' })
  pdf.setFontSize(12)
  pdf.text(
    `${report?.reportStartTime ? format(new Date(report.reportStartTime), "yyyy-MM-dd' T'HH:mm:ss'Z'") : ''} - ${
      report?.reportStopTime ? format(new Date(report.reportStopTime), "yyyy-MM-dd' T'HH:mm:ss'Z'") : ''
    }`,
    pdfWidth / 2,
    pdfHeight / 2 - 30,
    { align: 'center' }
  )
  currentPage = addPageWithNumber(pdf, currentPage)

  setPdfSectionTitleFormatting(pdf)
  pdf.text('Lane Direction of Travel', pdf.internal.pageSize.getWidth() / 2, 20, { align: 'center' })
  await captureGraph(
    pdf,
    'lane-direction-of-travel-graph',
    { x: 0, y: 25 },
    setProgress,
    totalGraphs,
    ++currentGraph,
    signal
  )
  setPdfDescriptionFormatting(pdf)
  pdf.text(
    'The number of events triggered when vehicles passed a lane segment.',
    pdf.internal.pageSize.getWidth() / 2,
    pdfHeight / 2,
    { align: 'center' }
  )
  await captureGraph(
    pdf,
    'lane-direction-distance-graph',
    { x: 0, y: pdfHeight / 2 + 3 },
    setProgress,
    totalGraphs,
    ++currentGraph,
    signal
  )
  setPdfDescriptionFormatting(pdf)
  pdf.text(
    'The median deviation in distance between vehicles and the center of the lane as defined by the MAP.',
    pdf.internal.pageSize.getWidth() / 2,
    pdfHeight - 15,
    { align: 'center' }
  )
  currentPage = addPageWithNumber(pdf, currentPage)

  await captureGraph(
    pdf,
    'lane-direction-heading-graph',
    { x: 0, y: 25 },
    setProgress,
    totalGraphs,
    ++currentGraph,
    signal
  )
  setPdfDescriptionFormatting(pdf)
  pdf.text(
    'The median deviation in heading between vehicles and the lanes as defined by the MAP.',
    pdf.internal.pageSize.getWidth() / 2,
    pdfHeight / 2 + 10,
    { align: 'center' }
  )
  currentPage = addPageWithNumber(pdf, currentPage)

  if (includeLaneSpecificCharts) {
    // Add Distance From Centerline Graphs
    setPdfSectionTitleFormatting(pdf)
    pdf.text('Distance From Centerline Over Time', pdf.internal.pageSize.getWidth() / 2, 25, { align: 'center' })
    setPdfDescriptionFormatting(pdf)
    pdf.text(
      'The average of median distances between vehicles and the centerline of each lane as it changed over time.',
      pdf.internal.pageSize.getWidth() / 2,
      32,
      { align: 'center' }
    )
    currentPage = await addDistanceFromCenterlineGraphs(
      pdf,
      laneIds,
      pdfHeight,
      setProgress,
      totalGraphs,
      currentGraph,
      signal,
      currentPage
    )
    currentGraph += laneIds.length
    currentPage = addPageWithNumber(pdf, currentPage)

    // Add Heading Error Graphs
    setPdfSectionTitleFormatting(pdf)
    pdf.text('Vehicle Heading Error Delta Over Time', pdf.internal.pageSize.getWidth() / 2, 25, { align: 'center' })
    setPdfDescriptionFormatting(pdf)
    pdf.text(
      'The median deviation in heading between vehicles and the expected heading as defined by the MAP.',
      pdf.internal.pageSize.getWidth() / 2,
      32,
      { align: 'center' }
    )
    currentPage = await addHeadingErrorGraphs(
      pdf,
      laneIds,
      pdfHeight,
      setProgress,
      totalGraphs,
      currentGraph,
      signal,
      currentPage
    )
    currentGraph += laneIds.length
    currentPage = addPageWithNumber(pdf, currentPage)
  }

  setPdfSectionTitleFormatting(pdf)
  pdf.text('Connection of Travel', pdf.internal.pageSize.getWidth() / 2, 20, { align: 'center' })
  await captureGraph(
    pdf,
    'connection-of-travel-graph',
    { x: 0, y: 25 },
    setProgress,
    totalGraphs,
    ++currentGraph,
    signal
  )
  setPdfDescriptionFormatting(pdf)
  pdf.text(
    'The number of events triggered when a vehicle entered and exited the intersection.',
    pdf.internal.pageSize.getWidth() / 2,
    pdfHeight / 2,
    { align: 'center' }
  )
  currentPage = addPageWithNumber(pdf, currentPage)

  // Add Valid and Invalid Connection of Travel Graphs
  await captureGraph(
    pdf,
    'valid-connection-of-travel-graph',
    { x: 0, y: 25 },
    setProgress,
    totalGraphs,
    ++currentGraph,
    signal
  )
  setPdfDescriptionFormatting(pdf)
  pdf.text(
    'The number of vehicles that followed the defined ingress-egress lane pairings for each lane at the intersection.',
    pdf.internal.pageSize.getWidth() / 2,
    pdfHeight / 2,
    { align: 'center' }
  )
  await captureGraph(
    pdf,
    'invalid-connection-of-travel-graph',
    { x: 0, y: pdfHeight / 2 + 10 },
    setProgress,
    totalGraphs,
    ++currentGraph,
    signal
  )
  setPdfDescriptionFormatting(pdf)
  pdf.text(
    'The number of vehicles that did not follow the defined ingress-egress lane pairings for each lane at the intersection.',
    pdf.internal.pageSize.getWidth() / 2,
    pdfHeight - 15,
    { align: 'center' }
  )
  currentPage = addPageWithNumber(pdf, currentPage)

  setPdfSectionTitleFormatting(pdf)
  pdf.text('Signal State Events', pdf.internal.pageSize.getWidth() / 2, 20, { align: 'center' })

  await captureGraph(pdf, 'stop-line-stacked-graph', { x: 0, y: 25 }, setProgress, totalGraphs, ++currentGraph, signal)
  setPdfDescriptionFormatting(pdf)
  pdf.text(
    'A composite view comparing vehicles that stopped before passing through the intersection versus those that did not.',
    pdf.internal.pageSize.getWidth() / 2,
    pdfHeight / 2,
    { align: 'center' }
  )
  currentPage = addPageWithNumber(pdf, currentPage)

  await captureGraph(
    pdf,
    'signal-group-stop-line-graph',
    { x: 0, y: 25 },
    setProgress,
    totalGraphs,
    ++currentGraph,
    signal
  )
  setPdfDescriptionFormatting(pdf)
  pdf.text(
    'The percentage of time vehicles spent stopped at a light depending on the color of the light.',
    pdf.internal.pageSize.getWidth() / 2,
    pdfHeight / 2,
    { align: 'center' }
  )

  await captureGraph(
    pdf,
    'signal-group-passage-line-graph',
    { x: 0, y: pdfHeight / 2 + 10 },
    setProgress,
    totalGraphs,
    ++currentGraph,
    signal
  )
  setPdfDescriptionFormatting(pdf)
  pdf.text(
    'The percentage of vehicles that passed through a light depending on the color of the signal light.',
    pdf.internal.pageSize.getWidth() / 2,
    pdfHeight - 15,
    { align: 'center' }
  )
  currentPage = addPageWithNumber(pdf, currentPage)

  await captureGraph(
    pdf,
    'signal-state-conflict-graph',
    { x: 0, y: 25 },
    setProgress,
    totalGraphs,
    ++currentGraph,
    signal
  )
  setPdfDescriptionFormatting(pdf)
  pdf.text(
    'The number of times the system detected contradictory signal states, such as conflicting green lights.',
    pdf.internal.pageSize.getWidth() / 2,
    pdfHeight / 2,
    { align: 'center' }
  )
  pdf.text('Lower numbers indicate better performance.', pdf.internal.pageSize.getWidth() / 2, pdfHeight / 2 + 5, {
    align: 'center',
  })

  await captureGraph(
    pdf,
    'time-change-details-graph',
    { x: 0, y: pdfHeight / 2 + 5 },
    setProgress,
    totalGraphs,
    ++currentGraph,
    signal
  )
  setPdfDescriptionFormatting(pdf)
  pdf.text(
    'The number of times the system detected differences in timing between expected and actual signal state changes.',
    pdf.internal.pageSize.getWidth() / 2,
    pdfHeight - 20,
    { align: 'center' }
  )
  pdf.text('Lower numbers indicate better performance.', pdf.internal.pageSize.getWidth() / 2, pdfHeight - 15, {
    align: 'center',
  })
  currentPage = addPageWithNumber(pdf, currentPage)

  setPdfSectionTitleFormatting(pdf)
  pdf.text('Intersection Reference Alignments Per Day', pdf.internal.pageSize.getWidth() / 2, 20, { align: 'center' })
  await captureGraph(
    pdf,
    'intersection-reference-alignment-graph',
    { x: 0, y: 25 },
    setProgress,
    totalGraphs,
    ++currentGraph,
    signal
  )
  setPdfDescriptionFormatting(pdf)
  pdf.text(
    'The number of events flagging a mismatch between intersection ID and road regulator ID.',
    pdf.internal.pageSize.getWidth() / 2,
    pdfHeight / 2,
    { align: 'center' }
  )
  pdf.text('Lower numbers indicate better performance.', pdf.internal.pageSize.getWidth() / 2, pdfHeight / 2 + 5, {
    align: 'center',
  })
  currentPage = addPageWithNumber(pdf, currentPage)

  setPdfItemTitleFormatting(pdf)
  pdf.text('MAP', pdf.internal.pageSize.getWidth() / 2, 20, { align: 'center' })
  await captureGraph(pdf, 'map-broadcast-rate-graph', { x: 0, y: 25 }, setProgress, totalGraphs, ++currentGraph, signal)
  setPdfDescriptionFormatting(pdf)
  pdf.text(
    'The number of broadcast windows in which the system flagged more or less frequent MAP broadcasts than the expected',
    pdf.internal.pageSize.getWidth() / 2,
    pdfHeight / 2,
    { align: 'center' }
  )
  pdf.text(
    'rate of 1 Hz. Each day has a total of 8,640 broadcast windows. Lower numbers indicate better performance.',
    pdf.internal.pageSize.getWidth() / 2,
    pdfHeight / 2 + 5,
    { align: 'center' }
  )
  await captureGraph(
    pdf,
    'map-minimum-data-graph',
    { x: 0, y: pdfHeight / 2 + 10 },
    setProgress,
    totalGraphs,
    ++currentGraph,
    signal
  )
  pdf.text(
    'The number of times the system flagged MAP messages with missing or incomplete data.',
    pdf.internal.pageSize.getWidth() / 2,
    pdfHeight - 15,
    { align: 'center' }
  )
  pdf.text('Lower numbers indicate better performance.', pdf.internal.pageSize.getWidth() / 2, pdfHeight - 10, {
    align: 'center',
  })

  // Process and add MAP Missing Data Elements page
  if (report?.latestMapMinimumDataEventMissingElements?.length) {
    currentPage = addPageWithNumber(pdf, currentPage)
    setPdfItemTitleFormatting(pdf)
    pdf.text('MAP Missing Data Elements', pdf.internal.pageSize.getWidth() / 2, 20, { align: 'center' })
    setPdfBodyFormatting(pdf)
    const processedMapElements = processMissingElements(report.latestMapMinimumDataEventMissingElements)
    let yOffset = 30
    processedMapElements.forEach((element, index) => {
      const lines = pdf.splitTextToSize(element, pdf.internal.pageSize.getWidth() - 40)
      if (yOffset + lines.length * 7 > pdfHeight - 20) {
        currentPage = addPageWithNumber(pdf, currentPage)
        yOffset = 30
      }
      pdf.text(lines, 20, yOffset)
      yOffset += lines.length * 7
    })
  }

  currentPage = addPageWithNumber(pdf, currentPage)
  setPdfItemTitleFormatting(pdf)
  pdf.text('SPaT', pdf.internal.pageSize.getWidth() / 2, 20, { align: 'center' })
  await captureGraph(
    pdf,
    'spat-broadcast-rate-graph',
    { x: 0, y: 25 },
    setProgress,
    totalGraphs,
    ++currentGraph,
    signal
  )
  setPdfDescriptionFormatting(pdf)
  pdf.text(
    'The number of broadcast windows in which the system flagged more or less frequent SPaT broadcasts than the expected',
    pdf.internal.pageSize.getWidth() / 2,
    pdfHeight / 2,
    { align: 'center' }
  )
  pdf.text(
    'rate of 10 Hz. Each day has a total of 8,640 broadcast windows. Lower numbers indicate better performance.',
    pdf.internal.pageSize.getWidth() / 2,
    pdfHeight / 2 + 5,
    { align: 'center' }
  )
  await captureGraph(
    pdf,
    'spat-minimum-data-graph',
    { x: 0, y: pdfHeight / 2 + 10 },
    setProgress,
    totalGraphs,
    ++currentGraph,
    signal
  )
  pdf.text(
    'The number of times the system flagged SPaT messages with missing or incomplete data.',
    pdf.internal.pageSize.getWidth() / 2,
    pdfHeight - 15,
    { align: 'center' }
  )
  pdf.text('Lower numbers indicate better performance.', pdf.internal.pageSize.getWidth() / 2, pdfHeight - 10, {
    align: 'center',
  })

  // Process and add SPaT Missing Data Elements page
  if (report?.latestSpatMinimumDataEventMissingElements?.length) {
    currentPage = addPageWithNumber(pdf, currentPage)
    setPdfItemTitleFormatting(pdf)
    pdf.text('SPaT Missing Data Elements', pdf.internal.pageSize.getWidth() / 2, 20, { align: 'center' })
    setPdfBodyFormatting(pdf)
    const processedSpatElements = processMissingElements(report.latestSpatMinimumDataEventMissingElements)
    let yOffset = 30
    processedSpatElements.forEach((element, index) => {
      const lines = pdf.splitTextToSize(element, pdf.internal.pageSize.getWidth() - 40)
      if (yOffset + lines.length * 7 > pdfHeight - 20) {
        addPageWithNumber(pdf, currentPage++)
        yOffset = 30
      }
      pdf.text(lines, 20, yOffset)
      yOffset += lines.length * 7
    })
  }
  if (signal.aborted) return

  if (isModalOpen()) {
    pdf.save(report?.reportName + '.pdf' || 'report.pdf')
  }
  setLoading(false)
}
