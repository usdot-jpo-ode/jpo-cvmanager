import React from 'react'
import BarChartComponent from './bar-chart-component'
import reportColorPalette from '../report-color-palette'

interface SignalStateEventGraphProps {
  data: { name: string; value: number }[]
  getInterval: (dataLength: number) => number
}

const StopLinePassageGraph: React.FC<SignalStateEventGraphProps> = ({ data, getInterval }) => (
  <BarChartComponent
    title="Stop Line Passage Events Per Day"
    data={data}
    getInterval={getInterval}
    barColor={reportColorPalette[4]}
  />
)

export default StopLinePassageGraph
