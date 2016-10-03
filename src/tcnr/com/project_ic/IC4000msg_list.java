package tcnr.com.project_ic;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import tcnr.com.project_ic.providers.DBContentProvider;

/*******************************
 * 直接把SQLite裡面所存的訊息整個輸出 
 * 
 * 
 * ******************************/
public class IC4000msg_list extends Activity {

      private TextView icTexVie4230;
      
      List<Map<String, Object>> mList;
      private ListView icLisVie4230;
   // --------------
  	private static ContentResolver mContRes;
  	private String[] MYMSGCOLUMN = new String[] {"id","msgDate","GCMmsg" };
  	String[][] getMsgData=null;
  	//--------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ic4000msg_list);
		setupviewcomponent();
//		sendToService(IC7001.LoginAccount);
	}
	private void sendToService(String loginAccount) {
		Intent iCareService = new Intent(getBaseContext(), ICareService.class);//用intent連接M1104跟MyService
		Bundle bundle = new Bundle(); //Bundle()可以裝入要傳送的訊息 在整捆傳送
		bundle.putString("account", loginAccount);//傳送變數 String action="showtime"放進Bundle裡面
		iCareService .putExtras(bundle);//用intent傳送Bundle
		startService(iCareService);//參考intent開啟服務;這裡是MyService	
}
	
	private void setupviewcomponent() {
		icLisVie4230=(ListView)findViewById(R.id.ic4000LisVie4230);
		icTexVie4230=(TextView)findViewById(R.id.icTexVie4230);
		
		//-------------
		mContRes = getContentResolver();
		//--------------
		 getMsgData=SQLiteWriter.getSQLiteData(mContRes,DBContentProvider.CONTENT_URI_GCMmsg, MYMSGCOLUMN);
		
		if (getMsgData != null) {
			icTexVie4230.setText("歷史訊息:共"+getMsgData.length+"筆");
			mList = new ArrayList<Map<String, Object>>();
			for (int i = 0; i < getMsgData.length; i++) {

				Map<String, Object> item = new HashMap<String, Object>();
				item.put("imgView", R.drawable.member1);
				item.put("txtView", "日期:" + getMsgData[i][1] + "\n訊息:" + getMsgData[i][2]);
				mList.add(item);
			}

			SimpleAdapter adapter = new SimpleAdapter(this, mList, R.layout.ic4000list_view_style,
					new String[] { "imgView", "txtView" }, new int[] { R.id.imgView, R.id.txtView });
			icLisVie4230.setAdapter(adapter);
		   
			icLisVie4230.setTextFilterEnabled(true);
			icLisVie4230.setOnItemClickListener(listviewOnItemClkLis);
		}else{
			icTexVie4230.setText("歷史訊息:查無資料....");
			
		}
	}
	AdapterView.OnItemClickListener listviewOnItemClkLis = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			MyAlertDialog alertDailog=new MyAlertDialog(IC4000msg_list.this);
			alertDailog.setTitle(getMsgData[position][1]);
			alertDailog.setIcon(android.R.drawable.star_big_on);
			alertDailog.setMessage(getMsgData[position][2]);
			alertDailog.setCancelable(true);
			alertDailog.show();
			
			
		}
		
	};
//--------生命週期--------------------------
	@Override
	protected void onDestroy() {
//		stopService(IC7001.iCareService);
		super.onDestroy();
	}
//------------------------------
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
	    getMenuInflater().inflate(R.menu.ic4000msg_list, menu);
		return super.onCreateOptionsMenu(menu);
	}
	private DialogInterface.OnClickListener btnYes = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			switch (which) {
			case -1://Cancel
				
				break;
			case -2://YES
				SQLiteWriter.sqlDelete(mContRes,DBContentProvider.CONTENT_URI_GCMmsg, MYMSGCOLUMN);
				Toast.makeText(IC4000msg_list.this,"所有歷史訊息已刪除...", Toast.LENGTH_SHORT).show();
				onCreate(null);
//				Intent intent=new Intent();
//				intent.setClass(IC4000msg_list.this, IC4000gcm.class);
//				startActivity(intent);
				break;
			case -3://No
				
				break;

			}
			
		}
		
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case R.id.ic4000msg_list_clean ://清除資料表
			MyAlertDialog myAlertDialog=new MyAlertDialog(IC4000msg_list.this);
			
			myAlertDialog.setTitle("警告");
			myAlertDialog.setIcon(android.R.drawable.btn_star_big_on);
			myAlertDialog.setMessage("確定要刪除全部訊息?");
			myAlertDialog.setCancelable(false);
			myAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE,"Cancel",btnYes);//-1
			myAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,"Yes", btnYes);//-2
			myAlertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,"No",btnYes);//-3
			myAlertDialog.show();
			
			break;
		case R.id.ic4000msg_list_back:
			IC4000msg_list.this.finish();
			
			break;
	
		}
		
		return super.onOptionsItemSelected(item);
	}

}
