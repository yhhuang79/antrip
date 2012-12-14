package tw.plash.antrip.offline;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
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
	private static final int DATABASE_VERSION = 1; // required by SQLite tool
	
	private static final String TRIP_DATA_TABLE = "tripdatatable";
	private static final String TRIP_INFO_TABLE = "tripinfotable";
	
	private Context mContext;
	
	private SQLiteDatabase db;
	
	private static final String CREATE_TABLE_TRIP_DATA = "CREATE TABLE " + TRIP_DATA_TABLE
			+ "(id INTEGER PRIMARY KEY, " 
			+ "latitude REAL, " 
			+ "longitude REAL, " 
			+ "timestamp TEXT, "
			+ "altitude TEXT, " 
			+ "speed TEXT, " 
			+ "bearing TEXT, " 
			+ "accuracy TEXT, " 
			+ "accelerometerX TEXT, " 
			+ "accelerometerY TEXT, " 
			+ "accelerometerZ TEXT, " 
			+ "tripid TEXT, " 
			+ "picture TEXT, " 
			+ "emotion TEXT, "
			+ "note TEXT, " 
			+ "uploadstatus INTEGER default 0)";
	
	private static final String CREATE_TABLE_TRIP_INFO = "CREATE TABLE " + TRIP_INFO_TABLE
			+ "(id INTEGER PRIMARY KEY, " 
			+ "tripid TEXT, " 
			+ "name TEXT default 'Untitled Trip', " 
			+ "starttime TEXT, "
			+ "endtime TEXT, " 
			+ "length INTEGER, " 
			+ "count INTEGER, " 
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
			+ "servertripid TEXT, "
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
	public DBHelper2(Context context) {
		mContext = context;
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
			ContentValues cv = new ContentValues();
			cv.put("latitude", location.getLatitude());
			cv.put("longitude", location.getLongitude());
			cv.put("timestamp", new Timestamp(Long.valueOf(location.getTime())).toString());
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
			cv.put("timestamp", new Timestamp(Long.valueOf(loc.getTime())).toString());
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
			cv.put("starttime", stats.getButtonStartTime());
			cv.put("endtime", stats.getNonNullEndTime());
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
	
	synchronized public long updateUploadStage(String tripid, int stage){
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
	
	synchronized public String getTripid(String uniqueid){
		if(db.isOpen()){
			Cursor mCursor = db.query(TRIP_INFO_TABLE, new String[]{"tripid"}, "id=" + uniqueid, null, null, null, null);
			if(mCursor != null){
				if(mCursor.moveToFirst()){
					String tripid = mCursor.getString(mCursor.getColumnIndexOrThrow("tripid"));
					mCursor.close();
					return tripid;
				} else{
					if(!mCursor.isClosed()){
						mCursor.close();
					}
					return null;
				}
			} else{
				return null;
			}
		} else{
			return null;
		}
	}
	
	/**
	 * to mark the upload status of a certain record or an entire trip, depending on the input
	 * @param userid
	 * @param actualTripid
	 * @param table, 0: trip info, 1: trip data
	 * @param code, 
	 * upload stage, 
	 * tripinfo: 0 -> not uploaded, 1~7 -> stages; 
	 * upload status, 
	 * tripdata: 0 -> not uploaded, 1 -> data uploaded, not pic, 2 -> data and pic all uploaded, 3->some pics not uploaded
	 * @param imagepath, the image to mark uploaded, null if no image was uploaded
	 * @return number of rows updated, or -2 if any error occurs
	 */
	synchronized public int markUploaded(String localTripid, int table, int code, String imagepath) {
		if (db.isOpen()) {
			ContentValues cv = new ContentValues();
			switch(table){
			case 0:
				cv.put("uploadstage", code);
				return db.update(TRIP_INFO_TABLE, cv, " AND tripid=" + localTripid, null);
			case 1:
				cv.put("uploadstatus", code);
				if(imagepath != null){
					return db.update(TRIP_DATA_TABLE, cv, " AND tripid=" + localTripid + " AND picture LIKE '%" + imagepath.substring(imagepath.lastIndexOf("/") + 1) + "'", null);
				} else{
					return db.update(TRIP_DATA_TABLE, cv, " AND tripid=" + localTripid, null);
				}
			default:
				return -3;
			}
		} else {
			//db is not opened
			return -2;
		}
	}
	
	synchronized public long setServerTripid(String localTripid, String serverTripid){
		if(db.isOpen()){
			ContentValues cv = new ContentValues();
			cv.put("servertripid", serverTripid);
			return db.update(TRIP_INFO_TABLE, cv, "tripid=" + localTripid, null);
		} else{
			return -1;
		}
	}
	
	synchronized public long getNumberofPoints(String tripid) {
		String sql = "SELECT COUNT(id) FROM " + TRIP_DATA_TABLE + " WHERE tripid=" + tripid;
		if (db.isOpen()) {
			SQLiteStatement statement = db.compileStatement(sql);
			long result = statement.simpleQueryForLong();
			statement.close();
			return result;
		} else {
			return -2;
		}
	}
	
	synchronized public CachedPoints getOnePoint(String tripid, boolean wantFirst) {
		if (db.isOpen()) {
			Cursor mCursor = db.query(TRIP_DATA_TABLE, new String[] { "latitude", "longitude" }, "tripid=" + tripid
					+ " AND latitude!=-999", null, null, null, wantFirst ? "id ASC" : "id DESC", "1");
			if (mCursor != null) {
				if (mCursor.moveToFirst()) {
					CachedPoints cp = new CachedPoints(mCursor.getDouble(mCursor.getColumnIndex("latitude")),
							mCursor.getDouble(mCursor.getColumnIndex("longitude")));
					if (!mCursor.isClosed()) {
						mCursor.close();
					}
					Log.e("dbhelper", "getonepoint: " + cp.latitude + ", " + cp.longitude);
					return cp;
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
	
	synchronized public JSONObject getOneTripInfo(String tripid) {
		if (db.isOpen()) {
			Cursor mCursor = db.query(TRIP_INFO_TABLE, null, "tripid=" + tripid, null, null,
					null, null);
			if (mCursor != null) {
				if (mCursor.moveToFirst()) {
					JSONObject tmp = new JSONObject();
					try {
						tmp.put("update_status", "3");
						
						tmp.put("trip_length", mCursor.getInt(mCursor.getColumnIndexOrThrow("length")));
						tmp.put("trip_et", mCursor.getString(mCursor.getColumnIndexOrThrow("endtime")));
						tmp.put("trip_st", mCursor.getString(mCursor.getColumnIndexOrThrow("starttime")));
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
	
	synchronized public ArrayList<String> getOneTripPicturePaths(String tripid, boolean needall){
		if(db.isOpen()){
			Cursor mCursor;
			if(needall){
				mCursor = db.query(TRIP_DATA_TABLE, new String[]{"picture"}, "tripid=" + tripid + " AND picture!='NULL'", null, null, null, null);
			} else{
				mCursor = db.query(TRIP_DATA_TABLE, new String[]{"picture"}, "tripid=" + tripid + " AND picture!='NULL' AND uploadstatus<2", null, null, null, null);
			}
			if(mCursor != null){
				if(mCursor.moveToFirst()){
					ArrayList<String> result = new ArrayList<String>();
					do{
						result.add(mCursor.getString(mCursor.getColumnIndexOrThrow("picture")));
					} while(mCursor.moveToNext());
					mCursor.close();
					return result;
				} else{
					if(!mCursor.isClosed()){
						mCursor.close();
					}
					return null;
				}
			} else{
				return null;
			}
		} else{
			return null;
		}
	}
	
	synchronized public Location getLatestPointFromCurrentTripData(String currentTripid){
		if(db.isOpen()){
			Cursor mCursor = db.query(TRIP_DATA_TABLE, null, "tripid=" + currentTripid, null, null, null, "timestamp DESC", "1");
			if(mCursor != null){
				if(mCursor.moveToFirst()){
					Location loc = new Location("");
					
					loc.setAccuracy(Float.parseFloat(mCursor.getString(mCursor.getColumnIndexOrThrow("accuracy"))));
					loc.setAltitude(Double.parseDouble(mCursor.getString(mCursor.getColumnIndexOrThrow("altitude"))));
					loc.setBearing(Float.parseFloat(mCursor.getString(mCursor.getColumnIndexOrThrow("bearing"))));
					loc.setLatitude(Double.parseDouble(mCursor.getString(mCursor.getColumnIndexOrThrow("latitude"))));
					loc.setLongitude(Double.parseDouble(mCursor.getString(mCursor.getColumnIndexOrThrow("longitude"))));
					loc.setSpeed(Float.parseFloat(mCursor.getString(mCursor.getColumnIndexOrThrow("speed"))));
					long time = 0;
					DateFormat.format(mCursor.getString(mCursor.getColumnIndexOrThrow("timestamp")), time);
					Log.w("dbhelper2", "getLatestPointFromCurrentTripData: timestamp-time= " + mCursor.getString(mCursor.getColumnIndexOrThrow("timestamp")) + "-" + time);
					loc.setTime(time);
					
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
	
	synchronized public ArrayList<JSONObject> getCurrentTripData(String currentTripid) {
		if (db.isOpen()) {
			Cursor mCursor = db.query(TRIP_DATA_TABLE, 
					new String[] { "latitude", "longitude", "picture", "emotion","note" }, 
					"tripid=" + currentTripid + " AND accuracy<1500 AND latitude > -998", 
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
							
							tmp.put("latitude", mCursor.getDouble(mCursor.getColumnIndexOrThrow("latitude")));
							tmp.put("longitude", mCursor.getDouble(mCursor.getColumnIndexOrThrow("longitude")));
							{
								boolean checkinValid = false;
								
								String pic = mCursor.getString(mCursor.getColumnIndexOrThrow("picture"));
								String emo = mCursor.getString(mCursor.getColumnIndexOrThrow("emotion"));
								String note = mCursor.getString(mCursor.getColumnIndexOrThrow("note"));
								
								JSONObject checkin = new JSONObject();
								if(pic != null && pic.length() > 0){
									checkin.put("picture", pic);
									checkinValid = true;
								}
								if(emo != null && emo.length() > 0){
									checkin.put("emotion", emo);
									checkinValid = true;
								}
								if(note != null && note.length() > 0){
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
					return result;
				}
				mCursor.close();
				mCursor = null;
			}
		}
		return null;
	}
	
	/**
	 * get a complete trip data wrapped in CheckInDataList styled JSONObject
	 * format
	 * 
	 * @param userid
	 * @param tripid
	 * @return CheckInDataList styled trip data JSONObject
	 */
	synchronized public JSONObject getOneTripData(String tripid, boolean forUpload) {
		if (db.isOpen()) {
			Cursor mCursor = db.query(TRIP_DATA_TABLE, null, "tripid=" + tripid, null, null,
					null, "id ASC");
			
			if (mCursor != null) {
				if (mCursor.moveToFirst()) {
					JSONObject result = new JSONObject();
					try {
						JSONArray cidl = new JSONArray();
						do {
							JSONObject tmp = new JSONObject();
							tmp.put("lat", mCursor.getDouble(mCursor.getColumnIndexOrThrow("latitude")));
							tmp.put("lng", mCursor.getDouble(mCursor.getColumnIndexOrThrow("longitude")));
							tmp.put("timestamp", mCursor.getString(mCursor.getColumnIndexOrThrow("timestamp")));
							tmp.put("alt", mCursor.getString(mCursor.getColumnIndexOrThrow("altitude")));
							tmp.put("spd", mCursor.getString(mCursor.getColumnIndexOrThrow("speed")));
							tmp.put("bear", mCursor.getString(mCursor.getColumnIndexOrThrow("bearing")));
							tmp.put("accu", mCursor.getString(mCursor.getColumnIndexOrThrow("accuracy")));
							// empty jsonobject to put in check-in info
							JSONObject checkin = new JSONObject();
							//if picture is not null nor "null", put it in the checkin object
							if (mCursor.getString(mCursor.getColumnIndexOrThrow("picture")) != null && !mCursor.getString(mCursor.getColumnIndexOrThrow("picture")).equalsIgnoreCase("null")) {
								// picture exists! put it in
								// XXX cannot pass the complete path, need to
								// strip it till only filename is left
								if (forUpload) {
									// this call is for uploading, put in only
									// the filename
									checkin.put("picture_uri",mCursor.getString(mCursor.getColumnIndexOrThrow("picture")).substring(mCursor.getString(mCursor.getColumnIndexOrThrow("picture")).lastIndexOf("/")+1));
								} else {
									// return complete path, this call is for
									// drawing purpose
									checkin.put("picture_uri",mCursor.getString(mCursor.getColumnIndexOrThrow("picture")));
								}
							}
							
							//if emotion value is not null nor "null", put it in the checkin object
							if (mCursor.getString(mCursor.getColumnIndexOrThrow("emotion")) != null && !mCursor.getString(mCursor.getColumnIndexOrThrow("emotion")).equalsIgnoreCase("null")) {
								// emotion exists! put it in
								checkin.put("emotion", mCursor.getString(mCursor.getColumnIndexOrThrow("emotion")));
							}
							
							//if check-in text is not null nor "null", put it in the checkin object
							if (mCursor.getString(mCursor.getColumnIndexOrThrow("note")) != null && !mCursor.getString(mCursor.getColumnIndexOrThrow("note")).equalsIgnoreCase("null")) {
								// message exists! put it in
								checkin.put("message", mCursor.getString(mCursor.getColumnIndexOrThrow("note")));
							}
							// upload if at least one key/value pair in checkin object
							if(checkin.length() > 0){
								tmp.put("CheckIn", checkin);
							}
							cidl.put(tmp);
						} while (mCursor.moveToNext());
						result.put("CheckInDataList", cidl);
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
	
	synchronized public JSONObject getOneTripData(Integer id) {
		if (db.isOpen()) {
			Cursor tmpCursor = db.query(TRIP_INFO_TABLE, null, "id=" + id, null, null, null, null);
			String tripid = tmpCursor.moveToFirst() ? tmpCursor.getString(tmpCursor.getColumnIndexOrThrow("tripid"))
					: "-1";
			
			Cursor mCursor = db.query(TRIP_DATA_TABLE, null, "tripid=" + tripid, null, null,
					null, "id ASC");
			if (mCursor != null) {
				if (mCursor.moveToFirst()) {
					JSONObject result = new JSONObject();
					try {
						result.put("trip_id", tripid);
						JSONArray cidl = new JSONArray();
						do {
							JSONObject tmp = new JSONObject();
							tmp.put("lat", mCursor.getDouble(mCursor.getColumnIndexOrThrow("latitude")));
							tmp.put("lng", mCursor.getDouble(mCursor.getColumnIndexOrThrow("longitude")));
							tmp.put("timestamp", mCursor.getString(mCursor.getColumnIndexOrThrow("timestamp")));
							tmp.put("alt", mCursor.getString(mCursor.getColumnIndexOrThrow("altitude")));
							tmp.put("spd", mCursor.getString(mCursor.getColumnIndexOrThrow("speed")));
							tmp.put("bear", mCursor.getString(mCursor.getColumnIndexOrThrow("bearing")));
							tmp.put("accu", mCursor.getString(mCursor.getColumnIndexOrThrow("accuracy")));
							// empty jsonobject to put in check-in info
							JSONObject checkin = new JSONObject();
							if (mCursor.getString(mCursor.getColumnIndexOrThrow("picture")) != null && !mCursor.getString(mCursor.getColumnIndexOrThrow("picture")).equalsIgnoreCase("null")) {
								// picture exists! put it in
								// XXX cannot pass the complete path, need to
								// strip it till only filename is left
								checkin.put("picture_uri", mCursor.getString(mCursor.getColumnIndexOrThrow("picture")));
							}
							if (mCursor.getString(mCursor.getColumnIndexOrThrow("emotion")) != null && !mCursor.getString(mCursor.getColumnIndexOrThrow("emotion")).equalsIgnoreCase("null")) {
								// emotion exists! put it in
								checkin.put("emotion", mCursor.getString(mCursor.getColumnIndexOrThrow("emotion")));
							}
							if (mCursor.getString(mCursor.getColumnIndexOrThrow("note")) != null && !mCursor.getString(mCursor.getColumnIndexOrThrow("note")).equalsIgnoreCase("null")) {
								// message exists! put it in
								checkin.put("message", mCursor.getString(mCursor.getColumnIndexOrThrow("note")));
							}
							if (checkin.length() > 0) {
								tmp.put("CheckIn", checkin);
							}
							cidl.put(tmp);
						} while (mCursor.moveToNext());
						result.put("CheckInDataList", cidl);
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
	
	synchronized public HashSet<String> getTripIdsFromTripData(String excludeThisTid){
		if(db.isOpen()){
			Cursor mCursor = null;
			if(excludeThisTid != null){
				mCursor = db.query(TRIP_DATA_TABLE, new String[]{"tripid"}, "tripid !=" + excludeThisTid, null, null, null, null);
			} else{
				mCursor = db.query(TRIP_DATA_TABLE, new String[]{"tripid"}, null, null, null, null, null);
			}
			if(mCursor != null){
				if(mCursor.moveToFirst()){
					HashSet<String> result = new HashSet<String>();
					do{
						//there should be plenty of duplicated trip IDs, but hashset does not allow duplicate entries, so this will not be an issue
						result.add(mCursor.getString(mCursor.getColumnIndexOrThrow("tripid")));
					} while(mCursor.moveToNext());
					mCursor.close();
					mCursor = null;
					return result;
				} else{
					mCursor.close();
					mCursor = null;
					return null;
				}
			} else{
				return null;
			}
		} else{
			return null;
		}
	}
	
	synchronized public HashSet<String> getTripIdsFromTripInfo(String excludeThisTid){
		if(db.isOpen()){
			Cursor mCursor = null;
			if(excludeThisTid != null){
				mCursor = db.query(TRIP_INFO_TABLE, new String[]{"tripid"}, "tripid !=" + excludeThisTid, null, null, null, null);
			} else{
				mCursor = db.query(TRIP_INFO_TABLE, new String[]{"tripid"}, null, null, null, null, null);
			}
			if(mCursor != null){
				if(mCursor.moveToFirst()){
					HashSet<String> result = new HashSet<String>();
					do{
						result.add(mCursor.getString(mCursor.getColumnIndexOrThrow("tripid")));
					} while(mCursor.moveToNext());
					mCursor.close();
					mCursor = null;
					return result;
				} else{
					mCursor.close();
					mCursor = null;
					return null;
				}
			} else{
				return null;
			}
		} else{
			return null;
		}
	}
	
	
	
	/**
	 * Get all trip info formatted to yu-hsiang's php component with the given
	 * userid
	 * 
	 * does not show ongoing trip is there is any
	 * 
	 * does not show any partially uploaded trip(upload stage > 0)
	 * 
	 * @param userid
	 * @return JSONObject format trip info
	 */
	@Deprecated
	synchronized public JSONObject getAllTripInfoForHTML(String currentTripid) {
		if (db.isOpen()) {
			Cursor mCursor = null;
			if (currentTripid != null) {
				//there is a on going trip, don't show it in the trip list
				//Log.e("getalltripinfoforhtml", "tripid!=null");
				mCursor = db.query(TRIP_INFO_TABLE, null, "tripid!=" + currentTripid, null,
						null, null, "starttime DESC");
			} else {
				//there is no on going trip, show everything
				//Log.e("getalltripinfoforhtml", "tripid=null");
				mCursor = db.query(TRIP_INFO_TABLE, null, null, null, null, null, "starttime DESC");
			}
			
			if (mCursor != null) {
				if (mCursor.moveToFirst()) {
					JSONObject result = new JSONObject();
					try {
						JSONArray list = new JSONArray();
						// movetofirst is true, there is at least one entry of
						// data
						do {
							// construct one entry of trip info
							JSONObject tmp = new JSONObject();
							tmp.put("id", mCursor.getInt(mCursor.getColumnIndexOrThrow("id")));
							// XXX
							
							// XXX
							// added and extra "a" to bypass eval function in
							// javascript from turning tripid into numbers with
							// scientific display format
							// XXX
							// tmp.put("trip_id", "a" +
							// mCursor.getString(mCursor.getColumnIndexOrThrow("tripid")));
							tmp.put("trip_id", mCursor.getString(mCursor.getColumnIndexOrThrow("tripid")));
							tmp.put("trip_length", mCursor.getInt(mCursor.getColumnIndexOrThrow("length")));
							tmp.put("trip_et", mCursor.getString(mCursor.getColumnIndexOrThrow("endtime")));
							tmp.put("trip_st", mCursor.getString(mCursor.getColumnIndexOrThrow("starttime")));
							tmp.put("trip_name", mCursor.getString(mCursor.getColumnIndexOrThrow("name")));
							tmp.put("num_of_pts", mCursor.getInt(mCursor.getColumnIndexOrThrow("count")));
							tmp.put("et_addr_prt2", mCursor.getString(mCursor.getColumnIndexOrThrow("endaddrpt2")));
							tmp.put("et_addr_prt3", mCursor.getString(mCursor.getColumnIndexOrThrow("endaddrpt3")));
							tmp.put("et_addr_prt4", mCursor.getString(mCursor.getColumnIndexOrThrow("endaddrpt4")));
							tmp.put("st_addr_prt2", mCursor.getString(mCursor.getColumnIndexOrThrow("startaddrpt2")));
							tmp.put("st_addr_prt3", mCursor.getString(mCursor.getColumnIndexOrThrow("startaddrpt3")));
							tmp.put("st_addr_prt4", mCursor.getString(mCursor.getColumnIndexOrThrow("startaddrpt4")));
							// put the entry in the list
							list.put(tmp);
						} while (mCursor.moveToNext());
						// jsonarray all filled, put into result object
						result.put("tripInfoList", list);
					} catch (JSONException e) {
						e.printStackTrace();
						// if anything goes wrong, return null
						if (!mCursor.isClosed()) {
							mCursor.close();
						}
						return null;
					}
					// all is well
					if (!mCursor.isClosed()) {
						mCursor.close();
					}
					return result;
				} else {
					// cursor is empty, close it
					if (!mCursor.isClosed()) {
						mCursor.close();
					}
					return null;
				}
			} else {
				// something wrong with the query
				return null;
			}
		} else {
			// db not even opened
			return null;
		}
	}
	
	synchronized public JSONArray getAllTripInfoForHTML2(String currentTripid) {
		if (db.isOpen()) {
			Cursor mCursor = null;
			if (currentTripid != null) {
				//there is a on going trip, don't show it in the trip list
				//Log.e("getalltripinfoforhtml", "tripid!=null");
				mCursor = db.query(TRIP_INFO_TABLE, null, "tripid!=" + currentTripid, null, null, null, "starttime DESC");
			} else {
				//there is no on going trip, show everything
				//Log.e("getalltripinfoforhtml", "tripid=null");
				mCursor = db.query(TRIP_INFO_TABLE, null, null, null, null, null, "starttime DESC");
			}
			
			if (mCursor != null) {
				if (mCursor.moveToFirst()) {
					JSONArray list = null;
					try {
						list = new JSONArray();
						// movetofirst is true, there is at least one entry of data
						do {
							// construct one entry of trip info
							JSONObject tmp = new JSONObject();
							tmp.put("id", mCursor.getInt(mCursor.getColumnIndexOrThrow("id")));
							tmp.put("trip_id", mCursor.getString(mCursor.getColumnIndexOrThrow("tripid")));
							tmp.put("trip_length", mCursor.getInt(mCursor.getColumnIndexOrThrow("length")));
							tmp.put("trip_et", mCursor.getString(mCursor.getColumnIndexOrThrow("endtime")));
							tmp.put("trip_st", mCursor.getString(mCursor.getColumnIndexOrThrow("starttime")));
							tmp.put("trip_name", mCursor.getString(mCursor.getColumnIndexOrThrow("name")));
							tmp.put("num_of_pts", mCursor.getInt(mCursor.getColumnIndexOrThrow("count")));
							tmp.put("et_addr_prt2", mCursor.getString(mCursor.getColumnIndexOrThrow("endaddrpt2")));
							tmp.put("et_addr_prt3", mCursor.getString(mCursor.getColumnIndexOrThrow("endaddrpt3")));
							tmp.put("et_addr_prt4", mCursor.getString(mCursor.getColumnIndexOrThrow("endaddrpt4")));
							tmp.put("st_addr_prt2", mCursor.getString(mCursor.getColumnIndexOrThrow("startaddrpt2")));
							tmp.put("st_addr_prt3", mCursor.getString(mCursor.getColumnIndexOrThrow("startaddrpt3")));
							tmp.put("st_addr_prt4", mCursor.getString(mCursor.getColumnIndexOrThrow("startaddrpt4")));
							tmp.put("triplistheader", mCursor.getInt(mCursor.getColumnIndexOrThrow("triplistheader")));
							// put the entry in the list
							list.put(tmp);
						} while (mCursor.moveToNext());
						// jsonarray all filled, put into result object
					} catch (JSONException e) {
						e.printStackTrace();
						list = null;
					}
					if (!mCursor.isClosed()) {
						mCursor.close();
						mCursor = null;
					}
					return list;
				}
				if (!mCursor.isClosed()) {
					mCursor.close();
					mCursor = null;
				}
			}
		}
		return null;
	}
	
	synchronized public long deleteTrip(String id){
		if(db.isOpen()){
			long rows = db.delete(TRIP_INFO_TABLE, "id=" + id, null);
			//also need to delete all pictures
//			ArrayList<String> tmp = getOneTripPicturePaths(sid, id, true);
			//need to write a delete pictures method that accept uniqueID also
			ArrayList<String> tmp = getOneTripPicturePaths(id, true);
			if(tmp != null){
				if(tmp.size() > 0){
					Iterator<String> it = tmp.iterator();
					while(it.hasNext()){
						File f = new File(it.next());
						f.delete();
					}
				}
			}
			rows += db.delete(TRIP_DATA_TABLE, "id=" + id, null);
			
			return rows;
		} else{
			return -2;
		}
	}
	
	synchronized public long deleteLocalTrip(String tripid){
		if(db.isOpen()){
			long rows = db.delete(TRIP_INFO_TABLE, "tripid=" + tripid, null);
			//also need to delete all pictures
			ArrayList<String> tmp = getOneTripPicturePaths(tripid, true);
			if(tmp != null){
				if(tmp.size() > 0){
					Iterator<String> it = tmp.iterator();
					while(it.hasNext()){
						File f = new File(it.next());
						f.delete();
					}
				}
			}
			rows += db.delete(TRIP_DATA_TABLE, "tripid=" + tripid, null);
			
			return rows;
		} else{
			return -2;
		}
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
								// construct one entry of trip info
								JSONObject tmp = new JSONObject();
								tmp.put("id", mCursor.getInt(mCursor.getColumnIndexOrThrow("id")));
								tmp.put("trip_id", mCursor.getString(mCursor.getColumnIndexOrThrow("tripid")));
								tmp.put("userid", mCursor.getString(mCursor.getColumnIndexOrThrow("userid")));
								tmp.put("trip_name", mCursor.getString(mCursor.getColumnIndexOrThrow("name")));
								tmp.put("trip_st", mCursor.getString(mCursor.getColumnIndexOrThrow("starttime")));
								tmp.put("trip_et", mCursor.getString(mCursor.getColumnIndexOrThrow("endtime")));
								tmp.put("trip_length", mCursor.getInt(mCursor.getColumnIndexOrThrow("length")));
								tmp.put("num_of_pts", mCursor.getInt(mCursor.getColumnIndexOrThrow("count")));
								tmp.put("et_addr_prt1", mCursor.getString(mCursor.getColumnIndexOrThrow("endaddrpt1")));
								tmp.put("et_addr_prt2", mCursor.getString(mCursor.getColumnIndexOrThrow("endaddrpt2")));
								tmp.put("et_addr_prt3", mCursor.getString(mCursor.getColumnIndexOrThrow("endaddrpt3")));
								tmp.put("et_addr_prt4", mCursor.getString(mCursor.getColumnIndexOrThrow("endaddrpt4")));
								tmp.put("et_addr_prt5", mCursor.getString(mCursor.getColumnIndexOrThrow("endaddrpt5")));
								tmp.put("st_addr_prt1",
										mCursor.getString(mCursor.getColumnIndexOrThrow("startaddrpt1")));
								tmp.put("st_addr_prt2",
										mCursor.getString(mCursor.getColumnIndexOrThrow("startaddrpt2")));
								tmp.put("st_addr_prt3",
										mCursor.getString(mCursor.getColumnIndexOrThrow("startaddrpt3")));
								tmp.put("st_addr_prt4",
										mCursor.getString(mCursor.getColumnIndexOrThrow("startaddrpt4")));
								tmp.put("st_addr_prt5",
										mCursor.getString(mCursor.getColumnIndexOrThrow("startaddrpt5")));
								tmp.put("uploaded", mCursor.getInt(mCursor.getColumnIndexOrThrow("uploaded")));
								// put the entry in the list
								list.put(tmp);
							} while (mCursor.moveToNext());
							// jsonarray all filled, put into result object
							result.put("tripInfoList", list);
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
//						osw.flush();
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
						JSONObject result = new JSONObject();
						try {
							JSONArray list = new JSONArray();
							// movetofirst is true, there is at least one entry
							// of data
							do {
								JSONObject tmp = new JSONObject();
								tmp.put("id", mCursor.getInt(mCursor.getColumnIndexOrThrow("id")));
								tmp.put("lat", mCursor.getDouble(mCursor.getColumnIndexOrThrow("latitude")));
								tmp.put("lng", mCursor.getDouble(mCursor.getColumnIndexOrThrow("longitude")));
								tmp.put("timestamp", mCursor.getString(mCursor.getColumnIndexOrThrow("timestamp")));
								tmp.put("alt", mCursor.getString(mCursor.getColumnIndexOrThrow("altitude")));
								tmp.put("spd", mCursor.getString(mCursor.getColumnIndexOrThrow("speed")));
								tmp.put("bear", mCursor.getString(mCursor.getColumnIndexOrThrow("bearing")));
								tmp.put("accu", mCursor.getString(mCursor.getColumnIndexOrThrow("accuracy")));
								tmp.put("userid", mCursor.getString(mCursor.getColumnIndexOrThrow("userid")));
								tmp.put("tripid", mCursor.getString(mCursor.getColumnIndexOrThrow("tripid")));
								// empty jsonobject to put in check-in info
								JSONObject checkin = new JSONObject();
								if (mCursor.getString(mCursor.getColumnIndexOrThrow("picture")) != null) {
									// picture exists! put it in
									// XXX cannot pass the complete path, need to
									// strip it till only filename is left
									checkin.put("picture_uri", mCursor.getString(mCursor.getColumnIndexOrThrow("picture")));
								} else if (mCursor.getString(mCursor.getColumnIndexOrThrow("emotion")) != null) {
									// emotion exists! put it in
									checkin.put("emotion", mCursor.getString(mCursor.getColumnIndexOrThrow("emotion")));
								} else if (mCursor.getString(mCursor.getColumnIndexOrThrow("note")) != null) {
									// message exists! put it in
									checkin.put("message", mCursor.getString(mCursor.getColumnIndexOrThrow("note")));
								} else {
									// no check-in info exists, set jsonobject to
									// null
									checkin = null;
								}
								if (checkin != null) {
									// only put in the check-in objecty if it is not
									// null
									tmp.put("CheckIn", checkin);
								}
								list.put(tmp);
							} while (mCursor.moveToNext());
							// jsonarray all filled, put into result object
							result.put("CheckInDataList", list);
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
						// XXX save result to file
						osw.write(result.toString());
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
