import { Button, ButtonProps, useTheme, alpha } from '@mui/material'
import React from 'react'

export const PrimaryButton = (props: ButtonProps) => {
  const theme = useTheme()
  return (
    <Button
      {...props}
      variant="contained"
      sx={{
        backgroundColor: theme.palette.primary.main,
        '&:hover': {
          backgroundColor: alpha(theme.palette.primary.dark, 1),
        },
        ...props.sx,
      }}
    />
  )
}
