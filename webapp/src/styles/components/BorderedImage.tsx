import { styled } from '@mui/material'

// display: block;
// margin-left: auto;
// margin-right: auto;
// width: 40%;
// margin-top: 4em;
// margin-bottom: 4em;
// border: 0.5px solid white;

export const BorderedImage = styled('img')(({ theme }) => ({
  display: 'block',
  marginLeft: 'auto',
  marginRight: 'auto',
  width: '40%',
  marginTop: '4em',
  marginBottom: '4em',
  border: `0.5px solid ${theme.palette.text.primary}}`,
}))
