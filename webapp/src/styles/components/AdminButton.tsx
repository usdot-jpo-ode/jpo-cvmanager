import { styled } from '@mui/material'

// font-family: Arial, Helvetica, sans-serif;
// font-weight: 550;
// background-color: var(--mui-palette-primary-dark);
// border: none;
// color: white;
// padding: 8px 10px;
// text-align: center;
// font-size: 12px;
// cursor: pointer;
// border-radius: 3px;
// max-width: 400px;

export const AdminButton = styled('button')(({ theme }) => ({
  fontFamily: 'Arial, Helvetica, sans-serif',
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
}))
