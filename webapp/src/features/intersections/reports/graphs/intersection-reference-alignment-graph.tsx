import React from 'react'
import BarChartComponent from './bar-chart-component'
import reportColorPalette from '../report-color-palette'

interface IntersectionReferenceAlignmentGraphProps {
  data: { name: string; value: number }[]
  getInterval: (dataLength: number) => number
}

const IntersectionReferenceAlignmentGraph: React.FC<IntersectionReferenceAlignmentGraphProps> = ({
  data,
  getInterval,
}) => {
  return (
    <BarChartComponent
      title="Intersection Reference Alignments Per Day"
      data={data}
      getInterval={getInterval}
      barColor={reportColorPalette.pink}
    />
  )
}

export default IntersectionReferenceAlignmentGraph
