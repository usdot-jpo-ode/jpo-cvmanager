import { ReactElement } from 'react'
import { LayerProps } from 'react-map-gl'

export type MapLayer = LayerProps & { label: string; tag?: FEATURE_KEY; control?: ReactElement }
