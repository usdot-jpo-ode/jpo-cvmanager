import React, { useEffect } from 'react'
import { SelectedSrm } from '../models/Srm'

import { Button, Divider, Paper, Typography } from '@mui/material'

export type SsmSrmItemProps = {
  elem: SelectedSrm
  setSelectedSrm: (elem: SelectedSrm) => void
}

const SsmSrmItem = (props: SsmSrmItemProps) => {
  const { setSelectedSrm } = props
  useEffect(() => {
    return () => {
      setSelectedSrm({} as SelectedSrm)
    }
  }, [setSelectedSrm])

  return (
    <Paper
      style={{
        display: 'flex',
        justifyContent: 'space-evenly',
        alignItems: 'center',
        textAlign: 'center',
        padding: '10px',
      }}
    >
      <Divider />
      <Typography variant="h4">{props.elem['type'] === 'srmTx' ? 'SRM' : 'SSM'}</Typography>
      <Typography>{props.elem['time']}</Typography>
      <Typography>{props.elem['requestId']}</Typography>
      <Typography>{props.elem['role']}</Typography>
      <Typography>{props.elem['status']}</Typography>
      <Button
        variant="contained"
        onClick={() => props.setSelectedSrm(props.elem)}
        disabled={props.elem['type'] !== 'srmTx'}
      >
        View
      </Button>
    </Paper>
  )
}

export default SsmSrmItem
