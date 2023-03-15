import React, { useEffect, useState } from "react";
import ReactMapGL, { Marker, Popup, Source, Layer } from "react-map-gl";
import mbStyle from "../styles/mb_style.json";
import EnvironmentVars from "../EnvironmentVars";
import { useSelector, useDispatch } from "react-redux";
import { selectWzdxData, getWzdxData } from "../slices/wzdxSlice";
import { selectAuthLoginData } from "../slices/userSlice";

function WzdxMap(props) {
  const dispatch = useDispatch();

  const wzdxData = useSelector(selectWzdxData);
  const authLoginData = useSelector(selectAuthLoginData);

  const [viewport, setViewport] = useState({
    latitude: 39.7392,
    longitude: -104.9903,
    width: "100%",
    height: props.auth ? "calc(100vh - 135px)" : "calc(100vh - 100px)",
    zoom: 10,
  });

  const [selectedMarkerIndex, setSelectedMarkerIndex] = useState(null);
  const [selectedMarker, setSelectedMarker] = useState(null);
  const [wzdxMarkers, setWzdxMarkers] = useState([]);

  useEffect(() => {
    // Refresh Data
    dispatch(getWzdxData());
  }, [authLoginData, dispatch]);

  useEffect(() => {
    if (selectedMarkerIndex !== null)
      setSelectedMarker(wzdxMarkers[selectedMarkerIndex]);
    else setSelectedMarker(null);
  }, [selectedMarkerIndex, wzdxMarkers]);

  useEffect(() => {
    function createPopupTable(data) {
      let rows = [];
      for (var i = 0; i < data.length; i++) {
        let rowID = `row${i}`;
        let cell = [];
        for (var idx = 0; idx < 2; idx++) {
          let cellID = `cell${i}-${idx}`;
          cell.push(
            <td key={cellID} id={cellID}>
              <pre>{data[i][idx]}</pre>
            </td>
          );
        }
        rows.push(
          <tr key={i} id={rowID}>
            {cell}
          </tr>
        );
      }
      return (
        <div className="container">
          <table id="simple-board">
            <tbody>{rows}</tbody>
          </table>
        </div>
      );
    }

    function getWzdxTable(obj) {
      let arr = [];
      arr.push([
        "road_name",
        obj["properties"]["core_details"]["road_names"][0],
      ]);
      arr.push(["direction", obj["properties"]["core_details"]["direction"]]);
      arr.push(["vehicle_impact", obj["properties"]["vehicle_impact"]]);
      arr.push(["workers_present", obj["properties"]["workers_present"]]);
      arr.push([
        "description",
        break_line(obj["properties"]["core_details"]["description"]),
      ]);
      arr.push(["start_date", obj["properties"]["start_date"]]);
      arr.push(["end_date", obj["properties"]["end_date"]]);
      return arr;
    }

    function openPopup(index) {
      setSelectedMarkerIndex(index);
    }

    function customMarker(feature, index, lat, lng) {
      return (
        <Marker
          key={feature.id}
          latitude={lat}
          longitude={lng}
          offsetLeft={-30}
          offsetTop={-30}
          feature={feature}
          index={index}
        >
          <div onClick={() => openPopup(index)}>
            <img src="./workzone_icon.png" height={60} alt="Work Zone Icon" />
          </div>
        </Marker>
      );
    }

    const getAllMarkers = (wzdxData) => {
      var i = -1;
      var markers = wzdxData.features.map((feature) => {
        const localFeature = { ...feature };
        var center_coords_index = Math.round(
          feature.geometry.coordinates.length / 2
        );
        var lng = feature.geometry.coordinates[0][0];
        var lat = feature.geometry.coordinates[0][1];
        if (center_coords_index !== 1) {
          lat = feature.geometry.coordinates[center_coords_index][1];
          lng = feature.geometry.coordinates[center_coords_index][0];
        } else {
          lat =
            (feature.geometry.coordinates[0][1] +
              feature.geometry.coordinates[1][1]) /
            2;
          lng =
            (feature.geometry.coordinates[0][0] +
              feature.geometry.coordinates[1][0]) /
            2;
        }
        i++;
        localFeature.properties = { ...feature.properties };
        localFeature.properties.table = createPopupTable(getWzdxTable(feature));
        return customMarker(localFeature, i, lat, lng);
      });
      return markers;
    };

    setWzdxMarkers(getAllMarkers(wzdxData));
  }, [wzdxData]);

  const layerStyle = {
    id: "linestring",
    type: "line",
    paint: {
      "line-color": "#F29543",
      "line-width": 8,
    },
  };

  function break_line(val) {
    var arr = [];
    for (var i = 0; i < val.length; i += 100) {
      arr.push(val.substring(i, i + 100));
    }
    return arr.join("\n");
  }

  function closePopup() {
    setSelectedMarkerIndex(null);
  }

  const CustomPopup = ({ marker, closePopup }) => {
    return (
      <Popup
        latitude={marker.props.latitude}
        longitude={marker.props.longitude}
        altitude={12}
        onClose={closePopup}
        closeButton={true}
        closeOnClick={false}
        offsetTop={-25}
      >
        {marker.props.feature.properties.table}
      </Popup>
    );
  };

  return (
    <div className="container">
      <ReactMapGL
        {...viewport}
        mapboxApiAccessToken={EnvironmentVars.MAPBOX_TOKEN}
        mapStyle={mbStyle}
        onViewportChange={(viewport) => {
          setViewport(viewport);
        }}
        onClick={() => setSelectedMarkerIndex(null)}
      >
        {wzdxMarkers}

        {selectedMarker !== null && (
          <CustomPopup marker={selectedMarker} closePopup={closePopup} />
        )}

        <Source id="wzdx" type="geojson" data={wzdxData}>
          <Layer {...layerStyle} />
        </Source>
      </ReactMapGL>
    </div>
  );
}

export default WzdxMap;
