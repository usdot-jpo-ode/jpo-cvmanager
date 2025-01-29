import React from 'react'
import BarChartComponent from './bar-chart-component'
import reportColorPalette from '../report-color-palette'

interface SignalStateConflictGraphProps {
  data: { name: string; value: number }[]
  getInterval: (dataLength: number) => number
}

const SignalStateConflictGraph: React.FC<SignalStateConflictGraphProps> = ({ data, getInterval }) => (
  <BarChartComponent
    title="Signal State Conflict Events per Day"
    data={data}
    getInterval={getInterval}
    barColor={reportColorPalette.blueGrey}
  />
)

export default SignalStateConflictGraph
