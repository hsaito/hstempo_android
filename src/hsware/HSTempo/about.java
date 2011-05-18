package hsware.HSTempo;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;

public class about extends Activity {
	WebView webview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.info);

		webview = (WebView) findViewById(R.id.webview);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.getSettings().setPluginsEnabled(true);
		webview.loadDataWithBaseURL(null, formatText(), "text/html", "utf-8",
				null);
	}

	private String formatText() {
		StringBuilder sb = new StringBuilder();

		sb.append("<body style=\"background-color: #000000\">"
				+ "<div style=\"color: #FFFFFF\">"
				+ "Write in version information here" + "</div></body>");

		return sb.toString();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
			webview.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
