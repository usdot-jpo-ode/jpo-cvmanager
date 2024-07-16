const cv_manager_user = process.env.MONGO_ADMIN_DB_USER
const cv_manager_pass = process.env.MONGO_ADMIN_DB_PASS

db = connect('mongodb://' + cv_manager_user + ':' + cv_manager_pass + '@mongo:27017/admin')
if (db === null) {
  print('Error connecting to the MongoDB instance')
} else {
  print('Successfully connected to the MongDB instance')
}

var currentDate = new Date()
var currentDateIso = currentDate.toISOString().slice(0, -5)

var datePreviousHour = new Date()
datePreviousHour.setHours(currentDate.getHours() - 1)
var datePreviousHourIso = datePreviousHour.toISOString().slice(0, -5)

ConflictMonitor = db.getSiblingDB('ConflictMonitor')

// Insert CVCounts documents
ConflictMonitor.CVCounts.insertMany([
  {
    messageType: 'Map',
    rsuIp: '10.0.0.180',
    timestamp: new Date(currentDateIso.slice(0, -5) + '00:00Z'),
    count: 153,
  },
  {
    messageType: 'Map',
    rsuIp: '10.0.0.78',
    timestamp: new Date(currentDateIso.slice(0, -5) + '00:00Z'),
    count: 12,
  },
  {
    messageType: 'SPaT',
    rsuIp: '10.0.0.180',
    timestamp: new Date(currentDateIso.slice(0, -5) + '00:00Z'),
    count: 24,
  },
  {
    messageType: 'SPaT',
    rsuIp: '10.0.0.78',
    timestamp: new Date(currentDateIso.slice(0, -5) + '00:00Z'),
    count: 15,
  },
  {
    messageType: 'SPaT',
    rsuIp: '10.0.0.180',
    timestamp: new Date(datePreviousHourIso.slice(0, -5) + '00:00Z'),
    count: 17,
  },
  {
    messageType: 'SPaT',
    rsuIp: '10.0.0.78',
    timestamp: new Date(datePreviousHourIso.slice(0, -5) + '00:00Z'),
    count: 12,
  },
  {
    messageType: 'Map',
    rsuIp: '10.0.0.180',
    timestamp: new Date(datePreviousHourIso.slice(0, -5) + '00:00Z'),
    count: 145,
  },
  {
    messageType: 'Map',
    rsuIp: '10.0.0.78',
    timestamp: new Date(datePreviousHourIso.slice(0, -5) + '00:00Z'),
    count: 10,
  },
])

// insert V2XGeoJson documents
ConflictMonitor.V2XGeoJson.insertMany([
  {
    type: 'Feature',
    geometry: {
      type: 'Point',
      coordinates: [-104.997663, 39.708309],
    },
    properties: {
      id: '10.0.0.180',
      timestamp: new Date(currentDateIso.slice(0, -5) + '15:00Z'),
      msg_type: 'Bsm',
    },
  },
  {
    type: 'Feature',
    geometry: {
      type: 'Point',
      coordinates: [-105.014169, 39.740033],
    },
    properties: {
      id: '10.0.0.180',
      timestamp: new Date(currentDateIso.slice(0, -5) + '00:00Z'),
      msg_type: 'Bsm',
    },
  },
  {
    type: 'Feature',
    geometry: {
      type: 'Point',
      coordinates: [-104.990622, 39.7718],
    },
    properties: {
      id: '10.0.0.180',
      timestamp: new Date(datePreviousHourIso.slice(0, -5) + '45:00Z'),
      msg_type: 'Bsm',
    },
  },
  {
    type: 'Feature',
    geometry: {
      type: 'Point',
      coordinates: [-104.982867, 39.817387],
    },
    properties: {
      id: '10.0.0.78',
      timestamp: new Date(datePreviousHourIso.slice(0, -5) + '30:00Z'),
      msg_type: 'Bsm',
    },
  },
  {
    type: 'Feature',
    geometry: {
      type: 'Point',
      coordinates: [-104.987132, 39.856097],
    },
    properties: {
      id: '10.0.0.78',
      timestamp: new Date(datePreviousHourIso.slice(0, -5) + '15:00Z'),
      msg_type: 'Bsm',
    },
  },
  {
    type: 'Feature',
    geometry: {
      type: 'Point',
      coordinates: [-104.98752, 39.882289],
    },
    properties: {
      id: '10.0.0.78',
      timestamp: new Date(datePreviousHourIso.slice(0, -5) + '00:00Z'),
      msg_type: 'Bsm',
    },
  },
])

// insert OdeSsmJson document
ConflictMonitor.OdeSsmJson.insertOne({
  metadata: {
    logFileName: '',
    recordType: 'ssmTx',
    securityResultCode: 'success',
    receivedMessageDetails: { rxSource: 'NA' },
    payloadType: 'us.dot.its.jpo.ode.model.OdeSsmPayload',
    serialId: {
      streamId: 'daccfc21-e356-4481-be37-5963a056bfc0',
      bundleSize: 1,
      bundleId: 0,
      recordId: 0,
      serialNumber: 0,
    },
    odeReceivedAt: currentDate,
    schemaVersion: 6,
    maxDurationTime: 0,
    recordGeneratedAt: '',
    recordGeneratedBy: 'RSU',
    sanitized: false,
    odePacketID: '',
    odeTimStartDateTime: '',
    originIp: '10.0.0.78',
    ssmSource: 'RSU',
  },
  payload: {
    data: {
      second: 0,
      status: {
        signalStatus: [
          {
            sequenceNumber: 0,
            id: {
              id: 12110,
            },
            sigStatus: {
              signalStatusPackage: [
                {
                  requester: {
                    id: {
                      stationID: 2366845094,
                    },
                    request: 3,
                    sequenceNumber: 0,
                    typeData: {
                      role: 'publicTransport',
                    },
                  },
                  inboundOn: {
                    lane: 2,
                  },
                  status: 'granted',
                },
              ],
            },
          },
        ],
      },
    },
    dataType: 'us.dot.its.jpo.ode.plugin.j2735.J2735SSM',
  },
})

print('Successfully inserted sample data')
