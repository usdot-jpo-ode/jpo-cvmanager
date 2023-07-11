import React from 'react'
import '../components/css/Help.css'
import popup from '../images/rsu_popup.PNG'
import status from '../images/rsu_status.PNG'
import table from '../images/rsu_count.PNG'
import menu from '../images/rsu_menu.PNG'
import heatmap from '../images/rsu_heatmap.PNG'
import configure from '../images/rsu_configure.PNG'
import { DotName } from '../constants'

const Help = () => {
  return (
    <div id="help">
      <h1 className="helpHeader">Welcome to the {DotName} CV Manager Website</h1>
      <div className="spacer">
        <p className="pHelp">
          This application shows the physical location and message counts for each RSU installed by the Colorado
          Department of Transportation at various road sites throughout Colorado.
        </p>
      </div>
      <div className="spacer">
        <p className="pHelp">
          The map on this website will represent the location of each RSU with a red, green, or yellow dot (shown
          below). A green dot represents an RSU that is online. A yellow dot represents an RSU that is offline but was
          recently active. A red dot represents an RSU that is not currently online.
        </p>
      </div>
      <img id="helpimage" src={status} alt="RSU Statuses Shown on Map" />
      <div className="spacer">
        <p className="pHelp">
          Clicking on the RSU will give the following information: IP address, online status, time last online, milepost
          number, serial number, and number of message counts (shown below).
        </p>
      </div>
      <img id="helpimage" src={popup} alt="RSU Popup with Data" />
      <div className="spacer">
        <p className="pHelp">
          The menu on the right side of the webpage will allow users to select the time range and message type to be
          displayed for the RSUs. The menu will toggle between being hidden and visible each time the red X is clicked.
          The time frame can be changed by adjusting the date and time values in the date-time pickers. The top
          date-time picker represents the start time while the bottom represents the end time. Below the date-time
          pickers is a dropdown menu where the RSU message type can be selected. Each time a value is updated in either
          the date-time pickers or message selection dropdown the webpage will update accordingly. These elements are
          shown in the image below:
        </p>
      </div>
      <img id="helpimage" src={menu} alt="RSU Menu" />
      <div className="spacer">
        <p className="pHelp">
          The table included in the menu will show the number of message counts for each RSU in the given time frame
          regardless of online status (shown below). The table can be sorted by RSU, road, or count by clicking on the
          desired column header. Each time a column header is clicked, it will toggle between sorting the data in an
          ascending/descending fashion.
        </p>
      </div>
      <img id="helpimage" src={table} alt="RSU Message Count Table" />
      <div className="spacer">
        <p className="pHelp">
          Selecting 'Heat Map' on the navigation bar will show a heat map representing RSU message counts for the time
          range and message type selected in the menu (shown below). Any update to time range or message type in the
          menu will carry over between the heat map and RSU map.
        </p>
      </div>
      <img id="helpimage" src={heatmap} alt="CV Manager Heat Map" />
      <div className="spacer">
        <p className="pHelp">
          Selecting 'Configure' on the navigation bar will allow users to perform certain actions to the RSU based on
          their role. An RSU must first be selected on the RSU Map before any options become available. Users will be
          able to perform one or more of the following: pull current message forwarding configuration, add/delete
          message forwarding configurations, and perform an RSU reboot.
        </p>
      </div>
      <img id="helpimage" src={configure} alt="CV Manager Configuration Page" />
    </div>
  )
}

export default Help
