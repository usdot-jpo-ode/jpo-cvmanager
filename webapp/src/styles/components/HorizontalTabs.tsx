import { LinkProps, styled } from '@mui/material'
import { Link } from 'react-router-dom'

// font-family: Arial, Helvetica, sans-serif;
// font-weight: 550;
// border-bottom: 1px solid #0e2052;
// padding-left: 0;
// background-color: #0e2052;
// color: white;

export const TabListContainer = styled('ol')(({ theme }) => ({
  fontFamily: 'Arial, Helvetica, sans-serif',
  fontWeight: 550,
  borderBottom: `1px solid ${theme.palette.secondary.dark}`,
  paddingLeft: 0,
  backgroundColor: theme.palette.secondary.dark,
  color: 'white',
}))

// .tab-list-item
// display: inline-block;
// list-style: none;
// margin-bottom: -1px;
// padding: 0.5rem 0.75rem;
// cursor: pointer;
// color: white;
// text-decoration: none;
//
// .tab-list-active
// font-family: Arial, Helvetica, sans-serif;
// font-weight: 550;
// background-color: #b55e12;
// color: white;
// border: solid #b55e12;
// border-width: 1px 1px 0 1px;
// border-top: 0.5px solid #ffffff;

interface TabItemStyledProps {
  isActive?: boolean
}

export const TabItemStyled = styled(Link)<TabItemStyledProps>(({ theme, isActive }) => ({
  display: 'inline-block',
  listStyle: 'none',
  marginBottom: '-1px',
  padding: '0.5rem 0.75rem',
  cursor: 'pointer',
  color: 'white',
  textDecoration: 'none',
  ...(isActive && {
    fontFamily: 'Arial, Helvetica, sans-serif',
    fontWeight: 550,
    backgroundColor: theme.palette.primary.main,
    color: 'white',
    border: `solid ${theme.palette.primary.main}`,
    borderWidth: '1px 1px 0 1px',
    borderTop: '0.5px solid #ffffff',
  }),
}))
