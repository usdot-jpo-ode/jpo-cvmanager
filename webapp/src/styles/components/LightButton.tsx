import { Button, ButtonProps } from '@mui/material'
import React from 'react'
import { useTheme } from '@mui/material/styles'

export const LightButton = (props: ButtonProps) => {
  const theme = useTheme()
  return (
    <Button
      {...props}
      variant="contained"
      sx={{
        backgroundColor: theme.palette.primary.light,
        color: theme.palette.secondary.dark,
        '&:hover': {
          backgroundColor: theme.palette.primary.main,
        },
        padding: 2,
        ...props.sx,
      }}
    />
  )
}
