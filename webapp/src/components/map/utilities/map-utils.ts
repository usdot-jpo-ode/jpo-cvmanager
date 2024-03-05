export function deg2rad(deg: number) {
  return deg * (Math.PI / 180);
}

export function rad2deg(rad: number) {
  return rad * (180 / Math.PI);
}

// get bearing between two lat/long points
export function getBearingBetweenPoints(start: number[], end: number[]) {
  if (!start || !end) return 0;
  const lat1 = deg2rad(start[1]!);
  const lon1 = deg2rad(start[0]!);
  const lat2 = deg2rad(end[1]!);
  const lon2 = deg2rad(end[0]!);
  const dLon = lon2 - lon1;
  const y = Math.sin(dLon) * Math.cos(lat2);
  const x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
  const brng = Math.atan2(y, x);
  return rad2deg(brng);
}

export const getTimeRange = (startDate: Date, endDate: Date) => {
  return (endDate.getTime() - startDate.getTime()) / 1000;
};
