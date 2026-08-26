import '../components/css/Help.css'
import popup from '../icons/help/rsu_popup_and_config_menu.png'
import organizationSelection from '../icons/help/organization_selection.png'
import mapOverview from '../icons/help/map_overview.png'
import statusMenu from '../icons/help/rsu_status_menu.png'
import countMenu from '../icons/help/rsu_count_menu.png'
import table from '../icons/help/rsu_count.png'
import EnvironmentVars from '../EnvironmentVars'
import ContactSupportMenu from './ContactSupportMenu'
import { BorderedImage } from '../styles/components/BorderedImage'
import {
  Stack,
  Container,
  useTheme,
  TableContainer,
  Table,
  TableHead,
  TableRow,
  TableCell,
  Paper,
  TableBody,
} from '@mui/material'
import { Link } from 'react-router-dom'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import CancelIcon from '@mui/icons-material/Cancel'

const Help = () => {
  const theme = useTheme()

  const permissions = [
    { action: 'View Map Data (including RSUs)', user: true, operator: true, admin: true },
    { action: 'Register, Manage, and Configure RSUs', user: false, operator: true, admin: true },
    { action: 'Manage Intersection Settings and Notifications', user: false, operator: true, admin: true },
    { action: 'Manage Users', user: false, operator: false, admin: true },
    { action: 'Add and Remove Resources Within Organizations', user: false, operator: false, admin: true },
  ]

  const permissionsTable = (
    <TableContainer component={Paper} sx={{ maxWidth: 'fit-content', margin: '10px 0' }}>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell
              colSpan={4}
              sx={{
                fontSize: '1.5rem',
                fontWeight: 'bold',
                textAlign: 'center',
                border: `1px solid ${theme.palette.divider}`,
                padding: '14px',
              }}
            >
              User Roles and Permissions (By Organization)
            </TableCell>
          </TableRow>
          <TableRow>
            <TableCell sx={{ fontWeight: 'bold', border: `1px solid ${theme.palette.divider}` }}>
              {/* Empty cell for action column */}
            </TableCell>
            <TableCell sx={{ fontWeight: 'bold', textAlign: 'center', border: `1px solid ${theme.palette.divider}` }}>
              User
            </TableCell>
            <TableCell sx={{ fontWeight: 'bold', textAlign: 'center', border: `1px solid ${theme.palette.divider}` }}>
              Operator
            </TableCell>
            <TableCell sx={{ fontWeight: 'bold', textAlign: 'center', border: `1px solid ${theme.palette.divider}` }}>
              Admin
            </TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {permissions.map((row, index) => (
            <TableRow key={index}>
              <TableCell sx={{ border: `1px solid ${theme.palette.divider}`, padding: '20px' }}>{row.action}</TableCell>
              <TableCell sx={{ border: `1px solid ${theme.palette.divider}`, textAlign: 'center' }}>
                {row.user ? (
                  <CheckCircleIcon sx={{ color: theme.palette.success.light, fontSize: '1.5rem' }} />
                ) : (
                  <CancelIcon sx={{ color: theme.palette.error.light, fontSize: '1.5rem' }} />
                )}
              </TableCell>
              <TableCell sx={{ border: `1px solid ${theme.palette.divider}`, textAlign: 'center' }}>
                {row.operator ? (
                  <CheckCircleIcon sx={{ color: theme.palette.success.light, fontSize: '1.5rem' }} />
                ) : (
                  <CancelIcon sx={{ color: theme.palette.error.light, fontSize: '1.5rem' }} />
                )}
              </TableCell>
              <TableCell sx={{ border: `1px solid ${theme.palette.divider}`, textAlign: 'center' }}>
                {row.admin ? (
                  <CheckCircleIcon sx={{ color: theme.palette.success.light, fontSize: '1.5rem' }} />
                ) : (
                  <CancelIcon sx={{ color: theme.palette.error.light, fontSize: '1.5rem' }} />
                )}
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  )

  return (
    <Container maxWidth={false} id="help" sx={{ textAlign: 'left', backgroundColor: theme.palette.background.default }}>
      <Stack spacing={2}>
        <h2>{`Welcome to the ${EnvironmentVars.DOT_NAME} CV Manager`}</h2>
        <p>
          This application helps organizations manage and monitor deployed RSUs, monitor and detect issues with
          connected intersections, and view numerous data types all in one application.
        </p>

        <h2>Organizations and Profiles</h2>
        <p>
          This application uses organizations to manage permissions. All devices and users within the CV Manager are
          associated with one or more organizations. When using the CV Manager, you can only view one organization at a
          time. At the top right corner, you will see the User Profile menu. This menu allows the user to change
          organizations, if they are a member of multiple, as well as logging out of the application.
        </p>

        <BorderedImage src={organizationSelection} alt="Profile Dropdown" width="200" />

        <h2>Permissions</h2>

        <p>
          There are 3 roles within the CV Manager: User, Operator, and Admin. Some users can also be granted Super User
          permissions, which enables access to all resources within an organization and increases their ability to move
          resources between organizations.
        </p>

        {permissionsTable}

        <h3>Enabled Features</h3>
        <p>
          The CV Manager can have different feature sets enabled or disabled. In this application, the following
          features are currently enabled or disabled as shown below:
        </p>
        <ul>
          <li>{`RSU Monitoring and Configuration: ${EnvironmentVars.ENABLE_RSU_FEATURES ? 'ENABLED' : 'DISABLED'}`}</li>
          <li>{`Intersection Map/Dashboard: ${EnvironmentVars.ENABLE_INTERSECTION_FEATURES ? 'ENABLED' : 'DISABLED'}`}</li>
          <li>{`WZDx Viewer: ${EnvironmentVars.ENABLE_WZDX_FEATURES ? 'ENABLED' : 'DISABLED'}`}</li>
          <li>{`HAAS Alert Viewer: ${EnvironmentVars.ENABLE_HAAS_FEATURES ? 'ENABLED' : 'DISABLED'}`}</li>
          <li>{`RSU Status Monitor: ${EnvironmentVars.ENABLE_RSU_STATUS_MONITOR_FEATURES ? 'ENABLED' : 'DISABLED'}`}</li>
        </ul>

        <h2>
          <Link to="/dashboard/map">Map Dashboard</Link>
        </h2>

        <p>
          The map dashboard is composed of a Mapbox map (background), the
          <a href="#map-layers"> Map Layer Menu</a> (red), and
          <a href="#rsu-status-and-message-counts"> RSU Status and Message Counts Menu</a> (green).
        </p>

        <BorderedImage src={mapOverview} alt="Map Layer Selection Options" />

        <h3 id="map-layers">Map Layers</h3>

        <p>
          The menu on the left contains three sections:<strong> Map Layers</strong>,<strong> RSU Filters</strong>, and
          <strong> RSU Configuration</strong>. The Map Layers section allows users to visualize various data types.
          Available layers include:
        </p>

        <ul>
          <li>
            <strong>RSU Viewer:</strong> Displays RSU locations and status. Selecting one opens a popup and side panel
            for configuration. Colors:
            <ul>
              <li>Green = online and reporting</li>
              <li>Yellow = recently offline</li>
              <li>Red = offline for extended time</li>
            </ul>
          </li>
          <li>
            <strong>Heatmap:</strong> Displays a message-count heatmap
          </li>
          <li>
            <strong>V2X Message Viewer:</strong> Query messages for a map region
          </li>
          <li>
            <strong>WZDx Viewer:</strong> Displays work zone events
          </li>
          <li>
            <strong>Intersections:</strong> Shows connected intersections and IDs
          </li>
          <li>
            <strong>HAAS Alert Viewer:</strong> Query alert incidents by time
          </li>
        </ul>

        <p>
          The RSU Filters section allows filtering RSUs by vendor and status. The RSU Configuration section allows
          Operators/Admins to select RSUs on the map and configure them in bulk.
        </p>

        <h4 id="configuring-rsus">Configuring RSUs (Requires Operator or Admin)</h4>

        <p>
          Selecting an RSU opens a popup and configuration menu. It displays IP address, online status, last report
          time, milepost, serial number, and message count. Depending on access, users can retrieve configurations,
          modify them, check/apply firmware updates, or reboot the RSU.
        </p>

        <BorderedImage src={popup} alt="RSU Popup and Configuration Panel" />

        <h5>Message Forwarding Current Configuration</h5>
        <p>Message forwarding rules come in three types:</p>

        <ul>
          <li>TX (transmitted messages: TIM, MAP, SPAT, SSM)</li>
          <li>RX (received messages: BSM, SRM, SDSM)</li>
          <li>Generic (SNMP 4.1: applies to all messages)</li>
        </ul>

        <p>Each rule includes:</p>

        <ul>
          <li>Message Type</li>
          <li>Destination IP</li>
          <li>Port</li>
          <li>Start / End date</li>
          <li>Security header enabled</li>
          <li>Active state</li>
          <li>Delete button</li>
        </ul>

        <h5>Message Forwarding Management</h5>
        <p>Create forwarding rules by entering:</p>
        <ul>
          <li>Destination IP</li>
          <li>Message type</li>
          <li>Security header</li>
        </ul>

        <h5>Firmware Management</h5>
        <p>Check for firmware updates and apply if available.</p>

        <h5>Reboot</h5>
        <p>Reboot the selected RSU.</p>

        <h3 id="rsu-status-and-message-counts">RSU Status and Message Counts</h3>

        <p>
          On the right side are two menus:<strong> RSU Status Menu</strong> and
          <strong> Message Count Menu</strong>.
        </p>

        <p>
          The RSU Status Menu lists all RSUs and their status. Users can print a full or error-only report. For each
          device you will find:
        </p>

        <ul>
          <li>Location</li>
          <li>Online status (green / red / yellow)</li>
          <li>SCMS certificate status</li>
          <li>RSU IP address</li>
        </ul>

        <BorderedImage src={statusMenu} alt="RSU Status Menu Display" width="400" />

        <p>
          The Message Count Menu filters RSU message counts by time range and message type. Changing any filter updates
          the map and table automatically.
        </p>

        <BorderedImage src={countMenu} alt="RSU Count Menu" width="300" />

        <p>A table below shows the number of messages from each RSU, sortable by RSU name, road, or message count.</p>

        <BorderedImage src={table} alt="RSU Message Count Table" width="300" />

        <p>
          The RSU Configuration section allows applying configuration changes to multiple RSUs based on a selected
          geographic region.
        </p>
      </Stack>
      <ContactSupportMenu />
    </Container>
  )
}

export default Help
