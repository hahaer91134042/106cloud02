package tcnr.com.project_ic;

import static tcnr.com.project_ic.CommonUtilities.API_Key;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import tcnr.com.project_ic.providers.DBContentProvider;


public  class SQLiteWriter {
    static String TAG="tcnr6==>";
	//---------取得系統目前時間 輸出格式:yyyy/MM/dd HH:mm:ss---------
	public static String getSystemTime() {
		SimpleDateFormat Dateformatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		 Date curDate = new Date(System.currentTimeMillis()); //  獲取當前時間
		 String sysTime = Dateformatter.format(curDate);
		 
		return sysTime;
	}
	//----------將格式yyyy/MM/dd HH:mm:ss的時間轉成長整數的絕對時間------
	public static long parseSaveDate(String getDate) {
	    long saveDate=0;
		SimpleDateFormat Dateformatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		try {
			saveDate = Dateformatter.parse(getDate).getTime();
			Log.d(TAG, "parse_getSystime=>" + getDate);
		} catch (ParseException e) {
			
			e.printStackTrace();
		}
		return saveDate;
	}
    //----------將資料寫進SQLite的method-------------------
	public static void wirteToSQLite(ContentResolver mContRes, Uri uri,
			String[] COLUMN, ContentValues newRow) {
		
		Cursor c = mContRes.query(uri, COLUMN, null, null, null);
		c.moveToFirst();// 一定要寫，不然會出錯	
		// --------insert SQLite------------------
		mContRes.insert(uri, newRow);// insert
		//--------------------------
		c.close();
		
	}
    //-----------清除掉SQLite目標表格的內容--------------
	public static void sqlDelete(ContentResolver mContRes, Uri uri, String[] COLUMN) {
		Cursor c = mContRes.query(uri, COLUMN, null, null, null);
		c.moveToFirst();// 一定要寫，不然會出錯	
		//-----------delete---
		mContRes.delete(uri, null, null); // 刪除所有資料
		//------------------
		c.close();
		
	}
    //-----------修改SQLite目標資料的method---------------
	public static void sqlUpdate(ContentResolver mContRes, Uri uri, String[] COLUMN,
			ContentValues newRow, String whereClause) {
		Cursor c = mContRes.query(uri, COLUMN, null, null, null);
		c.moveToFirst();// 一定要寫，不然會出錯
		//--------update--------
		mContRes.update(uri, newRow, whereClause, null);
		//-----------------------
		c.close();
	}

	
	//--------輸出目標表格所有資料的method-----------
	public static String[][] getSQLiteData(ContentResolver mContRes, Uri uri,
			String[] COLUMN) {
		
		Cursor c = mContRes.query(uri, COLUMN, null, null, null);
		c.moveToFirst();// 一定要寫，不然會出錯
		
		int columNum=c.getColumnCount();
		int count=c.getCount();
	
		String[][] getValue = new String[count][columNum] ;
		Log.d(TAG, "getCount=>"+count+"getCoumn=>"+columNum);
		
		if(count>0&&columNum>0){
			for(int i=0;i<count;i++){
				c.moveToPosition(i);
				for(int j=0;j<columNum;j++){
					getValue[i][j]=c.getString(j);				
					
				}				
			}	
			
			c.close();
			Log.d(TAG, "getValue=>"+getValue);
			return getValue;
			
		}else {
			c.close();
			return null;
		}
				
	}
	//---------------發送GCM的method--------------------------
	public static void sendGCMmsg(final String msg, final String reg_id) {

		new Thread(new Runnable() {

			@Override
			public void run() {
				String devices = reg_id;//Client 端的 Registration ID  
		   	      Log.d("tcnr6==>", devices);
		    	  Sender sender = new Sender(API_Key);// Android API KEY  gcm的方法

		    		   Message message = new Message.Builder().addData("message", msg).build();//傳送的訊息      gcm的方法
		       	       Result result = null;
		     	      try {
		     	       result = sender.send(message, devices, 5);
		     	      } catch (IOException e) {
		     	       // TODO Auto-generated catch block
		     	       e.printStackTrace();
		     	      }
		     	      if (result.getMessageId() != null) {
		     	       Log.i("result", "getMessageId = "+result.getMessageId());
		     	       String canonicalRegId = result.getCanonicalRegistrationId();
		     	       Log.i("canonicalRegId", "canonicalRegId = "+canonicalRegId);
		     	      }   
		   	     }

			
		}).start();
	}
	
    //密码加密 与php加密一致
	public static String md5(String input) throws NoSuchAlgorithmException {
		String result = input;
		if (input != null) {
			MessageDigest md = MessageDigest.getInstance("MD5"); // or "SHA-1"
			md.update(input.getBytes());
			BigInteger hash = new BigInteger(1, md.digest());
			result = hash.toString(16);
			while (result.length() < 32) {
				result = "0" + result;
			}
		}
		return result;
	}
//-----------計算2組經緯度的method-----------單位:公尺---------
	public static double getDistance(double latitude1, double longitude1,double latitude2, double longitude2){
		   double radLatitude1 = latitude1 * Math.PI / 180;
		   double radLatitude2 = latitude2 * Math.PI / 180;
		   double l = radLatitude1 - radLatitude2;
		   double p = longitude1 * Math.PI / 180 - longitude2 * Math.PI / 180;
		   double distance = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(l / 2), 2)
		                    + Math.cos(radLatitude1) * Math.cos(radLatitude2)
		                    * Math.pow(Math.sin(p / 2), 2)));
		   distance = distance * 6378137.0;
		   distance = Math.round(distance * 10000) / 10000;

		   return distance ;
		}
   

}
