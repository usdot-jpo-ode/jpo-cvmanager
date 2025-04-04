import { styled } from '@mui/material'
import { Link } from 'react-router-dom'

export const TabListContainer = styled('ol')(({ theme }) => ({
  fontFamily: 'Arial, Helvetica, sans-serif',
  fontWeight: 500,
  paddingLeft: 0,
  margin: '8px 24px 0px 24px',
}))

interface TabItemStyledProps {
  isActive?: boolean
}

export const TabItemStyled = styled(Link)<TabItemStyledProps>(({ theme, isActive }) => ({
  display: 'inline-block',
  listStyle: 'none',
  padding: '9px 16px',
  cursor: 'pointer',
  color: theme.palette.text.secondary,
  textDecoration: 'none',
  ...(isActive && {
    fontFamily: 'Arial, Helvetica, sans-serif',
    color: theme.palette.primary.contrastText,
    borderBottom: '2px solid white',
  }),
}))
