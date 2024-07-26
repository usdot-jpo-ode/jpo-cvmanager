type OdeBsmData = {
  metadata: OdeMsgMetadata;
  payload: OdeMsgPayload;
};

type OdeMsgMetadata = {
  payloadType: string;
  serialId: SerialId;
  odeReceivedAt: metadata;
  schemaVersion: number;
  maxDurationTime: number;
  odePacketID: string;
  odeTimStartDateTime: string;
  recordGeneratedAt: string;
  recordGeneratedBy: GeneratedBy;
  sanitized: boolean;
  originIp: string;
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
