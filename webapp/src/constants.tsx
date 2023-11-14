export const MessageTypes = ['BSM', 'SSM', 'SPAT', 'SRM', 'MAP'] as const
export type MessageType = (typeof MessageTypes)[number]
export const DotName = 'CDOT'
export const MapboxInitViewState = {
  latitude: 39.7392,
  longitude: -104.9903,
  zoom: 10,
}
