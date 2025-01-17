import React from 'react'
import BarChartComponent from './bar-chart-component'
import reportColorPalette from '../report-color-palette'

interface MapBroadcastRateGraphProps {
  data: { name: string; value: number }[]
  getInterval: (dataLength: number) => number
}

const MapBroadcastRateGraph: React.FC<MapBroadcastRateGraphProps> = ({ data, getInterval }) => (
  <BarChartComponent
    title="MAP Broadcast Rate Events per Day"
    data={data}
    getInterval={getInterval}
    barColor={reportColorPalette[6]}
  />
)

export default MapBroadcastRateGraph
