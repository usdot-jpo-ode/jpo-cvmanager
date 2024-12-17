import { IconButton, IconButtonProps, useTheme } from '@mui/material'
import React from 'react'

export const ContainedIconButton = (props: IconButtonProps) => {
  const theme = useTheme()

  return (
    <IconButton
      {...props}
      sx={{
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
