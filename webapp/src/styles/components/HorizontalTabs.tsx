import { styled } from '@mui/material'
import { Link } from 'react-router-dom'

export const TabListContainer = styled('ol')(({ theme }) => ({
  fontFamily: 'Arial, Helvetica, sans-serif',
  fontWeight: 550,
  borderBottom: `1px solid ${theme.palette.custom.mapLegendBackground}`,
  paddingLeft: 0,
  backgroundColor: theme.palette.custom.mapLegendBackground,
}))

interface TabItemStyledProps {
  isActive?: boolean
}

export const TabItemStyled = styled(Link)<TabItemStyledProps>(({ theme, isActive }) => ({
  display: 'inline-block',
  listStyle: 'none',
  marginBottom: '-1px',
  padding: '0.5rem 0.75rem',
  cursor: 'pointer',
  color: theme.palette.text.primary,
  textDecoration: 'none',
  ...(isActive && {
    fontFamily: 'Arial, Helvetica, sans-serif',
    fontWeight: 550,
    backgroundColor: theme.palette.primary.main,
    color: theme.palette.primary.contrastText,
    border: `solid ${theme.palette.primary.main}`,
    borderWidth: '1px 1px 0 1px',
    borderTop: `0.5px solid ${theme.palette.secondary.dark}`,
  }),
}))
