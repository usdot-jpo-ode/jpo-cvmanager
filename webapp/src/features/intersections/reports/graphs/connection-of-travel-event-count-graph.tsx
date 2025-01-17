import React from 'react'
import BarChartComponent from './bar-chart-component'
import reportColorPalette from '../report-color-palette'

interface ConnectionOfTravelGraphProps {
  data: { name: string; value: number }[]
  getInterval: (dataLength: number) => number
}

const ConnectionOfTravelGraph: React.FC<ConnectionOfTravelGraphProps> = ({ data, getInterval }) => (
  <BarChartComponent
    title="Connection of Travel Events Per Day"
    data={data}
    getInterval={getInterval}
    barColor={reportColorPalette[2]}
  />
)

export default ConnectionOfTravelGraph
