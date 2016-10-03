package tcnr.com.project_ic;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import tcnr.com.project_ic.providers.DBContentProvider;

public class IC7003 extends Activity {
	private static ContentResolver mContRes;
	private String[] MYCOLUMN = new String[] { "id", "account", "code","phone","mail","reg_id","user_ip","account_status","account_lvl","date"};

	private Button icBtn7007;
    String TAG="tcnr6==>";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.ic7003);
		setupViewComponent();
	}

	private void setupViewComponent() {
		// TODO Auto-generated method stub
		icBtn7007=(Button)findViewById(R.id.icBtn7007);
		
		icBtn7007.setOnClickListener(icBtn7007On);
	}
	
	private Button.OnClickListener icBtn7007On=new Button.OnClickListener(){

		@Override
		public void onClick(View v) {
			/*
			 * 可以在這設定載入動畫
			 * */
			Intent intent=new Intent();
			intent.setClass(IC7003.this, IC7004.class);
			startActivity(intent);
			
			improtAll_SQLData();
			
		}
		
	};
	//------------------將所有成員的資料匯入sqlite----------
	private void improtAll_SQLData() {
		try {
						
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("query_string", "import"));
			
			String result = DBConnector.executeQuery("import",params);
			/**************************************************************************
			 * SQL 結果有多筆資料時使用JSONArray 只有一筆資料時直接建立JSONObject物件 JSONObject
			 * jsonData = new JSONObject(result);
			 **************************************************************************/
			 
			String r = result.toString().trim();
			   //以下程式碼一定要放在前端藍色程式碼執行之後，才能取得狀態碼
			   //存取類別成員 DBConnector.httpstate 判定是否回應 200(連線要求成功)
			
			   Log.d(TAG, "httpstate="+DBConnector.httpstate );
			   if (DBConnector.httpstate == 200) {
			    
			    Toast.makeText(getBaseContext(), "已經完成由伺服器匯入資料",
			      Toast.LENGTH_LONG).show();
			    u_writeLoginData(result);
			    
			    } else {
			    Toast.makeText(getBaseContext(), "伺服器無回應，請稍後在試",
			      Toast.LENGTH_LONG).show();
			   }	  		
	
			
		}catch (Exception e) {
			Log.d(TAG,"error->"+ e.toString());
		}
		
	}
	private void u_writeLoginData(String result) {
		// -------------------------
		mContRes = getContentResolver();
		//-----------------------------		
//		Cursor c = mContRes.query(DBContentProvider.CONTENT_URI_login, MYCOLUMN, null, null, null);
//		c.moveToFirst(); // 一定要寫，不然會出錯	
		 try {
			JSONArray jsonArray = new JSONArray(result);
			
			if (jsonArray.length() > 0) {

				SQLiteWriter.sqlDelete(mContRes, DBContentProvider.CONTENT_URI_login, MYCOLUMN);
			}
			
			ContentValues newRow = new ContentValues();
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonData = jsonArray.getJSONObject(i);

				// 取出 jsonObject 中的字段的值的空格
				Iterator itt = jsonData.keys();
				while (itt.hasNext()) {
					String key = itt.next().toString();
					String value = jsonData.getString(key);
					if (value == null) {
						continue;
					} else if ("".equals(value.trim())) {
						continue;
					} else {
						newRow.put(key, value.trim());
					}

				}

				SQLiteWriter.wirteToSQLite(mContRes, DBContentProvider.CONTENT_URI_login, MYCOLUMN, newRow);
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		c.close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
//		getMenuInflater().inflate(R.menu.ic7001, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		int id = item.getItemId();
//		switch (id) {
//		case R.id.action_finish://結束
//			finish();
//			break;		
//		}
		return super.onOptionsItemSelected(item);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
;