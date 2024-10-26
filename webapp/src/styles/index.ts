import { createTheme } from '@mui/material'

// declare module '@mui/material/styles' {
//   interface SimplePaletteColorOptions {
//     lightButton?: string // Used for light colored button backgrounds
//     lightButtonHovered?: string // Used for light colored button backgrounds when hovered
//   }
// }

export const headerTabHeight = 141

const primary = '#e37120'
const lightGrey = '#cecece'
const primaryLight = '#eb8841'
const primaryDark = '#b55e12'
const secondary = '#0e2052'
const secondaryLight = '#26329f'
const secondaryDark = '#0a0f3f'
// const primary = '#fd3a3a'
// const secondary = '#3afdfd'
const mode = 'dark'

// Global Theme
export const theme = createTheme({
  palette: {
    mode,
    common: {
      black: '#000000',
      white: '#ffffff',
    },
    primary: {
      main: primary,
      light: lightGrey,
      dark: primaryDark,
      //   lightButton:
      contrastText: '#fff',
    },
    secondary: {
      main: secondary,
      light: secondaryLight,
      dark: secondaryDark,
      contrastText: '#fff',
    },
    text: {
      primary: '#ffffff',
      secondary: primaryLight,
      disabled: '#acacac',
    },
    divider: '#111',
    background: {
      paper: '#333',
      default: '#1c1d1f',
    },
  },
  //   components: {
  //     MuiIcon: {
  //       styleOverrides: {
  //         root: {
  //           color: primary,
  //         },
  //       },
  //     },
  //     MuiSvgIcon: {
  //       styleOverrides: {
  //         root: {
  //           // Match 24px = 3 * 2 + 1.125 * 16
  //           color: primary,
  //         },
  //       },
  //     },
  //     MuiTextField: {},
  //     MuiInputLabel: {
  //       styleOverrides: {
  //         // This is the global theme styling for Form.Label
  //         root: {
  //           color: 'white', // Set the color to white
  //         },
  //       },
  //     },
  //   },
  //   input: {
  //     color: '#11ff00',
  //   },
})

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
