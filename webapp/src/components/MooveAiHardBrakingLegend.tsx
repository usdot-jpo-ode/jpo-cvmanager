import React from 'react'
import { Typography } from '@mui/material'

const MooveAiHardBrakingLegend = () => {
  return (
    <div className="hardBrakingLegend">
      <Typography fontFamily="Arial, Helvetica, sans-serif" fontSize="small" align="center">
        <b>Hard Braking Color Guide</b>
      </Typography>
      <div style={gridStyle}>
        <div style={gridRowStyle}>
          <div style={{ width: '20px', height: '20px', backgroundColor: 'rgb(0, 255, 0)', marginRight: '10px' }}></div>
          <Typography fontFamily="Arial, Helvetica, sans-serif" fontSize="small">
            0-249 Hard Brakes
          </Typography>
        </div>
        <div style={gridRowStyle}>
          <div
            style={{ width: '20px', height: '20px', backgroundColor: 'rgb(255, 255, 0)', marginRight: '10px' }}
          ></div>
          <Typography fontFamily="Arial, Helvetica, sans-serif" fontSize="small">
            250-499 Hard Brakes
          </Typography>
        </div>
        <div style={gridRowStyle}>
          <div
            style={{ width: '20px', height: '20px', backgroundColor: 'rgb(255, 165, 0)', marginRight: '10px' }}
          ></div>
          <Typography fontFamily="Arial, Helvetica, sans-serif" fontSize="small">
            500-749 Hard Brakes
          </Typography>
        </div>
        <div style={gridRowStyle}>
          <div style={{ width: '20px', height: '20px', backgroundColor: 'rgb(255, 0, 0)', marginRight: '10px' }}></div>
          <Typography fontFamily="Arial, Helvetica, sans-serif" fontSize="small">
            750+ Hard Brakes
          </Typography>
        </div>
        <div style={gridBottomStyle}></div>
      </div>
      <Typography fontFamily="Arial, Helvetica, sans-serif" fontSize="small" style={{ marginLeft: '10px' }}>
        Determined by a weekly average
      </Typography>
    </div>
  )
}

const gridStyle: React.CSSProperties = {
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'flex-start',
  marginTop: '10px',
  marginLeft: '10px',
}

const gridRowStyle: React.CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  marginBottom: '5px',
}

const gridBottomStyle: React.CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  marginBottom: '10px',
}

export default MooveAiHardBrakingLegend
