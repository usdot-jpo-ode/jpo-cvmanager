import React from 'react'
import BarChartComponent from './bar-chart-component'
import reportColorPalette from '../report-color-palette'

interface StopLineStopGraphProps {
  data: { name: string; value: number }[]
  getInterval: (dataLength: number) => number
}

const StopLineStopGraph: React.FC<StopLineStopGraphProps> = ({ data, getInterval }) => (
  <BarChartComponent
    title="Stop Line Stop Events Per Day"
    data={data}
    getInterval={getInterval}
    barColor={reportColorPalette.blueGrey}
  />
)

export default StopLineStopGraph
