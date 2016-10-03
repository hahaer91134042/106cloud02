package tcnr.com.project_ic;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;




public class IC4000 extends Activity  {

	String TAG="tcnr6==>";
	


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ic4000);
		// <!----------使用並設定ActionBarar----------------------->
		ActionBar actBar = getActionBar();
		actBar.setDisplayShowTitleEnabled(false);// 隱藏title
		actBar.setDisplayUseLogoEnabled(true);// 開啟使用logo
		// actBar.setBackgroundDrawable(new ColorDrawable(R.drawable.Lime));
		actBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		// <!-------------------------------------------->
		// 設定心跳Tab標籤頁
		MyTableListener<IC4000heartrate> tabListenerMainFrag = new MyTableListener<IC4000heartrate>(IC4000.this,
				"HeartRate Fragment", IC4000heartrate.class);
		actBar.addTab(actBar.newTab().setText("心跳紀錄")
				.setIcon(getResources().getDrawable(android.R.drawable.ic_lock_idle_alarm))
				.setTabListener(tabListenerMainFrag));
		// 設定心跳Tab標籤頁
		MyTableListener<IC4000bodytem> tabListenerPersInfoFrag = new MyTableListener<IC4000bodytem>(IC4000.this,
				"BodyTemp Fragment", IC4000bodytem.class);
		actBar.addTab(
				actBar.newTab().setText("體溫紀錄").setIcon(getResources().getDrawable(android.R.drawable.star_big_on))
						.setTabListener(tabListenerPersInfoFrag));
       
//		sendToService(IC7001.LoginAccount);
		improtIOTData();
	}
	
	private void improtIOTData() {
		try {
						
			
			
			String result = DBConnector.getIOTDATA();
			/**************************************************************************
			 * SQL 結果有多筆資料時使用JSONArray 只有一筆資料時直接建立JSONObject物件 JSONObject
			 * jsonData = new JSONObject(result);
			 **************************************************************************/
			 
			String r = result.toString().trim();
			   //以下程式碼一定要放在前端藍色程式碼執行之後，才能取得狀態碼
			   //存取類別成員 DBConnector.httpstate 判定是否回應 200(連線要求成功)
			
			   Log.d(TAG, "httpstate="+DBConnector.httpstate );
//			   if (DBConnector.httpstate == 200) {
//			    
//			    Toast.makeText(getBaseContext(), "已經完成由伺服器匯入資料",
//			      Toast.LENGTH_LONG).show();
//			    u_writeLoginData(result);
//			    
//			    } else {
//			    Toast.makeText(getBaseContext(), "伺服器無回應，請稍後在試",
//			      Toast.LENGTH_LONG).show();
//			   }	
			   
			   u_readIOTData(result);
			
		}catch (Exception e) {
			Log.d(TAG,"error->"+ e.toString());
		}
		
	}
	/************************************************
	 * 	iotData[i][0]=ID				
	 *	iotData[i][1]=Time
	 *	iotData[i][2]=Name
	 *	iotData[i][3]=age
	 *	iotData[i][4]=post 心跳
	 *	iotData[i][5]=body_temp
	 ***************************************************/

//	public static String[] iotID,iotName,iotbody_temp,iotpost,iotage,iotTime ;
	public static String[][] iotData;
	private void u_readIOTData(String result) {
		iotData=null;
//		iotID=null;iotName=null;iotbody_temp=null;iotpost=null;iotage=null;iotTime=null;
		 try {
			JSONArray jsonArray = new JSONArray(result);
			
			iotData=new String[jsonArray.length()][6];

		
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonData = jsonArray.getJSONObject(i);
				
				iotData[i][0]=jsonData.getString("ID").toString().trim();				
				iotData[i][1]=jsonData.getString("Time").toString().trim();
				iotData[i][2]=jsonData.getString("Name").toString().trim();
				iotData[i][3]=jsonData.getString("age").toString().trim();
				iotData[i][4]=jsonData.getString("post").toString().trim();
				iotData[i][5]=jsonData.getString("body_temp").toString().trim();

//				iotID[i]=jsonData.getString("ID").toString().trim();				
//				iotTime[i]=jsonData.getString("Time").toString().trim();
//				iotName[i]=jsonData.getString("Name").toString().trim();
//				iotage[i]=jsonData.getString("age").toString().trim();
//				iotpost[i]=jsonData.getString("post").toString().trim();
//				iotbody_temp[i]=jsonData.getString("body_temp").toString().trim();
				
				// 取出 jsonObject 中的字段的值的空格
//				Log.d(TAG, "IOT_Data->"+iotTemp[i]+"/"+iotHumi[i]+"/"+iotDust[i]);
			}
			
		} catch (JSONException e) {			
			e.printStackTrace();
		}
		
	}

	@Override
	protected void onDestroy() {
//		stopService(IC7001.iCareService);
		super.onDestroy();
	}





}
