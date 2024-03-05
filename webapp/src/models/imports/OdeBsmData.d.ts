type OdeBsmData = {
  metadata: OdeMsgMetadata;
  payload: OdeMsgPayload;
};

type OdeMsgMetadata = {
  payloadType: String;
  serialId: SerialId;
  odeReceivedAt: String;
  schemaVersion: number;
  maxDurationTime: number;
  odePacketID: String;
  odeTimStartDateTime: String;
  recordGeneratedAt: String;
  recordGeneratedBy: GeneratedBy;
  sanitized: boolean;
};

type GeneratedBy = "TMC" | "OBU" | "RSU" | "TMC_VIA_SAT" | "TMC_VIA_SNMP" | "UNKNOWN";

type SerialId = {
  streamId: string;
  bundleSize: number;
  bundleId: number;
  recordId: number;
  serialNumber: number;
};

type OdeMsgPayload = {
  dataType: string;
  data: OdeObject;
};

type OdeObject = {
  coreData: J2735BsmCoreData;
}; // TODO: check
