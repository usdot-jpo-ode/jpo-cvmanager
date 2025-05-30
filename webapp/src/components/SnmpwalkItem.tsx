import React from 'react'

import { Box, Button, Typography, useTheme } from '@mui/material'
import DeleteIcon from '@mui/icons-material/Delete'

export type SnmpwalkRowProps = {
  title: string
  value: string
}

export const SnmpWalkRow = (props: SnmpwalkRowProps) => {
  return (
    <div style={{ display: 'flex', flexDirection: 'row', alignItems: 'center', marginTop: -2 }}>
      <Typography sx={{ display: 'flex', alignItems: 'center', marginLeft: 1.5, lineHeight: 0.2 }}>
        <Typography component="span" color="textSecondary" sx={{ fontWeight: 'bold' }}>
          {props.title}
        </Typography>
        <Typography component="span" sx={{ paddingLeft: '8px' }}>
          {props.value}
        </Typography>
      </Typography>
    </div>
  )
}

export type SnmpwalkItemProps = {
  index: string
  content: any
  handleDelete: (countsMsgType: string, ip: string) => void
}

const SnmpwalkItem = (props: SnmpwalkItemProps) => {
  const theme = useTheme()

  return (
    <Box
      id="snmpitemdiv"
      sx={{
        borderRadius: 2,
        padding: 2,
        backgroundColor: theme.palette.background.paper,
        '&:hover': {
          backgroundColor: theme.palette.background.default,
        },
      }}
    >
      <h3 id="snmpitemheader">{props.index}</h3>
      <SnmpWalkRow title="Message Type:" value={props.content['Message Type']} />
      <SnmpWalkRow title="Destination IP:" value={props.content['IP']} />
      <SnmpWalkRow title="Port:" value={props.content['Port']} />
      <SnmpWalkRow title="Start:" value={props.content['Start DateTime']} />
      <SnmpWalkRow title="End:" value={props.content['End DateTime']} />
      <SnmpWalkRow title="Security:" value={props.content['Full WSMP']} />
      <SnmpWalkRow title="Active:" value={props.content['Config Active']} />

      <Button
        sx={{ marginLeft: 20, marginTop: -4 }}
        onClick={() => props.handleDelete(props.content['Message Type'], props.content['IP'])}
        startIcon={<DeleteIcon />}
      >
        Delete
      </Button>
    </Box>
  )
}

export default SnmpwalkItem
