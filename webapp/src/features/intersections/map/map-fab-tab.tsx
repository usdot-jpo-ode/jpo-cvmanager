import { Box, Fab, IconButton, Paper, Typography, useTheme } from '@mui/material'
import { Close } from '@mui/icons-material'

type MapFabTabProps = {
  title: string
  width: string | number
  right: any
  panelId: string
  openPanel: string
  setOpenPanel: (panel: string) => void
  icon: JSX.Element
  content: JSX.Element
}

function MapFabTab(props: MapFabTabProps) {
  const theme = useTheme()

  const toggleOpen = () => {
    if (props.openPanel === props.panelId) {
      props.setOpenPanel('')
    } else {
      props.setOpenPanel(props.panelId)
    }
  }

  return (
    <>
      <Fab
        size="small"
        onClick={() => {
          toggleOpen()
        }}
        sx={{
          position: 'absolute',
          zIndex: 10,
          top: theme.spacing(3),
          right: props.right,
          backgroundColor: theme.palette.background.paper,
          '&:hover': {
            backgroundColor: theme.palette.custom.intersectionMapButtonHover,
          },
        }}
      >
        {props.icon}
      </Fab>
      <div
        style={{
          position: 'absolute',
          zIndex: 10,
          bottom: theme.spacing(3),
          maxHeight: 'calc(100vh - 240px)',
          right: 0,
          width: props.openPanel === props.panelId ? props.width : 0,
          fontSize: '16px',
          overflow: 'auto',
          scrollBehavior: 'auto',
          borderRadius: '4px',
        }}
      >
        <Box style={{ position: 'relative', height: '100%', width: '100%' }}>
          <Paper sx={{ height: '100%', width: '100%', px: 2, pb: 2 }} square>
            <Box>
              {props.openPanel !== props.panelId ? null : (
                <>
                  <Box
                    sx={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      padding: '8px 16px',
                    }}
                  >
                    <Typography fontSize="16px">{props.title}</Typography>
                    <IconButton
                      onClick={() => {
                        toggleOpen()
                      }}
                    >
                      <Close color="info" />
                    </IconButton>
                  </Box>
                  {props.content}
                </>
              )}
            </Box>
          </Paper>
        </Box>
      </div>
    </>
  )
}

export default MapFabTab
