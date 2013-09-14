package tw.plash.antrip.offline;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Address;
import android.location.Location;
import android.text.format.DateFormat;
import android.util.Log;

/**
 * @author CSZU
 * 
 */
public class DBHelper2 {
	
	private static final String DATABASE_NAME = "antripoffline"; // database name
	private static final int DATABASE_VERSION = 9; // required by SQLite tool
	
	private static final String TRIP_DATA_TABLE = "tripdatatable";
	private static final String TRIP_INFO_TABLE = "tripinfotable";
	
	private Context mContext;
	
	private SQLiteDatabase db;
	
	private static final String CREATE_TABLE_TRIP_DATA = "CREATE TABLE " + TRIP_DATA_TABLE
			+ "(id INTEGER PRIMARY KEY, " 
			+ "latitude REAL, " 
			+ "longitude REAL, " 
			+ "timestamp TEXT, " //it is now in the format of long, should be cast to timestamp format before upload
			+ "altitude TEXT, " 
			+ "speed TEXT, " 
			+ "bearing TEXT, " 
			+ "accuracy REAL, " 
			+ "accelerometerX TEXT, " 
			+ "accelerometerY TEXT, " 
			+ "accelerometerZ TEXT, " 
			+ "tripid TEXT, " 
			+ "picture TEXT default '-1', " 
			+ "emotion TEXT default '-1', "
			+ "note TEXT default '-1', " 
			+ "uploadstatus INTEGER default 0, "
			+ "todisplay INTEGER default 0)"; //0: don't draw this point, 1 or above: draw this point
	
	private static final String CREATE_TABLE_TRIP_INFO = "CREATE TABLE " + TRIP_INFO_TABLE
			+ "(id INTEGER PRIMARY KEY, " 
			+ "tripid TEXT, " 
			+ "name TEXT default 'Untitled Trip', " 
			+ "starttime TEXT default 'START', "
			+ "endtime TEXT default 'END', " 
			+ "length INTEGER default -1, " 
			+ "count INTEGER default -1, " 
			+ "startaddrpt1 TEXT default 'NULL', " 
			+ "startaddrpt2 TEXT default 'NULL', "
			+ "startaddrpt3 TEXT default 'NULL', " 
			+ "startaddrpt4 TEXT default 'NULL', " 
			+ "startaddrpt5 TEXT default 'NULL', " 
			+ "endaddrpt1 TEXT default 'NULL', "
			+ "endaddrpt2 TEXT default 'NULL', " 
			+ "endaddrpt3 TEXT default 'NULL', " 
			+ "endaddrpt4 TEXT default 'NULL', " 
			+ "endaddrpt5 TEXT default 'NULL', "
			+ "uploadstage INTEGER default 0, "
			+ "servertripid TEXT default '-1', "
			+ "triplistheader INTEGER default 999)";
	
	/**
	 * Extends SQLiteOpenHelper, this subclass is only good for creating a
	 * database and a default table with pre-written schema. The onUpgrade
	 * method simply drops the old table then creates a new version, can be
	 * modified if required.
	 * 
	 * @author CSZU
	 * 
	 */
	private static class OpenHelper extends SQLiteOpenHelper {
		
		OpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
		// only run on the first time the DB is created
		public void onCreate(SQLiteDatabase db) {
			try { // create a table with given name and columns
				db.execSQL(CREATE_TABLE_TRIP_DATA);
				db.execSQL(CREATE_TABLE_TRIP_INFO);
			} catch (SQLException e) {
				e.printStackTrace();
			} // invalid SQL statement
		}
		
		@Override
		// for upgrading table schema only, not useful at the moment
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			try { // drop the old version and create a updated one
				db.execSQL("DROP TABLE IF EXIST " + DATABASE_NAME + "." + TRIP_DATA_TABLE);
				db.execSQL("DROP TABLE IF EXIST " + DATABASE_NAME + "." + TRIP_INFO_TABLE);
			} catch (SQLException e) {
				e.printStackTrace();
			} // invalid SQL statement
			onCreate(db);
		}
	} // OpenHelper class
	
	/**
	 * Constructor for DBHelper, retrieve a writable database and compile the
	 * default SQL statement
	 * 
	 * @param context
	 */
	
	final private int memoryClass;
	final private int largeTripThreshold;
	
	public int getLargeTripThreshold(){
		return largeTripThreshold;
	}
	
	public DBHelper2(Context context) {
		mContext = context;
		memoryClass = ((ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
		if(memoryClass < 16){
			largeTripThreshold = 550;
		} else if (memoryClass > 48){
			largeTripThreshold = 3050;
		} else{
			largeTripThreshold = memoryClass * 60;
		}
		
		OpenHelper openHelper = new OpenHelper(mContext);
		// get the DB with write permission
		try {
			this.db = openHelper.getWritableDatabase();
		} catch (SQLiteException e) {
			e.printStackTrace();
		} // something's wrong with the DB
	}
	
	synchronized public long insert(Location location, String tripid) {
		if (db.isOpen()) {
//			Log.i("dbhelper 2 insert", "location: " + location + ", tripid: " + tripid);
			ContentValues cv = new ContentValues();
			cv.put("latitude", location.getLatitude());
			cv.put("longitude", location.getLongitude());
			cv.put("timestamp", String.valueOf(location.getTime()));
			cv.put("altitude", location.getAltitude());
			cv.put("speed", location.getSpeed());
			cv.put("bearing", location.getBearing());
			cv.put("accuracy", location.getAccuracy());
			cv.put("tripID", tripid);
			return db.insert(TRIP_DATA_TABLE, null, cv);
		} else {
			return -2;
		}
	}
	
	synchronized public long insert(CandidateCheckinObject cco, String tripid) {
		if (db.isOpen()) {
			Location loc = cco.getLocation();
			ContentValues cv = new ContentValues();
			cv.put("latitude", loc.getLatitude());
			cv.put("longitude", loc.getLongitude());
			cv.put("timestamp", String.valueOf(loc.getTime()));
			cv.put("altitude", loc.getAltitude());
			cv.put("speed", loc.getSpeed());
			cv.put("bearing", loc.getBearing());
			cv.put("accuracy", loc.getAccuracy());
			cv.put("tripID", tripid);
			// cco fields
			cv.put("picture", cco.getPicturePath());
			cv.put("emotion", cco.getEmotionID());
			cv.put("note", cco.getCheckinText());
			return db.insert(TRIP_DATA_TABLE, null, cv);
		} else {
			return -2;
		}
	}
	
	synchronized public long saveTripStats(String tripid, TripStats stats){
		if(db.isOpen()){
			ContentValues cv = new ContentValues();
			cv.put("tripid", tripid);
			cv.put("starttime", String.valueOf(stats.getButtonStartTime()));
			cv.put("endtime", String.valueOf(stats.getNonNullEndTime()));
			cv.put("length", stats.getTotalValidLength().intValue());
			cv.put("count", stats.getTotalAccuratePointCount());
			return db.insert(TRIP_INFO_TABLE, null, cv);
		} else{
			return -2;
		}
	}
	
	synchronized public long setStartaddr(String tripid, List<Address> startaddr) {
		if (db.isOpen()) {
			if(startaddr != null){
				ContentValues cv = new ContentValues();
				cv.put("startaddrpt1", (startaddr.get(0).getCountryName() != null?startaddr.get(0).getCountryName():"NULL"));
				cv.put("startaddrpt2", (startaddr.get(0).getAdminArea() != null?startaddr.get(0).getAdminArea():"NULL"));
				cv.put("startaddrpt3", (startaddr.get(0).getLocality() != null?startaddr.get(0).getLocality():"NULL"));
				cv.put("startaddrpt4", (startaddr.get(0).getSubLocality() != null?startaddr.get(0).getSubLocality():"NULL"));
				cv.put("startaddrpt5", (startaddr.get(0).getThoroughfare() != null?startaddr.get(0).getThoroughfare():"NULL"));
//				Log.e("dbhelper", "insert start address: " + cv.toString());
				return db.update(TRIP_INFO_TABLE, cv, "tripid=" + tripid, null);
			} else{
				return -1;
			}
			
		} else {
			return -2;
		}
	}
	
	synchronized public long setEndaddr(String tripid, List<Address> endaddr) {
		if (db.isOpen()) {
			if(endaddr != null){
				ContentValues cv = new ContentValues();
				cv.put("endaddrpt1", (endaddr.get(0).getCountryName() != null?endaddr.get(0).getCountryName():"NULL"));
				cv.put("endaddrpt2", (endaddr.get(0).getAdminArea() != null?endaddr.get(0).getAdminArea():"NULL"));
				cv.put("endaddrpt3", (endaddr.get(0).getLocality() != null?endaddr.get(0).getLocality():"NULL"));
				cv.put("endaddrpt4", (endaddr.get(0).getSubLocality() != null?endaddr.get(0).getSubLocality():"NULL"));
				cv.put("endaddrpt5", (endaddr.get(0).getThoroughfare() != null?endaddr.get(0).getThoroughfare():"NULL"));
//				Log.e("dbhelper", "insert end address: " + cv.toString());
				return db.update(TRIP_INFO_TABLE, cv, "tripid=" + tripid, null);
			} else{
				return -1;
			}
			
		} else {
			return -2;
		}
	}
	
	/*
	 * upload stage
	 * 1: get address done
	 * 2: upload trip info/data done
	 * 3: upload picture done
	 * 4: confirm upload done
	 */
	synchronized public long setUploadStage(String tripid, int stage){
		if(db.isOpen()){
			ContentValues cv = new ContentValues();
			cv.put("uploadstage", stage);
			return db.update(TRIP_INFO_TABLE, cv, "tripid=" + tripid, null);
		} else{
			return -2;
		}
	}
	
	synchronized public int getUploadStage(String tripid) {
		if (db.isOpen()) {
			Cursor mCursor = db.query(TRIP_INFO_TABLE, new String[] { "uploadstage" }, "tripid=" + tripid, null, null, null, null);
			if (mCursor != null) {
				if (mCursor.moveToFirst()) {
					int status = mCursor.getInt(mCursor.getColumnIndexOrThrow("uploadstage"));
					mCursor.close();
					mCursor = null;
					return status;
				} else {
					mCursor.close();
					mCursor = null;
				}
			}
		}
		return -2;
	}

	synchronized public long setTripName(String tripid, String name) {
		if (db.isOpen()) {
			ContentValues cv = new ContentValues();
			// pop up screen asking for user input
			cv.put("name", name);
			return db.update(TRIP_INFO_TABLE, cv, "tripid=" + tripid, null);
		} else {
			return -2;
		}
	}
	
	synchronized public String getTripid(String tripid){
		if(db.isOpen()){
			Cursor mCursor = db.query(TRIP_INFO_TABLE, new String[]{"servertripid"}, "tripid=" + tripid, null, null, null, null);
			if(mCursor != null){
				if(mCursor.moveToFirst()){
					String servertripid = mCursor.getString(mCursor.getColumnIndexOrThrow("servertripid"));
					mCursor.close();
					return servertripid;
				}
				mCursor.close();
				mCursor = null;
			} 
		} 
		return null;
	}
	
	synchronized public boolean setServerTripid(String localTripid, String serverTripid){
		if(db.isOpen()){
			ContentValues cv = new ContentValues();
			cv.put("servertripid", serverTripid);
			//tripinfo table will save both local and server tripid
			db.update(TRIP_INFO_TABLE, cv, "tripid=" + localTripid, null);
			
			cv = new ContentValues();
			cv.put("tripid", serverTripid);
			//update all entries of tripdataa table to proper server tripid
			db.update(TRIP_DATA_TABLE, cv, "tripid=" + localTripid, null);
			//as long as there's no error, it's all good
			return true;
		}
		return false;
	}
	
	/*
	 * does not return the actual number of records in the trip data table of the given tripid
	 * but rather return whether the trip data of the given tripid have more than 2000 points(split threshold)
	 */
	synchronized public long getNumberofPoints(String tripid) {
		if (db.isOpen()) {
			
			Cursor mCursor = db.query(TRIP_DATA_TABLE, new String[]{"id"}, "tripid=" + tripid, null, null, null, null, String.valueOf(largeTripThreshold));
			if(mCursor != null){
				int count = mCursor.getCount();
				mCursor.close();
				mCursor = null;
				return count;
			}
		} 
		return -2;
	}
	
	//XXX think about the tripid vs servertripid issue
	synchronized public Location getOnePoint(String tripid, boolean wantFirst, boolean wantAccurate) {
		if (db.isOpen()) {
			Cursor mCursor = db.query(TRIP_DATA_TABLE, new String[] { "latitude", "longitude", "timestamp"}, "tripid=" + tripid
					+ " AND latitude>-998" + (wantAccurate?" AND accuracy < 1499":""), null, null, null, wantFirst ? "id ASC" : "id DESC", "1");
			if (mCursor != null) {
				if (mCursor.moveToFirst()) {
					Location loc = new Location("");
					loc.setLatitude(mCursor.getDouble(mCursor.getColumnIndex("latitude")));
					loc.setLongitude(mCursor.getDouble(mCursor.getColumnIndex("longitude")));
					loc.setTime(Long.valueOf(mCursor.getString(mCursor.getColumnIndex("timestamp"))));
					if (!mCursor.isClosed()) {
						mCursor.close();
					}
					return loc;
				}
				if (!mCursor.isClosed()) {
					mCursor.close();
				}
			}
		}
		return null;
	}
	
	synchronized public JSONObject getOneTripInfo(String userid, String tripid) {
		if (db.isOpen()) {
			Cursor mCursor = db.query(TRIP_INFO_TABLE, null, "tripid=" + tripid, null, null, null, null);
			if (mCursor != null) {
				if (mCursor.moveToFirst()) {
					JSONObject tmp = new JSONObject();
					try {
						tmp.put("userid", userid);
						tmp.put("update_status", "3");
						//XXX no trip_id here, server will automatically generate a new one
						tmp.put("trip_length", mCursor.getInt(mCursor.getColumnIndexOrThrow("length")));
						tmp.put("trip_et", new Timestamp(Long.valueOf(mCursor.getString(mCursor.getColumnIndexOrThrow("endtime")))).toString());
						tmp.put("trip_st", new Timestamp(Long.valueOf(mCursor.getString(mCursor.getColumnIndexOrThrow("starttime")))).toString());
						tmp.put("trip_name", mCursor.getString(mCursor.getColumnIndexOrThrow("name")));
						tmp.put("num_of_pts", mCursor.getInt(mCursor.getColumnIndexOrThrow("count")));
						tmp.put("et_addr_prt1", mCursor.getString(mCursor.getColumnIndexOrThrow("endaddrpt1")));
						tmp.put("et_addr_prt2", mCursor.getString(mCursor.getColumnIndexOrThrow("endaddrpt2")));
						tmp.put("et_addr_prt3", mCursor.getString(mCursor.getColumnIndexOrThrow("endaddrpt3")));
						tmp.put("et_addr_prt4", mCursor.getString(mCursor.getColumnIndexOrThrow("endaddrpt4")));
						tmp.put("et_addr_prt5", mCursor.getString(mCursor.getColumnIndexOrThrow("endaddrpt5")));
						tmp.put("st_addr_prt1", mCursor.getString(mCursor.getColumnIndexOrThrow("startaddrpt1")));
						tmp.put("st_addr_prt2", mCursor.getString(mCursor.getColumnIndexOrThrow("startaddrpt2")));
						tmp.put("st_addr_prt3", mCursor.getString(mCursor.getColumnIndexOrThrow("startaddrpt3")));
						tmp.put("st_addr_prt4", mCursor.getString(mCursor.getColumnIndexOrThrow("startaddrpt4")));
						tmp.put("st_addr_prt5", mCursor.getString(mCursor.getColumnIndexOrThrow("startaddrpt5")));
					} catch (JSONException e) {
						e.printStackTrace();
					}
					if (!mCursor.isClosed()) {
						mCursor.close();
					}
					return tmp;
				} else {
					if (!mCursor.isClosed()) {
						mCursor.close();
					}
					return null;
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	synchronized public ArrayList<String> getOneTripPicturePaths(String tripid){
		if(db.isOpen()){
			
			Cursor mCursor = db.query(TRIP_INFO_TABLE, new String[]{"servertripid"}, "tripid=" + tripid, null, null, null, null);
			
			if(mCursor != null){
				if(mCursor.moveToFirst()){
					String servertripid = mCursor.getString(mCursor.getColumnIndex("servertripid"));
					if(servertripid != null && !servertripid.equals("-1")){
						tripid = servertripid;
					}
				}
			}
			
			mCursor = db.query(TRIP_DATA_TABLE, new String[]{"picture"}, "tripid=" + tripid + " AND picture!='-1'", null, null, null, null);
			if(mCursor != null){
				if(mCursor.moveToFirst()){
					ArrayList<String> result = new ArrayList<String>();
					do{
						//XXX check if the picture is already uploaded
						//XXX see if the filename is modified with the prefix u
						String filename = mCursor.getString(mCursor.getColumnIndexOrThrow("picture"));
						if(filename != null){
							File f = new File(filename);
							if(f.exists()){
								result.add(filename);
							}
							//filename in DB will never be marked with the u prefix...
							//only the filename in real file system will
//							if(!String.valueOf((filename.charAt(filename.lastIndexOf("/") + 1))).equals("u")){
								//filename is not marked with "uploaded" symbol "u"
								
//							}
						}
					} while(mCursor.moveToNext());
					mCursor.close();
					return result;
				}
				if(!mCursor.isClosed()){
					mCursor.close();
				}
			}
		} 
		return null;
	}
	
	/*
	 * Will return the latest(timestamp desc) good point(valid and accurate) point from the given tripid
	 */
	synchronized public Location getLatestGoodPointFromCurrentTripData(String currentTripid){
		if(db.isOpen()){
			Cursor mCursor = db.query(TRIP_DATA_TABLE, null, "tripid=" + currentTripid + " AND accuracy<1499 AND latitude>-998", null, null, null, "timestamp DESC", "1");
			if(mCursor != null){
				if(mCursor.moveToFirst()){
					Location loc = new Location("");
					
					loc.setAccuracy(Float.parseFloat(mCursor.getString(mCursor.getColumnIndexOrThrow("accuracy"))));
					loc.setAltitude(Double.parseDouble(mCursor.getString(mCursor.getColumnIndexOrThrow("altitude"))));
					loc.setBearing(Float.parseFloat(mCursor.getString(mCursor.getColumnIndexOrThrow("bearing"))));
					loc.setLatitude(Double.parseDouble(mCursor.getString(mCursor.getColumnIndexOrThrow("latitude"))));
					loc.setLongitude(Double.parseDouble(mCursor.getString(mCursor.getColumnIndexOrThrow("longitude"))));
					loc.setSpeed(Float.parseFloat(mCursor.getString(mCursor.getColumnIndexOrThrow("speed"))));
					loc.setTime(Long.valueOf(mCursor.getString(mCursor.getColumnIndexOrThrow("timestamp"))));
					
					mCursor.close();
					mCursor = null;
					
					return loc;
				}
				mCursor.close();
				mCursor = null;
			}
		}
		return null;
	}
	
	/*
	 * Will return a arraylist containing valid and accurate points as jsonobject from the given tripid
	 */
	synchronized public ArrayList<JSONObject> getCurrentTripData(String currentTripid) {
//		Log.e("dbhelper current trip data", "input tripid=" + currentTripid);
		if (db.isOpen()) {
			Cursor mCursor = db.query(TRIP_DATA_TABLE, 
					new String[] { "latitude", "longitude", "picture", "emotion","note" },
					"tripid=" + currentTripid + " AND accuracy<1499 AND latitude>-998",
					null, 
					null, 
					null, 
					"timestamp ASC");
			if (mCursor != null) {
				if (mCursor.moveToFirst()) {
					ArrayList<JSONObject> result = new ArrayList<JSONObject>();
					try {
						do {
							JSONObject tmp = new JSONObject();
							//XXX something to think about
							//XXX add distance constraint between two consecutive point
							//XXX can calculate the reasonable value based on time and speed...
							tmp.put("latitude", mCursor.getDouble(mCursor.getColumnIndexOrThrow("latitude")));
							tmp.put("longitude", mCursor.getDouble(mCursor.getColumnIndexOrThrow("longitude")));
							{
								boolean checkinValid = false;
								
								String pic = mCursor.getString(mCursor.getColumnIndexOrThrow("picture"));
								String emo = mCursor.getString(mCursor.getColumnIndexOrThrow("emotion"));
								String note = mCursor.getString(mCursor.getColumnIndexOrThrow("note"));
								
								JSONObject checkin = new JSONObject();
								if(pic != null && !pic.equals("-1")){
									checkin.put("picture", pic);
									checkinValid = true;
								}
								if(emo != null && !emo.equals("-1")){
									checkin.put("emotion", emo);
									checkinValid = true;
								}
								if(note != null && !note.equals("-1")){
									checkin.put("note", note);
									checkinValid = true;
								}
								if (checkinValid) {
									tmp.put("checkin", checkin);
								}
							}
							
							result.add(tmp);
						} while (mCursor.moveToNext());
					} catch (JSONException e) {
						e.printStackTrace();
						result = null;
					}
					mCursor.close();
					mCursor = null;
//					Log.e("dbhelper current trip data", "result= " + result);
					return result;
				}
				mCursor.close();
				mCursor = null;
			}
		}
		return null;
	}
	
	/*
	 * 
	 */
	synchronized public JSONObject getSmallTripData(String userid, String tripid) {
//		Log.i("DBHelper2", "getOneTripData: received(" + userid + ", " + tripid + ")");
		if (db.isOpen()) {
			Cursor mCursor = db.query(TRIP_DATA_TABLE, null, "tripid=" + tripid, null, null, null, null);
			
			if (mCursor != null) {
				if (mCursor.moveToFirst()) {
					Log.e("getonetripdata", "small trip");
					// small trip, return normal jsonobject result
					JSONObject result = new JSONObject();
					try {
						JSONArray cidl = new JSONArray();
						do {
							JSONObject tmp = new JSONObject();
							
							tmp.put("lat", mCursor.getDouble(mCursor.getColumnIndexOrThrow("latitude")));
							tmp.put("lng", mCursor.getDouble(mCursor.getColumnIndexOrThrow("longitude")));
							tmp.put("timestamp", new Timestamp(Long.valueOf(mCursor.getString(mCursor.getColumnIndexOrThrow("timestamp")))).toString());
							tmp.put("alt", mCursor.getString(mCursor.getColumnIndexOrThrow("altitude")));
							tmp.put("spd", mCursor.getString(mCursor.getColumnIndexOrThrow("speed")));
							tmp.put("bear", mCursor.getString(mCursor.getColumnIndexOrThrow("bearing")));
							tmp.put("accu", mCursor.getString(mCursor.getColumnIndexOrThrow("accuracy")));
							// empty jsonobject to put in check-in info
							JSONObject checkin = new JSONObject();
							// if picture is not null nor "null", put it in the
							// checkin object
							if (mCursor.getString(mCursor.getColumnIndexOrThrow("picture")) != null && !mCursor.getString(mCursor.getColumnIndexOrThrow("picture")).equals("-1")) {
								// picture exists! put it in
								checkin.put("picture_uri", mCursor.getString(mCursor.getColumnIndexOrThrow("picture")).substring(mCursor.getString(mCursor.getColumnIndexOrThrow("picture")).lastIndexOf("/") + 1));
							}
							
							// if emotion value is not null nor "null", put it
							// in the checkin object
							if (mCursor.getString(mCursor.getColumnIndexOrThrow("emotion")) != null && !mCursor.getString(mCursor.getColumnIndexOrThrow("emotion")).equals("-1")) {
								// emotion exists! put it in
								checkin.put("emotion", mCursor.getString(mCursor.getColumnIndexOrThrow("emotion")));
							}
							
							// if check-in text is not null nor "null", put it
							// in the checkin object
							if (mCursor.getString(mCursor.getColumnIndexOrThrow("note")) != null && !mCursor.getString(mCursor.getColumnIndexOrThrow("note")).equals("-1")) {
								// message exists! put it in
								checkin.put("message", mCursor.getString(mCursor.getColumnIndexOrThrow("note")));
							}
							// upload if at least one key/value pair in checkin
							// object
							if (checkin.length() > 0) {
								tmp.put("CheckIn", checkin);
							}
							cidl.put(tmp);
						} while (mCursor.moveToNext());
						result.put("CheckInDataList", cidl);
						result.put("userid", userid);
					} catch (JSONException e) {
						e.printStackTrace();
						result = null;
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
						result = null;
					}
					if (!mCursor.isClosed()) {
						mCursor.close();
					}
					return result;
				}
				if (!mCursor.isClosed()) {
					mCursor.close();
				}
			}
		}
		return null;
	}
	
	synchronized public JSONObject getLargeTripData(String userid, String tripid){
		//order query with timestamp
		Log.e("getonetripdata", "large trip");
		if(db.isOpen()){
			//XXX the tripid here might be servertripid, but the checking is left for the caller to do
			Cursor mCursor = db.query(TRIP_DATA_TABLE, null, "tripid=" + tripid + " AND uploadstatus > -1", null, null, null, "id ASC", String.valueOf(largeTripThreshold));
			if(mCursor != null){
				if (mCursor.moveToFirst()) {
					JSONObject result = new JSONObject();
					try {
						JSONArray cidl = new JSONArray();
						int lastid = -1;
						do{
							JSONObject tmp = new JSONObject();
							lastid = mCursor.getInt(mCursor.getColumnIndexOrThrow("id"));
							
							//tripid is to be put at the same level as checkindatalist, not in each and every point
//							tmp.put("trip_id", mCursor.getString(mCursor.getColumnIndexOrThrow("tripid")));
							
							tmp.put("lat", mCursor.getDouble(mCursor.getColumnIndexOrThrow("latitude")));
							tmp.put("lng", mCursor.getDouble(mCursor.getColumnIndexOrThrow("longitude")));
							tmp.put("timestamp", new Timestamp(Long.valueOf(mCursor.getString(mCursor.getColumnIndexOrThrow("timestamp")))).toString());
							tmp.put("alt", mCursor.getString(mCursor.getColumnIndexOrThrow("altitude")));
							tmp.put("spd", mCursor.getString(mCursor.getColumnIndexOrThrow("speed")));
							tmp.put("bear", mCursor.getString(mCursor.getColumnIndexOrThrow("bearing")));
							tmp.put("accu", mCursor.getString(mCursor.getColumnIndexOrThrow("accuracy")));
							
							// empty jsonobject to put in check-in info
							JSONObject checkin = new JSONObject();
							if (mCursor.getString(mCursor.getColumnIndexOrThrow("picture")) != null && !mCursor.getString(mCursor.getColumnIndexOrThrow("picture")).equals("-1")) {
								// picture exists! put it in this call is for uploading, put in only the filename
								checkin.put("picture_uri", mCursor.getString(mCursor.getColumnIndexOrThrow("picture")).substring(mCursor.getString(mCursor.getColumnIndexOrThrow("picture")).lastIndexOf("/") + 1));
							}
							
							// if emotion value is not null nor "null", put it in the checkin object
							if (mCursor.getString(mCursor.getColumnIndexOrThrow("emotion")) != null && !mCursor.getString(mCursor.getColumnIndexOrThrow("emotion")).equals("-1")) {
								// emotion exists! put it in
								checkin.put("emotion", mCursor.getString(mCursor.getColumnIndexOrThrow("emotion")));
							}
							
							// if check-in text is not null nor "null", put it in the checkin object
							if (mCursor.getString(mCursor.getColumnIndexOrThrow("note")) != null && !mCursor.getString(mCursor.getColumnIndexOrThrow("note")).equals("-1")) {
								// message exists! put it in
								checkin.put("message", mCursor.getString(mCursor.getColumnIndexOrThrow("note")));
							}
							// upload if at least one key/value pair in checkin object
							if (checkin.length() > 0) {
								tmp.put("CheckIn", checkin);
							}
							cidl.put(tmp);
						} while (mCursor.moveToNext());
						result.put("CheckInDataList", cidl);
						result.put("userid", userid);
						result.put("trip_id", tripid);
						result.put("lastpointid", lastid);
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
						result = null;
					} catch (JSONException e) {
						e.printStackTrace();
						result = null;
					}
					
					mCursor.close();
					mCursor = null;
					
					return result;
				}
				if(!mCursor.isClosed()){
					mCursor.close();
				}
				mCursor = null;
			}
		}
		return null;
	}
	
	synchronized public long setUploadStatus(String tripid, int id){
		if(db.isOpen()){
			if(id == -1){ //invalid id
				return -1;
			}
			ContentValues cv = new ContentValues();
			cv.put("uploadstatus", -1);
			return db.update(TRIP_DATA_TABLE, cv, "tripid=" + tripid + " AND id <=" + id, null);
		}
		return -2;
	}
	
	private Integer getTripLength(String tripid){
		if(db.isOpen()){
			Cursor mCursor = db.query(TRIP_DATA_TABLE, new String[]{"latitude", "longitude"}, "tripid=" + tripid + " AND latitude > -998 AND accuracy < 1499", null, null, null, "timestamp ASC");
			if(mCursor != null){
				if(mCursor.moveToFirst()){
					ArrayList<Location> list = new ArrayList<Location>();
					do{
						Location loc = new Location("");
						loc.setLatitude(mCursor.getDouble(mCursor.getColumnIndex("latitude")));
						loc.setLongitude(mCursor.getDouble(mCursor.getColumnIndex("longitude")));
						list.add(loc);
					}while(mCursor.moveToNext());
					mCursor.close();
					mCursor = null;
					return ((int) calcTripLength(list));
				}
				mCursor.close();
				mCursor = null;
			}
		}
		return 0;
	}
	
	private double calcTripLength(ArrayList<Location> list){
		if(list != null){
			if(list.size() > 1){
				double length = 0.0;
				Location previous = null;
				for(Location item : list){
					if(previous != null){
						length = greatCircleDistance(previous, item);
					}
					previous = item;
				}
				return length;
			}
		}
		return 0.0;
	}
	
	private final Double radius = 6371008.7714;
	
	private Double greatCircleDistance(Location one, Location two) {
		Double dlat = toRad(two.getLatitude() - one.getLatitude());
		Double dlon = toRad(two.getLongitude() - one.getLongitude());
		Double latone = toRad(one.getLatitude());
		Double lattwo = toRad(two.getLatitude());
		Double a = Math.sin(dlat / 2) * Math.sin(dlat / 2) + Math.sin(dlon / 2) * Math.sin(dlon / 2) * Math.cos(latone)
				* Math.cos(lattwo);
		Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return radius * c;
	}
	
	private Double toRad(Double degree) {
		return degree / 180 * Math.PI;
	}
	
	private void fixtripinfo(String tripid){
		if(db.isOpen()){
			
			/*complete trip info includes 
			"(id INTEGER PRIMARY KEY, " 
			+ "tripid TEXT, " 
			+ "starttime TEXT, "
			+ "endtime TEXT, " 
			+ "length INTEGER, " 
			+ "count INTEGER, " 
			*/
			
			ContentValues cv = new ContentValues();
			cv.put("tripid", tripid);
			cv.put("starttime", String.valueOf(getOnePoint(tripid, true, false).getTime()));
			cv.put("endtime", String.valueOf(getOnePoint(tripid, false, false).getTime()));
//			cv.put("length", getTripLength(tripid));
			cv.put("count", (int) getNumberofPoints(tripid));
			db.insert(TRIP_INFO_TABLE, null, cv);
		}
	}
	
	private void checktripinfo(String tripid){
		if(db.isOpen()){
			Cursor mCursor = db.query(TRIP_INFO_TABLE, null, "tripid=" + tripid, null, null, null, null);
			if(mCursor != null){
				if(mCursor.moveToFirst()){
					ContentValues cv = new ContentValues();
					if(mCursor.getString(mCursor.getColumnIndexOrThrow("starttime")).equals("START")){
						cv.put("starttime", String.valueOf(getOnePoint(tripid, true, false).getTime()));
					}
					if(mCursor.getString(mCursor.getColumnIndexOrThrow("endtime")).equals("END")){
						cv.put("endtime", String.valueOf(getOnePoint(tripid, false, false).getTime()));
					}
					if(mCursor.getInt(mCursor.getColumnIndexOrThrow("count")) == -1){
						cv.put("count", (int) getNumberofPoints(tripid));
					}
					//if there's anything to be updated
					if(cv.size() > 0){
						db.update(TRIP_INFO_TABLE, cv, "tripid=" + tripid, null);
					}
				}
			}
		}
	}
	
	private void checkDBIntegrity(String currentTripid){
		if(db.isOpen()){
			//first check DB integrity
			Cursor mDataCursor = null;
			if (currentTripid != null) {
				mDataCursor = db.query(true, TRIP_DATA_TABLE, new String[]{"tripid"}, "tripid!=" + currentTripid, null, null, null, null, null);
			} else{
				mDataCursor = db.query(true, TRIP_DATA_TABLE, new String[]{"tripid"}, null, null, null, null, null, null);
			}
			HashSet<String> dataTripid = new HashSet<String>();
			if(mDataCursor != null){
				if(mDataCursor.moveToFirst()){
					//we have at least one entry
					do{
						dataTripid.add(mDataCursor.getString(0));
					}while(mDataCursor.moveToNext());
					mDataCursor.close();
					mDataCursor = null;
					if(!dataTripid.isEmpty()){
						//we have at least one tripid, now query tripinfo to check for integrity
						Cursor mInfoCursor = null;
						if(currentTripid != null){
							mInfoCursor = db.query(true, TRIP_INFO_TABLE, new String[]{"tripid", "servertripid"}, "tripid!=" + currentTripid, null, null, null, null, null);
						} else{
							mInfoCursor = db.query(true, TRIP_INFO_TABLE, new String[]{"tripid", "servertripid"}, null, null, null, null, null, null);
						}
						HashSet<String> infoTripid = new HashSet<String>();
						if(mInfoCursor != null){
							if(mInfoCursor.moveToFirst()){
								//at least one entry
								do{
									if(mInfoCursor.getString(1) != null && !mInfoCursor.getString(1).equals("-1")){
										//server tripid exists
										infoTripid.add(mInfoCursor.getString(1));
									} else{
										//no server tripid
										infoTripid.add(mInfoCursor.getString(0));
									}
								}while(mInfoCursor.moveToNext());
								mInfoCursor.close();
								mInfoCursor = null;
								
								//just in case there's any column missing in trip info
								//check if any column is still using default value...
								//notably starttime, endtime and count, because name and length are fine with the default values
								for(String item : infoTripid){
									checktripinfo(item);
								}
								
								//now we start the matching...
								if(infoTripid.containsAll(dataTripid)){
									//all the entries in info tripid set matches the entries in data tripid set
								} else{
									//entries from two sets do not match, time to rebuild the trip info table!
									
									//first we check which tripids are missing from trip info table
									//remove all the tripids that appeaers in the info tripids set
									//what's left in the data tripid set are the ones missing in the trip info table
									dataTripid.removeAll(infoTripid);
									
									//now we iterate through all the leftover tripids
									for(String item : dataTripid){
										fixtripinfo(item);
									}
								}
							}
						}
					}
				}
			}
			dataTripid = null;
		}
	}
	
	//should also check DB integrity...
	synchronized public JSONArray getAllTripInfoForHTML2(String currentTripid) {
		if (db.isOpen()) {
			
			checkDBIntegrity(currentTripid);
			
			Cursor mInfoCursor = null;
			if (currentTripid != null) {
				//there is a on going trip, don't show it in the trip list
				//Log.e("getalltripinfoforhtml", "tripid!=null");
				mInfoCursor = db.query(TRIP_INFO_TABLE, null, "tripid!=" + currentTripid, null, null, null, "starttime DESC");
			} else {
				//there is no on going trip, show everything
				//Log.e("getalltripinfoforhtml", "tripid=null");
				mInfoCursor = db.query(TRIP_INFO_TABLE, null, null, null, null, null, "starttime DESC");
			}
			
			if (mInfoCursor != null) {
				if (mInfoCursor.moveToFirst()) {
					JSONArray list = null;
					try {
						list = new JSONArray();
						// movetofirst is true, there is at least one entry of data
						do {
							// construct one entry of trip info
							JSONObject tmp = new JSONObject();
							tmp.put("id", mInfoCursor.getInt(mInfoCursor.getColumnIndexOrThrow("id")));
							tmp.put("trip_id", mInfoCursor.getString(mInfoCursor.getColumnIndexOrThrow("tripid")));
							tmp.put("trip_length", mInfoCursor.getInt(mInfoCursor.getColumnIndexOrThrow("length")));
							tmp.put("trip_et", new Timestamp(Long.valueOf(mInfoCursor.getString(mInfoCursor.getColumnIndexOrThrow("endtime")))).toString());
							tmp.put("trip_st", new Timestamp(Long.valueOf(mInfoCursor.getString(mInfoCursor.getColumnIndexOrThrow("starttime")))).toString());
							tmp.put("trip_name", mInfoCursor.getString(mInfoCursor.getColumnIndexOrThrow("name")));
							tmp.put("num_of_pts", mInfoCursor.getInt(mInfoCursor.getColumnIndexOrThrow("count")));
							tmp.put("et_addr_prt2", mInfoCursor.getString(mInfoCursor.getColumnIndexOrThrow("endaddrpt2")));
							tmp.put("et_addr_prt3", mInfoCursor.getString(mInfoCursor.getColumnIndexOrThrow("endaddrpt3")));
							tmp.put("et_addr_prt4", mInfoCursor.getString(mInfoCursor.getColumnIndexOrThrow("endaddrpt4")));
							tmp.put("st_addr_prt2", mInfoCursor.getString(mInfoCursor.getColumnIndexOrThrow("startaddrpt2")));
							tmp.put("st_addr_prt3", mInfoCursor.getString(mInfoCursor.getColumnIndexOrThrow("startaddrpt3")));
							tmp.put("st_addr_prt4", mInfoCursor.getString(mInfoCursor.getColumnIndexOrThrow("startaddrpt4")));
							tmp.put("triplistheader", mInfoCursor.getInt(mInfoCursor.getColumnIndexOrThrow("triplistheader")));
							// put the entry in the list
							list.put(tmp);
						} while (mInfoCursor.moveToNext());
						// jsonarray all filled, put into result object
					} catch (JSONException e) {
						e.printStackTrace();
						list = null;
					}
					//close the cursor
					if (!mInfoCursor.isClosed()) {
						mInfoCursor.close();
						mInfoCursor = null;
					}
					//if the list is empty, set list=null, we don't want to return empty list
					if(list != null && list.length() < 1){
						Log.w("dbhelper2", "get local tripinfo warning, empty list");
						list = null;
					}
					return list;
				}
				if (!mInfoCursor.isClosed()) {
					mInfoCursor.close();
					mInfoCursor = null;
				}
			}
		}
		return null;
	}
	
	//XXX check, this method is called from both deleting local trip and upload complete...
	synchronized public long deleteLocalTrip(String tripid){
		if(db.isOpen()){
			Cursor mCursor = db.query(TRIP_INFO_TABLE, new String[]{"servertripid"}, "tripid=" + tripid, null, null, null, null);
			if(mCursor != null){
				if(mCursor.moveToFirst()){
					String servertripid = mCursor.getString(mCursor.getColumnIndexOrThrow("servertripid"));
					long rows = db.delete(TRIP_INFO_TABLE, "tripid=" + tripid, null);
					if(servertripid != null && !servertripid.equals("-1")){
						rows += db.delete(TRIP_DATA_TABLE, "tripid=" + servertripid, null);
					} else{
						rows += db.delete(TRIP_DATA_TABLE, "tripid=" + tripid, null);
					}
					return rows;
				}
			}
		} 
		return -2;
	}
	
	/**
	 * check if DB is opened
	 * 
	 * @return true if opened, false if not
	 */
	public boolean DBIsOpen() {
		if (db != null) {
			return db.isOpen();
		} else {
			return false;
		}
	}
	
	/**
	 * Close the database
	 */
	public void closeDB() {
		if (db != null) { // if null, close will cause null pointer exception
			db.close();
		}
	}
	
	public void exportEverything() {
		if (db.isOpen()) {
			try {
				//XXX emmc?
				FileOutputStream fos = new FileOutputStream("/sdcard/" + System.currentTimeMillis() + ".txt");
				OutputStreamWriter osw = new OutputStreamWriter(fos);
				Cursor mCursor = db.query(TRIP_INFO_TABLE, null, null, null, null, null, null);
				if (mCursor != null) {
					if (mCursor.moveToFirst()) {
						JSONObject result = new JSONObject();
						try {
							JSONArray list = new JSONArray();
							// movetofirst is true, there is at least one entry
							// of data
							do {
								JSONObject tmp = new JSONObject();
								String [] names = mCursor.getColumnNames();
								for(String item : names){
									tmp.put(item, mCursor.getString(mCursor.getColumnIndex(item)));
								}
								// put the entry in the list
								list.put(tmp);
							} while (mCursor.moveToNext());
							// jsonarray all filled, put into result object
							result.put("tripInfoList", list);
							list = null;
						} catch (JSONException e) {
							e.printStackTrace();
							// if anything goes wrong, return null
							if (!mCursor.isClosed()) {
								mCursor.close();
							}
							return;
						}
						// XXX save result to file
						osw.write(result.toString());
						osw.write("\n\n\n\n");
					} else {
						if (!mCursor.isClosed()) {
							mCursor.close();
						}
						return;
					}
				} else {
					return;
				}
				mCursor = null;
				mCursor = db.query(TRIP_DATA_TABLE, null, null, null, null, null, null);
				if (mCursor != null) {
					if (mCursor.moveToFirst()) {
//						JSONObject result = new JSONObject();
						osw.append("{\"CheckInDataList\":[");
						try {
							// movetofirst is true, there is at least one entry
							// of data
							do {
								JSONObject tmp = new JSONObject();
								String[] names = mCursor.getColumnNames();
								for(String item : names){
									tmp.put(item, mCursor.getString(mCursor.getColumnIndex(item)));
								}
								
								osw.append(tmp.toString() + ",");
							} while (mCursor.moveToNext());
							// jsonarray all filled, put into result object
							osw.append("]");
						} catch (JSONException e) {
							e.printStackTrace();
							// if anything goes wrong, return null
							if (!mCursor.isClosed()) {
								mCursor.close();
							}
							return;
						}
						// all is well
						if (!mCursor.isClosed()) {
							mCursor.close();
						}
						
						osw.flush();
						osw.close();
						return;
					} else {
						if (!mCursor.isClosed()) {
							mCursor.close();
						}
						if(osw != null){
							osw.close();
						}
						return;
					}
				} else {
					if(osw != null){
						osw.close();
					}
					return;
				}
			} catch (IOException e) {
				return;
			}
		} else {
			return;
		}
	}
}