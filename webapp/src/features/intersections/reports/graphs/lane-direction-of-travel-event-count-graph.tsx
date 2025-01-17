import React from 'react'
import BarChartComponent from './bar-chart-component'
import reportColorPalette from '../report-color-palette'

interface LaneDirectionOfTravelGraphProps {
  data: { name: string; value: number }[]
  getInterval: (dataLength: number) => number
}

const LaneDirectionOfTravelGraph: React.FC<LaneDirectionOfTravelGraphProps> = ({ data, getInterval }) => (
  <BarChartComponent
    title="Lane Direction of Travel Events Per Day"
    data={data}
    getInterval={getInterval}
    barColor={reportColorPalette[9]}
  />
)

export default LaneDirectionOfTravelGraph
