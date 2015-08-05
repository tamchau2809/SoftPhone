package chau.vpphone;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.WebView;

public class AboutActivity extends Activity {

	WebView webview;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		webview = (WebView)findViewById(R.id.webview);
		webview.getSettings();
		
		String text = "<html><body>" + "<p align=\"justify\">"
				+ getString(R.string.about) + "</p> " + "</body></html>";

		webview.loadData(text, "text/html", "utf-8");
	}
}
