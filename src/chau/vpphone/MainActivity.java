package chau.vpphone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class MainActivity extends Activity {

	Button btnLogin01, btnRegis, btnAbout;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        btnLogin01 = (Button)findViewById(R.id.btnLogin01);
        btnLogin01.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this,LoginActivity.class);
				startActivity(intent);
			}
		});
        
        btnRegis = (Button)findViewById(R.id.btnRegis);
        btnRegis.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this,RegisterActivity.class);
				startActivity(intent);
			}
		});
        
        btnAbout = (Button)findViewById(R.id.btnAbout);
        btnAbout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this,AboutActivity.class);
				startActivity(intent);
			}
		});
    }
}
