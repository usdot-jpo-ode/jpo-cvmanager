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

export const theme = createTheme({
  palette: {
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
    MuiIcon: {
      styleOverrides: {
        root: {
          // Match 24px = 3 * 2 + 1.125 * 16
          color: '#d16d15',
        },
      },
    },
    MuiTextField: {},
  },
  input: {
    color: '#11ff00',
  },
})

export const tableTheme = createTheme({
  palette: {
    common: {
      black: '#000000',
      white: '#ffffff',
    },
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
          padding: '2px',
        },
      },
    },
    MuiInputAdornment: {
      styleOverrides: {
        positionStart: {
          color: '#333333',
        },
        positionEnd: {
          color: '#333333',
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
    MuiButtonBase: {
      styleOverrides: {
        root: {
          color: '#ffffff',
        },
      },
    },
    MuiCheckbox: {
      styleOverrides: {
        root: {
          color: '#ffffff',
        },
      },
    },
    MuiMenuItem: {
      styleOverrides: {
        root: {
          color: '#ffffff',
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
    MuiIconButton: {
      styleOverrides: {
        colorInherit: {
          color: '#333333',
        },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        footer: {
          backgroundColor: '#dadde5',
          borderBottom: 'None',
        },
      },
    },
    MuiTablePagination: {
      styleOverrides: {
        root: {
          color: '#333333',
        },
      },
    },
  },
})
