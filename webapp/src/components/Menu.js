import React from "react";
import "./css/Menu.css";
import { DateTimePickerComponent } from "@syncfusion/ej2-react-calendars";
import BounceLoader from "react-spinners/BounceLoader";
import Select from "react-select";
import { useEffect, useState } from "react";
import { MessageTypes } from "../constants/messageTypes";
import { useSelector, useDispatch } from "react-redux";
import {
  selectRequestOut,
  selectMsgType,
  selectCountList,
  selectStartDate,
  selectEndDate,
  selectWarningMessage,
  selectMessageLoading,
  updateRowData,
  updateMessageType,
} from "../slices/rsuSlice";

const { DateTime } = require("luxon");

const menuStyle = {
  background: "#0e2052",
  textAlign: "left",
  position: "absolute",
  zIndex: "90",
  height: "calc(100vh - 135px)", // : "calc(100vh - 100px)",
  width: "350px",
  top: "135px", // : "100px",
  right: "0%",
  overflow: "auto",
};

const Menu = () => {
  const dispatch = useDispatch();
  const requestOut = useSelector(selectRequestOut);
  const msgType = useSelector(selectMsgType);
  const startDate = useSelector(selectStartDate);
  const endDate = useSelector(selectEndDate);
  const warning = useSelector(selectWarningMessage);
  const messageLoading = useSelector(selectMessageLoading);
  const countList = useSelector(selectCountList);

  const [previousRequest, setPreviousRequest] = useState(null);
  const [display, setDisplay] = useState(true);
  const [currentSort, setCurrentSort] = useState(null);
  const [sortedCountList, setSortedCountList] = useState(countList);
  const messageTypeOptions = MessageTypes.map((type) => {
    return { value: type, label: type };
  });

  useEffect(() => {
    setSortedCountList(countList);
  }, [countList]);

  const compareBy = (key) => {
    // Support both descending and ascending sort
    // based on the current sort
    // Default is ascending
    if (key === currentSort) {
      setCurrentSort(key + "desc");
      return function (a, b) {
        if (a[key] > b[key]) return -1;
        if (a[key] < b[key]) return 1;
        return 0;
      };
    } else {
      setCurrentSort(key);
      return function (a, b) {
        if (a[key] < b[key]) return -1;
        if (a[key] > b[key]) return 1;
        return 0;
      };
    }
  };

  const sortBy = (key) => {
    let arrayCopy = [...countList];
    arrayCopy.sort(compareBy(key));
    setSortedCountList(arrayCopy);
  };

  const dateChanged = (e, type) => {
    let tmp = new Date(e.value.toString().substring(0, 24));
    let mst = DateTime.fromISO(tmp.toISOString());
    mst.setZone("America/Denver");
    let data;
    if (type === "start") {
      data = { start: mst.toString() };
    } else {
      data = { end: mst.toString() };
    }
    updateRowDataLocal(data);
  };

  const updateRowDataLocal = (data) => {
    if (requestOut) {
      previousRequest.abort();
      setPreviousRequest(null);
    }
    dispatch(updateRowData(data));
  };

  const handleClick = () => {
    setDisplay(!display);
  };

  const formatRows = (rows) => rows.map((rowData) => <Row {...rowData} />);

  const getWarningMessage = (warning) =>
    warning ? (
      <span className="warningMessage">
        <p>
          Warning: time ranges greater than 24 hours may have longer load times.
        </p>
      </span>
    ) : (
      <span></span>
    );

  const getTable = (messageLoading, sortedCountList) =>
    messageLoading ? (
      <div>
        <div className="table">
          <div className="header">
            <div>RSU</div>
            <div>Road</div>
            <div>Count</div>
          </div>
        </div>
        <span className="bounceLoader">
          <BounceLoader loading={true} color={"#ffffff"}></BounceLoader>
        </span>
      </div>
    ) : (
      <div className="table">
        <div className="header">
          <div onClick={() => sortBy("rsu")}>RSU</div>
          <div onClick={() => sortBy("road")}>Road</div>
          <div onClick={() => sortBy("count")}>Count</div>
        </div>
        <div className="body">{formatRows(sortedCountList)}</div>
      </div>
    );

  return (
    <div>
      {display ? (
        <div style={menuStyle} id="sideBarBlock" className="visibleProp">
          <button id="toggle" onClick={handleClick}>
            X
          </button>
          <div id="container" className="sideBarOn">
            <h1 className="h1">{msgType} Counts</h1>
            <div className="DateRangeContainer">
              <DateTimePickerComponent
                placeholder="Select start date"
                value={startDate}
                max={endDate}
                step={15}
                onChange={(e) => {
                  dateChanged(e, "start");
                }}
              />
              <DateTimePickerComponent
                placeholder="Select end date"
                value={endDate}
                min={startDate}
                max={new Date()}
                step={15}
                onChange={(e) => {
                  dateChanged(e, "end");
                }}
              />
            </div>
            <Select
              options={messageTypeOptions}
              defaultValue={messageTypeOptions.filter(
                (o) => o.label === msgType
              )}
              placeholder="Select Message Type"
              className="selectContainer"
              onChange={(value) => dispatch(updateMessageType(value.value))}
            />
            {getWarningMessage(warning)}
            {getTable(messageLoading, sortedCountList)}
          </div>
        </div>
      ) : (
        <button id="toggle" onClick={handleClick}>
          Display Counts
        </button>
      )}
    </div>
  );
};

const Row = ({ rsu, road, count }) => (
  <div className="row">
    <div>{rsu}</div>
    <div>{road}</div>
    <div>{count}</div>
  </div>
);

export default Menu;
