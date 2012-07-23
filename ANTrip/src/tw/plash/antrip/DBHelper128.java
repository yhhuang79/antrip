package tw.plash.antrip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.location.Location;

/**
 * @author CSZU
 * 
 */
public class DBHelper128 {

	private static final String DATABASE_NAME = "antrip"; // database
																		// name
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
		+ "time TEXT, " 
		+ "altitude TEXT, " 
		+ "speed TEXT, " 
		+ "bearing TEXT, "
		+ "accuracy TEXT, "
		+ "userid TEXT, "
		+ "tripid TEXT)";
	
	private static final String CREATE_TABLE_TRIP_INFO = "CREATE TABLE " 
		+ TRIP_INFO_TABLE
		+ "(tripid TEXT PRIMARY KEY, "
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
	
	synchronized public long insert(Location location, String userid, Long tripid) {
		if (db.isOpen()) {
			ContentValues cv = new ContentValues();
			locationStringConverter l2s = new locationStringConverter(location);
			cv.put("latitude", l2s.getLatitude());
			cv.put("longitude", l2s.getLongitude());
			cv.put("time", l2s.getTime());
			cv.put("altitude", l2s.getAltitude());
			cv.put("speed", l2s.getSpeed());
			cv.put("bearing", l2s.getBearing());
			cv.put("accuracy", l2s.getAccuracy());
			cv.put("userID", userid);
			cv.put("tripID", String.valueOf(tripid));
			return db.insert(TRIP_POINT_TABLE, null, cv);
		} else {
			return -2;
		}
	}
	
	synchronized public long insert(Location location, String userid, Long tripid, CandidateCheckinObject cco) {
		if (db.isOpen()) {
			ContentValues cv = new ContentValues();
			locationStringConverter l2s = new locationStringConverter(location);
			cv.put("latitude", l2s.getLatitude());
			cv.put("longitude", l2s.getLongitude());
			cv.put("time", l2s.getTime());
			cv.put("altitude", l2s.getAltitude());
			cv.put("speed", l2s.getSpeed());
			cv.put("bearing", l2s.getBearing());
			cv.put("accuracy", l2s.getAccuracy());
			cv.put("userID", userid);
			cv.put("tripID", String.valueOf(tripid));
			//cco fields
			return db.insert(TRIP_POINT_TABLE, null, cv);
		} else {
			return -2;
		}
	}
	
	synchronized public long createNewTripInfo(Long tripid, String userid){
		if(db.isOpen()){
			ContentValues cv = new ContentValues();
			cv.put("tripid", String.valueOf(tripid));
			cv.put("userid", userid);
			cv.put("uploaded", 0);
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
	synchronized public long insertStarttime(String tripid, String starttime){
		if(db.isOpen()){
			ContentValues cv = new ContentValues();
			cv.put("starttime", starttime);
			return db.update(TRIP_INFO_TABLE, cv, "tripid=" + tripid, null);
		} else{
			return -2;
		}
	}
	
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
	synchronized public long insertStartaddr(String tripid, String startaddrpt1, String startaddrpt2, String startaddrpt3, String startaddrpt4, String startaddrpt5){
		if(db.isOpen()){
			ContentValues cv = new ContentValues();
			cv.put("startaddrpt1", startaddrpt1);
			cv.put("startaddrpt2", startaddrpt2);
			cv.put("startaddrpt3", startaddrpt3);
			cv.put("startaddrpt4", startaddrpt4);
			cv.put("startaddrpt5", startaddrpt5);
			return db.update(TRIP_INFO_TABLE, cv, "tripid=" + tripid, null);
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
	synchronized public long insertEndInfo(String tripid, String name, String endtime, Double length){
		if(db.isOpen()){
			ContentValues cv = new ContentValues();
			//pop up screen asking for user input
			cv.put("name", name);
			cv.put("endtime", endtime);
			cv.put("length", length);
			//count the number of points directly from DB
			int count = (int) getNumberofPoints(tripid);
			cv.put("count", count);
			return db.update(TRIP_INFO_TABLE, cv, "tripid=" + tripid, null);
		} else{
			return -2;
		}
	}
	
	synchronized public long insertEndaddr(String tripid, String endaddrpt1, String endaddrpt2, String endaddrpt3, String endaddrpt4, String endaddrpt5){
		if(db.isOpen()){
			ContentValues cv = new ContentValues();
			cv.put("endaddrpt1", endaddrpt1);
			cv.put("endaddrpt2", endaddrpt2);
			cv.put("endaddrpt3", endaddrpt3);
			cv.put("endaddrpt4", endaddrpt4);
			cv.put("endaddrpt5", endaddrpt5);
			return db.update(TRIP_INFO_TABLE, cv, "tripid=" + tripid, null);
		} else{
			return -2;
		}
	}
	
	/**
	 * get all points from realtime data table for drawing purpose
	 * 
	 * @return a list of cachedPoints with lat, lon values for drawing, null if
	 *         anything goes wrong
	 */
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
	}

	synchronized public int markUploaded(String tripid) {
		if (db.isOpen()) {
			ContentValues cv = new ContentValues();
			cv.put("uploaded", 1);
			return db.update(TRIP_INFO_TABLE, cv, "tripid=" + tripid, null);
		} else {
			return -2;
		}
	}

	synchronized public long getNumberofPoints(String tripid) {
		String sql = "SELECT COUNT(id) FROM " + TRIP_POINT_TABLE + " WHERE uploaded=0";
		if (db.isOpen()) {
			SQLiteStatement statement = db.compileStatement(sql);
			long result = statement.simpleQueryForLong();
			statement.close();
			return result;
		} else {
			return -2;
		}
	}

	synchronized public PLASHLocationObject getTrip(String tripid) {
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
	}

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
