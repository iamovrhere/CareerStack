package com.ovrhere.android.careerstack.ui.fragments;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources.Theme;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ovrhere.android.careerstack.R;
import com.ovrhere.android.careerstack.dao.CareerItem;
import com.ovrhere.android.careerstack.prefs.PreferenceUtils;
import com.ovrhere.android.careerstack.utils.CompatClipboard;
import com.ovrhere.android.careerstack.utils.ShareIntentUtil;
import com.ovrhere.android.careerstack.utils.ToastManager;

/**
 * The listing of a job item. Provides ability to open, copy or share link.
 * @author Jason J.
 * @version 0.4.3-20141119
 */
public class CareerItemFragment extends Fragment implements OnClickListener {
	/** Class name for debugging purposes. */
	final static private String CLASS_NAME = CareerItemFragment.class
			.getSimpleName();
	/** Logtag for debugging. */
	final static private String LOGTAG = CLASS_NAME;
	/** Whether or not debugging. */
	final static private boolean DEBUG = false;
	
	
	/** Bundle Key. The career item for this fragment. Parcelable/CareerItem. */
	final static private String KEY_CAREER_ITEM =
			CLASS_NAME +".KEY_CAREER_ITEM";
	/** Bundle Key. The scroll position as a ratio of 
	 * <code>pos/height</code>. Double. */
	final static private String KEY_SCROLL_POSITION_RATIO =
			CLASS_NAME +".KEY_SCROLL_POSITION_RATIO";
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End keys
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The output date format string to use. */
	/* We could use DateUtils like in CareerItemFilterListAdapter, but I favour
	 * this format above all.	 */
	final static private String DATE_FORMAT = "yyyy-MM-dd HH:mm";
	
	/** The dark theme css to use with {@link #HTML_WRAPPER}. */
	final static private String CSS_DARK = "careeritem_darktheme";
	/** The dark theme css to use with {@link #HTML_WRAPPER}. */
	final static private String CSS_LIGHT = "careeritem_lighttheme";
	
	/** The wrapper for the content description html. Takes two strings:
	 * <ul><ol>The css file name (Either #CSS_DARK or #CSS_LIGHT)</ol>
	 * <ol>The html to render</ol></li>
	 */
	final static private String HTML_WRAPPER = "<!DOCTYPE html><html><head>"+
			"<link rel='stylesheet' href='./careeritem_styling.css' type='text/css' media='screen'>" +
			"<link rel='stylesheet' href='./%s.css' type='text/css' media='screen'>" +
			"<!--This line refer the css file--></head>" +
			"<body> %s </body></html>";
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Start share constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The character limit of twitter. */
	final static private int TWITTER_LIMIT = 140;
	/** The character limit urls. */
	final static private int TWITTER_URL_LIMIT = 22;
	
	/** The prepared twitter string; takes 2 strings - 1)job, 2) url. */
	final static private String TWITTER_STUB = 
			"%s (via Stack Overflow Careers & CareerStack app) %s @StackCareers";
	/** Based on values in {@link #TWITTER_LIMIT} & {@link #TWITTER_STUB}: 
	 * Remaining length for company name. */
	final static private int TWITTER_COMPANY_LENGTH = 
			(int) ((float) (TWITTER_LIMIT - TWITTER_STUB.length() 
					- TWITTER_URL_LIMIT) * 0.3);	
	/** Based on values in {@link #TWITTER_LIMIT} & {@link #TWITTER_STUB}:
	 * Remaining length for job title. */
	final static private int TWITTER_TITLE_LENGTH = 
			(int) ((float) (TWITTER_LIMIT - TWITTER_STUB.length() 
					- TWITTER_URL_LIMIT) * 0.6);
	
	/** The prepared generic string; takes 2 strings - 1)job title + company, 
	 * 2) url, 3) description. */
	final static private String GENERIC_MSG_STUB = 
			"%s \n%s - (via Stack Overflow Careers & CareerStack app) \n\n%s";
	/** Character from description to include. */
	final static private int GENERIC_MSG_DESCRIP_LENGTH = 80;
	
	/** Short ellipses to use. */
	final static private String SHORT_ELLIPSIS = "..";
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** The webview reference for proper destruction. */
	private WebView wv_jobDescription = null; 
	
	/** The local instance of career item to display. */
	private CareerItem careerItem = null;
	/** The simple toast manager. */
	private ToastManager toastManager = null;
	/** The parent scroll view. */
	private ScrollView sv_scrollView = null;
	
	/** Bool for backstack safety of whether view has been built. */
	private boolean viewBuilt = false;
	
	/** Bool for when scroll view is pending. Set <code>true</code> in {@link #webViewListener}*/
	private boolean scrollViewPending = false;
	
	/** The current scroll ratio for how far down the scroll. */
	/* A ratio is used since the height WILL almost always change between 
	 * orientation shifts. As such, scrolling to a pixel will be inaccurate 
	 * between rotations, quickly creeping upwards. 
	 * We use double to reduce rounding-creep. 	*/
	private double scrollRatio = 0.0d;
	
	
	
	/**
	 * Use this factory method create a new Career fragment.
	 * 
	 * @param careerItem The item to pass directly on.
	 * @return A new instance of fragment CareerItemFragment.
	 */
	public static CareerItemFragment newInstance(CareerItem careerItem) {
		CareerItemFragment fragment = new CareerItemFragment();
		Bundle args = new Bundle();
		args.putParcelable(KEY_CAREER_ITEM, careerItem);
		fragment.setArguments(args);
		return fragment;
	}

	public CareerItemFragment() {
		// Required empty public constructor
	}
	
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(KEY_CAREER_ITEM, careerItem);
		if (viewBuilt){
			double scrollRatio = 
					/* Note that the webview height is added. 
					 * The reason for this is that the scroll view will ignore the
					 * WebView height as its contents (thus height) 
					 * is not present at loading.
					 */
					(double) sv_scrollView.getScrollY()/
					(double) (sv_scrollView.getHeight() + wv_jobDescription.getHeight());
			
			if (DEBUG){
				Log.d(LOGTAG, "Scroll ratio (" + sv_scrollView.getScrollY() 
						+ "/"+sv_scrollView.getChildAt(0).getHeight()+") ");
				Log.d(LOGTAG, "Scroll ratio stored: " + scrollRatio);
			}
			outState.putDouble(KEY_SCROLL_POSITION_RATIO, scrollRatio);
		} else {
			//use the preset value if no view is available
			outState.putDouble(KEY_SCROLL_POSITION_RATIO, scrollRatio);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			careerItem = getArguments().getParcelable(KEY_CAREER_ITEM);
		} else if (savedInstanceState != null){
			careerItem = savedInstanceState.getParcelable(KEY_CAREER_ITEM);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_career_item, 
				container, false);
		toastManager = new ToastManager(getActivity());
		initOutputViews(rootView);
		initButtons(rootView);
		initScrollView(rootView, savedInstanceState);
		viewBuilt = true;
		return rootView;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (wv_jobDescription != null){
			wv_jobDescription.setWebViewClient(null);
			wv_jobDescription.post(new Runnable() {@Override
				public void run() {
					/* destroying is required or we get a:
					 * java.lang.IllegalArgumentException:  bitmap exceeds 32 bits exception */
					wv_jobDescription.destroy(); 
				}
			});
		}
		viewBuilt = false;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Initialization & helpers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Initializes the scroll view. This must be done last. */
	private void initScrollView(View rootView, Bundle saveState){
		sv_scrollView = (ScrollView)
				rootView.findViewById(R.id.careerstack_careerItem_scrollView);
		if (saveState == null){
			return;
		}
		scrollRatio = saveState.getDouble(KEY_SCROLL_POSITION_RATIO);
		if (DEBUG){
			Log.d(LOGTAG, "Scroll ratio retrieved: "+scrollRatio);
		}
		
		if (scrollViewPending){
			resetScrollView();
		}
	}
	
	/** Initializes the output views. */
	private void initOutputViews(View rootView){
		TextView jobTitle = (TextView)
				rootView.findViewById(R.id.careerstack_careerItem_text_jobTitle);
		jobTitle.setText(careerItem.getTitle());
				
		TextView companyEtc = (TextView)
				rootView.findViewById(R.id.careerstack_careerItem_text_companyLocationEtc);
		companyEtc.setText(careerItem.getCompanyLocationEtc());	
		
		initJobDescription(rootView);
		
		TextView updateDate = (TextView)
				rootView.findViewById(R.id.careerstack_careerItem_text_updateDate);
		updateDate.setText(
				String.format(getString(R.string.careerstack_formatString_publishUpdateDate),
						processDate(careerItem.getPublishDate()),
						processDate(careerItem.getUpdateDate()))
				);
	}

	/** Sets up the job description, including styling. */
	@SuppressLint({ "InlinedApi", "NewApi" })
	private void initJobDescription(View rootView) {
		wv_jobDescription = (WebView)
				rootView.findViewById(R.id.careerstack_careerItem_webview_jobDescription);
		wv_jobDescription.setScrollContainer(false);
		
		int colour = getBackgroundColour();		
		wv_jobDescription.setBackgroundColor(colour);
		
		WebSettings webSettings = wv_jobDescription.getSettings();
		//webSettings.setUseWideViewPort(true);
		webSettings.setDefaultFontSize(14); //TODO abstract
		webSettings.setSupportZoom(false);
		
		
		/* Tried: wv_jobDescription.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		 * but to no avail
		 */
			
		
		loadJobDescription();		

		wv_jobDescription.setContentDescription(
				//strip html for accessibility 
				Html.fromHtml(careerItem.getDescription()).toString()
				);
		wv_jobDescription.setWebViewClient(webViewListener);
	}

	

	
	
	
	/** Initializes the buttons. */
	private void initButtons(View rootView){
		Button share = (Button)
				rootView.findViewById(R.id.careerstack_careerItem_button_share);
		share.setOnClickListener(this);
		
		Button openLink = (Button)
				rootView.findViewById(R.id.careerstack_careerItem_button_openLink);
		openLink.setOnClickListener(this);
		
		Button copyUrl = (Button)
				rootView.findViewById(R.id.careerstack_careerItem_button_copyUrl);
		copyUrl.setOnClickListener(this);  
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Helper/Utility section
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Loads the job description into the WebView, if not null. */
	private void loadJobDescription() {
		if (wv_jobDescription != null){
			String htmlData = wrapDescription(careerItem.getDescription());
			wv_jobDescription.loadDataWithBaseURL( "file:///android_asset/", 
					htmlData, "text/html", "UTF-8", null);
		}
	}
	
	
	/** Resets scroll view to scrollRatio according to its height, if not <code>null</code>. */
	private void resetScrollView(){
		if (sv_scrollView != null){
			sv_scrollView.post(new Runnable() {@Override
				public void run() {
					double height = 
							sv_scrollView.getHeight() + wv_jobDescription.getHeight();
					if (sv_scrollView.getScrollY() > 0 ){
						//perhaps we have scrolled already?
						return;
					}
					sv_scrollView.scrollBy(0, (int) (height * scrollRatio));
					scrollViewPending = false;
				}
			});
		}
	}
	
	/** @returns The background colour based on theme via careerstack_background_color item. */
	private int getBackgroundColour() {
		TypedValue typedValue = new TypedValue();
		Theme theme = getActivity().getTheme();
		theme.resolveAttribute(R.attr.careerstack_background_color, typedValue, true);
		int color = typedValue.data;
		return color;
	}
	
	/** Uses the preference to determine whether to apply the dark or light theme. 
	 * @param description The description to wrap and theme. */
	private String wrapDescription(String description){
		SharedPreferences prefs = PreferenceUtils.getPreferences(getActivity());
		final String theme = prefs.getString(
				getString(R.string.careerstack_pref_KEY_THEME_PREF), 
				"");
		String cssTheme = "";
		if (theme.equals(getString(R.string.careerstack_pref_VALUE_THEME_LIGHT))){
			//light theme
			cssTheme = CSS_LIGHT; 
					
		} else {
			//implied is dark theme or:
			//if (theme.equals(getString(R.string.careerstack_pref_VALUE_THEME_DARK)))
			cssTheme = CSS_DARK;
		}
		
		return String.format(HTML_WRAPPER, cssTheme, description);
		
	}
	
	/** Launches internal url. */
	private void launchUrl(){
		String url = careerItem.getUrl();
		try {
			Intent browserIntent = 
					new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(browserIntent);
		} catch(Exception e){
			Log.e(LOGTAG, "Unexpected error: "+ e);
		}
	}
	
	/** Copies url. */
	private void copyUrl(){
		String url = careerItem.getUrl();
		CompatClipboard.copyToClipboard(getActivity(), 
				getString(android.R.string.copyUrl), 
				url);
		toastManager.toastShort(getString(R.string.careerstack_toast_copiedLink));
	}
	
	/** Shares the post. */
	private void shareIntent(){
		String shareTitle = getString(R.string.careerstack_careerItem_button_shareJob);
		String jobTitle = careerItem.getTitle();
		String company = 
				Html.fromHtml(careerItem.getCompanyLocationEtc()).toString();
		String url = careerItem.getUrl();
		
		//create generic string
		String description = 
				careerItem.getDescription().substring(0, GENERIC_MSG_DESCRIP_LENGTH)
				+ SHORT_ELLIPSIS;
		String genericMsg = 
				String.format(GENERIC_MSG_STUB, 
						getString(R.string.careerstack_formatString_jobAtPlace, jobTitle, 
								company), 
						url, description);
		
		//create twitter string
		String twitterMsg = ""; 
		if (jobTitle.length() >= TWITTER_TITLE_LENGTH){
			jobTitle = jobTitle.substring(0, TWITTER_TITLE_LENGTH - 1) + 
					SHORT_ELLIPSIS;
		}
		
		if (company.length() >= TWITTER_COMPANY_LENGTH){
			company = company.substring(0, TWITTER_COMPANY_LENGTH - 1)
					+SHORT_ELLIPSIS;
		}
		twitterMsg = String.format(TWITTER_STUB, 
				getString(R.string.careerstack_formatString_jobAtPlace, jobTitle, company),
				url);		
		
		//prepare launch intent
		ShareIntentUtil share = 
				new ShareIntentUtil(shareTitle, url, genericMsg, twitterMsg);
		if (DEBUG){
			Log.d(LOGTAG, "Share: " + share.toString());
		}
		share.launchShare(getActivity()); //launch
	}
	
	/** Processes the date into a semi-readable string of 
	 * {@link #DATE_FORMAT}.
	 * @param date The date to process
	 * @return The readable date.	 */
	static private String processDate(Date date){
		SimpleDateFormat dateFormat = 
				new SimpleDateFormat(DATE_FORMAT, Locale.US);
		dateFormat.setTimeZone(TimeZone.getDefault());
		return dateFormat.format(date);
	}

	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Implemented listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The web view client to listen to when the view has been loaded. */
	private WebViewClient webViewListener = new WebViewClient(){
		/** The number of load attempts. */
		protected int attempts = 0;
		
		/** Number of retries before giving up on loading the job description. */ 
		final static private int RETRY_LIMIT = 5;
		
		@Override
		public void onPageFinished(final WebView webView, String url) {
			webView.post(new Runnable() {				
				@Override
				public void run() {
					if (DEBUG){
						Log.d(LOGTAG, "webview height: " + webView.getHeight());
						if (sv_scrollView != null){
							Log.d(LOGTAG, "full height: " + 
								sv_scrollView.getChildAt(0).getHeight());
						}
					}
					//TODO add progress bar + hide here
					
					/* Occasionally, the webview won't load. This may be caused
					 * by the wrap_content layout. A "fix" is to force reload. */
					if (webView.getHeight() <= 0){
						//we have failed to load, retry
						if (attempts++ < RETRY_LIMIT){
							//webView.loadUrl("about:blank"); //clear first
							loadJobDescription();
							
							//make webview invisible for animation purposes
							webView.setVisibility(View.INVISIBLE);
						}
						return;
					} else {
						//if invisible, we must want to fade it in.
						if (webView.getVisibility() == View.INVISIBLE){
							webView.setAnimation(
									AnimationUtils.loadAnimation(
											webView.getContext(), 
											R.anim.quick_fade_in)
									);
							webView.setVisibility(View.VISIBLE);
						}
						
						//we have finished loading, reset scroll
						scrollViewPending = true;
						resetScrollView();
					}
					
				}
			});
		};
	};
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.careerstack_careerItem_button_openLink:
			launchUrl();
			break;
		case R.id.careerstack_careerItem_button_copyUrl:
			copyUrl();
			break;			
		case R.id.careerstack_careerItem_button_share:
			shareIntent();
			break;

		default:
			//fall through
		}
	}

}
