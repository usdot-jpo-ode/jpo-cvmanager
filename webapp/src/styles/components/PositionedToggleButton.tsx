import { Button, ButtonProps, IconButton, IconButtonProps, useTheme } from '@mui/material'
import React from 'react'

// height: 35px;
// border: none;
// padding: 1px 12px;
// text-align: center;
// text-decoration: none;
// font-size: 18px;
// margin: 4px 2px;
// cursor: pointer;
// border-radius: 30px;
// margin-left: 100px;
// position: absolute;
// z-index: 100;
// margin-top: 10px;
// right: 10px;

export const PositionedToggleButton = (props: ButtonProps) => {
  return (
    <Button
      {...props}
      variant="contained"
      sx={{
        height: '35px',
        padding: 2,
        fontSize: '18px',
        margin: '4px 2px',
        marginLeft: '100px',
        position: 'absolute',
        zIndex: 100,
        marginTop: '10px',
        right: '10px',
        borderRadius: 10,
        ...props.sx,
      }}
    >
      {props.children}
    </Button>
  )
}

export const PositionedToggleIconButton = (props: IconButtonProps) => {
  const theme = useTheme()

  return (
    <IconButton
      {...props}
      sx={{
        height: '35px',
        fontSize: '18px',
        margin: '4px 2px',
        marginLeft: '100px',
        position: 'absolute',
        zIndex: 100,
        marginTop: '10px',
        right: '10px',
        backgroundColor: theme.palette.primary.main,
        color: theme.palette.primary.contrastText,
        '&:hover': {
          backgroundColor: theme.palette.primary.dark,
        },
        ...props.sx,
      }}
    >
      {props.children}
    </IconButton>
  )
}
