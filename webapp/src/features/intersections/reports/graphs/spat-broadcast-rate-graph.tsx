import React from 'react'
import BarChartComponent from './bar-chart-component'
import reportColorPalette from '../report-color-palette'

interface SpatBroadcastRateGraphProps {
  data: { name: string; value: number }[]
  getInterval: (dataLength: number) => number
}

const SpatBroadcastRateGraph: React.FC<SpatBroadcastRateGraphProps> = ({ data, getInterval }) => (
  <BarChartComponent
    title="SPaT Broadcast Rate Events per Day"
    data={data}
    getInterval={getInterval}
    barColor={reportColorPalette.purple}
  />
)

export default SpatBroadcastRateGraph
