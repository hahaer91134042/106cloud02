package tcnr.com.project_ic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Fragment;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import static tcnr.com.project_ic.IC4000.iotData;
//import static tcnr.com.project_ic.IC4000.iotID;
//import static tcnr.com.project_ic.IC4000.iotTime;
//import static tcnr.com.project_ic.IC4000.iotName;
//import static tcnr.com.project_ic.IC4000.iotage;
//import static tcnr.com.project_ic.IC4000.iotpost;
//import static tcnr.com.project_ic.IC4000.iotbody_temp;

public class IC4000bodytem extends Fragment 
                           implements SearchView.OnQueryTextListener, 
                                      SearchView.OnCloseListener {

	private SearchView icSearchView4100;// actionBar裡面的searchView
	private Spinner icSpin4101, icSpin4102;// 4101用來選擇最新/歷史紀錄 4102是用來選擇
											// 一周/一個月/三個月紀錄
	List<Map<String, Object>> mList;
	private ListView icLisVie4103;// 顯示DB裡面的資料
	private TextView icTexVie4104;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		return inflater.inflate(R.layout.ic4000bodytem, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// <!--------------設定使用物件的ID--------------------------------->
		icSpin4101 = (Spinner) getView().findViewById(R.id.icSpi4101);
		icSpin4102 = (Spinner) getView().findViewById(R.id.icSpi4102);
		
		// <!------------------------------------------------------------->
		// <!--------------設定4101這個spinner所使用的字串陣列------------------------------------------>
		ArrayAdapter<CharSequence> adap_icSpin4101_List = ArrayAdapter.createFromResource(getActivity(),
				R.array.icSpiArr4101, android.R.layout.simple_spinner_item);// 設定Adaptor
		icSpin4101.setAdapter(adap_icSpin4101_List);// 4101使用這個Adaptor
		icSpin4101.setOnItemSelectedListener(icSpin4101on);// 設定監聽
		// <!---------------------------------------------------------------------------------->
		// <!--------------設定4102這個Spinner所使用的字串陣列--------------------------------------->
		ArrayAdapter<CharSequence> adap_icSpin4102_List = ArrayAdapter.createFromResource(getActivity(),
				R.array.icSpiArr4102, android.R.layout.simple_spinner_item);// 設定Adaptor
		icSpin4102.setAdapter(adap_icSpin4102_List);// 4102使用這個Adaptor
		icSpin4102.setVisibility(View.INVISIBLE);// 先預設隱藏
		icSpin4102.setOnItemSelectedListener(icSpin4102on);// 設定監聽
		// <!------------------------------------------------------------------------>
		setupListView();
		super.onActivityCreated(savedInstanceState);
	}
	/************************************************
	 * 	iotData[i][0]=ID				
	 *	iotData[i][1]=Time
	 *	iotData[i][2]=Name
	 *	iotData[i][3]=age
	 *	iotData[i][4]=post 心跳
	 *	iotData[i][5]=body_temp
	 ***************************************************/
	private void setupListView() {
		   icTexVie4104=(TextView)getView().findViewById(R.id.icTexVie4104);
		   icLisVie4103=(ListView)getView().findViewById(R.id.icLisVie4103);
		   icTexVie4104.setText("目前最新的:"+iotData.length+"筆資料");
			mList = new ArrayList<Map<String, Object>>();
			for (String[] getIOTdata : iotData) {
				
//			}for (int i = 0; i < iotID.length; i++) {
				Map<String, Object> item = new HashMap<String, Object>();
				item.put("imgView", R.drawable.tempmeter);
				item.put("txtView", "日期:" + getIOTdata[1] +"\n姓名:"+getIOTdata[2]+
						"\n年齡:"+getIOTdata[3]+"\n體溫:" + getIOTdata[5]+" 度C");
				mList.add(item);
			}
			SimpleAdapter adapter = new SimpleAdapter(getActivity(), mList, R.layout.ic4000list_view_style,
					new String[] { "imgView", "txtView" }, new int[] { R.id.imgView, R.id.txtView });
			icLisVie4103.setAdapter(adapter);
		   
			icLisVie4103.setTextFilterEnabled(true);
		   
			
		}

	// <!------------------------4101所使用的監聽------------------------------------->
	private Spinner.OnItemSelectedListener icSpin4101on = new Spinner.OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			// TODO Auto-generated method stub//parent可取初選到的字串
			// position可取初選到的字串陣列位置
			switch (position) {
			case 0:// 選最新紀錄
				icSpin4102.setVisibility(View.INVISIBLE);
				break;
			case 1:// 選歷史紀錄
				icSpin4102.setVisibility(View.VISIBLE);// 顯示出spinner4102
				break;
			}

		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// TODO Auto-generated method stub

		}

	};
	// <!-----------------------------4102所使用的監聽-------------------------------------->
	private Spinner.OnItemSelectedListener icSpin4102on = new Spinner.OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position2, long id) {
			switch (position2) {
			case 0:// 三個月內紀錄

				break;
			case 1:// 1個月內紀錄

				break;
			case 2:// 一周內紀錄

				break;

			}

		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// TODO Auto-generated method stub

		}

	};
	
	

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		inflater.inflate(R.menu.ic4000, menu);
		// <!------------把menu裡面的searchItem設定成actionBar來開啟功能------------------------------------>
		icSearchView4100 = (SearchView) menu.findItem(R.id.ic4000searchItem).getActionView();
		u_setupSearchView();// 使用自訂義method
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		int id = item.getItemId();
		switch (id) {
		case R.id.action_settings:// 設定

			break;
		case R.id.action_finish:// 結束
			getActivity().finish();
			break;

		case R.id.ic4000trackItem:// 跳到追蹤頁面
			Intent itTrack = new Intent();
			itTrack.setClass(getActivity(), IC4000track.class);
			startActivity(itTrack);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
// <!-----------------searchView所使用的自訂一頁面-----把searchView所輸入的字串輸出給監聽-------------------------------------->
	private void u_setupSearchView() {
		// TODO Auto-generated method stub
		icSearchView4100.setIconifiedByDefault(true);// 使用預設的icon
		SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
		if (searchManager != null) {
			List<SearchableInfo> searchables = searchManager.getSearchablesInGlobalSearch();

			// Try to use the "applications" global search provider
			SearchableInfo info = searchManager.getSearchableInfo(getActivity().getComponentName());
			for (SearchableInfo inf : searchables) {
				if (inf.getSuggestAuthority() != null && inf.getSuggestAuthority().startsWith("applications")) {
					info = inf;
				}
			}
			icSearchView4100.setSearchableInfo(info);
		}
		icSearchView4100.setOnQueryTextListener(this);
		icSearchView4100.setOnCloseListener(this);
	}

	@Override
	public boolean onClose() {// 搜尋關閉
		// TODO Auto-generated method stub
		return false;
	}
	// <!------------監聽按下送出搜尋紐------------------->
	@Override
	public boolean onQueryTextSubmit(String query) {
		// 度C
//		icTexV4103.setText("Query = " + query + " : submitted");
		return false;
	}
	// <!-------- 監聽輸入字串改變------------------------>
	@Override
	public boolean onQueryTextChange(String newText) {
		// TODO Auto-generated method stub
//		icTexV4103.setText("Query = " + newText);
		return false;
	}
}
