import { useState } from 'react'
import {
  Alert,
  Box,
  Button,
  Checkbox,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  FormControlLabel,
  IconButton,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
  Tooltip,
} from '@mui/material'
import AddCircleOutline from '@mui/icons-material/AddCircleOutline'
import DeleteOutline from '@mui/icons-material/DeleteOutline'
import toast from 'react-hot-toast'
import { SideBarHeader } from '../../styles/components/SideBarHeader'
import { FirmwareRule } from '../../models/Firmware'
import {
  useAssignFirmwareRulesMutation,
  useDeleteFirmwareRuleMutation,
  useGetFirmwareRuleOptionsQuery,
} from '../api/firmwareApiSlice'

const errorMessage = (error: unknown) => {
  const response = error as { data?: { detail?: string; message?: string } }
  return response?.data?.detail || response?.data?.message || 'Could not save upgrade rules. Please retry.'
}

const requiresImpactAcknowledgment = (error: unknown) => {
  const response = error as { status?: number; data?: { code?: string } }
  return response.status === 409 && response.data?.code === 'UPGRADE_IMPACT_ACKNOWLEDGMENT_REQUIRED'
}

type FirmwareRulesDialogProps = {
  firmwareId: number
  onClose: () => void
  onChanged: () => void
}

const FirmwareRulesDialog = ({ firmwareId, onClose, onChanged }: FirmwareRulesDialogProps) => {
  const {
    currentData: data,
    isFetching,
    isError,
    refetch,
  } = useGetFirmwareRuleOptionsQuery(firmwareId, {
    refetchOnMountOrArgChange: true,
  })
  const [assign] = useAssignFirmwareRulesMutation()
  const [remove] = useDeleteFirmwareRuleMutation()
  const [editingRules, setEditingRules] = useState(false)
  const [selected, setSelected] = useState<number[]>([])
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [confirmReplace, setConfirmReplace] = useState(false)
  const [removeRule, setRemoveRule] = useState<FirmwareRule>()
  const [impact, setImpact] = useState<{ operation: 'save' | 'delete'; message: string }>()
  const currentRules = new Map(data?.rules.map((rule) => [rule.source.firmware_id, rule]))
  const incoming = data?.rules.filter((rule) => rule.destination.firmware_id === firmwareId) ?? []
  const outgoing = data?.rules.filter((rule) => rule.source.firmware_id === firmwareId) ?? []
  const replacements = selected
    .map((id) => currentRules.get(id))
    .filter((rule): rule is FirmwareRule => Boolean(rule && rule.destination.firmware_id !== firmwareId))

  const closeDialog = () => {
    if (!busy) onClose()
  }

  const reload = () => {
    setError('')
    setSelected([])
    setConfirmReplace(false)
    setRemoveRule(undefined)
    setImpact(undefined)
    void refetch()
  }

  const save = async (acknowledgeUpgradeImpact = false) => {
    if (!data || busy) return
    setBusy(true)
    setError('')
    try {
      // Send the destinations displayed in this editor so a concurrent change
      // produces a conflict instead of silently replacing somebody else's rule.
      await assign({
        destinationId: firmwareId,
        ...(acknowledgeUpgradeImpact && { acknowledgeUpgradeImpact: true }),
        sources: selected.map((id) => ({
          source_id: id,
          expected_target_id: currentRules.get(id)?.destination.firmware_id ?? null,
        })),
      }).unwrap()
      setEditingRules(false)
      toast.success('Upgrade rules saved')
      reload()
      onChanged()
    } catch (error_) {
      if (requiresImpactAcknowledgment(error_)) {
        setImpact({ operation: 'save', message: errorMessage(error_) })
      } else {
        setImpact(undefined)
        setError(errorMessage(error_))
      }
    } finally {
      setBusy(false)
      setConfirmReplace(false)
    }
  }

  const deleteRule = async (acknowledgeUpgradeImpact = false) => {
    if (!removeRule || busy) return
    setBusy(true)
    setError('')
    try {
      await remove({ ruleId: removeRule.rule_id, expectedTargetId: removeRule.destination.firmware_id,
        ...(acknowledgeUpgradeImpact && { acknowledgeUpgradeImpact: true }) }).unwrap()
      toast.success('Upgrade rule deleted')
      reload()
      onChanged()
    } catch (error_) {
      if (requiresImpactAcknowledgment(error_)) {
        setImpact({ operation: 'delete', message: errorMessage(error_) })
      } else {
        setImpact(undefined)
        setRemoveRule(undefined)
        setError(errorMessage(error_))
      }
    } finally {
      setBusy(false)
    }
  }

  const ruleEditor = data?.can_target && editingRules && (
    <Box id="firmware-rule-editor" sx={{ mt: 2 }}>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
        Select source versions to upgrade to <Box component="span" sx={{ fontWeight: 'bold' }}>{data.destination.version}</Box>.
        {' Existing targets will be replaced when saved. Unselected rules are preserved.'}
        {selected.length > 0 && ' Save or cancel your selections before deleting a rule.'}
      </Typography>
      <Box
        role="group"
        aria-label="Source firmware versions"
        tabIndex={0}
        sx={{
          maxHeight: 320,
          overflowY: 'auto',
          border: 1,
          borderColor: 'divider',
          borderRadius: 1,
          bgcolor: 'action.hover',
          p: 2,
        }}
      >
        {data.sources.length === 0 && (
          <Typography>No other firmware versions are registered for this model.</Typography>
        )}
        {data.sources.map((source) => {
          const rule = currentRules.get(source.firmware_id)
          const alreadyAssigned = rule?.destination.firmware_id === firmwareId
          return (
            <FormControlLabel
              key={source.firmware_id}
              sx={{ display: 'flex' }}
              control={
                <Checkbox
                  disabled={busy || alreadyAssigned || !!removeRule || !!impact}
                  checked={alreadyAssigned || selected.includes(source.firmware_id)}
                  onChange={(_, checked) => {
                    setConfirmReplace(false)
                    setSelected((ids) =>
                      checked ? [...ids, source.firmware_id] : ids.filter((id) => id !== source.firmware_id)
                    )
                  }}
                />
              }
              label={
                <>
                  <Box component="span" sx={{ fontWeight: 'bold' }}>{source.version}</Box>
                  {source.legacy && ' (Legacy)'}
                  <Box component="span" sx={{ color: busy || alreadyAssigned || !!removeRule ? 'text.disabled' : 'text.secondary' }}>
                    {' · '}{rule ? `Target: ${rule.destination.version}` : 'No target'}
                  </Box>
                </>
              }
            />
          )
        })}
      </Box>
      {confirmReplace && !impact && (
        <Alert severity="warning" sx={{ mt: 2, alignItems: 'center' }}>
          Redirect {replacements.length} existing {replacements.length === 1 ? 'rule' : 'rules'} to {data.destination.version}?
          <Button onClick={() => void save()} disabled={busy}>
            Confirm
          </Button>
          <Button onClick={() => setConfirmReplace(false)} disabled={busy}>
            Cancel
          </Button>
        </Alert>
      )}
      {impact?.operation === 'save' && (
        <Alert severity="warning" sx={{ mt: 2, alignItems: 'center' }}>
          {impact.message}
          <Button disabled={busy} onClick={() => void save(true)}>Confirm</Button>
          <Button disabled={busy} onClick={() => setImpact(undefined)}>Cancel</Button>
        </Alert>
      )}
      <Stack direction="row" spacing={1} justifyContent="flex-end" sx={{ mt: 2 }}>
        <Button disabled={busy} onClick={() => {
          setEditingRules(false)
          setSelected([])
          setConfirmReplace(false)
          setError('')
          setImpact(undefined)
        }}>Cancel</Button>
        <Button
          variant="contained"
          disabled={
            busy || isFetching || isError || !data?.can_target || !selected.length || confirmReplace || !!removeRule || !!impact
          }
          onClick={() => (replacements.length ? setConfirmReplace(true) : void save())}
        >
          Save Rules
        </Button>
      </Stack>
    </Box>
  )

  return (
    <Dialog open onClose={closeDialog} maxWidth="md" fullWidth>
      <DialogContent>
        <SideBarHeader title="Firmware Upgrade Rules" onClick={closeDialog} />
        {isFetching && <CircularProgress aria-label="Loading upgrade rules" size={24} />}
        {isError && (
          <Alert severity="error">Could not load upgrade rules. Close and reopen this dialog to try again.</Alert>
        )}
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}
        {data && !isFetching && !isError && (
          <Stack spacing={2}>
            <Typography variant="h6">
              {data.destination.manufacturer} / {data.destination.model} / {data.destination.version}
            </Typography>
            {[
              {
                incoming: true,
                title: 'Upgrades to this firmware',
                description: `Versions that can upgrade to ${data.destination.version}.`,
                rules: incoming,
                empty: 'No versions have a rule to this firmware.',
              },
              {
                incoming: false,
                title: 'Upgrade from this firmware',
                description: `The target version that ${data.destination.version} can upgrade to. Only one target is allowed.`,
                rules: outgoing,
                empty: 'This firmware has no upgrade target.',
              },
            ].map((section) => (
              <Box key={section.title} component="section" aria-label={section.title}>
                <Box sx={{ border: 1, borderColor: 'divider', borderRadius: 1, bgcolor: 'action.hover', overflow: 'hidden' }}>
                  <Box sx={{ p: 2 }}>
                    <Stack direction="row" alignItems="center" justifyContent="space-between" spacing={2}>
                      <Typography variant="subtitle1" fontWeight="bold">{section.title}</Typography>
                      {section.incoming && data.can_target && (
                        <Button variant="contained" size="small" startIcon={<AddCircleOutline />}
                          sx={{ padding: '6px 8px', margin: '0px 4px' }}
                          disabled={editingRules || busy || !!removeRule} aria-expanded={editingRules}
                          aria-controls={editingRules ? 'firmware-rule-editor' : undefined}
                          onClick={() => setEditingRules(true)}>
                          <Typography className="capital-case museo-slab" fontSize="12px">New</Typography>
                        </Button>
                      )}
                    </Stack>
                    <Typography variant="body2" color="text.secondary">
                      {section.description}
                    </Typography>
                  </Box>
                  {section.rules.length === 0 ? (
                    <Typography sx={{ p: 2 }}>{section.empty}</Typography>
                  ) : (
                    <TableContainer>
                      <Table
                        aria-label={section.title}
                        size="small"
                        sx={{
                          tableLayout: 'fixed',
                          '& th': { textTransform: 'none !important', bgcolor: 'custom.tableHeaderBackground' },
                          '& td': { overflowWrap: 'anywhere' },
                        }}
                      >
                        <colgroup>
                          <col style={{ width: '40%' }} />
                          <col style={{ width: '40%' }} />
                          <col style={{ width: '20%' }} />
                        </colgroup>
                        <TableHead>
                          <TableRow>
                            <TableCell>From Version</TableCell>
                            <TableCell>To Version</TableCell>
                            <TableCell>Actions</TableCell>
                          </TableRow>
                        </TableHead>
                        <TableBody>
                          {section.rules.map((rule) => (
                            <TableRow key={rule.rule_id}>
                              <TableCell>{rule.source.version}</TableCell>
                              <TableCell>
                                {rule.destination.version}
                                {rule.legacy_destination && ' (Legacy rule)'}
                              </TableCell>
                              <TableCell>
                                <Tooltip title="Delete Rule">
                                  <span>
                                    <IconButton aria-label="Delete Rule" disabled={busy || selected.length > 0} onClick={() => setRemoveRule(rule)}
                                      sx={{ color: 'custom.rowActionIcon', borderRadius: 1 }}>
                                      <DeleteOutline />
                                    </IconButton>
                                  </span>
                                </Tooltip>
                              </TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    </TableContainer>
                  )}
                </Box>
                {removeRule && section.rules.some(rule => rule.rule_id === removeRule.rule_id) && (
                  <Alert severity="warning" sx={{ mt: 2, alignItems: 'center' }}>
                    {impact?.operation === 'delete' ? impact.message
                      : `Delete rule ${removeRule.source.version} → ${removeRule.destination.version}? Firmware files will remain available.`}
                    <Button onClick={() => void deleteRule(impact?.operation === 'delete')} disabled={busy}>
                      Confirm
                    </Button>
                    <Button onClick={() => { setRemoveRule(undefined); setImpact(undefined) }} disabled={busy}>
                      Cancel
                    </Button>
                  </Alert>
                )}
                {section.incoming && ruleEditor}
              </Box>
            ))}
            {data.eligibility_error && <Alert severity="warning">{data.eligibility_error}</Alert>}
            {!data.can_target && !data.eligibility_error && (
              <Alert severity="info">
                New rules must target a verified file that is present and unchanged. Existing legacy rules remain
                operational. To replace an existing destination, open Upgrade Rules on a verified firmware version for
                this model.
              </Alert>
            )}

          </Stack>
        )}
      </DialogContent>
      <DialogActions>
        <Button disabled={busy} onClick={onClose}>
          Close
        </Button>

      </DialogActions>
    </Dialog>
  )
}

export default FirmwareRulesDialog
