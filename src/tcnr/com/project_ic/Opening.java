package tcnr.com.project_ic;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;


public class Opening extends Activity {


String TAG="tcnr6==>";
		//開場動畫
		private ImageView opening;
		Handler mhandler = new Handler();
		
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.opening);
			setupViewCompoment();
		}
		
		private void setupViewCompoment() {
			opening = (ImageView)findViewById(R.id.imageView_opening);
			mhandler.post(openZoomIn);
			mhandler.postDelayed(openZoomOut, 3200);
//			mhandler.postDelayed(openFadeOut, 4500);
			mhandler.postDelayed(openredir, 4500);
			
		}

		private final Runnable openZoomIn = new Runnable() {
			
			@Override
			public void run() {
				YoYo.with(Techniques.ZoomIn).duration(3000).playOn(opening);
//				Toast.makeText(getApplicationContext(), "動畫開始", Toast.LENGTH_SHORT).show();
			}
		};
		private final Runnable openZoomOut=new Runnable() {
			
			@Override
			public void run() {
				YoYo.with(Techniques.ZoomOutRight).duration(1500).playOn(opening);
				
			}
		};
		
//		private final Runnable openFadeOut = new Runnable() {
//			
//			@Override
//			public void run() {
//				YoYo.with(Techniques.ZoomOut).duration(4000).playOn(opening);
////				opening.setVisibility(8);
//			}
//		};
		
		private final Runnable openredir = new Runnable() {
			
			@Override
			public void run() {
				Intent intent=new Intent();
				intent.setClass(Opening.this, IC7001.class);
				startActivity(intent);
				Opening.this.finish();
			}
		};
		@Override
		protected void onStop() {
			Log.d(TAG, "Opening->onStop");
			

					
			
//			Toast.makeText(getApplicationContext(), "轉跳", Toast.LENGTH_SHORT).show();

			super.onStop();
		}
		
		
}
