type J2735AdvisorySpeedList = {
  advisorySpeedList?: J2735AdvisorySpeed[];
};

type J2735AdvisorySpeed = {
  type?: J2735AdvisorySpeedType;
  speed?: number;
  confidence?: J2735SpeedConfidence;
  distance?: number;
  classId?: number;
};

type J2735AdvisorySpeedType = "NONE" | "GREENWAVE" | "ECODRIVE" | "TRANSIT";

type J2735SpeedConfidence =
  | "UNAVAILABLE"
  | "PREC100MS"
  | "PREC10MS"
  | "PREC5MS"
  | "PREC1MS"
  | "PREC0_1MS"
  | "PREC0_05MS"
  | "PREC0_01MS";
