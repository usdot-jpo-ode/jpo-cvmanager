import JSZip from "jszip";
import { MAP_QUERY_PARAMS, RAW_MESSAGE_DATA_EXPORT } from "../map-slice";
import FileSaver from "file-saver";

export const downloadAllData = (rawData: RAW_MESSAGE_DATA_EXPORT, queryParams: MAP_QUERY_PARAMS) => {
  var zip = new JSZip();
  zip.file(`intersection_${queryParams.intersectionId}_MAP_data.json`, JSON.stringify(rawData.map));
  zip.file(`intersection_${queryParams.intersectionId}_SPAT_data.json`, JSON.stringify(rawData.spat));
  zip.file(`intersection_${queryParams.intersectionId}_BSM_data.json`, JSON.stringify(rawData.bsm));
  if (rawData.event)
    zip.file(`intersection_${queryParams.intersectionId}_Event_data.json`, JSON.stringify(rawData.event));
  if (rawData.assessment)
    zip.file(`intersection_${queryParams.intersectionId}_Assessment_data.json`, JSON.stringify(rawData.assessment));
  if (rawData.notification)
    zip.file(`intersection_${queryParams.intersectionId}_Notification_data.json`, JSON.stringify(rawData.notification));

  zip.generateAsync({ type: "blob" }).then(function (content) {
    FileSaver.saveAs(content, `intersection_${queryParams.intersectionId}_data.zip`);
  });
};
