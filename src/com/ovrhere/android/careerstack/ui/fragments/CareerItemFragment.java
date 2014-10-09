package com.ovrhere.android.careerstack.ui.fragments;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ovrhere.android.careerstack.R;
import com.ovrhere.android.careerstack.dao.CareerItem;
import com.ovrhere.android.careerstack.utils.CompatClipboard;
import com.ovrhere.android.careerstack.utils.ShareIntentUtil;
import com.ovrhere.android.careerstack.utils.ToastManager;

/**
 * The listing of a job item. Provides ability to open, copy or share link.
 * @author Jason J.
 * @version 0.4.0-20141008
 */
public class CareerItemFragment extends Fragment implements OnClickListener {
	/** Class name for debugging purposes. */
	final static private String CLASS_NAME = CareerItemFragment.class
			.getSimpleName();
	/** Logtag for debuggin. */
	final static private String LOGTAG = CLASS_NAME;
	/** Whether or not debuggin. */
	final static private boolean DEBUG = true;
	
	
	/** Bundle Key. The career item for this fragment. Parcelable/CareerItem. */
	final static private String KEY_CAREER_ITEM =
			CLASS_NAME +".KEY_CAREER_ITEM";
	/** Bundle Key. The scroll position as a ratio of 
	 * <code>pos/height</code>. Float. */
	final static private String KEY_SCROLL_POSITION_RATIO =
			CLASS_NAME +".KEY_SCROLL_POSITION_RATIO";
	
	/** The output date format string to use. */
	final static private String DATE_FORMAT = "yyyy-MM-dd HH:mm";
	
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
	
	/** The local instance of career item to display. */
	private CareerItem careerItem = null;
	/** The simple toast manager. */
	private ToastManager toastManager = null;
	/** The parent scroll view. */
	private ScrollView sv_scrollView = null;
	
	/** Bool for backstack safety of whether view has been built. */
	private boolean viewBuilt = false;
	
	/**
	 * Use this factory method createa new Career framgnet.
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
			outState.putFloat(
				KEY_SCROLL_POSITION_RATIO, 
				(float)sv_scrollView.getScrollY()/(float)sv_scrollView.getHeight());
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
		viewBuilt = false;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Initialization & helpers
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Initializes the scroll view. */
	private void initScrollView(View rootView, Bundle saveState){
		sv_scrollView = (ScrollView)
				rootView.findViewById(R.id.careerstack_careerItem_scrollView);
		if (saveState == null){
			return;
		}
		/* We use a ratio because the height WILL change between orientation.
		 * As such, scrolling to a pixel will be inaccurate between rotations,
		 * slowly creeping upwards.	 */
		final float scrollRatio = 
				saveState.getFloat(KEY_SCROLL_POSITION_RATIO);
		
		sv_scrollView.post(new Runnable() {@Override
			public void run() {
				float height = sv_scrollView.getHeight();
				sv_scrollView.scrollBy(0, (int) (height * scrollRatio));
			}
		});
	}
	
	/** Initializes the output views. */
	private void initOutputViews(View rootView){
		TextView jobTitle = (TextView)
				rootView.findViewById(R.id.careerstack_careerItem_text_jobTitle);
		jobTitle.setText(careerItem.getTitle());
				
		TextView companyEtc = (TextView)
				rootView.findViewById(R.id.careerstack_careerItem_text_companyLocationEtc);
		companyEtc.setText(careerItem.getCompanyLocationEtc());	
		
		
		/* How tired was I when I wrote this? 
		 * Job description needs parsing not company! */
		
		TextView jobDescription = (TextView)
				rootView.findViewById(R.id.careerstack_careerItem_text_jobDescription);
		
		jobDescription.setText(
				/* Be warned: This will keep html styling; 
				toString() will remove it. */
					Html.fromHtml(
								careerItem.getDescription()		
							)
					);
		
		
		TextView updateDate = (TextView)
				rootView.findViewById(R.id.careerstack_careerItem_text_updateDate);
		updateDate.setText(
				String.format(getString(R.string.careerstack_formatString_publishUpdateDate),
						processDate(careerItem.getPublishDate()),
						processDate(careerItem.getUpdateDate()))
				);
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
	/// Utility section
	////////////////////////////////////////////////////////////////////////////////////////////////
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
	/// Implements listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
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
