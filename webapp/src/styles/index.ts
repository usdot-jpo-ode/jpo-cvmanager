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
      primary: '#000000',
      secondary: '#333',
      disabled: '#333',
      hint: '#000000',
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
    MuiInputLabel: {
      styleOverrides: {
        // This is the global theme styling for Form.Label
        root: {
          color: 'white', // Set the color to white
        },
      },
    },
  },
  input: {
    color: '#11ff00',
  },
})
