import { createTheme, Theme } from '@mui/material'

declare module '@mui/material/styles' {
  interface PaletteOptions {
    custom: {
      mapLegendBackground: string
      tableHeaderBackground: string
      tableErrorBackground: string
      mapStyleFilePath: string
    }
  }
  interface Palette {
    custom: {
      mapLegendBackground: string
      tableHeaderBackground: string
      tableErrorBackground: string
      mapStyleFilePath: string
    }
  }
}

export const headerTabHeight = 141

// Global Theme
const themeCdotDark = createTheme({
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
  },
  palette: {
    mode: 'dark',
    common: {
      black: '#000000',
      white: '#ffffff',
    },
    primary: {
      main: '#e37120',
      light: '#cecece',
      dark: '#b55e12',
      contrastText: '#fff',
    },
    secondary: {
      main: '#0e2052',
      light: '#26329f',
      dark: '#0a0f3f',
      //   lightButton:
      contrastText: '#fff',
    },
    error: {
      main: '#FD7C7C',
    },
    success: {
      main: '#90EE90',
    },
    text: {
      primary: '#ffffff',
      secondary: '#eb8841',
      disabled: '#acacac',
    },
    custom: {
      mapLegendBackground: '#0e2052',
      tableHeaderBackground: '#252525',
      tableErrorBackground: '#4d2e2e',
      mapStyleFilePath: 'mapbox-styles/cdot-dark.json',
    },
    divider: '#111',
    background: {
      paper: '#333',
      default: '#1c1d1f',
    },
  },
})

// Light Theme - https://www.realtimecolors.com/?colors=0a1424-e7eef8-213e73-7978d9-4431af&fonts=Inter-Inter
// --text: #0a1424;
// --background: #e7eef8;
// --primary: #213e73;
// --secondary: #7978d9;
// --accent: #4431af;
const themeMainLight = createTheme({
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
  },
  palette: {
    mode: 'light',
    common: {
      black: '#000000',
      white: '#ffffff',
    },
    primary: {
      main: '#5048E5',
      light: '#a6aef4',
      dark: '#413bbc',
      contrastText: '#FFFFFF',
    },
    secondary: {
      main: '#53aaf1',
      light: '#91c6f2',
      dark: '#071f32',
      contrastText: '#FFFFFF',
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
    },
  },
})

// Dark Theme - https://www.realtimecolors.com/?colors=dbe5f5-070e18-8ca9de-282687-6350ce&fonts=Inter-Inter
// --text: #dbe5f5;
// --background: #070e19;
// --primary: #8ca9de;
// --secondary: #282688;
// --accent: #614fcd;
const themeMainDark = createTheme({
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
  },
  palette: {
    mode: 'dark',
    common: {
      black: '#000000',
      white: '#ffffff',
    },
    primary: {
      main: '#315fb6',
      light: '#cecece',
      dark: '#23488c',
      contrastText: '#fff',
    },
    secondary: {
      main: '#282688',
      light: '#535297',
      dark: '#161563',
      contrastText: '#fff',
    },
    error: {
      main: '#FD7C7C',
    },
    success: {
      main: '#90EE90',
    },
    text: {
      primary: '#dbe5f5',
      secondary: '#dbe5f5',
      disabled: '#acacac',
    },
    custom: {
      mapLegendBackground: '#070e19',
      tableHeaderBackground: '#252525',
      tableErrorBackground: '#4d2e2e',
      mapStyleFilePath: 'mapbox-styles/main-dark.json',
    },
    divider: '#111',
    background: {
      paper: '#282828',
      default: '#070e19',
    },
  },
})

// All available themes
export const THEMES = {
  light: themeMainLight,
  dark: themeMainDark,
  cdotDark: themeCdotDark,
}

export const getCurrentTheme = (isDarkTheme: boolean, defaultLightTheme: string, defaultDarkTheme: string) => {
  let theme = THEMES[defaultLightTheme] ?? THEMES.light
  if (isDarkTheme) {
    theme = THEMES[defaultDarkTheme] ?? THEMES.dark
    if (defaultDarkTheme && !THEMES[defaultDarkTheme]) {
      console.warn(`Unknown dark theme name: ${defaultDarkTheme}. Defaulting to browser theme.`)
    }
  } else if (defaultLightTheme && !THEMES[defaultLightTheme]) {
    console.warn(`Unknown default theme name: ${defaultLightTheme}. Defaulting to browser theme.`)
  }
  return theme
}
