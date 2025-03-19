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

/**
 * Generates an intersection-specific client-side PDF-style report from Conflict Monitor datasets, including various graphs and data visualizations. This report shows breakdowns of various assessments, broken up by different sections.
 *
 * @param {ReportMetadata} report - The metadata for the report, including intersection details and data.
 * @param {(loading: boolean) => void} setLoading - A function to set the loading state of the report generation process.
 * @param {boolean} includeLaneSpecificCharts - Whether to include lane-specific charts in the report.
 * @param {() => boolean} isModalOpen - A function to check if the modal for the report is still open.
 * @param {(progress: number) => void} setProgress - A function to update the progress of the report generation process.
 * @param {AbortSignal} signal - An AbortSignal to handle cancellation of the report generation process.
 * @returns {Promise<void>} - A promise that resolves when the PDF generation is complete.
 */
export const generatePdf = async (
  report: ReportMetadata,
  setLoading: (loading: boolean) => void,
  includeLaneSpecificCharts: boolean,
  isModalOpen: () => boolean,
  setProgress: (progress: number) => void,
  signal: AbortSignal
): Promise<void> => {
  // Set the loading state to true to indicate the process has started
  setLoading(true)

  // Initialize a new PDF document with A4 size
  const pdf = new jsPDF('p', 'mm', 'a4')
  const pdfHeight = pdf.internal.pageSize.getHeight()
  const pdfWidth = pdf.internal.pageSize.getWidth()

  // Extract unique lane IDs from the report data
  const laneIds = Array.from(new Set(report.laneDirectionOfTravelReportData.map((item) => item.laneID)))

  // Calculate the total number of graphs to be included in the report
  const totalGraphs = 14 + (includeLaneSpecificCharts ? 2 * laneIds.length : 0)

  let currentGraph = 0 // Tracks the current graph being processed
  let currentPage = 1 // Tracks the current page number in the PDF

  // Add the report title and intersection details to the first page
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

  // Add the Lane Direction of Travel section
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

  // Add the Lane Direction Heading section
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

  // Add lane-specific charts if requested
  if (includeLaneSpecificCharts) {
    // Add Distance From Centerline Over Time section
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

    // Add Vehicle Heading Error Delta Over Time section
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

  // Add Connection of Travel section
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

  // Add Signal State Events section
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

  // Add MAP and SPaT sections
  setPdfItemTitleFormatting(pdf)
  pdf.text('MAP', pdf.internal.pageSize.getWidth() / 2, 20, { align: 'center' })
  await captureGraph(pdf, 'map-broadcast-rate-graph', { x: 0, y: 25 }, setProgress, totalGraphs, ++currentGraph, signal)
  setPdfDescriptionFormatting(pdf)
  pdf.text(
    'The number of broadcast windows in which the system flagged more or less frequent MAP broadcasts than the expected rate of 1 Hz.',
    pdf.internal.pageSize.getWidth() / 2,
    pdfHeight / 2,
    { align: 'center' }
  )

  // Save the PDF if the modal is still open
  if (isModalOpen()) {
    pdf.save(report?.reportName + '.pdf' || 'report.pdf')
  }

  // Set the loading state to false to indicate the process has completed
  setLoading(false)
}
