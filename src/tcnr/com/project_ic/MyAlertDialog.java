package tcnr.com.project_ic;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

public class MyAlertDialog extends AlertDialog {
	
	
	
	

	protected MyAlertDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public Button getButton(int whichButton) {
		// TODO Auto-generated method stub
		return super.getButton(whichButton);
	}

	@Override
	public ListView getListView() {
		// TODO Auto-generated method stub
		return super.getListView();
	}

	@Override
	public void setTitle(CharSequence title) {
		// TODO Auto-generated method stub
		super.setTitle(title);
	}

	@Override
	public void setCustomTitle(View customTitleView) {
		// TODO Auto-generated method stub
		super.setCustomTitle(customTitleView);
	}

	@Override
	public void setMessage(CharSequence message) {
		// TODO Auto-generated method stub
		super.setMessage(message);
	}

	@Override
	public void setView(View view) {
		// TODO Auto-generated method stub
		super.setView(view);
	}

	@Override
	public void setView(View view, int viewSpacingLeft, int viewSpacingTop, int viewSpacingRight,
			int viewSpacingBottom) {
		// TODO Auto-generated method stub
		super.setView(view, viewSpacingLeft, viewSpacingTop, viewSpacingRight, viewSpacingBottom);
	}

	@Override
	public void setButton(int whichButton, CharSequence text, Message msg) {
		// TODO Auto-generated method stub
		super.setButton(whichButton, text, msg);
	}

	@Override
	public void setButton(int whichButton, CharSequence text, OnClickListener listener) {
		// TODO Auto-generated method stub
		super.setButton(whichButton, text, listener);
	}

	@Override
	public void setButton(CharSequence text, Message msg) {
		// TODO Auto-generated method stub
		super.setButton(text, msg);
	}

	@Override
	public void setButton2(CharSequence text, Message msg) {
		// TODO Auto-generated method stub
		super.setButton2(text, msg);
	}

	@Override
	public void setButton3(CharSequence text, Message msg) {
		// TODO Auto-generated method stub
		super.setButton3(text, msg);
	}

	@Override
	public void setButton(CharSequence text, OnClickListener listener) {
		// TODO Auto-generated method stub
		super.setButton(text, listener);
	}

	@Override
	public void setButton2(CharSequence text, OnClickListener listener) {
		// TODO Auto-generated method stub
		super.setButton2(text, listener);
	}

	@Override
	public void setButton3(CharSequence text, OnClickListener listener) {
		// TODO Auto-generated method stub
		super.setButton3(text, listener);
	}

	@Override
	public void setIcon(int resId) {
		// TODO Auto-generated method stub
		super.setIcon(resId);
	}

	@Override
	public void setIcon(Drawable icon) {
		// TODO Auto-generated method stub
		super.setIcon(icon);
	}

	@Override
	public void setIconAttribute(int attrId) {
		// TODO Auto-generated method stub
		super.setIconAttribute(attrId);
	}

	@Override
	public void setInverseBackgroundForced(boolean forceInverseBackground) {
		// TODO Auto-generated method stub
		super.setInverseBackgroundForced(forceInverseBackground);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return super.onKeyUp(keyCode, event);
	}

}
