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
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.ovrhere.android.careerstack.R;
import com.ovrhere.android.careerstack.dao.CareerItem;
import com.ovrhere.android.careerstack.utils.CompatClipboard;
import com.ovrhere.android.careerstack.utils.ToastManager;

/**
 * The listing of a job item.
 * @author Jason J.
 * @version 0.2.0-20140819
 */
public class CareerItemFragment extends Fragment implements OnClickListener {
	/** Class name for debugging purposes. */
	final static private String CLASS_NAME = CareerItemFragment.class
			.getSimpleName();
	/** Logtag for debuggin. */
	final static private String LOGTAG = CLASS_NAME;
	
	/** Bundle Key. The career item for this fragment. Parcelable/CareerItem. */
	final static private String KEY_CAREER_ITEM =
			CLASS_NAME +".KEY_CAREER_ITEM";
	/** The output date format string to use. */
	final static private String DATE_FORMAT = "yyyy-MM-dd HH:mm";
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The local instance of career item to display. */
	private CareerItem careerItem = null;
	/** The simple toast manager. */
	private ToastManager toastManager = null;
	
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
		View rootView = inflater.inflate(R.layout.fragment_career_item, container, false);
		toastManager = new ToastManager(getActivity());
		initOutputViews(rootView);
		initButtons(rootView);
		return rootView;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Initialization & helpers
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Initializes the output views. */
	private void initOutputViews(View rootView){
		TextView jobTitle = (TextView)
				rootView.findViewById(R.id.careerstack_careerItem_text_jobTitle);
		jobTitle.setText(careerItem.getTitle());
		
		TextView jobDescription = (TextView)
				rootView.findViewById(R.id.careerstack_careerItem_text_jobDescription);
		jobDescription.setText(careerItem.getDescription());
		
		TextView companyEtc = (TextView)
				rootView.findViewById(R.id.careerstack_careerItem_text_companyLocationEtc);
		companyEtc.setText( /* Be warned: This will keep html styling; 
								toString() will remove it. */
					Html.fromHtml(careerItem.getCompanyLocationEtc()),  
					BufferType.SPANNABLE
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
		share.setVisibility(View.GONE); //TODO finish share button
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
	
	/** Processes the date into a semi-readble string of 
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
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.careerstack_careerItem_button_openLink:
			launchUrl();
			break;
		case R.id.careerstack_careerItem_button_copyUrl:
			copyUrl();
			break;			
		case R.id.careerstack_careerItem_button_share:
			break;

		default:
			//fall through
		}
	}

}
