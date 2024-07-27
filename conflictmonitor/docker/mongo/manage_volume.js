
// Mongo Data Managment Script

// Features
// Automatically Logs Collection Sizes 
// Automatically Updates Data Retention Periods to prevent overflow
// Performs Emergency Record Deletion when Collections get to large

// Database to Perform operation on.
const CM_DATABASE_NAME = process.env.CM_DATABASE_NAME || "ConflictMonitor";

// The name of the collection to store the data as.
const CM_DATABASE_STORAGE_COLLECTION_NAME = process.env.CM_DATABASE_STORAGE_COLLECTION_NAME || "MongoStorage";

// Total Size of the database disk in GB. This script will work to ensure all data fits within this store.
const CM_DATABASE_SIZE_GB = process.env.CM_DATABASE_SIZE_GB || 1000;

// Specified as a percent of total database size. This is the storage target the database will try to hit. 
const CM_DATABASE_SIZE_TARGET_PERCENT = process.env.CM_DATABASE_SIZE_TARGET_PERCENT || 0.8;

// Specified as a percent of total database size.
const CM_DATABASE_DELETE_THRESHOLD_PERCENT = process.env.CM_DATABASE_DELETE_THRESHOLD_PERCENT || 0.9;

// The maximum amount of time data should be retained. Measured in Seconds.
const CM_DATABASE_MAX_TTL_RETENTION_SECONDS = process.env.CM_DATABASE_MAX_TTL_RETENTION_SECONDS || 5184000; // 60 Days

// The minimum amount of time data should be retained. Measured in Seconds. This only effects TTL's set on the data. It will not prevent the database from manual data deletion.
const CM_DATABASE_MIN_TTL_RETENTION_SECONDS = process.env.CM_DATABASE_MIN_TTL_RETENTION_SECONDS || 604800; // 7 Days

// When the free space of a collection exceeds this percent of the collections total volume, automatic compaction should occur
const CM_DATABASE_COMPACTION_TRIGGER_PERCENT = process.env.CM_DATABASE_COMPACTION_TRIGGER_PERCENT || 0.5;


const CM_MONGO_ROOT_USERNAME = process.env.MONGO_INITDB_ROOT_USERNAME || "root";
const CM_MONGO_ROOT_PASSWORD = process.env.MONGO_INITDB_ROOT_PASSWORD || "root";

const MS_PER_HOUR = 60 * 60 * 1000;
const BYTE_TO_GB = 1024 * 1024 * 1024;
const DB_TARGET_SIZE_BYTES = CM_DATABASE_SIZE_GB * CM_DATABASE_SIZE_TARGET_PERCENT * BYTE_TO_GB;
const DB_DELETE_SIZE_BYETS = CM_DATABASE_SIZE_GB * CM_DATABASE_DELETE_THRESHOLD_PERCENT * BYTE_TO_GB;


print("Managing Mongo Data Volumes");

db = db.getSiblingDB("admin");
db.auth(CM_MONGO_ROOT_USERNAME, CM_MONGO_ROOT_PASSWORD);
db = db.getSiblingDB("ConflictMonitor");

class CollectionStats{
	constructor(name, allocatedSpace, freeSpace, indexSpace){
		this.name = name;
		this.allocatedSpace = allocatedSpace;
		this.freeSpace = freeSpace;
		this.indexSize = indexSpace;
	}
}


class StorageRecord{
	constructor(collectionStats, totalAllocatedStorage, totalFreeSpace, totalIndexSize){
		this.collectionStats = collectionStats;
		this.recordGeneratedAt = ISODate();
		this.totalAllocatedStorage = totalAllocatedStorage;
		this.totalFreeSpace = totalFreeSpace;
		this.totalIndexSize = totalIndexSize;
		this.totalSize = totalAllocatedStorage + totalFreeSpace + totalIndexSize;
	}
}

function ema_deltas(records){
	const a = 0.5;
	let average_delta = 0;

	for(let i=0; i< records.length-1; i++){
		const delta = records[i+1] - records[i];
		average_delta += Math.pow(a, records.length -i -1) * delta;
	}

	return average_delta;
	
}

function updateTTL(){

	print("Updating TTL")
	const ttl = getLatestTTL();
	if(ttl == 0){
		print("Skipping TTL Update")
		// Do not update TTL's
		return;
	}

	
	const newestRecords = db.getCollection(CM_DATABASE_STORAGE_COLLECTION_NAME).find().sort({"recordGeneratedAt":-1}).limit(10);

	let sizes = [];
	newestRecords.forEach(doc => {
		let total = 0;
		for(let i=0; i < doc.collectionStats.length; i++){
			total += doc.collectionStats[i].allocatedSpace + doc.collectionStats[i].freeSpace + doc.collectionStats[i].indexSize;
		}

		sizes.push(total);
	});
	

	// Overshoot Prevention
	const growth = ema_deltas(sizes);
	const oldestSpat = db.getCollection("ProcessedSpat").find().sort({"recordGeneratedAt":1}).limit(1);

	let new_ttl = ttl;
	let possible_ttl = ttl;

	// Check if collection is still growing to capacity, or if it in steady state
	if(oldestSpat.recordGeneratedAt > ISODate() - ttl + MS_PER_HOUR && growth > 0){
		possible_ttl = DB_TARGET_SIZE_BYTES / growth;
	}else{
		possible_ttl = 3600 * ((DB_TARGET_SIZE_BYTES - sizes[0])/BYTE_TO_GB) + ttl; // Shift the TTL by roughly 1 hour for every GB of data over or under
	}

	// Clamp TTL and assign to new TTL;

	if(!isNaN(possible_ttl) && possible_ttl != 0){
		if(possible_ttl > CM_DATABASE_MAX_TTL_RETENTION_SECONDS){
			new_ttl = CM_DATABASE_MAX_TTL_RETENTION_SECONDS;
		}else if(possible_ttl < CM_DATABASE_MIN_TTL_RETENTION_SECONDS){
			new_ttl = CM_DATABASE_MIN_TTL_RETENTION_SECONDS;
		}else{
			new_ttl = Math.round(possible_ttl);
		}
		new_ttl = Number(new_ttl);
		print("Calculated New TTL for MongoDB: " + new_ttl);
		applyNewTTL(new_ttl);
	}else{
		print("Not Updating TTL New TTL is NaN");
	}
}

function getLatestTTL(){
	const indexes = db.getCollection("ProcessedSpat").getIndexes();
	for (let i=0; i < indexes.length; i++){
		if(indexes[i].hasOwnProperty("expireAfterSeconds")){
			return indexes[i]["expireAfterSeconds"];
		}
	}
	return 0;
}

function getTTLKey(collection){
	const indexes = db.getCollection(collection).getIndexes();
	for (let i=0; i < indexes.length; i++){
		if(indexes[i].hasOwnProperty("expireAfterSeconds")){
			return [indexes[i]["key"], indexes[i]["expireAfterSeconds"]];
		}
	}
	return [null, null];
}

function applyNewTTL(ttl){
	var collections = db.getCollectionNames();
	for(let i=0; i< collections.length; i++){
		const collection = collections[i];
		let [key, oldTTL] = getTTLKey(collection);
		if(oldTTL != ttl && key != null){
			print("Updating TTL For Collection: " + collection, ttl);
			db.runCommand({
				"collMod": collection,
				"index": {
					keyPattern: key,
					expireAfterSeconds: ttl
			}});
		}
	}
}


function addNewStorageRecord(){
	var collections = db.getCollectionNames();
	let totalAllocatedStorage = 0;
	let totalFreeSpace = 0;
	let totalIndexSize = 0;

	let records = [];

	for (var i = 0; i < collections.length; i++) {
		let stats = db.getCollection(collections[i]).stats();
		let colStats = db.runCommand({"collstats": collections[i]});
		let blockManager = colStats["wiredTiger"]["block-manager"];

		let freeSpace = Number(blockManager["file bytes available for reuse"]);
		let allocatedStorage = Number(blockManager["file size in bytes"]);
		let indexSize = Number(stats.totalIndexSize);

		records.push(new CollectionStats(collections[i], allocatedStorage, freeSpace, indexSize));

		totalAllocatedStorage += allocatedStorage
		totalFreeSpace += freeSpace;
		totalIndexSize += indexSize;

		print(collections[i], allocatedStorage / BYTE_TO_GB, freeSpace/ BYTE_TO_GB, indexSize / BYTE_TO_GB);
	}

	const storageRecord = new StorageRecord(records, totalAllocatedStorage, totalFreeSpace, totalIndexSize);
	db.getCollection(CM_DATABASE_STORAGE_COLLECTION_NAME).insertOne(storageRecord);
}

function compactCollections(){
	print("Checking Collection Compaction");

	var collections = db.getCollectionNames();

	let activeCompactions = [];
	db.currentOp({ "active": true, "secs_running": { "$gt": 0 } }).inprog.forEach(op => {
		if (op.msg && op.msg.includes("compact")) {
			print("Found Active Compactions");
			activeCompactions.push(op.command.compact);
		}
	});

	for (var i = 0; i < collections.length; i++) {
		let colStats = db.runCommand({"collstats": collections[i]});
		let blockManager = colStats["wiredTiger"]["block-manager"];

		let freeSpace = Number(blockManager["file bytes available for reuse"]);
		let allocatedStorage = Number(blockManager["file size in bytes"]);

		// If free space makes up a significant proportion of allocated storage
		if(freeSpace > allocatedStorage * CM_DATABASE_COMPACTION_TRIGGER_PERCENT && allocatedStorage > (1 * BYTE_TO_GB)){
			if(!activeCompactions.includes(collections[i])){
				print("Compacting Collection", collections[i]);
				db.runCommand({compact: collections[i], force:true});
			}else{
				print("Skipping Compaction, Collection Compaction is already scheduled");
			}
		}
	}
}

addNewStorageRecord();
updateTTL();
compactCollections();
