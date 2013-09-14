package tw.plash.antrip.offline.connection;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;


public class InternetUtility {
	public static String encode(String input){
		String output = null;
		try {
			output = URLEncoder.encode(input, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			output = input;
		}
//		Log.e("correct url encoder", "outParam=" + output);
		return output;
	}
	
	public static String getMD5(String md5) {
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			byte[] array = md.digest(md5.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
			}
			return sb.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String correctURLEncoder(final String inURL) {
		String inParameter = inURL.substring(inURL.lastIndexOf("?") + 1);
//		Log.e("correct url encoder", "inParam=" + inParameter);
		String outParameter = null;
		try {
			outParameter = URLEncoder.encode(inParameter, "UTF-8");
//			Log.e("correct url encoder", "outParam=" + outParameter);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String result = inURL.replace(inParameter, outParameter);
//		Log.e("correct url encoder", "result=" + result);
		return result;
	}
	
	public static boolean isNetworkAvailable(Context mContext) {
		ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni != null) { // in airplane mode, there are no "Active Network",
							// networkInfo will be null
			return ni.isConnected();
		} else {
			return false;
		}
	}
}
