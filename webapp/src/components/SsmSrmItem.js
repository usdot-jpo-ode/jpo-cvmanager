import React, { useEffect } from "react";

import "./css/SsmSrmItem.css";

const SsmSrmItem = (props) => {
  const { setSelectedSrm } = props;
  useEffect(() => {
    return () => {
      setSelectedSrm({});
    };
  }, [setSelectedSrm]);

  return (
    <div id={props.elem["type"] === "srmTx" ? "srmitemdiv" : "ssmitemdiv"}>
      <p className="ssmsrmitemtext">{props.elem["time"]}</p>
      <p className="ssmsrmitemtext">{props.elem["requestId"]}</p>
      <p className="ssmsrmitemtext">{props.elem["role"]}</p>
      <p className="ssmsrmitemtext">{props.elem["status"]}</p>
      {props.elem["type"] === "srmTx" ? (
        <button className="btnActive" onClick={() => props.setSelectedSrm(props.elem)}>
          View
        </button>
      ) : (
        <button className="btnDisabled" disabled={true}>
          View
        </button>
      )}
    </div>
  );
};

export default SsmSrmItem;
