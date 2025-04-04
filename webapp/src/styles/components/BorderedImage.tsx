import { styled } from '@mui/material'

export const BorderedImage = styled('img')(({ theme }) => ({
  display: 'block',
  marginLeft: 'auto',
  marginRight: 'auto',
  marginTop: '4em',
  marginBottom: '4em',
  border: `0.5px solid ${theme.palette.text.primary}}`,
}))
