import { Typography, TypographyProps, useTheme } from '@mui/material'
import React from 'react'

export const ErrorMessageText = (props: TypographyProps) => {
  const theme = useTheme()
  return (
    <Typography
      {...props}
      sx={{
        color: theme.palette.error.main,
        ...props.sx,
      }}
    />
  )
}

export const SuccessMessageText = (props: TypographyProps) => {
  const theme = useTheme()
  return (
    <Typography
      {...props}
      sx={{
        color: theme.palette.success.main,
        ...props.sx,
      }}
    />
  )
}
