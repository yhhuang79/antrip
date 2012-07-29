package tw.plash.antrip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import android.location.Location;
import android.util.Log;

/**
 * @author CSZU
 * 
 */
public class DBHelper128 {

	private static final String DATABASE_NAME = "antrip"; // database name
	private static final int DATABASE_VERSION = 1; // required by SQLite tool

	private static final String TRIP_POINT_TABLE = "trippointtable";
	private static final String TRIP_INFO_TABLE = "tripinfotable";
	
	private Context mContext;

	private SQLiteDatabase db;

	private static final String CREATE_TABLE_TRIP_POINT = "CREATE TABLE " 
		+ TRIP_POINT_TABLE
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
		+ "note TEXT)";
	
	private static final String CREATE_TABLE_TRIP_INFO = "CREATE TABLE " 
		+ TRIP_INFO_TABLE
		+ "(id INTEGER PRIMARY KEY, " 
		+ "tripid TEXT, "
		+ "userid TEXT, "
		+ "name TEXT, "
		+ "starttime TEXT, "
		+ "endtime TEXT, "
		+ "length REAL, "
		+ "count INTEGER, "
		+ "startaddrpt1 TEXT, "
		+ "startaddrpt2 TEXT, "
		+ "startaddrpt3 TEXT, "
		+ "startaddrpt4 TEXT, "
		+ "startaddrpt5 TEXT, "
		+ "endaddrpt1 TEXT, "
		+ "endaddrpt2 TEXT, "
		+ "endaddrpt3 TEXT, "
		+ "endaddrpt4 TEXT, "
		+ "endaddrpt5 TEXT, "
		+ "uploaded INTEGER)";
	
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
				db.execSQL(CREATE_TABLE_TRIP_POINT);
				db.execSQL(CREATE_TABLE_TRIP_INFO);
			} catch (SQLException e) {
			} // invalid SQL statement
		}

		@Override
		// for upgrading table schema only, not useful at the moment
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			try { // drop the old version and create a updated one
				db.execSQL("DROP TABLE IF EXIST " + DATABASE_NAME + "." + TRIP_POINT_TABLE);
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

		public String getTime(){
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
			cv.put("timestamp", l2s.getTime());
			cv.put("altitude", l2s.getAltitude());
			cv.put("speed", l2s.getSpeed());
			cv.put("bearing", l2s.getBearing());
			cv.put("accuracy", l2s.getAccuracy());
			cv.put("userID", userid);
			cv.put("tripID", tripid);
			return db.insert(TRIP_POINT_TABLE, null, cv);
		} else {
			return -2;
		}
	}
	
	synchronized public long insert(Location location, String userid, String tripid, CandidateCheckinObject cco) {
		if (db.isOpen()) {
			ContentValues cv = new ContentValues();
			locationStringConverter l2s = new locationStringConverter(location);
			cv.put("latitude", l2s.getLatitude());
			cv.put("longitude", l2s.getLongitude());
			cv.put("timestamp", l2s.getTime());
			cv.put("altitude", l2s.getAltitude());
			cv.put("speed", l2s.getSpeed());
			cv.put("bearing", l2s.getBearing());
			cv.put("accuracy", l2s.getAccuracy());
			cv.put("userID", userid);
			cv.put("tripID", tripid);
			//cco fields
			cv.put("picture", cco.getPicturePath());
			cv.put("emotion", String.valueOf(cco.getEmotionID()));
			cv.put("note", cco.getCheckinText());
			return db.insert(TRIP_POINT_TABLE, null, cv);
		} else {
			return -2;
		}
	}
	
	synchronized public long createNewTripInfo(String userid, String tripid, String starttime){
		if(db.isOpen()){
			ContentValues cv = new ContentValues();
			cv.put("tripid", tripid);
			cv.put("userid", userid);
			cv.put("uploaded", 0);
			cv.put("starttime", starttime);
			return db.insert(TRIP_INFO_TABLE, null, cv);
		} else{
			return -2;
		}
	}
	
	/**
	 * Insert start time from the first location report
	 * @param tripid
	 * @param starttime
	 * @return
	 */
	/*
	synchronized public long insertStarttime(String tripid, String starttime){
		if(db.isOpen()){
			ContentValues cv = new ContentValues();
			cv.put("starttime", starttime);
			return db.update(TRIP_INFO_TABLE, cv, "tripid=" + tripid, null);
		} else{
			return -2;
		}
	}*/
	
	/**
	 * Insert starting address from the first non-null location report
	 * @param tripid
	 * @param startaddrpt1
	 * @param startaddrpt2
	 * @param startaddrpt3
	 * @param startaddrpt4
	 * @param startaddrpt5
	 * @return
	 */
	synchronized public long insertStartaddr(String userid, String tripid, String startaddrpt1, String startaddrpt2, String startaddrpt3, String startaddrpt4, String startaddrpt5){
		if(db.isOpen()){
			ContentValues cv = new ContentValues();
			cv.put("startaddrpt1", startaddrpt1);
			cv.put("startaddrpt2", startaddrpt2);
			cv.put("startaddrpt3", startaddrpt3);
			cv.put("startaddrpt4", startaddrpt4);
			cv.put("startaddrpt5", startaddrpt5);
			return db.update(TRIP_INFO_TABLE, cv, "tripid=" + tripid + " AND userid=" + userid, null);
		} else{
			return -2;
		}
	}
	
	/**
	 * Insert user inputed trip name, and the calculated trip length, also the time from last location report
	 * @param tripid
	 * @param name
	 * @param endtime
	 * @param length
	 * @return
	 */
	synchronized public long insertEndInfo(String userid, String tripid, String name, String endtime, Double length){
		if(db.isOpen()){
			ContentValues cv = new ContentValues();
			//pop up screen asking for user input
			cv.put("name", name);
			cv.put("endtime", endtime);
			cv.put("length", length);
			//count the number of points directly from DB
			int count = (int) getNumberofPoints(userid, tripid);
			cv.put("count", count);
			
			Log.e("insertEndInfo", "name=" + name + ", endtime=" + endtime + ", length=" + length + ", count=" + count);
			return db.update(TRIP_INFO_TABLE, cv, "tripid=" + tripid + " AND userid=" + userid, null);
		} else{
			return -2;
		}
	}
	
	synchronized public long insertEndaddr(String userid, String tripid, String endaddrpt1, String endaddrpt2, String endaddrpt3, String endaddrpt4, String endaddrpt5){
		if(db.isOpen()){
			ContentValues cv = new ContentValues();
			cv.put("endaddrpt1", endaddrpt1);
			cv.put("endaddrpt2", endaddrpt2);
			cv.put("endaddrpt3", endaddrpt3);
			cv.put("endaddrpt4", endaddrpt4);
			cv.put("endaddrpt5", endaddrpt5);
			return db.update(TRIP_INFO_TABLE, cv, "tripid=" + tripid + " AND userid=" + userid, null);
		} else{
			return -2;
		}
	}
	
	synchronized public JSONObject getPositionList(String userid, String tripid){
		if(db.isOpen()){
			Cursor mCursor = db.query(TRIP_POINT_TABLE, new String[]{""}, "something=something", null, null, null, "timestamp ASC");
			if(mCursor != null){
				if(mCursor.moveToFirst()){
					JSONObject result = new JSONObject();
					
					
					
					
					if(!mCursor.isClosed()){
						mCursor.close();
					}
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
	 * get all points from realtime data table for drawing purpose
	 * 
	 * @return a list of cachedPoints with lat, lon values for drawing, null if
	 *         anything goes wrong
	 */
	/*
	synchronized public List<CachedPoints> getDrawingPoints(String tripid) {
		if (db.isOpen()) {
			Cursor mCursor = db.query(TRIP_POINT_TABLE, new String[] { "latitude", "longitude" },
					"tripid = " + tripid, null, null, null, "id ASC");
			if (mCursor != null) {
				if (mCursor.moveToFirst()) {
					List<CachedPoints> list = new ArrayList<CachedPoints>();
					do {
						list.add(new CachedPoints(mCursor.getDouble(0), mCursor.getDouble(1)));
					} while (mCursor.moveToNext());
					if (!mCursor.isClosed()) {
						mCursor.close();
					}
					return list;
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
	}*/
	
	/**
	 * update the temporary random tripid with actual tripid from plash server
	 * @param tripid, generated randomly at trip craetion
	 * @return number of rows updated, info + data, or -1 when anything goes wrong
	 */
	synchronized public int updateTripid(String oldTripid, String newTripid){
		if(db.isOpen()){
			ContentValues cv = new ContentValues();
			cv.put("tripid", newTripid);
			int info = db.update(TRIP_INFO_TABLE, cv, "tripid=" + oldTripid, null);
			return info + db.update(TRIP_POINT_TABLE, cv, "tripid=" + oldTripid, null);
		} else{
			return -1;
		}
	}
	
	/**
	 * mark the successfully uploaded trip
	 * @param actualTripid, the tripid synced with server, not the random generated number
	 * @return 
	 */
	synchronized public int markUploaded(String actualTripid) {
		if (db.isOpen()) {
			ContentValues cv = new ContentValues();
			cv.put("uploaded", 1);
			return db.update(TRIP_INFO_TABLE, cv, "tripid=" + actualTripid, null);
		} else {
			return -2;
		}
	}
	
	synchronized public long getNumberofPoints(String userid, String tripid) {
		String sql = "SELECT COUNT(id) FROM " + TRIP_POINT_TABLE + " WHERE userid=" + userid + " AND tripid=" + tripid;
		if (db.isOpen()) {
			SQLiteStatement statement = db.compileStatement(sql);
			long result = statement.simpleQueryForLong();
			statement.close();
			return result;
		} else {
			return -2;
		}
	}
	synchronized public CachedPoints getOnePoint(String userid, String tripid, boolean wantFirst){
		if(db.isOpen()){
			Cursor mCursor = db.query(TRIP_POINT_TABLE, new String[] { "latitude", "longitude" },"tripid=" + tripid + " AND userid=" + userid, null, null, null, wantFirst?"id ASC":"id DESC", "1");
			if(mCursor != null){
				if(mCursor.moveToFirst()){
					CachedPoints cp = new CachedPoints(mCursor.getDouble(mCursor.getColumnIndex("latitude")), mCursor.getDouble(mCursor.getColumnIndex("longitude")));
					if(!mCursor.isClosed()){
						mCursor.close();
					}
					return cp;
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
	
	synchronized public JSONObject getOneTripInfo(String userid, String tripid){
		if(db.isOpen()){
			Cursor mCursor = db.query(TRIP_INFO_TABLE, null, "tripid=" + tripid + " AND userid=" + userid, null, null, null, null);
			if(mCursor != null){
				if(mCursor.moveToFirst()){
					JSONObject tmp = new JSONObject();
					try {
						tmp.put("trip_length", mCursor.getDouble(mCursor.getColumnIndexOrThrow("length")));
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
					if(!mCursor.isClosed()){
						mCursor.close();
					}
					return tmp;
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
	 * get a complete trip data wrapped in CheckInDataList styled JSONObject format
	 * @param userid
	 * @param tripid
	 * @return CheckInDataList styled trip data JSONObject
	 */
	synchronized public JSONObject getOneTripData(String userid, String tripid, boolean forUpload){
		if(db.isOpen()){
			Cursor mCursor = db.query(TRIP_POINT_TABLE, null, "userid=" + userid + " AND tripid=" + tripid, null, null, null, "id ASC");
			if(mCursor != null){
				if(!mCursor.moveToFirst()){
					JSONObject result = new JSONObject();
					try {
						result.put("userid", userid);
						result.put("trip_id", tripid);
						JSONArray cidl = new JSONArray();
						do{
							JSONObject tmp = new JSONObject();
							tmp.put("lat", mCursor.getDouble(mCursor.getColumnIndex("latitude")));
							tmp.put("lng", mCursor.getDouble(mCursor.getColumnIndex("longitude")));
							tmp.put("timestamp", mCursor.getString(mCursor.getColumnIndex("timestamp")));
							tmp.put("alt", mCursor.getString(mCursor.getColumnIndex("altitude")));
							tmp.put("spd", mCursor.getString(mCursor.getColumnIndex("speed")));
							tmp.put("bear", mCursor.getString(mCursor.getColumnIndex("bearing")));
							tmp.put("accu", mCursor.getString(mCursor.getColumnIndex("accuracy")));
							//empty jsonobject to put in check-in info
							JSONObject checkin = new JSONObject();
							if(mCursor.getString(mCursor.getColumnIndex("picture")) != null){
								//picture exists! put it in
								//XXX cannot pass the complete path, need to strip it till only filename is left
								if(forUpload){
									//this call is for uploading, put in only the filename
									checkin.put("picture_uri", mCursor.getString(mCursor.getColumnIndex("picture")).substring(mCursor.getString(mCursor.getColumnIndex("picture")).lastIndexOf("/")));
								} else{
									//return complete path, this call is for drawing purpose
									checkin.put("picture_uri", mCursor.getString(mCursor.getColumnIndex("picture")));
								}
							} else if(mCursor.getString(mCursor.getColumnIndex("emotion")) != null){
								//emotion exists! put it in
								checkin.put("emotion", mCursor.getString(mCursor.getColumnIndex("emotion")));
							} else if(mCursor.getString(mCursor.getColumnIndex("note")) != null){
								//message exists! put it in
								checkin.put("message", mCursor.getString(mCursor.getColumnIndex("note")));
							} else{
								//no check-in info exists, set jsonobject to null
								checkin = null;
							}
							if(checkin != null){
								//only put in the check-in objecty if it is not null
								tmp.put("CheckIn", checkin);
							}
							cidl.put(tmp);
						} while(mCursor.moveToNext());
						result.put("CheckInDataList", cidl);
					} catch (JSONException e) {
						e.printStackTrace();
						if(!mCursor.isClosed()){
							mCursor.close();
						}
						result = null;
					}
					if(!mCursor.isClosed()){
						mCursor.close();
					}
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
	 * Theoretically, multiple users could share the same device, thus userid is a required input
	 * This function extracts all trip point in a trip, identified by userid+tripid
	 * @param userid
	 * @param tripid
	 * @return JSONObject, formatted like the return value from yu-hsiang's php component
	 */
	/*
	synchronized public PLASHLocationObject getTrip(String userid, String tripid) {
		if (db.isOpen()) {
			// get the newest point first
			Cursor mCursor = db.query(TRIP_POINT_TABLE, null, "tripid=" + tripid, null, null, null,
					"id DESC");
			if (mCursor != null) {
				if (mCursor.moveToFirst()) {
					PLASHLocationObject obj = new PLASHLocationObject(mCursor.getInt(0),
							mCursor.getDouble(1), mCursor.getDouble(2), mCursor.getString(3),
							mCursor.getString(4), mCursor.getString(5), mCursor.getString(6),
							mCursor.getString(7), mCursor.getString(8), mCursor.getString(9));
					if (!mCursor.isClosed()) { // we're done with the cursor,
												// close it here
						mCursor.close();
					}
					return obj;
				} else { // cursor not null, but empty query, close the cursor
							// and exit
					if (!mCursor.isClosed()) {
						mCursor.close();
					}
					return null;
				}
			} else { // cursor is null, possibly error in query
				return null;
			}
		} else { // db not even opened
			return null;
		}
	}*/
	
	/**
	 * Get all trip info formatted to yu-hsiang's php component with the given userid
	 * @param userid
	 * @return JSONObject format trip info
	 */
	synchronized public JSONObject getAllTripInfoForHTML(String userid){
		if(db.isOpen()){
			Cursor mCursor = db.query(TRIP_INFO_TABLE, null, "userid=" + userid, null, null, null, "tripid DESC");
			if(mCursor != null){
				if(mCursor.moveToFirst()){
					JSONObject result = new JSONObject();
					JSONArray list = new JSONArray();
					try {
						//movetofirst is true, there is at least one entry of data
						do{
							//construct one entry of trip info
							JSONObject tmp = new JSONObject();
							tmp.put("trip_id", mCursor.getString(mCursor.getColumnIndexOrThrow("tripid")));
							tmp.put("trip_length", mCursor.getDouble(mCursor.getColumnIndexOrThrow("length")));
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
							//put the entry in the list
							list.put(tmp);
						}while(mCursor.moveToNext());
						//jsonarray all filled, put into result object
						result.put("tripInfoList", list);
					} catch (JSONException e) {
						e.printStackTrace();
						//if anything goes wrong, return null
						if(!mCursor.isClosed()){
							mCursor.close();
						}
						return null;
					}
					//all is well
					if(!mCursor.isClosed()){
						mCursor.close();
					}
					return result;
				} else{
					//cursor is empty, close it
					if(!mCursor.isClosed()){
						mCursor.close();
					}
					return null;
				}
			} else{
				//something wrong with the query
				return null;
			}
		} else{
			//db not even opened
			return null;
		}
	}
	/*
	synchronized public ArrayList<HashMap<String, Object>> getAllTripInfo(){
		if(db.isOpen()){
			Cursor mCursor = db.query(TRIP_INFO_TABLE, null, null, null, null, null, "tripid DESC");
			if(mCursor != null){
				if(mCursor.moveToFirst()){
					//do stuff here
					ArrayList<HashMap<String, Object>> allInfos = new ArrayList<HashMap<String,Object>>();
					HashMap<String, Object> row;
					do{
						row = new HashMap<String, Object>();
						row.put("tripid", mCursor.getString(0));
						row.put("userid", mCursor.getString(1));
						row.put("name", mCursor.getString(2));
						row.put("starttime", mCursor.getString(3));
						row.put("endtime", mCursor.getString(4));
						row.put("length", mCursor.getDouble(5));
						row.put("count", mCursor.getInt(6));
						row.put("startaddrpt1", mCursor.getString(7));
						row.put("startaddrpt2", mCursor.getString(8));
						row.put("startaddrpt3", mCursor.getString(9));
						row.put("startaddrpt4", mCursor.getString(10));
						row.put("startaddrpt5", mCursor.getString(11));
						row.put("endaddrpt1", mCursor.getString(12));
						row.put("endaddrpt2", mCursor.getString(13));
						row.put("endaddrpt3", mCursor.getString(14));
						row.put("endaddrpt4", mCursor.getString(15));
						row.put("endaddrpt5", mCursor.getString(16));
						row.put("uploaded", mCursor.getInt(17));
						allInfos.add(row);
					}while(mCursor.moveToNext());
					if(!mCursor.isClosed()){
						mCursor.close();
					}
					return allInfos;
				} else{
					if(!mCursor.isClosed()){
						mCursor.close();
					}
					return null;
				}
			} else{
				return null;
			}
		} else {
			return null;
		}
	}
	*/
	/**
	 * used to delete all the uploaded trip points from local cache when not
	 * recording
	 * 
	 * @return the number of rows deleted, or negative numbers when things go
	 *         wrong
	 */
	synchronized public long deleteCompletedPoints() {
		if (db.isOpen()) {
			return db.delete(TRIP_POINT_TABLE, "uploaded=1", null);
		} else {
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
}
