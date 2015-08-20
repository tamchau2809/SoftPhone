package chau.vpphone;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class AboutActivity extends Activity {

	TextView tv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		tv = (TextView)findViewById(R.id.tvLINK);
		tv.setMovementMethod(LinkMovementMethod.getInstance());
	}
}
