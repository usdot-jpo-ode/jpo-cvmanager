import { useEffect, useState } from 'react'
import AdminTable from '../../components/AdminTable'
import Accordion from '@mui/material/Accordion'
import AccordionSummary from '@mui/material/AccordionSummary'
import AccordionDetails from '@mui/material/AccordionDetails'
import Typography from '@mui/material/Typography'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import { confirmAlert } from 'react-confirm-alert'
import { Options } from '../../components/AdminDeletionOptions'
import {
  AdminOrgIntersection,
  adminOrgPatch,
  editOrg,
} from '../adminOrganizationTab/adminOrganizationTabSlice'
import {
  ADMIN_INTERSECTION_AVAILABLE_LIST_ID,
  ADMIN_INTERSECTION_LIST_ID,
  ADMIN_INTERSECTION_TAG,
  adminIntersectionApiSlice,
  useGetIntersectionsNotInOrganizationQuery,
  useLazyGetIntersectionQuery,
} from '../api/adminIntersectionApiSlice'
import { selectLoadingGlobal } from '../../generalSlices/userSlice'
import { useSelector, useDispatch } from 'react-redux'

import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../store'
import { Action, Column } from '@material-table/core'
import toast from 'react-hot-toast'
import { useTheme } from '@mui/material'
import { AddCircleOutline, DeleteOutline } from '@mui/icons-material'
import { Multiselect } from 'react-widgets/cjs'
import '../css/multiselect.css'
import { AdminIntersection } from '../../models/Intersection'

interface AdminOrganizationTabIntersectionProps {
  selectedOrg: string
  selectedOrgEmail: string
  tableData: AdminOrgIntersection[]
  updateTableData: (orgname: string) => void
}

const AdminOrganizationTabIntersection = (props: AdminOrganizationTabIntersectionProps) => {
  const { selectedOrg, selectedOrgEmail, updateTableData } = props
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const theme = useTheme()
  const [fetchIntersection] = useLazyGetIntersectionQuery()

  const { data: availableIntersectionsResponse } = useGetIntersectionsNotInOrganizationQuery(selectedOrg, {
    skip: !selectedOrg,
  })
  const availableIntersectionList = availableIntersectionsResponse?.intersection_data ?? []

  const [selectedIntersectionList, setSelectedIntersectionList] = useState<AdminIntersection[]>([])

  useEffect(() => {
    setSelectedIntersectionList([])
  }, [selectedOrg])

  const loadingGlobal = useSelector(selectLoadingGlobal)
  const [intersectionColumns] = useState<Column<any>[]>([
    { title: 'ID', field: 'intersection_id', id: 0, width: '45%' },
    { title: 'Name', field: 'intersection_name', id: 1, width: '45%' },
  ])

  const refreshTable = () => {
    updateTableData(selectedOrg)
    dispatch(
      adminIntersectionApiSlice.util.invalidateTags([
        { type: ADMIN_INTERSECTION_TAG, id: ADMIN_INTERSECTION_LIST_ID },
        { type: ADMIN_INTERSECTION_TAG, id: ADMIN_INTERSECTION_AVAILABLE_LIST_ID },
      ])
    )
  }

  const intersectionActions: Action<AdminOrgIntersection>[] = [
    {
      icon: () => <DeleteOutline sx={{ color: theme.palette.custom.rowActionIcon }} />,
      iconProps: {
        itemType: 'rowAction',
      },
      position: 'row',
      onClick: (event, rowData: AdminOrgIntersection) => {
        const buttons = [
          { label: 'Yes', onClick: () => intersectionOnDelete(rowData) },
          { label: 'No', onClick: () => {} },
        ]
        const alertOptions = Options(
          'Delete Intersection',
          'Are you sure you want to delete "' + rowData.intersection_id + '" from ' + selectedOrg + ' organization?',
          buttons
        )
        confirmAlert(alertOptions)
      },
    },
    {
      tooltip: 'Remove All Selected From Organization',
      icon: 'delete',
      position: 'toolbarOnSelect',
      iconProps: {
        itemType: 'rowAction',
      },
      onClick: (event, rowData: AdminOrgIntersection[]) => {
        const buttons = [
          { label: 'Yes', onClick: () => intersectionMultiDelete(rowData) },
          { label: 'No', onClick: () => {} },
        ]
        const alertOptions = Options(
          'Delete Selected Intersections',
          'Are you sure you want to delete ' + rowData.length + ' Intersections from ' + selectedOrg + ' organization?',
          buttons
        )
        confirmAlert(alertOptions)
      },
    },
    {
      position: 'toolbar',
      iconProps: {
        itemType: 'displayIcon',
      },
      icon: () => (
        <Multiselect
          dataKey="intersection_id"
          textField="intersection_name"
          placeholder="Click to add Intersections"
          data={availableIntersectionList}
          value={selectedIntersectionList}
          onChange={(value) => {
            setSelectedIntersectionList(value as AdminIntersection[])
          }}
          style={{
            fontSize: '1rem',
          }}
        />
      ),
      onClick: () => {},
    },
    {
      position: 'toolbar',
      iconProps: {
        title: 'Add Intersection',
        color: 'primary',
        itemType: 'contained',
      },
      icon: () => <AddCircleOutline />,
      onClick: () => intersectionMultiAdd(selectedIntersectionList),
    },
  ]

  const intersectionOnDelete = async (intersection: AdminOrgIntersection) => {
    const result = await fetchIntersection(intersection.intersection_id).unwrap()

    if (result?.intersection_data?.organizations?.length > 1) {
      const patchJson: adminOrgPatch = {
        name: selectedOrg,
        email: selectedOrgEmail,
        intersections_to_remove: [intersection.intersection_id],
      }
      const res = await dispatch(editOrg(patchJson))
      refreshTable()
      if ((res.payload as any).success) {
        toast.success('Intersection deleted successfully')
      } else {
        toast.error('Failed to delete Intersection')
      }
    } else {
      alert(
        'Cannot remove Intersection ' +
          intersection.intersection_id +
          ' from ' +
          selectedOrg +
          ' because it must belong to at least one organization.'
      )
    }
  }

  const intersectionMultiAdd = async (intersectionList: AdminIntersection[]) => {
    if (intersectionList.length === 0) {
      toast.error('Please select Intersections to add')
      return
    }
    const patchJson: adminOrgPatch = {
      name: selectedOrg,
      email: selectedOrgEmail,
      intersections_to_add: intersectionList.map((intersection) => intersection.intersection_id),
    }
    const res = await dispatch(editOrg(patchJson))
    setSelectedIntersectionList([])
    refreshTable()
    if ((res.payload as any).success) {
      toast.success('Intersection(s) added successfully')
    } else {
      toast.error('Failed to add Intersection(s)')
    }
  }

  const intersectionMultiDelete = async (rows: AdminOrgIntersection[]) => {
    const invalidIntersections: string[] = []
    const patchJson: adminOrgPatch = {
      name: selectedOrg,
      email: selectedOrgEmail,
      intersections_to_remove: [],
    }
    for (const row of rows) {
      const result = await fetchIntersection(row.intersection_id).unwrap()
      if (result?.intersection_data?.organizations?.length > 1) {
        patchJson.intersections_to_remove!.push(row.intersection_id)
      } else {
        invalidIntersections.push(row.intersection_id)
      }
    }
    if (invalidIntersections.length === 0) {
      const res = await dispatch(editOrg(patchJson))
      refreshTable()
      if ((res.payload as any).success) {
        toast.success('Intersection(s) deleted successfully')
      } else {
        toast.error('Failed to delete Intersection(s)')
      }
    } else {
      alert(
        'Cannot remove Intersection(s) ' +
          invalidIntersections.join(', ') +
          ' from ' +
          selectedOrg +
          ' because they must belong to at least one organization.'
      )
    }
  }

  return (
    <div className="accordion">
      <Accordion className="accordion-content" elevation={0}>
        <AccordionSummary expandIcon={<ExpandMoreIcon />} aria-controls="panel1a-content" id="panel1a-header">
          <Typography variant="h6">Intersections</Typography>
        </AccordionSummary>
        <AccordionDetails sx={{ padding: '8px 0px' }}>
          {loadingGlobal === false && [
            <div key="adminTable">
              <AdminTable
                title={''}
                data={props.tableData}
                columns={intersectionColumns}
                actions={intersectionActions}
              />
            </div>,
          ]}
        </AccordionDetails>
      </Accordion>
    </div>
  )
}

export default AdminOrganizationTabIntersection
