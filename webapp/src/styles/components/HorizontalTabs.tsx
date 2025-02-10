import { styled } from '@mui/material'
import { Link } from 'react-router-dom'

export const TabListContainer = styled('ol')(({ theme }) => ({
  fontFamily: 'Arial, Helvetica, sans-serif',
  fontWeight: 550,
  paddingLeft: 0,
}))

interface TabItemStyledProps {
  isActive?: boolean
}

export const TabItemStyled = styled(Link)<TabItemStyledProps>(({ theme, isActive }) => ({
  display: 'inline-block',
  listStyle: 'none',
  padding: '0.5rem 0.75rem',
  cursor: 'pointer',
  color: theme.palette.text.primary,
  textDecoration: 'none',
  ...(isActive && {
    fontFamily: 'Arial, Helvetica, sans-serif',
    fontWeight: 750,
    backgroundColor: theme.palette.primary.main,
    color: theme.palette.primary.contrastText,
    borderBottom: '2px solid white',
  }),
}))
