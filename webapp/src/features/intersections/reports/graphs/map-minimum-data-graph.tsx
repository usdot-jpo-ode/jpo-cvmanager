import React from 'react'
import BarChartComponent from './bar-chart-component'
import reportColorPalette from '../report-color-palette'

interface MapMinimumDataGraphProps {
  data: { name: string; value: number }[]
  getInterval: (dataLength: number) => number
}

const MapMinimumDataGraph: React.FC<MapMinimumDataGraphProps> = ({ data, getInterval }) => (
  <BarChartComponent
    title="MAP Minimum Data Events per Day"
    data={data}
    getInterval={getInterval}
    barColor={reportColorPalette[6]}
  />
)

export default MapMinimumDataGraph
