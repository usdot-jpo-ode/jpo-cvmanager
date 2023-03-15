import React, { useEffect, useState } from "react";
import MapGL, { Source, Layer } from "react-map-gl";
import EnvironmentVars from "../EnvironmentVars";
import "../components/css/RsuMapView.css";
import SsmSrmItem from "../components/SsmSrmItem";
import { useSelector, useDispatch } from "react-redux";
import {
  selectRsuMapData,
  selectSelectedRsu,
  selectSelectedSrm,
  selectMapDate,
  selectSsmDisplay,
  selectRsuIpv4,
  selectSrmSsmList,

  // actions
  toggleMapDisplay,
  toggleSsmSrmDisplay,
} from "../slices/rsuSlice";

function RsuMapView(props) {
  const dispatch = useDispatch();

  const rsuMapData = useSelector(selectRsuMapData);
  const selectedRsu = useSelector(selectSelectedRsu);
  const selectedSrm = useSelector(selectSelectedSrm);
  const mapDate = useSelector(selectMapDate);
  const ssmDisplay = useSelector(selectSsmDisplay);
  const rsuIpv4 = useSelector(selectRsuIpv4);
  const srmSsmList = useSelector(selectSrmSsmList);

  const [srmCount, setSrmCount] = useState(0);
  const [ssmCount, setSsmCount] = useState(0);
  const [msgList, setMsgList] = useState([]);
  const [egressData, setEgressData] = useState({
    type: "FeatureCollection",
    features: [],
  });
  const [ingressData, setIngressData] = useState({
    type: "FeatureCollection",
    features: [],
  });

  useEffect(() => {
    let localSrmCount = 0;
    let localSsmCount = 0;
    let localMsgList = [];
    for (const elem of srmSsmList) {
      if (elem.ip === rsuIpv4) {
        localMsgList.push(elem);
        if (elem.type === "srmTx") {
          localSrmCount += 1;
        } else {
          localSsmCount += 1;
        }
      }
    }
    setSrmCount(localSrmCount);
    setSsmCount(localSsmCount);
    setMsgList(localMsgList);
  }, [srmSsmList, rsuIpv4]);

  useEffect(() => {
    const ingressDataFeatures = [];
    const egressDataFeatures = [];

    Object.entries(rsuMapData?.["features"] ?? []).map((feature) => {
      if (feature[1].properties.ingressPath === "true") {
        ingressDataFeatures.push(feature[1]);
      }
      return null;
    });
    Object.entries(rsuMapData?.["features"] ?? []).map((feature) => {
      if (feature[1].properties.egressPath === "true") {
        egressDataFeatures.push(feature[1]);
      }
      return null;
    });

    setIngressData((prevIngressData) => {
      return { ...prevIngressData, features: ingressDataFeatures };
    });
    setEgressData((prevEgressData) => {
      return { ...prevEgressData, features: egressDataFeatures };
    });
  }, [rsuMapData]);

  const srmData = {
    type: "FeatureCollection",
    features: [],
  };

  if (selectedSrm.length > 0) {
    srmData.features.push({
      type: "Feature",
      geometry: {
        type: "Point",
        coordinates: [selectedSrm[0].long, selectedSrm[0].lat],
      },
    });
  }

  const ingressLayer = {
    id: "ingressLayer",
    type: "line",
    minzoom: 14,
    source: "ingressData",
    layout: {
      "line-join": "round",
      "line-cap": "round",
    },
    paint: {
      "line-color": "rgb(50,205,50)",
      "line-width": 3,
    },
  };

  const egressLayer = {
    id: "egressLayer",
    type: "line",
    minzoom: 14,
    source: "egressData",
    layout: {
      "line-join": "round",
      "line-cap": "round",
    },
    paint: {
      "line-color": "rgb(203, 4, 4)",
      "line-width": 3,
    },
  };

  const srmLayer = {
    id: "srmMarker",
    type: "circle",
    source: "srmData",
    minzoom: 12,
    paint: {
      "circle-radius": 8,
      "circle-color": "rgb(14, 32, 82)",
    },
  };

  const [viewport, setViewport] = useState({
    latitude: selectedRsu.geometry.coordinates[1],
    longitude: selectedRsu.geometry.coordinates[0],
    width: "100%",
    height: props.auth ? "calc(100vh - 135px)" : "calc(100vh - 100px)",
    zoom: 17,
  });

  return (
    <div className="container">
      <MapGL
        {...viewport}
        mapboxApiAccessToken={EnvironmentVars.MAPBOX_TOKEN}
        mapStyle={"mapbox://styles/mapbox/satellite-v9"}
        onViewportChange={(viewport) => {
          setViewport(viewport);
        }}
      >
        <Source type="geojson" data={ingressData}>
          <Layer {...ingressLayer} />
        </Source>
        <Source type="geojson" data={egressData}>
          <Layer {...egressLayer} />
        </Source>
        <Source type="geojson" data={srmData}>
          <Layer {...srmLayer} />
        </Source>
      </MapGL>
      <button
        className="backButton"
        onClick={(e) => dispatch(toggleMapDisplay())}
      >
        Back
      </button>
      <div className="dateStyle">MAP data from {mapDate}</div>
      {ssmDisplay ? (
        <div className="ssmSrmContainer">
          <button id="toggle" onClick={() => dispatch(toggleSsmSrmDisplay())}>
            X
          </button>
          <h3 id="ssmsrmDataHeader">SSM / SRM Data For {rsuIpv4}</h3>
          <div id="ssmSrmHeaderContainer">
            <p id="timeHeader"> Time </p>
            <p id="requestHeader"> Request Id</p>
            <p id="roleHeader"> Role </p>
            <p id="ssmSrmHeader"> Status </p>
            <p id="ssmSrmHeader"> Display </p>
          </div>
          {Object.keys(msgList).map((index) => (
            <SsmSrmItem
              key={index}
              index={index}
              elem={msgList[index]}
              setSelectedSrm={selectedSrm}
            />
          ))}
          <h3 id="countsHeader"> Total Counts </h3>
          <div id="countsContainer">
            <h4 id="countsData"> SSM: {ssmCount} </h4>
            <h4 id="countsData"> SRM: {srmCount} </h4>
          </div>
        </div>
      ) : (
        <button
          className="srmSsmToggle"
          onClick={() => dispatch(toggleSsmSrmDisplay())}
        >
          SSM/SRM Display
        </button>
      )}
      <div className="keyContainer">
        <div id="keyTitle">
          <h3 id="keyTitle">Reference</h3>
        </div>
        <div id="referenceWrapper">
          <div id="ingressKey" />
          <p className="mapKeyInfo">- Ingress Lane</p>
        </div>
        <div id="referenceWrapper">
          <div id="egressKey" />
          <p className="mapKeyInfo">- Egress Lane</p>
        </div>
        <div id="referenceWrapper">
          <div id="ssmKey" />
          <p className="referenceInfo">- SSM (table)</p>
        </div>
        <div id="referenceWrapper">
          <div id="srmKey" />
          <p className="referenceInfo">- SRM (table)</p>
        </div>
        <div id="referenceWrapper">
          <div id="srmKeyMap" />
          <p className="referenceInfo">- SRM (map)</p>
        </div>
      </div>
    </div>
  );
}

export default RsuMapView;
