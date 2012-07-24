package tw.plash.antrip;

import java.io.Serializable;

public class CandidateCheckinObject implements Serializable{
	
	/**
	 * 
	 */
//	private static final long serialVersionUID = -5345143287718540755L;
	
	private Integer emotionID;
	private String checkinText;
	private String picturePath;
	
	/**
	 * initialize all fields with null value
	 */
	public CandidateCheckinObject() {
		emotionID = null;
		checkinText = null;
		picturePath = null;
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

	public String getPicturePath() {
		return picturePath;
	}

	public void setPicturePath(String picturePath) {
		this.picturePath = picturePath;
	}
}