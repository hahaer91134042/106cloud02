package tcnr.com.project_ic;

import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;


public class MyTableListener<T extends Fragment> implements TabListener {
    private Fragment mFragment=null;
    private final Activity mActivity;// 紀錄這個fragment所屬的activity
    private final String mTag;// 紀錄這個tab page的tag
    private final Class<T> mFragmentClass;// 記錄用來建立這個fragment的類別
	
    public MyTableListener(Activity activity, String tag, Class<T> fragmentClass) {
		// TODO Auto-generated constructor stub
		mActivity = activity;
		mTag = tag;
		mFragmentClass = fragmentClass;
		
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		// 檢查是否已經建立好fragment，tab page第一次顯示時要先建立fragment
		if(mFragment==null){
			mFragment = Fragment.instantiate(mActivity, mFragmentClass.getName());
			ft.add(R.id.icFrmLay4000, mFragment, mTag);
//			ft.add(R.id.framelayout, mFragment);
		}else
			ft.attach(mFragment);
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		if (mFragment != null)
			ft.detach(mFragment);
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub

	}

}
