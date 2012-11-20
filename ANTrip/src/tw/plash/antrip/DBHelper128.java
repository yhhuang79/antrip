package tw.plash.antrip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
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
import android.util.Log;

/**
 * @author CSZU
 * 
 */
public class DBHelper128 {
	
	private static final String DATABASE_NAME = "antrip"; // database name
	private static final int DATABASE_VERSION = 11; // required by SQLite tool
	
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
			+ "userid TEXT, "
			+ "tripid TEXT, " 
			+ "picture TEXT, " 
			+ "emotion TEXT, "
			+ "note TEXT, " 
			+ "uploadstatus INTEGER default 0)";
	
	private static final String CREATE_TABLE_TRIP_INFO = "CREATE TABLE " + TRIP_INFO_TABLE
			+ "(id INTEGER PRIMARY KEY, " 
			+ "tripid TEXT, " 
			+ "userid TEXT, " 
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
			+ "servertripid TEXT)";
	
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
			} // invalid SQL statement
		}
		
		@Override
		// for upgrading table schema only, not useful at the moment
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			try { // drop the old version and create a updated one
				db.execSQL("DROP TABLE IF EXIST " + DATABASE_NAME + "." + TRIP_DATA_TABLE);
				db.execSQL("DROP TABLE IF EXIST " + DATABASE_NAME + "." + TRIP_INFO_TABLE);
			} catch (SQLException e) {
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
	public DBHelper128(Context context) {
		mContext = context;
		OpenHelper openHelper = new OpenHelper(mContext);
		// get the DB with write permission
		try {
			this.db = openHelper.getWritableDatabase();
		} catch (SQLiteException e) {
			e.printStackTrace();
		} // something's wrong with the DB
	}
	
	public DBHelper128(Context context, int a) {
		mContext = context;
		OpenHelper openHelper = new OpenHelper(mContext);
		// get the DB with read permission
		try {
			this.db = openHelper.getReadableDatabase();
		} catch (SQLiteException e) {
			e.printStackTrace();
		} // something's wrong with the DB
	}
	
	private class locationStringConverter {
		
		Double latitude;
		Double longitude;
		String time;
		String altitude;
		String speed;
		String bearing;
		String accuracy;
		
		public locationStringConverter(Location loc) {
			this.latitude = loc.getLatitude();
			this.longitude = loc.getLongitude();
			this.time = String.valueOf(loc.getTime());
			this.altitude = String.valueOf(loc.getAltitude());
			this.speed = String.valueOf(loc.getSpeed());
			this.bearing = String.valueOf(loc.getBearing());
			this.accuracy = String.valueOf(loc.getAccuracy());
		}
		
		public Double getLatitude() {
			return this.latitude;
		}
		
		public Double getLongitude() {
			return this.longitude;
		}
		
		public String getTime() {
			return this.time;
		}
		
		public String getAltitude() {
			return this.altitude;
		}
		
		public String getSpeed() {
			return this.speed;
		}
		
		public String getBearing() {
			return this.bearing;
		}
		
		public String getAccuracy() {
			return this.accuracy;
		}
	}
	
	synchronized public long insert(Location location, String userid, String tripid) {
		if (db.isOpen()) {
			ContentValues cv = new ContentValues();
			locationStringConverter l2s = new locationStringConverter(location);
			cv.put("latitude", l2s.getLatitude());
			cv.put("longitude", l2s.getLongitude());
			cv.put("timestamp", new Timestamp(Long.valueOf(l2s.getTime())).toString());
			cv.put("altitude", l2s.getAltitude());
			cv.put("speed", l2s.getSpeed());
			cv.put("bearing", l2s.getBearing());
			cv.put("accuracy", l2s.getAccuracy());
			cv.put("userID", userid);
			cv.put("tripID", tripid);
			return db.insert(TRIP_DATA_TABLE, null, cv);
		} else {
			return -2;
		}
	}
	
	synchronized public long insert(CandidateCheckinObject cco, String userid, String tripid) {
		if (db.isOpen()) {
			ContentValues cv = new ContentValues();
			locationStringConverter l2s = new locationStringConverter(cco.getLocation());
			cv.put("latitude", l2s.getLatitude());
			cv.put("longitude", l2s.getLongitude());
			cv.put("timestamp", new Timestamp(Long.valueOf(l2s.getTime())).toString());
			cv.put("altitude", l2s.getAltitude());
			cv.put("speed", l2s.getSpeed());
			cv.put("bearing", l2s.getBearing());
			cv.put("accuracy", l2s.getAccuracy());
			cv.put("userID", userid);
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
	
	synchronized public long saveTripStats(String userid, String tripid, TripStats stats){
		if(db.isOpen()){
			ContentValues cv = new ContentValues();
			cv.put("tripid", tripid);
			cv.put("userid", userid);
			cv.put("starttime", stats.getButtonStartTime());
			cv.put("endtime", stats.getNonNullEndTime());
			cv.put("length", stats.getTotalValidLength().intValue());
			cv.put("count", stats.getTotalAccuratePointCount());
			return db.insert(TRIP_INFO_TABLE, null, cv);
		} else{
			return -2;
		}
	}
	
	synchronized public long setStartaddr(String userid, String tripid, List<Address> startaddr) {
		if (db.isOpen()) {
			if(startaddr != null){
				ContentValues cv = new ContentValues();
				cv.put("startaddrpt1", (startaddr.get(0).getCountryName() != null?startaddr.get(0).getCountryName():"NULL"));
				cv.put("startaddrpt2", (startaddr.get(0).getAdminArea() != null?startaddr.get(0).getAdminArea():"NULL"));
				cv.put("startaddrpt3", (startaddr.get(0).getLocality() != null?startaddr.get(0).getLocality():"NULL"));
				cv.put("startaddrpt4", (startaddr.get(0).getSubLocality() != null?startaddr.get(0).getSubLocality():"NULL"));
				cv.put("startaddrpt5", (startaddr.get(0).getThoroughfare() != null?startaddr.get(0).getThoroughfare():"NULL"));
//				Log.e("dbhelper", "insert start address: " + cv.toString());
				return db.update(TRIP_INFO_TABLE, cv, "tripid=" + tripid + " AND userid=" + userid, null);
			} else{
				return -1;
			}
			
		} else {
			return -2;
		}
	}
	
	synchronized public long setEndaddr(String userid, String tripid, List<Address> endaddr) {
		if (db.isOpen()) {
			if(endaddr != null){
				ContentValues cv = new ContentValues();
				cv.put("endaddrpt1", (endaddr.get(0).getCountryName() != null?endaddr.get(0).getCountryName():"NULL"));
				cv.put("endaddrpt2", (endaddr.get(0).getAdminArea() != null?endaddr.get(0).getAdminArea():"NULL"));
				cv.put("endaddrpt3", (endaddr.get(0).getLocality() != null?endaddr.get(0).getLocality():"NULL"));
				cv.put("endaddrpt4", (endaddr.get(0).getSubLocality() != null?endaddr.get(0).getSubLocality():"NULL"));
				cv.put("endaddrpt5", (endaddr.get(0).getThoroughfare() != null?endaddr.get(0).getThoroughfare():"NULL"));
//				Log.e("dbhelper", "insert end address: " + cv.toString());
				return db.update(TRIP_INFO_TABLE, cv, "tripid=" + tripid + " AND userid=" + userid, null);
			} else{
				return -1;
			}
			
		} else {
			return -2;
		}
	}
	
	synchronized public long updateUploadStage(String userid, String tripid, int stage){
		if(db.isOpen()){
			ContentValues cv = new ContentValues();
			cv.put("uploadstage", stage);
			return db.update(TRIP_INFO_TABLE, cv, "tripid=" + tripid + " AND userid=" + userid, null);
		} else{
			return -2;
		}
	}
	
	synchronized public int getUploadStage(String userid, String tripid) {
		if (db.isOpen()) {
			Cursor mCursor = db.query(TRIP_INFO_TABLE, new String[] { "uploadstage" }, "userid=" + userid
					+ " AND tripid=" + tripid, null, null, null, null);
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

	synchronized public long setTripName(String userid, String tripid, String name) {
		if (db.isOpen()) {
			ContentValues cv = new ContentValues();
			// pop up screen asking for user input
			cv.put("name", name);
			return db.update(TRIP_INFO_TABLE, cv, "tripid=" + tripid + " AND userid=" + userid, null);
		} else {
			return -2;
		}
	}
	
	
	/**
	 * for sync position function call
	 * 
	 * @param userid
	 * @param tripid
	 * @return
	 */
//	synchronized public JSONObject getSyncPositionList(String userid, String tripid) {
//		if (db.isOpen()) {
//			Cursor mCursor = db.query(TRIP_DATA_TABLE, new String[] { "" }, "something=something", null, null, null,
//					"timestamp ASC");
//			if (mCursor != null) {
//				if (mCursor.moveToFirst()) {
//					JSONObject result = new JSONObject();
//					
//					if (!mCursor.isClosed()) {
//						mCursor.close();
//					}
//					return result;
//				} else {
//					if (!mCursor.isClosed()) {
//						mCursor.close();
//					}
//					return null;
//				}
//			} else {
//				return null;
//			}
//		} else {
//			return null;
//		}
//	}
	
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
	 * update the temporary random tripid with actual tripid from plash server
	 * 
	 * @param tripid
	 *            , generated randomly at trip craetion
	 * @return number of rows updated, info + data, or -1 when anything goes
	 *         wrong
	 */
//	synchronized public int updateTripid(String oldTripid, String newTripid) {
//		if (db.isOpen()) {
//			ContentValues cv = new ContentValues();
//			cv.put("tripid", newTripid);
//			int info = db.update(TRIP_INFO_TABLE, cv, "tripid=" + oldTripid, null);
//			return info + db.update(TRIP_DATA_TABLE, cv, "tripid=" + oldTripid, null);
//		} else {
//			return -1;
//		}
//	}
	
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
	synchronized public int markUploaded(String userid, String localTripid, int table, int code, String imagepath) {
		if (db.isOpen()) {
			ContentValues cv = new ContentValues();
			switch(table){
			case 0:
				cv.put("uploadstage", code);
				return db.update(TRIP_INFO_TABLE, cv, "userid=" + userid + " AND tripid=" + localTripid, null);
			case 1:
				cv.put("uploadstatus", code);
				if(imagepath != null){
					return db.update(TRIP_DATA_TABLE, cv, "userid=" + userid + " AND tripid=" + localTripid + " AND picture LIKE '%" + imagepath.substring(imagepath.lastIndexOf("/") + 1) + "'", null);
				} else{
					return db.update(TRIP_DATA_TABLE, cv, "userid=" + userid + " AND tripid=" + localTripid, null);
				}
			default:
				return -3;
			}
		} else {
			//db is not opened
			return -2;
		}
	}
	
	synchronized public long setServerTripid(String userid, String localTripid, String serverTripid){
		if(db.isOpen()){
			ContentValues cv = new ContentValues();
			cv.put("servertripid", serverTripid);
			return db.update(TRIP_INFO_TABLE, cv, "userid=" + userid + " AND tripid=" + localTripid, null);
		} else{
			return -1;
		}
	}
	
	synchronized public long getNumberofPoints(String userid, String tripid) {
		String sql = "SELECT COUNT(id) FROM " + TRIP_DATA_TABLE + " WHERE userid=" + userid + " AND tripid=" + tripid;
		if (db.isOpen()) {
			SQLiteStatement statement = db.compileStatement(sql);
			long result = statement.simpleQueryForLong();
			statement.close();
			return result;
		} else {
			return -2;
		}
	}
	
	synchronized public CachedPoints getOnePoint(String userid, String tripid, boolean wantFirst) {
		if (db.isOpen()) {
			Cursor mCursor = db.query(TRIP_DATA_TABLE, new String[] { "latitude", "longitude" }, "tripid=" + tripid
					+ " AND userid=" + userid + " AND latitude!=-999", null, null, null, wantFirst ? "id ASC" : "id DESC", "1");
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
	
	synchronized public JSONObject getOneTripInfo(String userid, String tripid) {
		if (db.isOpen()) {
			Cursor mCursor = db.query(TRIP_INFO_TABLE, null, "tripid=" + tripid + " AND userid=" + userid, null, null,
					null, null);
			if (mCursor != null) {
				if (mCursor.moveToFirst()) {
					JSONObject tmp = new JSONObject();
					try {
						tmp.put("userid", userid);
//						tmp.put("trip_id", tripid);
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
	
	synchronized public ArrayList<String> getOneTripPicturePaths(String userid, String tripid, boolean needall){
		if(db.isOpen()){
			Cursor mCursor;
			if(needall){
				mCursor = db.query(TRIP_DATA_TABLE, new String[]{"picture"}, "userid=" + userid + " AND tripid=" + tripid + " AND picture!='NULL'", null, null, null, null);
			} else{
				mCursor = db.query(TRIP_DATA_TABLE, new String[]{"picture"}, "userid=" + userid + " AND tripid=" + tripid + " AND picture!='NULL' AND uploadstatus<2", null, null, null, null);
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
	
	/**
	 * get a complete trip data wrapped in CheckInDataList styled JSONObject
	 * format
	 * 
	 * @param userid
	 * @param tripid
	 * @return CheckInDataList styled trip data JSONObject
	 */
	synchronized public JSONObject getOneTripData(String userid, String tripid, boolean forUpload) {
		//Log.e("getOneTripData", "userid=" + userid + ", tripid=" + tripid
//				+ (forUpload ? ", forUpload true" : ", forUpload false"));
		if (db.isOpen()) {
			Cursor mCursor = db.query(TRIP_DATA_TABLE, null, "userid=" + userid + " AND tripid=" + tripid, null, null,
					null, "id ASC");
			
			if (mCursor != null) {
				if (mCursor.moveToFirst()) {
					JSONObject result = new JSONObject();
					try {
						result.put("userid", userid);
//						result.put("trip_id", tripid);
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
	//trip_name
	synchronized public JSONObject getOneTripData(String userid, Integer id) {
		//Log.e("getOneTripData", "userid=" + userid + ", id=" + id);
		if (db.isOpen()) {
			Cursor tmpCursor = db.query(TRIP_INFO_TABLE, null, "id=" + id, null, null, null, null);
			String tripid = tmpCursor.moveToFirst() ? tmpCursor.getString(tmpCursor.getColumnIndexOrThrow("tripid"))
					: "-1";
			String tripname = tmpCursor.moveToFirst() ? tmpCursor.getString(tmpCursor.getColumnIndexOrThrow("trip_name")) : "-1";
			JSONObject result = new JSONObject();
			
			Cursor mCursor = db.query(TRIP_DATA_TABLE, null, "userid=" + userid + " AND tripid=" + tripid, null, null,
					null, "id ASC");
			if (mCursor != null) {
				if (mCursor.moveToFirst()) {
					
					
					
					try {
						result.put("trip_name", tripname);
						result.put("userid", userid);
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
	
	synchronized public HashSet<String> getTripIdsFromTripData(String sid, String excludeThisTid){
		if(db.isOpen()){
			Cursor mCursor = null;
			if(excludeThisTid != null){
				mCursor = db.query(TRIP_DATA_TABLE, new String[]{"tripid"}, "userid=" + sid + " AND tripid !=" + excludeThisTid, null, null, null, null);
			} else{
				mCursor = db.query(TRIP_DATA_TABLE, new String[]{"tripid"}, "userid=" + sid, null, null, null, null);
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
	
	synchronized public HashSet<String> getTripIdsFromTripInfo(String sid, String excludeThisTid){
		if(db.isOpen()){
			Cursor mCursor = null;
			if(excludeThisTid != null){
				mCursor = db.query(TRIP_INFO_TABLE, new String[]{"tripid"}, "userid=" + sid + " AND tripid !=" + excludeThisTid, null, null, null, null);
			} else{
				mCursor = db.query(TRIP_INFO_TABLE, new String[]{"tripid"}, "userid=" + sid, null, null, null, null);
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
	
//	synchronized public HashMap<String, Integer> getAllUnfinishedUploads(String sid){
//		if(db.isOpen()){
//			Cursor mCursor = db.query(TRIP_INFO_TABLE, new String[]{"tripid", "uploadstage"}, "userid=" + sid + " AND uploadstage!=0", null, null, null, null);
//			if(mCursor != null){
//				if(mCursor.moveToFirst()){
//					HashMap<String, Integer> result = new HashMap<String, Integer>();
//					do{
//						result.put(mCursor.getString(mCursor.getColumnIndexOrThrow("tripid")), mCursor.getInt(mCursor.getColumnIndexOrThrow("uploadstage")));
//					} while(mCursor.moveToNext());
//					mCursor.close();
//					return result;
//				} else{
//					if(!mCursor.isClosed()){
//						mCursor.close();
//					}
//					return null;
//				}
//			} else{
//				return null;
//			}
//		} else{
//			return null;
//		}
//	}
	
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
	synchronized public JSONObject getAllTripInfoForHTML(String userid, String currentTripid) {
		if (db.isOpen()) {
			Cursor mCursor = null;
			if (currentTripid != null) {
				//there is a on going trip, don't show it in the trip list
				//Log.e("getalltripinfoforhtml", "tripid!=null");
				mCursor = db.query(TRIP_INFO_TABLE, null, "userid=" + userid + " AND tripid!=" + currentTripid, null,
						null, null, "starttime DESC");
			} else {
				//there is no on going trip, show everything
				//Log.e("getalltripinfoforhtml", "tripid=null");
				mCursor = db.query(TRIP_INFO_TABLE, null, "userid=" + userid, null, null, null, "starttime DESC");
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
	
	synchronized public long deleteTrip(String sid, String id){
		if(db.isOpen()){
			long rows = db.delete(TRIP_INFO_TABLE, "userid=" + sid + " AND id=" + id, null);
			//also need to delete all pictures
//			ArrayList<String> tmp = getOneTripPicturePaths(sid, id, true);
			//need to write a delete pictures method that accept uniqueID also
			ArrayList<String> tmp = getOneTripPicturePaths(sid, id, true);
			if(tmp != null){
				if(tmp.size() > 0){
					Iterator<String> it = tmp.iterator();
					while(it.hasNext()){
						File f = new File(it.next());
						f.delete();
					}
				}
			}
			rows += db.delete(TRIP_DATA_TABLE, "userid=" + sid + " AND id=" + id, null);
			
			return rows;
		} else{
			return -2;
		}
	}
	
	synchronized public long deleteLocalTrip(String sid, String tripid){
		if(db.isOpen()){
			long rows = db.delete(TRIP_INFO_TABLE, "userid=" + sid + " AND tripid=" + tripid, null);
			//also need to delete all pictures
			ArrayList<String> tmp = getOneTripPicturePaths(sid, tripid, true);
			if(tmp != null){
				if(tmp.size() > 0){
					Iterator<String> it = tmp.iterator();
					while(it.hasNext()){
						File f = new File(it.next());
						f.delete();
					}
				}
			}
			rows += db.delete(TRIP_DATA_TABLE, "userid=" + sid + " AND tripid=" + tripid, null);
			
			return rows;
		} else{
			return -2;
		}
	}
	
	/**
	 * used to delete all the uploaded trip points from local cache when not
	 * recording
	 * 
	 * @return the number of rows deleted, or negative numbers when things go
	 *         wrong
	 */
//	synchronized public long deleteCompletedPoints() {
//		if (db.isOpen()) {
//			return db.delete(TRIP_POINT_TABLE, "uploaded=1", null);
//		} else {
//			return -2;
//		}
//	}
	
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
	
	public String exportEverything() {
		if (db.isOpen()) {
			try {
				String testpath = "/mnt/sdcard/";
				String fullpath = null;
				File tester = new File(testpath);
				if (tester.exists() && tester.isDirectory()) {
					fullpath = testpath + System.currentTimeMillis() + ".txt";
				} else {
					testpath = "/mnt/emmc/";
					tester = new File(testpath);
					if (tester.exists() && tester.isDirectory()) {
						fullpath = testpath + System.currentTimeMillis() + ".txt";
					}
				}
				Log.e("export everything", "path= " + fullpath);
				FileOutputStream fos = new FileOutputStream(fullpath);
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
								if(mCursor.getColumnIndex("uploadstage") != -1){
									tmp.put("uploadstage", mCursor.getInt(mCursor.getColumnIndex("uploadstage")));
								}
								if(mCursor.getColumnIndex("servertripid") != -1){
									tmp.put("servertripid", mCursor.getString(mCursor.getColumnIndex("servertripid")));
								}
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
						// XXX save result to file
						osw.write(result.toString());
//						osw.flush();
					} else {
						if (!mCursor.isClosed()) {
							mCursor.close();
						}
						if(osw != null){
							osw.close();
						}
						return null;
					}
				} else {
					if(osw != null){
						osw.close();
					}
					return null;
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
								if(mCursor.getColumnIndex("uploadstatus") != -1){
									tmp.put("uploadstatus", mCursor.getInt(mCursor.getColumnIndex("uploadstatus")));
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
							return null;
						}
						// all is well
						if (!mCursor.isClosed()) {
							mCursor.close();
						}
						// XXX save result to file
						osw.write(result.toString());
						osw.flush();
						osw.close();
						return fullpath;
					} else {
						if (!mCursor.isClosed()) {
							mCursor.close();
						}
						if(osw != null){
							osw.close();
						}
						return null;
					}
				} else {
					if(osw != null){
						osw.close();
					}
					return null;
				}
			} catch (IOException e) {
				return null;
			}
		} else {
			return null;
		}
	}
}
