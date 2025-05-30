import { Button, ButtonProps, useTheme } from '@mui/material'
import React from 'react'
import '../../styles/fonts/museo-slab.css'

export const AdminButton = (props: ButtonProps) => {
  const theme = useTheme()
  return (
    <Button
      {...props}
      variant="contained"
      size="small"
      sx={{
        fontFamily: '"museo-slab" Arial Helvetica Sans-Serif',
        fontWeight: 550,
        backgroundColor: theme.palette.primary.dark,
        border: 'none',
        color: theme.palette.primary.contrastText,
        padding: '8px 10px',
        textAlign: 'center',
        fontSize: '12px',
        cursor: 'pointer',
        borderRadius: '3px',
        maxWidth: '400px',
        '&:hover': {
          backgroundColor: theme.palette.primary.main,
        },
        ...props.sx,
      }}
    />
  )
}
