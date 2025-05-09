import { alpha, createTheme, Theme } from '@mui/material'
import './fonts/museo-slab.css'
import '../App.css'

declare module '@mui/material/styles' {
  interface PaletteOptions {
    custom: {
      mapLegendBackground: string
      tableHeaderBackground: string
      tableErrorBackground: string
      mapStyleFilePath: string
      mapStyleHasTraffic: boolean
      mapMenuItemBackgroundSelected: string
      mapMenuItemBorderSelected: string
      mapMenuItemHoverSelected: string
      mapMenuItemHoverUnselected: string
      mapMenuBackground: string
      intersectionMapAccordionExpanded: string
      intersectionMapButtonHover: string
      rowActionIcon: string
    }
  }
  interface Palette {
    custom: {
      mapLegendBackground: string
      tableHeaderBackground: string
      tableErrorBackground: string
      mapStyleFilePath: string
      mapStyleHasTraffic: boolean
      mapMenuItemBackgroundSelected: string
      mapMenuItemBorderSelected: string
      mapMenuItemHoverSelected: string
      mapMenuItemHoverUnselected: string
      mapMenuBackground: string
      intersectionMapAccordionExpanded: string
      intersectionMapButtonHover: string
      rowActionIcon: string
    }
  }
}

export const headerTabHeight = 119

export const testTheme = createTheme({
  palette: {
    custom: {
      mapLegendBackground: '#000000',
      tableHeaderBackground: '#000000',
      tableErrorBackground: '#000000',
      mapStyleFilePath: 'mapbox-styles/cdot-dark.json',
      mapStyleHasTraffic: true,
      mapMenuItemBackgroundSelected: '#000000',
      mapMenuItemBorderSelected: '#000000',
      mapMenuItemHoverSelected: '#000000',
      mapMenuItemHoverUnselected: '#000000',
      mapMenuBackground: '#000000',
      intersectionMapAccordionExpanded: '#000000',
      intersectionMapButtonHover: '#000000',
      rowActionIcon: '#000000',
    },
  },
})

// Please note that the light theme is currently not being maintained.
// Light Theme - https://www.realtimecolors.com/?colors=0a1424-e7eef8-213e73-7978d9-4431af&fonts=Inter-Inter
// --text: #0a1424;
// --background: #e7eef8;
// --primary: #213e73;
// --secondary: #7978d9;
// --accent: #4431af;
const themeMainLight = createTheme({
  cssVariables: true,
  typography: {
    fontFamily: '"Trebuchet MS", Arial, Helvetica, sans-serif',
    h1: {
      fontFamily: '"museo-slab" Arial Helvetica Sans-Serif',
    },
    h2: {
      fontFamily: '"museo-slab" Arial Helvetica Sans-Serif',
    },
    h3: {
      fontFamily: '"museo-slab" Arial Helvetica Sans-Serif',
    },
    h4: {
      fontFamily: '"museo-slab" Arial Helvetica Sans-Serif',
    },
    h5: {
      fontFamily: '"museo-slab" Arial Helvetica Sans-Serif',
    },
    h6: {
      fontFamily: '"museo-slab" Arial Helvetica Sans-Serif',
    },
  },
  components: {
    MuiTableHead: {
      styleOverrides: {
        root: {
          backgroundColor: '#F3F4F6',
          '.MuiTableCell-root': {
            color: '#374151',
          },
          borderBottom: 'none',
          '& .MuiTableCell-root': {
            borderBottom: 'none',
            fontSize: '12px',
            fontWeight: 600,
            lineHeight: 1,
            letterSpacing: 0.5,
            textTransform: 'uppercase',
          },
          '& .MuiTableCell-paddingCheckbox': {
            paddingTop: 4,
            paddingBottom: 4,
          },
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          backgroundImage: 'unset',
        },
      },
    },
    MuiDivider: {
      styleOverrides: {
        root: {
          borderColor: '#061731',
        },
      },
    },
  },
  palette: {
    mode: 'light',
    primary: {
      main: '#6f69e0',
      light: '#2e3574',
      dark: '#a7a3e7',
      contrastText: '#0b041b',
    },
    secondary: {
      main: '#53aaf1',
      light: '#114875',
      dark: '#92bcde',
      contrastText: '#121212',
    },
    error: {
      main: '#713737',
    },
    success: {
      main: '#315131',
    },
    text: {
      primary: '#121828',
      secondary: '#061731',
      disabled: 'rgba(55, 65, 81, 0.48)',
    },
    divider: '#8f8f8f',
    background: {
      default: '#ececec',
      paper: '#FFFFFF',
    },
    custom: {
      mapLegendBackground: '#c8cfda',
      tableHeaderBackground: '#F3F4F6',
      tableErrorBackground: '#fdc7c7',
      mapStyleFilePath: 'mapbox-styles/main-light.json',
      mapStyleHasTraffic: true,
      mapMenuBackground: '#e0e0e0',
      mapMenuItemBackgroundSelected: '#c4c2c2',
      mapMenuItemBorderSelected: '1px solid black',
      mapMenuItemHoverSelected: '#a19f9f',
      mapMenuItemHoverUnselected: '#ffffff',
      intersectionMapAccordionExpanded: '#d7d7d7',
      intersectionMapButtonHover: '#d7d7d7',
      rowActionIcon: '#9DBDD3',
    },
  },
})

// --text: #dbe5f5;
// --background: #1b1d1f;
// --primary: #4383ad;
// --secondary: #dbe5f5;
// --accent: #614fcd;
const themeMainDark = createTheme({
  cssVariables: true,
  typography: {
    fontFamily: '"Trebuchet MS", Arial, Helvetica, sans-serif',
    h1: {
      fontFamily: '"museo-slab" Arial Helvetica Sans-Serif',
    },
    h2: {
      fontFamily: '"museo-slab" Arial Helvetica Sans-Serif',
    },
    h3: {
      fontFamily: '"museo-slab" Arial Helvetica Sans-Serif',
    },
    h4: {
      fontFamily: '"museo-slab" Arial Helvetica Sans-Serif',
    },
    h5: {
      fontFamily: '"museo-slab" Arial Helvetica Sans-Serif',
    },
    h6: {
      fontFamily: '"museo-slab" Arial Helvetica Sans-Serif',
    },
  },
  components: {
    MuiTableHead: {
      styleOverrides: {
        root: {
          backgroundColor: '#252525',
          borderBottom: 'none',
          '& .MuiTableCell-root': {
            borderBottom: 'none',
            fontSize: '12px',
            fontWeight: 600,
            lineHeight: 1,
            letterSpacing: 0.5,
            textTransform: 'uppercase',
          },
          '& .MuiTableCell-paddingCheckbox': {
            paddingTop: 4,
            paddingBottom: 4,
          },
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          backgroundImage: 'unset',
        },
      },
    },
    MuiDivider: {
      styleOverrides: {
        root: {
          borderColor: '#c7c7c7',
        },
      },
    },
  },
  palette: {
    mode: 'dark',
    primary: {
      main: '#3B7BA5',
      light: '#51a2d6',
      dark: '#3B7BA54D',
      contrastText: '#fff',
    },
    secondary: {
      main: '#dbe5f5',
      light: '#535297',
      dark: '#161563',
      contrastText: '#FAFAFA',
    },
    error: {
      light: '#FF6D57',
      main: '#E94F3766',
      dark: '#6e312a',
    },
    success: {
      light: '#75BD27',
      main: '#A0D36466',
      dark: '#9e0e0e',
    },
    text: {
      primary: '#FFFFFF',
      secondary: '#c7c7c7',
      disabled: '#acacac',
    },
    info: {
      main: '#dbe5f5',
    },
    custom: {
      mapLegendBackground: '#1b1d1f',
      tableHeaderBackground: '#252525',
      tableErrorBackground: '#4d2e2e',
      mapStyleFilePath: 'mapbox-styles/main-dark.json',
      mapStyleHasTraffic: true,
      mapMenuBackground: '#3c3c3c',
      mapMenuItemBackgroundSelected: '#333333',
      mapMenuItemBorderSelected: '1px solid black',
      mapMenuItemHoverSelected: '#333333',
      mapMenuItemHoverUnselected: '#575757',
      intersectionMapAccordionExpanded: '#2E2F31',
      intersectionMapButtonHover: '#2D5F7F',
      rowActionIcon: '#9DBDD3',
    },
    background: {
      paper: '#1b1d1f',
      default: '#333333',
    },
  },
})

// All available themes
export const THEMES = {
  light: themeMainLight,
  dark: themeMainDark,
}

export const getCurrentTheme = (isDarkTheme: boolean, lightThemeName: string, darkThemeName: string): Theme => {
  // Warnings to user if theme names are not known
  if (darkThemeName && !THEMES[darkThemeName]) {
    console.warn(`Unknown dark theme name: ${darkThemeName}`)
  }
  if (lightThemeName && !THEMES[lightThemeName]) {
    console.warn(`Unknown light theme name: ${lightThemeName}`)
  }
  if (isDarkTheme) {
    return THEMES[darkThemeName] ?? THEMES.dark
  } else {
    return THEMES[lightThemeName] ?? THEMES.light
  }
}
