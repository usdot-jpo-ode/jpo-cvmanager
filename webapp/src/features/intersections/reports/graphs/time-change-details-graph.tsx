import React from 'react'
import BarChartComponent from './bar-chart-component'
import reportColorPalette from '../report-color-palette'

interface TimeChangeDetailsGraphProps {
  data: { name: string; value: number }[]
  getInterval: (dataLength: number) => number
}

const TimeChangeDetailsGraph: React.FC<TimeChangeDetailsGraphProps> = ({ data, getInterval }) => (
  <BarChartComponent
    title="Time Change Details Events per Day"
    data={data}
    getInterval={getInterval}
    barColor={reportColorPalette.blueGrey}
  />
)

export default TimeChangeDetailsGraph
