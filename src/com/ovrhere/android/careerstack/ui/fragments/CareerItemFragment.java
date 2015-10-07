package com.ovrhere.android.careerstack.ui.fragments;

import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ovrhere.android.careerstack.R;
import com.ovrhere.android.careerstack.dao.CareerItem;
import com.ovrhere.android.careerstack.prefs.PreferenceUtils;
import com.ovrhere.android.careerstack.ui.widgets.floatingactionbutton.FloatingActionsMenu;
import com.ovrhere.android.careerstack.utils.CompatClipboard;
import com.ovrhere.android.careerstack.utils.ShareIntentUtil;
import com.ovrhere.android.careerstack.utils.ToastManager;
import com.ovrhere.android.careerstack.utils.Utility;

/**
 * The listing of a job item. Provides ability to open, copy or share link
 * or launch a search from tags (via {@link OnFragmentInteractionListener})
 * 
 * @author Jason J.
 * @version 0.8.1-20151007
 */
public class CareerItemFragment extends Fragment implements 
	OnClickListener, OnCheckedChangeListener, 
	SharedPreferences.OnSharedPreferenceChangeListener {
	
	/** Class name for debugging purposes. */
	private static final String CLASS_NAME = CareerItemFragment.class
			.getSimpleName();
	/** Logtag for debugging. */
	private static final String LOGTAG = CLASS_NAME;
	/** Whether or not debugging. */
	private static final boolean DEBUG = false;
	
	
	/** Bundle Key. The career item for this fragment. Parcellable/CareerItem. */
	private static final String KEY_CAREER_ITEM =
			CLASS_NAME +".KEY_CAREER_ITEM";
	/** Bundle Key. The scroll position as a ratio of 
	 * <code>pos/height</code>. Double. */
	private static final String KEY_SCROLL_POSITION_RATIO =
			CLASS_NAME +".KEY_SCROLL_POSITION_RATIO";
	
		
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End keys
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Start HTML_WRAPPERS
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The dark theme css to use with {@link #HTML_WRAPPER_DESCRIPTION}. */
	private static final String CSS_DARK = "careeritem_darktheme";
	/** The dark theme css to use with {@link #HTML_WRAPPER_DESCRIPTION}. */
	private static final String CSS_LIGHT = "careeritem_lighttheme";
	
	/** The light theme css to use with {@link #HTML_WRAPPER_TAGS}. */
	private static final String HTML_TAGS_LIGHT_THEME = 
			"<link rel='stylesheet' href='./careeritem_tags_lighttheme.css' "
			+ "type='text/css' media='screen'>";
	
	/** The wrapper for the content description html. Takes two strings:
	 * <ol><li>The css file name (Either #CSS_DARK or #CSS_LIGHT)</li>
	 * <li>The html to render</li></ol>
	 */
	private static final String HTML_WRAPPER_DESCRIPTION = "<!DOCTYPE html><html><head>"+
			"<link rel='stylesheet' href='./careeritem_styling.css' type='text/css' media='screen'>" +
			"<link rel='stylesheet' href='./%s.css' type='text/css' media='screen'>" +
			"<!--Previous lines refer to css files--></head>" +
			"<body> %s </body></html>";
	

	/** The link/href to use in link work around. */
	private static final String HREF_TAGS_ID = 
			//must be lowercase for urls, uppercase for readability
			"AndroidCareerItem:tag=".toLowerCase(Locale.US); 

	/** The wrapper for the tags html. Takes one strings:
	 * <ol><li>The list of #HTML_TAG_BUTTON s </li>
	 * <li>The light css html or blank</li></ol>	 */
	private static final String HTML_WRAPPER_TAGS = "<!DOCTYPE html><html><head>"+
			"<link rel='stylesheet' href='./careeritem_tags.css' type='text/css' media='screen'>" +
			"%2$s"+
			"<!--Previous lines refer to css files--></head>" +
			//ontouchstart required for the :active styling to work
			"<body ontouchstart='' > %1$s </body></html>";

	
	/** The HTML for the tag buttons. Takes one string (the name of the button). 
	 *   */
	private static final String HTML_TAG_BUTTON = 
			"<a class='job-tag' " +
			//dirt simple, href redirect
			" href=\""+HREF_TAGS_ID+"%1$s\"" +
			//force redraw
			" ontouchstart=\"this.className = 'job-tag'; \" " +
			//ontouchmove, prevents button "sliding" styling issues
			" ontouchmove=\"this.className = 'job-tag-inactive';\" " +
			">%1$s</a>";
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Start share constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The character limit of twitter. */
	private static final int TWITTER_LIMIT = 140;
	/** The character limit urls. */
	private static final int TWITTER_URL_LIMIT = 22;
	
	/** The prepared twitter string; takes 2 strings - 1)job, 2) url. */
	private static final String TWITTER_STUB = 
			"%s (via Stack Overflow Careers & CareerStack app) %s @StackCareers";
	/** Based on values in {@link #TWITTER_LIMIT} & {@link #TWITTER_STUB}: 
	 * Remaining length for company name. */
	private static final int TWITTER_COMPANY_LENGTH = 
			(int) ((float) (TWITTER_LIMIT - TWITTER_STUB.length() 
					- TWITTER_URL_LIMIT) * 0.3);	
	/** Based on values in {@link #TWITTER_LIMIT} & {@link #TWITTER_STUB}:
	 * Remaining length for job title. */
	private static final int TWITTER_TITLE_LENGTH = 
			(int) ((float) (TWITTER_LIMIT - TWITTER_STUB.length() 
					- TWITTER_URL_LIMIT) * 0.6);
	
	/** The prepared generic string; takes 2 strings - 1)job title + company, 
	 * 2) url, 3) description. */
	private static final String GENERIC_MSG_STUB = 
			"%s \n%s - (via Stack Overflow Careers & CareerStack app) \n\n%s";
	/** Character from description to include. */
	private static final int GENERIC_MSG_DESCRIP_LENGTH = 80;
	
	/** Short ellipses to use. */
	private static final String SHORT_ELLIPSIS = "..";
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The preference handle. */
	private SharedPreferences mPrefs = null;
	
	/** The fragment interaction listener, namely main. */
	private OnFragmentInteractionListener mFragInteractionListener = null;
	
	/** The simple toast manager. */
	private ToastManager mToastManager = null;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Start views 
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The job description webview reference for animation + proper destruction. */
	private WebView mWv_jobDescription = null;
	
	/** The tags webview reference for animation + proper destruction. */
	private WebView mWv_tags= null;
		
	/** The parent scroll view. */
	private ScrollView mSv_scrollView = null;
	
	/** The reference for toggling tags. */
	private CompoundButton mChk_toggleTags = null;
	
	/** Used to load the open, copy, share options. */
	private FloatingActionsMenu mFloatingActionButtonMenu = null;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End views
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The local instance of career item to display. */
	private CareerItem mCareerItem = null;
	
	/** Whether or not to show the "show tags" button. 
	 * Should only be true if: 1. The resolution supports it, 2. There are tags */
	private boolean mShowTagButtonVisible  = false;
	
	/** Bool for backstack safety of whether view has been built. */
	private boolean mViewBuilt = false;
	
	/** Bool for when scroll view is pending. 
	 * Set <code>true</code> in {@link #jobDescriptionWebViewListener}*/
	private boolean mScrollViewPending1 = false;	
	/** Bool for when scroll view is pending. 
	 * Set <code>true</code> in {@link #tagsWebViewListener}*/
	private boolean mScrollViewPending2 = false;
	
	/** The current scroll ratio for how far down the scroll. */
	/* A ratio is used since the height WILL almost always change between 
	 * orientation shifts. As such, scrolling to a pixel will be inaccurate 
	 * between rotations, quickly creeping upwards. 
	 * We use double to reduce rounding-creep. 	*/
	private double mScrollRatio = 0.0d;
	
	// TODO re-do old code to be cleaner and more functional.
	
	
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
		outState.putParcelable(KEY_CAREER_ITEM, mCareerItem);
		if (mViewBuilt){
			double scrollRatio = 
					/* Note that the webview height is added. 
					 * The reason for this is that the scroll view will ignore the
					 * WebView height as its contents (thus height) 
					 * is not present at loading.
					 */
					(double) mSv_scrollView.getScrollY()/
					(double) (mSv_scrollView.getHeight() + mWv_jobDescription.getHeight());
			
			if (DEBUG){
				Log.d(LOGTAG, "Scroll ratio (" + mSv_scrollView.getScrollY() 
						+ "/"+mSv_scrollView.getChildAt(0).getHeight()+") ");
				Log.d(LOGTAG, "Scroll ratio stored: " + scrollRatio);
			}
			outState.putDouble(KEY_SCROLL_POSITION_RATIO, scrollRatio);
		} else {
			//use the preset value if no view is available
			outState.putDouble(KEY_SCROLL_POSITION_RATIO, mScrollRatio);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mCareerItem = getArguments().getParcelable(KEY_CAREER_ITEM);
		} else if (savedInstanceState != null){
			mCareerItem = savedInstanceState.getParcelable(KEY_CAREER_ITEM);
		}
		
		mPrefs = PreferenceUtils.getPreferences(getActivity());
		
		if (mCareerItem.getCategories() != null ){
			/* If the resolution supports it AND there are actually tags to show. */ 
			mShowTagButtonVisible = mCareerItem.getCategories().length > 0 &&
				getResources().getBoolean(R.bool.careerstack_show_tags_toggle);
		}	
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_career_item, 
				container, false);
		
		mPrefs.registerOnSharedPreferenceChangeListener(this);
		mToastManager = new ToastManager(getActivity());
		
		initOutputViews(rootView);
		initButtons(rootView);
		initScrollView(rootView, savedInstanceState);
		
		mViewBuilt = true;
		return rootView;
	}
	
	@Override
	public void onPause() {
		super.onPause();
		collapseFloatingActionButton();
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mPrefs.unregisterOnSharedPreferenceChangeListener(this);
		
		destroyWebView(mWv_jobDescription);
		destroyWebView(mWv_tags);
		collapseFloatingActionButton();
		mViewBuilt = false;
	}

	

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			this.mFragInteractionListener = 
					(OnFragmentInteractionListener) activity;
		} catch (ClassCastException e){
			Log.e(LOGTAG, "Activity must implement :" +
					OnFragmentInteractionListener.class.getSimpleName());
			throw e;
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Initialization & helpers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Initializes the scroll view. This must be done last. */
	private void initScrollView(View rootView, Bundle saveState){
		mSv_scrollView = (ScrollView)
				rootView.findViewById(R.id.careerstack_careerItem_scrollView);
		if (saveState == null){
			return;
		}
		mScrollRatio = saveState.getDouble(KEY_SCROLL_POSITION_RATIO);
		if (DEBUG){
			Log.d(LOGTAG, "Scroll ratio retrieved: "+mScrollRatio);
		}
		
		if (mScrollViewPending1){
			resetScrollView();
		}
	}
	
	/** Initializes the output views. */
	private void initOutputViews(View rootView){
		findAndSetText(rootView, 
				R.id.careerstack_careerItem_text_jobTitle, mCareerItem.getTitle());
		
		findAndSetText(rootView, 
				R.id.careerstack_careerItem_text_companyLocationEtc, mCareerItem.getCompanyLocationEtc());
		
		initTags(rootView);
		initJobDescription(rootView);
		
		findAndSetText(rootView, 
				R.id.careerstack_careerItem_text_publishDate,
				String.format(getString(R.string.careerstack_formatString_publishDate),
						Utility.getRelativeTime(getActivity(), mCareerItem.getPublishDate())
				));
		findAndSetText(rootView, 
				R.id.careerstack_careerItem_text_updateDate,
				String.format(getString(R.string.careerstack_formatString_updateDate),
						Utility.getPreciseDate(mCareerItem.getUpdateDate())
				));
	}
	

	/* 
	 * Point of reference:
	 * There are multiple approaches one could have taken when implementing the tags:
	 * Firstly, in order to achieve horizontal wrapping (for 0 - n tags) 
	 * one could have used layouts and programmatically added sublayouts,
	 * organized a relative layout for the buttons, or used a webview. The latter
	 * was done.
	 * 
	 * When executing the button clicks it could be either be done in:
	 * - WebViewClient.shouldOverrideUrlLoading(WebView, String) 
	 * - JavaScript
	 * 
	 * Originally, I had planned to use JavaScript, however there are some issues
	 * in 2.3.3 namely:
	 * - https://code.google.com/p/android/issues/detail?id=12987
	 * 
	 * This issue is more common in emulators but in theory CAN happen on certain
	 * devices:
	 * - https://code.google.com/p/android/issues/detail?id=12987#c117. 
	 * 
	 * So, for safety, I'd like to address it. In order to this one can either create: 
	 * - JavaScript-Bridge like:
	 *  + http://www.jasonshah.com/handling-android-2-3-webviews-broken-addjavascriptinterface/
	 *  + https://github.com/twig/twigstechtips-snippets/blob/master/GingerbreadJSFixExample.java
	 * - The previous method (WebViewClient)
	 *  
	 * For this project I am going to opt for the simpler previous 
	 * method of the WebViewClient. It's simple and requires less code.
	 * 
	 * JavaScript remains enabled for the purposes of styling.
	 * 
	 * This is part of the reason why a separate WebView is used; we have little
	 * control over the job description content. Safety dictates isolating 
	 * execution in unsure states.
	 */
	
	/** Sets up the job description, including styling. */
	@SuppressLint("SetJavaScriptEnabled")
	private void initTags(View rootView) {
		mWv_tags = (WebView)
				rootView.findViewById(R.id.careerstack_careerItem_webview_tags);
		mWv_tags.setScrollContainer(false);
		
		int colour = getBackgroundColour();		
		mWv_tags.setBackgroundColor(colour);
		
		WebSettings webSettings = mWv_tags.getSettings();
		
		webSettings.setDefaultFontSize(12); //TODO abstract
		webSettings.setSupportZoom(false);
		
		//enabled for the sake of styling
		webSettings.setJavaScriptEnabled(true);
		
		/* Hack to disable copying. */
		mWv_tags.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
		
		/* ----- Initial setup complete. ----- */
		
		/* we either show/hide it based on preference;
		 * subsequently, this will control if it fades in or just loads quietly in background. */
		checkTagVisiblity();
		
		mWv_tags.setWebViewClient(tagsWebViewListener);
		mWv_tags.post(new Runnable() {	@Override
			public void run() {
				//increase likelihood of first time load.
				buildAndLoadTags();
			}
		
		});		

		//TODO accessibility
		/* wv_tags.setContentDescription(
				//strip html for accessibility 
				Html.fromHtml(careerItem.getDescription()).toString()
				); */
		
	}
	
	/** Sets up the job description, including styling. */
	private void initJobDescription(View rootView) {
		mWv_jobDescription = (WebView)
				rootView.findViewById(R.id.careerstack_careerItem_webview_jobDescription);
		mWv_jobDescription.setScrollContainer(false);
		
		int colour = getBackgroundColour();		
		mWv_jobDescription.setBackgroundColor(colour);
		
		WebSettings webSettings = mWv_jobDescription.getSettings();
		//webSettings.setUseWideViewPort(true);
		webSettings.setDefaultFontSize(14); //TODO abstract
		webSettings.setSupportZoom(false);
		
		/* ----- Initial setup complete. ----- */
		
		long delay = 1;
		if (mShowTagButtonVisible && 
			mPrefs.getBoolean(getString(R.string.careerstack_pref_KEY_SHOW_ITEM_TAGS), false)){
			//if the tags are being shown, delay job description
			delay = getResources().getInteger(R.integer.careerstack_jobdescription_delay);
		}
		
		//always fade in
		mWv_jobDescription.setVisibility(View.INVISIBLE);
		
		mWv_jobDescription.setWebViewClient(jobDescriptionWebViewListener);
		mWv_jobDescription.postDelayed((new Runnable() {	@Override
			public void run() {
				//increase likelihood of first time load.
				loadJobDescription();
			}
		}), delay);		

		mWv_jobDescription.setContentDescription(
				//strip html for accessibility 
				Html.fromHtml(mCareerItem.getDescription()).toString()
				);
		
	}

	

	
	/** Initializes the buttons. */
	private void initButtons(View rootView){
		mFloatingActionButtonMenu = 
				(FloatingActionsMenu) rootView.findViewById(R.id.careerstack_careerItem_button_link_actions);
		
		rootView.findViewById(R.id.careerstack_careerItem_button_share)
			.setOnClickListener(this);
		rootView.findViewById(R.id.careerstack_careerItem_button_openLink)
				.setOnClickListener(this);
		rootView.findViewById(R.id.careerstack_careerItem_button_copyUrl)
			.setOnClickListener(this);  
		
		mChk_toggleTags = (CompoundButton)
				rootView.findViewById(R.id.careerstack_careerItem_check_showTags);
		mChk_toggleTags.setVisibility(mShowTagButtonVisible ? View.VISIBLE : View.GONE);
		mChk_toggleTags.setOnCheckedChangeListener(this);
		
		setToggleTagsCheck();
	}

	/** Sets the toggle tag check according to preference. */
	private void setToggleTagsCheck() {
		if (mPrefs == null || mChk_toggleTags == null){
			return; //give up if null
		}
		boolean checked = mPrefs.getBoolean(getString(R.string.careerstack_pref_KEY_SHOW_ITEM_TAGS), false);
		mChk_toggleTags.setChecked(checked);
		onCheckedChanged(mChk_toggleTags, checked);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Helper/Utility section
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Finds and textview content. */
	private static TextView findAndSetText(View rootView, int id, String string) {
		TextView text = (TextView) rootView.findViewById(id);
		text.setText(string);
		return text;
	}
	
	/** Safely collapses the floating action button. */
	private void collapseFloatingActionButton() {
		if (mFloatingActionButtonMenu != null && mFloatingActionButtonMenu.isExpanded()) {
			mFloatingActionButtonMenu.toggle();
		}
	}

	/** Destroys the webview for safety reasons 
	 * ( java.lang.IllegalArgumentException:  bitmap exceeds 32 bits exception ) */
	private void destroyWebView(final WebView view) {
		if (view != null){
			view.setWebViewClient(null);
			view.post(new Runnable() {
				@Override
				public void run() {
					/* destroying is required or we get a:
					 * java.lang.IllegalArgumentException:  bitmap exceeds 32 bits exception */
				view.destroy(); 
				}
			});
		}
	}
	
	/** Checks the preference for tag visibility, and 
	 * sets visibility accordingly. */ 
	private void checkTagVisiblity(){
		final boolean isVisible = mPrefs.getBoolean(getString(R.string.careerstack_pref_KEY_SHOW_ITEM_TAGS), false);
		if (mWv_tags != null ){ //safety			
			mWv_tags.setVisibility(isVisible ? View.VISIBLE : View.GONE);
		}		
	}
	
	/** Builds tags from the careeritem and loads them into the webview. */ 
	private void buildAndLoadTags() {
		//extra safety
		try {
			/* ensure the view is valid and our fragment is still attached (for async calls) */
			if (mWv_tags != null && !isDetached()){
				String buttons = "";
				String cats[] = mCareerItem.getCategories();
				final int SIZE = cats.length;
				for (int index = 0; index < SIZE; index++){
					buttons += String.format(HTML_TAG_BUTTON, cats[index]);
				}
				String extraCss = isLightTheme() ? HTML_TAGS_LIGHT_THEME : "";
				String htmlData = String.format(HTML_WRAPPER_TAGS, buttons, extraCss);
				mWv_tags.loadDataWithBaseURL( "file:///android_asset/", 
				htmlData, "text/html", "UTF-8", null);
			}
		} catch (Exception e){
			Log.e(LOGTAG, "Unusual behaviour during tag build: " + e);
		}
	}
	
	/** Loads the job description into the WebView, if not null. */
	private void loadJobDescription() {
		//extra safety
		try {
			/* ensure the view is valid and our fragment is still attached (for async calls) */
			if (mWv_jobDescription != null && !isDetached()){
				String htmlData = wrapDescription(mCareerItem.getDescription());
				mWv_jobDescription.loadDataWithBaseURL( "file:///android_asset/", 
						htmlData, "text/html", "UTF-8", null);
			}
		} catch (Exception e){
			Log.e(LOGTAG, "Unusual behaviour during job description: " + e);
		}
	}
	
	
	/** Resets scroll view to scrollRatio according to its height, 
	 * if not <code>null</code> and scroll view is pending. */
	private void resetScrollView(){
		if (mSv_scrollView != null && mScrollViewPending1 && mScrollViewPending2){
			mSv_scrollView.post(new Runnable() {@Override
				public void run() {
					double height = 
							mSv_scrollView.getHeight() + mWv_jobDescription.getHeight();
					if (mSv_scrollView.getScrollY() > 0 ){
						//perhaps we have scrolled already?
						return;
					}
					mSv_scrollView.scrollBy(0, (int) (height * mScrollRatio));
					mScrollViewPending1 = false;
					mScrollViewPending2 = false;
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
		String cssTheme = isLightTheme() ? CSS_LIGHT : CSS_DARK;
		return String.format(HTML_WRAPPER_DESCRIPTION, cssTheme, description);		
	}
	
	/** @return <code>true</code> if it is light theme, <code>false</code> for dark. */
	private boolean isLightTheme(){		
		final String theme = mPrefs.getString(
				getString(R.string.careerstack_pref_KEY_THEME_PREF), 
				"");
		return theme.equals(getString(R.string.careerstack_pref_VALUE_THEME_LIGHT));
	}
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Start Intent methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Launches internal url. */
	private void launchUrl(){
		String url = mCareerItem.getUrl();
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
		String url = mCareerItem.getUrl();
		CompatClipboard.copyToClipboard(getActivity(), 
				getString(android.R.string.copyUrl), 
				url);
		mToastManager.toastShort(getString(R.string.careerstack_toast_copiedLink));
	}
	
	/** Shares the post. */
	private void shareIntent(){
		String shareTitle = getString(R.string.careerstack_careerItem_button_shareJob);
		String jobTitle = mCareerItem.getTitle();
		String company = 
				Html.fromHtml(mCareerItem.getCompanyLocationEtc()).toString();
		String url = mCareerItem.getUrl();
		
		//create generic string
		String description = 
				mCareerItem.getDescription().substring(0, GENERIC_MSG_DESCRIP_LENGTH)
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
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Internal classes
	////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * The JavaScript interface of the {@link #mWv_tags}
	 * @author Jason J.
	 * @version 0.1.1-20141124
	 */
	@Deprecated
	static private class JobTagsJSInterface {
		private CareerItemFragment careerItemFragment = null;
		public JobTagsJSInterface(CareerItemFragment careerItemFragment) {
			this.careerItemFragment = careerItemFragment;
		}
		
		/** The interface action for when a tag is clicked
		 * Name must match {@link #HTML_TAG_BUTTON}
		 * @param tag The tag that was clicked
		 */
		@JavascriptInterface
		public void onTagClick(final String tag){
			//send the tag to the main activity
			careerItemFragment.mWv_tags.post(new Runnable() {
				@Override
				public void run() {
					try {
						careerItemFragment.mFragInteractionListener.onTagClick(tag);
					} catch (Exception e){
						Log.e(LOGTAG, "Unusual behaviour: " + e);
					}
				}
			});
		}		
	}
	
	/** A simple class to reduce repetition. 
	 * @version 0.1.0-20141121 */
	private static class AnimatedRetryWebViewClient extends WebViewClient {
		/** The number of load attempts. */
		protected int attempts = 0;
		
		/** Number of retries before giving up on loading the job description. */ 
		private static final int RETRY_LIMIT = 5;
		
		private RetryListener retryListener = null;
		private CareerItemFragment careerItemFragment = null;
		
		/** Animates the WebView if the webview starts off visible, otherwise 
		 * it just quietly reloads it in background. */
		public AnimatedRetryWebViewClient(CareerItemFragment careerItemFragment, 
				RetryListener retryListener) {
			super();
			this.careerItemFragment = careerItemFragment;
			this.retryListener = retryListener;
		}
		
		@Override
		public void onPageFinished(final WebView webView, String url) {
			webView.post(new Runnable() {				
				@Override
				public void run() {
					if (DEBUG){
						Log.d(LOGTAG, "webview height: " + webView.getHeight());
						if (careerItemFragment.mSv_scrollView != null){
							Log.d(LOGTAG, "full height: " + 
									careerItemFragment.mSv_scrollView.getChildAt(0).getHeight());
						}
					}

					final int visibility = webView.getVisibility();
					//make webview invisible for animation purposes
					if (visibility == View.VISIBLE){
						webView.setVisibility(View.INVISIBLE);
					}
					
					/* In case the WebView does not load correctly. */
					if (webView.getHeight() <= 0 && visibility != View.GONE){
						//we have failed to load, retry
						if (attempts++ < RETRY_LIMIT){
							retryListener.onRetry();
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
						
						retryListener.onAnimationSet();
					}
					
				}
			});
		};
		
		/** @version 0.1.0-20141121 */
		public interface RetryListener {
			/** Executed when webview is attempted to retry. */
			public void onRetry();
			/** Executed when the animate is set. */
			public void onAnimationSet();
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Implemented listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The Job description web view client to listen to when the view has been loaded. */
	private AnimatedRetryWebViewClient tagsWebViewListener = 
			new AnimatedRetryWebViewClient(this, 
				new AnimatedRetryWebViewClient.RetryListener() {		
					@Override
					public void onRetry() {
						buildAndLoadTags();
					}
					
					@Override
					public void onAnimationSet() {
						//nothing to do here
						mScrollViewPending2 = true;
						resetScrollView();
					}
				}){
		

		/* Over ride the load resource to avoid the javascript bug in 2.3.3
		 * 
		 * (non-Javadoc)
		 * @see android.webkit.WebViewClient#shouldOverrideUrlLoading(android.webkit.WebView, java.lang.String)
		 */
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url){
			if (DEBUG){
				Log.d(LOGTAG, "url: " +url);
			}
			
			if (url.startsWith(HREF_TAGS_ID)){
				try {
					//this should take "AndroidCareerItem:tag=android" and produce "android"
					mFragInteractionListener.onTagClick(url.replaceAll(HREF_TAGS_ID, ""));
				} catch (Exception tagE){
					Log.e(LOGTAG, "Unusual tag behaviour: " + tagE);
					if (DEBUG){
						tagE.printStackTrace();
					}
				}
				return true; 
			}
			return false;
		}
	};
	
	/** The Job description web view client to listen to when the view has been loaded. */
	private AnimatedRetryWebViewClient jobDescriptionWebViewListener = 
			new AnimatedRetryWebViewClient(this, 
				new AnimatedRetryWebViewClient.RetryListener() {		
					@Override
					public void onRetry() {
						loadJobDescription();	
					}
					
					@Override
					public void onAnimationSet() {
						//we have finished loading, reset scroll
						mScrollViewPending1 = true;
						resetScrollView();
					}
				});
	
	
	
	@Override
	public void onClick(View v) {
		collapseFloatingActionButton();
		
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
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (DEBUG){
			Log.d(LOGTAG, "onCheckedChanged: " + isChecked);
		}
		if (buttonView.getId() == R.id.careerstack_careerItem_check_showTags){
			//change the button text
			buttonView.setText(isChecked ? getString(R.string.careerstack_careeritem_showtags_on) : 
											getString(R.string.careerstack_careeritem_showtags_off) );
			mPrefs.edit()
				.putBoolean(getString(R.string.careerstack_pref_KEY_SHOW_ITEM_TAGS), 
							isChecked)
				.commit();
			checkTagVisiblity();
			if (DEBUG){
				Log.d(LOGTAG, "show tags: " + isChecked);
			}
		}
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(getString(R.string.careerstack_pref_KEY_SHOW_ITEM_TAGS))){
			boolean prefValue = sharedPreferences.getBoolean(key, false);
			if (mChk_toggleTags.isChecked() != prefValue){
				//ensure the value is the same, the listener will deal with the rest
				mChk_toggleTags.setChecked(prefValue);
			}
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Internal listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * The interaction listener that the activity must implement to handle the 
	 * {@link CareerItemFragment}'s requests. 
	 * @author Jason J.
	 * @version 0.1.0-20141124
	 */
	public static interface OnFragmentInteractionListener {	
		
		/** Sends activity a search a tag. 
		 * @param tag The tag that is clicked and for the activity to search. 
		 * {@link MainFragment} keys.
		 * @return <code>true</code> if the activity has honoured the request,
		 * <code>false</code> if has been ignored.		 */
		public boolean onTagClick(String tag);
	}

}
