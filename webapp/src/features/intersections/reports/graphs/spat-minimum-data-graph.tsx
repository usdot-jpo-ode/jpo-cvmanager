import React from 'react'
import BarChartComponent from './bar-chart-component'
import reportColorPalette from '../report-color-palette'

interface SpatMinimumDataGraphProps {
  data: { name: string; value: number }[]
  getInterval: (dataLength: number) => number
}

const SpatMinimumDataGraph: React.FC<SpatMinimumDataGraphProps> = ({ data, getInterval }) => (
  <BarChartComponent
    title="SPaT Minimum Data Events per Day"
    data={data}
    getInterval={getInterval}
    barColor={reportColorPalette.purple}
  />
)

export default SpatMinimumDataGraph
