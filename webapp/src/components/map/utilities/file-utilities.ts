import JSZip from "jszip";
import { MAP_QUERY_PARAMS } from "../map-slice";
import FileSaver from "file-saver";

const downloadJsonFile = (contents: any, name: string) => {
  const element = document.createElement("a");
  const file = new Blob([JSON.stringify(contents)], {
    type: "text/plain",
  });
  element.href = URL.createObjectURL(file);
  element.download = name;
  document.body.appendChild(element); // Required for this to work in FireFox
  element.click();
};

const downloadAllData = (queryParams: MAP_QUERY_PARAMS, rawData: any) => {
  var zip = new JSZip();
  zip.file(`intersection_${queryParams.intersectionId}_MAP_data.json`, JSON.stringify(rawData["map"]));
  zip.file(`intersection_${queryParams.intersectionId}_SPAT_data.json`, JSON.stringify(rawData["spat"]));
  zip.file(`intersection_${queryParams.intersectionId}_BSM_data.json`, JSON.stringify(rawData["bsm"]));
  zip.file(
    `intersection_${queryParams.intersectionId}_Notification_data.json`,
    JSON.stringify(rawData["notification"])
  );
  zip.generateAsync({ type: "blob" }).then(function (content) {
    FileSaver.saveAs(content, `intersection_${queryParams.intersectionId}_data.zip`);
  });
};
