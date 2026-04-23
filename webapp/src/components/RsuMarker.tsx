export type RsuMarkerProps = {
  displayType: string
  onlineStatus?: string
  scmsStatus?: boolean | null
}

function RsuMarker(props: RsuMarkerProps) {
  const circleStyle = {
    padding: 5,
    display: 'inline-block',
    borderRadius: '50%',
    width: 5,
    height: 5,
    backgroundColor: '',
  }

  if (props.displayType === 'online') {
    if (props.onlineStatus === 'online') circleStyle.backgroundColor = '#A1D363'
    else if (props.onlineStatus === 'unstable') circleStyle.backgroundColor = '#D1A711'
    else if (props.onlineStatus === 'offline') circleStyle.backgroundColor = '#E94F37'
    else circleStyle.backgroundColor = '#B0B0B0'
  } else if (props.displayType === 'scms') {
    if (props.scmsStatus === true) circleStyle.backgroundColor = '#A1D363'
    else if (props.scmsStatus === false) circleStyle.backgroundColor = '#E94F37'
    else circleStyle.backgroundColor = '#B0B0B0'
  } else {
    circleStyle.backgroundColor = '#B0B0B0'
  }

  return <div style={circleStyle}></div>
}

export default RsuMarker
