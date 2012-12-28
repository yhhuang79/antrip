package tw.plash.antrip.offline;

import java.io.Serializable;

import android.location.Location;

public class CandidateCheckinObject implements Serializable{
	
	/**
	 * 
	 */
//	private static final long serialVersionUID = -5345143287718540755L;
	
	private Integer emotionID;
	private String checkinText;
	private String picturePath;
	private String pictureName;
	
	private Location location;
	
	/**
	 * initialize all fields with null value
	 */
	public CandidateCheckinObject() {
		emotionID = null;
		checkinText = null;
		picturePath = null;
		pictureName = null;
		location = null;
	}
	
	@Override
	public String toString() {
		return "mood:" + (emotionID != null?emotionID:"") + ";text:" + (checkinText!=null?checkinText:"") + ";pic:" + (picturePath!=null?picturePath:"");
	}
	
	public boolean isValid(){
		if(emotionID != null || pictureName != null || (checkinText != null && checkinText.length() > 0)){
			return true;
		} else{
			return false;
		}
	}

	public Integer getEmotionID() {
		return emotionID;
	}

	public void setEmotionID(Integer emotionID) {
		this.emotionID = emotionID;
	}

	public String getCheckinText() {
		return checkinText;
	}

	public void setCheckinText(String checkinText) {
		this.checkinText = checkinText;
	}
	
	public String getPictureName(){
		return pictureName;
	}
	
	public String getPicturePath() {
		return picturePath;
	}

	public void setPicturePath(String picturePath) {
		this.picturePath = picturePath;
		try{
			this.pictureName = picturePath.substring(picturePath.lastIndexOf("/"));
		}catch(IndexOutOfBoundsException e){
			e.printStackTrace();
			this.pictureName = picturePath;
		}
	}
	
	public Location getLocation(){
		return location;
	}
	
	public void setLocation(Location inputLoc){
		this.location = inputLoc;
	}
}
