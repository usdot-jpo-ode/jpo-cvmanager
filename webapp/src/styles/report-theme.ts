import { createTheme } from '@mui/material'

export const ReportTheme = createTheme({
  typography: {
    h1: {
      color: 'black',
    },
    h2: {
      color: 'black',
    },
    h3: {
      color: 'black',
    },
    h4: {
      color: 'black',
    },
    h5: {
      color: 'black',
    },
    h6: {
      color: 'black',
    },
    body1: {
      color: 'black',
    },
    body2: {
      color: 'black',
    },
  },
  palette: {
    mode: 'light',
    primary: {
      main: '#000000',
      light: '#000000',
      dark: '#000000',
      contrastText: '#FFFFFF',
    },
    secondary: {
      main: '#000000',
      light: '#000000',
      dark: '#000000',
      contrastText: '#FFFFFF',
    },
    error: {
      main: '#ad2626',
    },
    success: {
      main: '#2e942e',
    },
    text: {
      primary: '#030303',
      secondary: '#030303',
      disabled: 'rgba(66, 66, 66, 0.48)',
    },
    divider: '#575757',
    background: {
      default: '#ffffff',
      paper: '#FFFFFF',
    },
    custom: {
      mapLegendBackground: '#ffffff',
      tableHeaderBackground: '#ffffff',
      tableErrorBackground: '#ffffff',
      mapStyleFilePath: 'mapbox-styles/main-light.json',
      mapStyleHasTraffic: true,
      mapMenuBackground: '#ffffff',
      mapMenuItemBackgroundSelected: '#ffffff',
      mapMenuItemBorderSelected: '1px solid black',
      mapMenuItemHoverSelected: '#a19f9f',
      mapMenuItemHoverUnselected: '#ffffff',
      rowActionIcon: '#9DBDD3',
    },
  },
})
