import React from 'react'
import '../components/css/Help.css'
import popup from '../icons/rsu_popup.PNG'
import status from '../icons/rsu_status.PNG'
import table from '../icons/rsu_count.PNG'
import menu from '../icons/rsu_menu.PNG'
import heatmap from '../icons/rsu_heatmap.PNG'
import configure from '../icons/rsu_configure.PNG'
import EnvironmentVars from '../EnvironmentVars'
import ContactSupportMenu from './ContactSupportMenu'
import { BorderedImage } from '../styles/components/BorderedImage'
import { Stack, Container } from '@mui/material'

const Help = () => {
  return (
    <Stack spacing={2}>
      <Container maxWidth="xl" id="help" sx={{ textAlign: 'left' }}>
        <h2>Welcome to the {EnvironmentVars.DOT_NAME} CV Manager Website</h2>
        <p>
          This application shows the physical location and message counts for each RSU installed by the Colorado
          Department of Transportation at various road sites throughout Colorado.
        </p>
        <p>
          The map on this website will represent the location of each RSU with a red, green, or yellow dot (shown
          below). A green dot represents an RSU that is online. A yellow dot represents an RSU that is offline but was
          recently active. A red dot represents an RSU that is not currently online.
        </p>
        <BorderedImage src={status} alt="RSU Statuses Shown on Map" />
        <p>
          Clicking on the RSU will give the following information: IP address, online status, time last online, milepost
          number, serial number, and number of message counts (shown below).
        </p>
        <BorderedImage src={popup} alt="RSU Popup with Data" />
        <p>
          The menu on the right side of the webpage will allow users to select the time range and message type to be
          displayed for the RSUs. The menu will toggle between being hidden and visible each time the red X is clicked.
          The time frame can be changed by adjusting the date and time values in the date-time pickers. The top
          date-time picker represents the start time while the bottom represents the end time. Below the date-time
          pickers is a dropdown menu where the RSU message type can be selected. Each time a value is updated in either
          the date-time pickers or message selection dropdown the webpage will update accordingly. These elements are
          shown in the image below:
        </p>
        <BorderedImage src={menu} alt="RSU Menu" />
        <p>
          The table included in the menu will show the number of message counts for each RSU in the given time frame
          regardless of online status (shown below). The table can be sorted by RSU, road, or count by clicking on the
          desired column header. Each time a column header is clicked, it will toggle between sorting the data in an
          ascending/descending fashion.
        </p>
        <BorderedImage src={table} alt="RSU Message Count Table" />
        <p>
          Selecting 'Heat Map' on the navigation bar will show a heat map representing RSU message counts for the time
          range and message type selected in the menu (shown below). Any update to time range or message type in the
          menu will carry over between the heat map and RSU map.
        </p>
        <BorderedImage src={heatmap} alt="CV Manager Heat Map" />
        <p>
          Selecting 'Configure' on the navigation bar will allow users to perform certain actions to the RSU based on
          their role. An RSU must first be selected on the RSU Map before any options become available. Users will be
          able to perform one or more of the following: pull current message forwarding configuration, add/delete
          message forwarding configurations, and perform an RSU reboot.
        </p>
        <BorderedImage src={configure} alt="CV Manager Configuration Page" />
        <ContactSupportMenu />
      </Container>
    </Stack>
  )
}

export default Help
