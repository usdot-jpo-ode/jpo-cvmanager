import { createTheme } from '@mui/material'

declare module '@mui/material/styles' {
  interface PaletteOptions {
    custom: {
      mapLegendBackground: string
      mapStyleFilePath: string
      mapStylePath: string
    }
  }
  interface Palette {
    custom: {
      mapLegendBackground: string
      mapStyleFilePath: string
      mapStylePath: string
    }
  }
}

export const headerTabHeight = 141

// Global Theme
export const themeCdot = createTheme({
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
      //   lightButton:
      contrastText: '#fff',
    },
    secondary: {
      main: '#0e2052',
      light: '#26329f',
      dark: '#0a0f3f',
      contrastText: '#fff',
    },
    text: {
      primary: '#ffffff',
      secondary: '#eb8841',
      disabled: '#acacac',
    },
    custom: {
      mapLegendBackground: '#0e2052',
      mapStyleFilePath: 'mapbox-styles/cdot-dark.json',
      mapStylePath: 'mapbox://styles/mapbox/navigation-night-v1',
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
export const themeLight = createTheme({
  palette: {
    mode: 'light',
    common: {
      black: '#000000',
      white: '#ffffff',
    },
    primary: {
      main: '#213e73',
      light: '#cecece',
      dark: '#132445',
      //   lightButton:
      contrastText: '#ffffff',
    },
    secondary: {
      main: '#7978d9',
      light: '#9796d2',
      dark: '#4341d8',
      contrastText: '#000000',
    },
    text: {
      primary: '#0a1424',
      secondary: '#0a1424',
      disabled: '#7e7e7e',
    },
    custom: {
      mapLegendBackground: '#e7eef8',
      mapStyleFilePath: 'mapbox-styles/main-light.json',
      mapStylePath: 'mapbox://styles/mapbox/navigation-day-v1',
    },
    divider: '#2d2d2d',
    background: {
      paper: '#d2d2d2',
      default: '#aeadad',
    },
  },
})

// Dark Theme - https://www.realtimecolors.com/?colors=dbe5f5-070e18-8ca9de-282687-6350ce&fonts=Inter-Inter
// --text: #dbe5f5;
// --background: #070e19;
// --primary: #8ca9de;
// --secondary: #282688;
// --accent: #614fcd;
export const themeDark = createTheme({
  palette: {
    mode: 'dark',
    common: {
      black: '#000000',
      white: '#ffffff',
    },
    primary: {
      main: '#d91f1f',
      light: '#cecece',
      dark: '#670606',
      contrastText: '#fff',
    },
    secondary: {
      main: '#1e9393',
      light: '#52e4e4',
      dark: '#073c3c',
      contrastText: '#fff',
    },
    text: {
      primary: '#ffffff',
      secondary: '#eb8841',
      disabled: '#acacac',
    },
    custom: {
      mapLegendBackground: '#073c3c',
      mapStyleFilePath: 'mapbox-styles/main-dark.json',
      mapStylePath: 'mapbox://styles/mapbox/navigation-night-v1',
    },
    divider: '#111',
    background: {
      paper: '#333',
      default: '#1c1d1f',
    },
  },
})

export const theme = themeLight

// used by AdminTable.tsx
export const tableTheme = theme
// createTheme({
//   palette: {
//     primary: {
//       main: '#ffffff',
//       light: secondary,
//       contrastTextColor: secondary,
//     },
//     secondary: {
//       main: '#333333',
//       light: secondary,
//       contrastTextColor: secondary,
//     },
//     text: {
//       primary: '#ffffff',
//       secondary: '#ffffff',
//       disabled: '#ffffff',
//       hint: '#ffffff',
//     },
//     divider: '#333',
//     background: {
//       paper: '#333',
//       default: '#1c1d1f',
//     },
//   },
//   components: {
//     MuiPaper: {
//       styleOverrides: {
//         root: {
//           border: '1.5px solid #ffffff',
//         },
//       },
//     },
//     MuiIcon: {
//       styleOverrides: {
//         root: {
//           color: '#333333',
//           backgroundColor: '#dadde5',
//           borderRadius: '50%',
//           padding: 2,
//         },
//       },
//     },
//     MuiInputBase: {
//       styleOverrides: {
//         root: {
//           backgroundColor: '#dadde5',
//           color: '#333333',
//         },
//       },
//     },
//     MuiToolbar: {
//       styleOverrides: {
//         root: {
//           color: '#333333',
//           backgroundColor: '#dadde5',
//         },
//       },
//     },
//     MuiTableCell: {
//       styleOverrides: {
//         footer: {
//           backgroundColor: '#dadde5',
//         },
//       },
//     },
//   },
// })

// used by SnmpwalkMenu.tsx
export const snmpWalkMenuTheme = theme
// createTheme({
//   palette: {
//     primary: {
//       main: primary,
//       light: secondary,
//       contrastTextColor: secondary,
//     },
//     secondary: {
//       main: primary,
//       light: secondary,
//       contrastTextColor: secondary,
//     },
//     text: {
//       primary: '#ffffff',
//       secondary: '#ffffff',
//       disabled: '#ffffff',
//       hint: '#ffffff',
//     },
//     action: {
//       disabledBackground: 'rgba(209, 109, 21, 0.2)',
//       disabled: '#ffffff',
//     },
//   },
//   typography: {
//     button: {
//       textTransform: 'none',
//     },
//   },
// })

// Used by Map.tsx
export const mapTheme = theme
// createTheme({
//   palette: {
//     primary: {
//       main: primary,
//       light: secondary,
//       contrastTextColor: secondary,
//     },
//     secondary: {
//       main: primary,
//       light: secondary,
//       contrastTextColor: secondary,
//     },
//     text: {
//       primary: '#ffffff',
//       secondary: '#ffffff',
//       disabled: '#ffffff',
//       hint: '#ffffff',
//     },
//     action: {
//       disabledBackground: 'rgba(209, 109, 21, 0.2)',
//       disabled: '#ffffff',
//     },
//   },
//   components: {
//     MuiSvgIcon: {
//       styleOverrides: {
//         root: {
//           color: primary,
//         },
//       },
//     },
//     MuiButton: {
//       styleOverrides: {
//         root: {
//           fontSize: '1rem',
//           borderRadius: 15,
//         },
//       },
//     },
//   },
//   input: {
//     color: '#11ff00',
//   },
//   typography: {
//     allVariants: {
//       color: '#ffffff',
//     },
//     button: {
//       textTransform: 'none',
//     },
//   },
// })

// Used by AdminOrganizationTabRsu.tsx, AdminOrganizationTabUser.tsx, and ConfigureRSU.tsx
export const accordionTheme = theme
// createTheme({
//   palette: {
//     text: {
//       primary: '#fff',
//       secondary: '#fff',
//       disabled: '#fff',
//       hint: '#fff',
//     },
//     divider: '#333',
//     background: {
//       paper: '#333',
//     },
//   },
// })

// Used by AdminOrganizationTabRsu.tsx, AdminOrganizationTabUser.tsx
export const outerAccordionTheme = theme
// createTheme({
//   palette: {
//     text: {
//       primary: '#fff',
//       secondary: '#fff',
//       disabled: '#fff',
//       hint: '#fff',
//     },
//     divider: '#333',
//     background: {
//       paper: secondary,
//     },
//   },
// })
