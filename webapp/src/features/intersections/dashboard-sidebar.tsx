import React, { useEffect } from 'react'
import PropTypes from 'prop-types'
import { Box, Divider, Drawer, useMediaQuery, Theme } from '@mui/material'
import { ChartBar as ChartBarIcon } from '../../icons/chart-bar'
import { Cog as CogIcon } from '../../icons/cog'
import { Users as UsersIcon } from '../../icons/users'
import MapIcon from '@mui/icons-material/Map'
import NotificationsIcon from '@mui/icons-material/Notifications'
import ArticleIcon from '@mui/icons-material/Article'
import SubtitlesIcon from '@mui/icons-material/Subtitles'
import { Logo } from './logo'
import { DashboardSidebarSection } from './dashboard-sidebar-section'
import { useSelector } from 'react-redux'
import { selectToken } from '../../generalSlices/userSlice'
import { useNavigate, useLocation } from 'react-router-dom'
import { SecureStorageManager } from '../../managers'

const generalItems = [
  {
    path: '/',
    icon: <ChartBarIcon fontSize="small" />,
    title: 'Dashboard',
  },
  {
    path: '/notifications',
    icon: <NotificationsIcon fontSize="small" />,
    title: 'Notifications',
  },
  {
    path: '/reports',
    icon: <ArticleIcon fontSize="small" />,
    title: 'Performance Reports',
  },
  {
    path: '/map',
    icon: <MapIcon fontSize="small" />,
    title: 'Map',
  },
  {
    path: '/data-selector',
    icon: <ChartBarIcon fontSize="small" />,
    title: 'Data Selector',
  },
  {
    path: '/decoder',
    icon: <SubtitlesIcon fontSize="small" />,
    title: 'ASN.1 Decoder',
  },
  {
    path: '/configuration',
    icon: <CogIcon fontSize="small" />,
    title: 'Configuration',
  },
  {
    path: '/settings',
    icon: <CogIcon fontSize="small" />,
    title: 'Settings',
  },
]

const adminItems = [
  {
    path: '/users',
    icon: <UsersIcon fontSize="small" />,
    title: 'Users',
  },
]

const getSections = (role) =>
  role == 'admin'
    ? [
        {
          title: 'General',
          items: generalItems,
        },
        {
          title: 'Admin',
          items: adminItems,
        },
      ]
    : [
        {
          title: 'General',
          items: generalItems,
        },
      ]

export const DashboardSidebar = (props) => {
  const { open, onClose } = props
  const navigate = useNavigate()
  const location = useLocation()
  const lgUp = useMediaQuery((theme: Theme) => theme.breakpoints.up('lg'), {
    defaultMatches: true,
    noSsr: false,
  })
  const token = useSelector(selectToken)

  const content = (
    <>
      <Box
        sx={{
          display: 'flex',
          flexDirection: 'column',
          height: '100%',
        }}
      >
        <div>
          <Box sx={{ p: 3 }}>
            <a onClick={() => navigate('/')}>
              <Logo
                sx={{
                  height: 42,
                  width: 42,
                }}
              />
            </a>
          </Box>
        </div>
        <Divider
          sx={{
            borderColor: '#2D3748',
            my: 3,
          }}
        />
        <Box sx={{ flexGrow: 1 }}>
          {getSections(SecureStorageManager.getUserRole()).map((section) => (
            <DashboardSidebarSection
              key={section.title}
              path={location.pathname}
              //   sx={{
              //     mt: 2,
              //     "& + &": {
              //       mt: 2,
              //     },
              //   }}
              {...section}
            />
          ))}
        </Box>
      </Box>
    </>
  )

  if (lgUp) {
    return (
      <Drawer
        anchor="left"
        open
        PaperProps={{
          sx: {
            backgroundColor: 'neutral.900',
            color: '#FFFFFF',
            width: 280,
          },
        }}
        variant="permanent"
      >
        {content}
      </Drawer>
    )
  }

  return (
    <Drawer
      anchor="left"
      onClose={onClose}
      open={open}
      PaperProps={{
        sx: {
          backgroundColor: 'neutral.900',
          color: '#FFFFFF',
          width: 280,
        },
      }}
      sx={{ zIndex: (theme) => theme.zIndex.appBar + 100 }}
      variant="temporary"
    >
      {content}
    </Drawer>
  )
}

DashboardSidebar.propTypes = {
  onClose: PropTypes.func,
  open: PropTypes.bool,
}
