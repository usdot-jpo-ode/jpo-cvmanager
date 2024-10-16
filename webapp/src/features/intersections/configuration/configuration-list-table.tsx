import PerfectScrollbar from 'react-perfect-scrollbar'
import PropTypes from 'prop-types'
import {
  Box,
  Card,
  IconButton,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TablePagination,
  TableRow,
  Chip,
  Typography,
} from '@mui/material'
import React from 'react'
import { PencilAlt as PencilAltIcon } from '../../../icons/pencil-alt'
import CancelIcon from '@mui/icons-material/Cancel'
import AddIcon from '@mui/icons-material/Add'
import { useNavigate } from 'react-router-dom'

export const ConfigParamListTable = (props) => {
  const { intersectionId, parameters, parametersCount, onPageChange, onRowsPerPageChange, page, rowsPerPage } = props
  const navigate = useNavigate()
  console.log(parameters)

  const readOnlyRow = (param) => {
    return (
      <TableRow hover key={param.id}>
        <TableCell>{param.key}</TableCell>
        <TableCell>{param.value.toString()}</TableCell>
        <TableCell>{param.units?.toString()}</TableCell>
        <TableCell>{param.description}</TableCell>
        <TableCell align="right"></TableCell>
      </TableRow>
    )
  }

  const generalDefaultRow = (param) => {
    return (
      <TableRow hover key={param.id}>
        <TableCell>{param.key}</TableCell>
        <TableCell>{param.value?.toString()}</TableCell>
        <TableCell>{param.units?.toString()}</TableCell>
        <TableCell>{param.description}</TableCell>
        <TableCell align="right">
          <IconButton component="a" onClick={() => navigate(`${param.key}/edit`)}>
            <PencilAltIcon fontSize="small" />
          </IconButton>
        </TableCell>
      </TableRow>
    )
  }

  const generalIntersectionRow = (param) => {
    return (
      <TableRow hover key={param.id}>
        <TableCell>{param.key}</TableCell>
        <TableCell>{param.value.toString()}</TableCell>
        <TableCell>{param.units?.toString()}</TableCell>
        <TableCell>{param.description}</TableCell>
        <TableCell align="right">
          {intersectionId != -1 ? (
            <IconButton component="a" onClick={() => navigate(`${param.key}/create`)}>
              <AddIcon fontSize="small" />
            </IconButton>
          ) : null}
          <IconButton component="a" onClick={() => navigate(`${param.key}/edit`)}>
            <PencilAltIcon fontSize="small" />
          </IconButton>
        </TableCell>
      </TableRow>
    )
  }

  const intersectionRow = (param) => {
    return (
      <TableRow hover key={param.id}>
        <TableCell>{param.key}</TableCell>
        <TableCell>
          {param.value.toString()}
          {
            <Chip
              color="secondary"
              sx={{ ml: 3 }}
              label={
                <Typography
                  sx={{
                    fontSize: '10px',
                    fontWeight: '600',
                  }}
                >
                  Overridden
                </Typography>
              }
              size="small"
            />
          }
        </TableCell>
        <TableCell>{param.unit}</TableCell>
        <TableCell>{param.description}</TableCell>
        <TableCell align="right">
          <IconButton component="a" onClick={() => navigate(`${param.key}/edit`)}>
            <PencilAltIcon fontSize="small" />
          </IconButton>
          {intersectionId != -1 ? (
            <IconButton component="a" onClick={() => navigate(`${param.key}/remove`)}>
              <CancelIcon fontSize="small" />
            </IconButton>
          ) : null}
        </TableCell>
      </TableRow>
    )
  }

  return (
    <Card>
      <Box sx={{ minWidth: 1050, overflowY: 'scroll' }}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell sx={{ minWidth: 200 }}>Name</TableCell>
              <TableCell sx={{ minWidth: 120 }}>Value</TableCell>
              <TableCell sx={{ minWidth: 90 }}>Unit</TableCell>
              <TableCell sx={{ minWidth: 300 }}>Description</TableCell>
              <TableCell align="right" sx={{ minWidth: 80 }}>
                Actions
              </TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {(parameters as Config[]).map((param) => {
              switch (param.updateType) {
                case 'READ_ONLY':
                  return readOnlyRow(param)
                case 'DEFAULT':
                  return generalDefaultRow(param)
                case 'INTERSECTION':
                  console.log('intersectionRow')
                  return 'intersectionID' in param ? intersectionRow(param) : generalIntersectionRow(param)
                default:
                  return readOnlyRow(param)
              }
            })}
          </TableBody>
        </Table>
      </Box>
      <TablePagination
        component="div"
        count={parametersCount}
        onPageChange={onPageChange}
        onRowsPerPageChange={onRowsPerPageChange}
        page={page}
        rowsPerPage={rowsPerPage}
        rowsPerPageOptions={[5, 10, 25]}
      />
    </Card>
  )
}

ConfigParamListTable.propTypes = {
  intersectionId: PropTypes.number.isRequired,
  parameters: PropTypes.array.isRequired,
  parametersCount: PropTypes.number.isRequired,
  onPageChange: PropTypes.func.isRequired,
  onRowsPerPageChange: PropTypes.func,
  page: PropTypes.number.isRequired,
  rowsPerPage: PropTypes.number.isRequired,
}
