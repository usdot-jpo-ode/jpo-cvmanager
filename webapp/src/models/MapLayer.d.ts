import { ReactElement } from 'react'
import { LayerProps } from 'react-map-gl'

export type MapLayer = LayerProps & { id: string; label: string; tag?: FEATURE_KEY; control?: ReactElement }
