import { createTheme } from '@mui/material'

declare module '@mui/material/styles' {
  interface TypeText {
    hint: string // TODO: Make sure this does something
  }

  interface SimplePaletteColorOptions {
    contrastTextColor: string // TODO: Make sure this does something
  }

  interface ThemeOptions {
    input?: {
      color: string // TODO: Make sure this does something
    }
  }
}

// Global Theme
export const theme = createTheme({
  palette: {
    mode: 'dark',
    common: {
      black: '#000000',
      white: '#ffffff',
    },
    primary: {
      main: '#d16d15',
      light: '#0e2052',
      contrastTextColor: '#0e2052',
    },
    secondary: {
      main: '#d16d15',
      light: '#0e2052',
      contrastTextColor: '#0e2052',
    },
    text: {
      primary: '#ffffff',
      secondary: '#d16d15',
      disabled: '#acacac',
      hint: '#0e2052',
    },
    divider: '#333',
    background: {
      paper: '#333',
      default: '#1c1d1f',
    },
  },
  typography: {
    allVariants: {
      color: '#fff',
    },
  },
})

// used by AdminTable.tsx
export const tableTheme = createTheme({
  palette: {
    primary: {
      main: '#ffffff',
      light: '#0e2052',
      contrastTextColor: '#0e2052',
    },
    secondary: {
      main: '#333333',
      light: '#0e2052',
      contrastTextColor: '#0e2052',
    },
    text: {
      primary: '#ffffff',
      secondary: '#ffffff',
      disabled: '#ffffff',
      hint: '#ffffff',
    },
    divider: '#333',
    background: {
      paper: '#333',
      default: '#1c1d1f',
    },
  },
  components: {
    MuiPaper: {
      styleOverrides: {
        root: {
          border: '1.5px solid #ffffff',
        },
      },
    },
    MuiIcon: {
      styleOverrides: {
        root: {
          color: '#333333',
          backgroundColor: '#dadde5',
          borderRadius: '50%',
          padding: 2,
        },
      },
    },
    MuiInputBase: {
      styleOverrides: {
        root: {
          backgroundColor: '#dadde5',
          color: '#333333',
        },
      },
    },
    MuiToolbar: {
      styleOverrides: {
        root: {
          color: '#333333',
          backgroundColor: '#dadde5',
        },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        footer: {
          backgroundColor: '#dadde5',
        },
      },
    },
  },
})

// used by SnmpwalkMenu.tsx
export const snmpWalkMenuTheme = createTheme({
  palette: {
    primary: {
      main: '#d16d15',
      light: '#0e2052',
      contrastTextColor: '#0e2052',
    },
    secondary: {
      main: '#d16d15',
      light: '#0e2052',
      contrastTextColor: '#0e2052',
    },
    text: {
      primary: '#ffffff',
      secondary: '#ffffff',
      disabled: '#ffffff',
      hint: '#ffffff',
    },
    action: {
      disabledBackground: 'rgba(209, 109, 21, 0.2)',
      disabled: '#ffffff',
    },
  },
  typography: {
    button: {
      textTransform: 'none',
    },
  },
})

// Used by Map.tsx
export const mapTheme = createTheme({
  palette: {
    primary: {
      main: '#d16d15',
      light: '#0e2052',
      contrastTextColor: '#0e2052',
    },
    secondary: {
      main: '#d16d15',
      light: '#0e2052',
      contrastTextColor: '#0e2052',
    },
    text: {
      primary: '#ffffff',
      secondary: '#ffffff',
      disabled: '#ffffff',
      hint: '#ffffff',
    },
    action: {
      disabledBackground: 'rgba(209, 109, 21, 0.2)',
      disabled: '#ffffff',
    },
  },
  components: {
    MuiSvgIcon: {
      styleOverrides: {
        root: {
          color: '#d16d15',
        },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: {
          fontSize: '1rem',
          borderRadius: 15,
        },
      },
    },
  },
  input: {
    color: '#11ff00',
  },
  typography: {
    allVariants: {
      color: '#ffffff',
    },
    button: {
      textTransform: 'none',
    },
  },
})

// Used by AdminOrganizationTabRsu.tsx, AdminOrganizationTabUser.tsx, and ConfigureRSU.tsx
export const accordionTheme = createTheme({
  palette: {
    text: {
      primary: '#fff',
      secondary: '#fff',
      disabled: '#fff',
      hint: '#fff',
    },
    divider: '#333',
    background: {
      paper: '#333',
    },
  },
})

// Used by AdminOrganizationTabRsu.tsx, AdminOrganizationTabUser.tsx
export const outerAccordionTheme = createTheme({
  palette: {
    text: {
      primary: '#fff',
      secondary: '#fff',
      disabled: '#fff',
      hint: '#fff',
    },
    divider: '#333',
    background: {
      paper: '#0e2052',
    },
  },
})
