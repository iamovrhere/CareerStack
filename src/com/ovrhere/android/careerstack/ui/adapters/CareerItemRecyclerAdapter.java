package com.ovrhere.android.careerstack.ui.adapters;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ovrhere.android.careerstack.R;
import com.ovrhere.android.careerstack.dao.CareerItem;
import com.ovrhere.android.careerstack.utils.Utility;

/** The career item list adapter. Simple migration from {@link CareerItemFilterListAdapter}.
 * @author Jason J.
 * @version 0.1.0-20151007
 */
public class CareerItemRecyclerAdapter extends Adapter<CareerItemRecyclerAdapter.ViewHolder> {
	final static private String LOGTAG = CareerItemRecyclerAdapter.class
			.getSimpleName();	

	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private final Context mContext;
	/** The list of all career items. */
	private final List<CareerItem> mCareerItems = new ArrayList<CareerItem>();
	/**The layout resource, default is 
	* <code>android.R.layout.simple_list_item_2</code> */
	private final int mLayoutResource = R.layout.row_my_simple_list_item; 
	
	private final OnCareerItemClickListener mListener;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// end finals
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The first row message. */
	private String mFirstRowMessage = "";	
	/** The empty message. */
	private String mEmptyMessage = "";
	/** The sub text message. */
	private String mSubTitleMessage = "";
	
	
	/** Builds the adapter using the layout:
	 * {@link R.layout#row_my_simple_list_item}
	 * @param context The current context. 	 */
	public CareerItemRecyclerAdapter(Context context, OnCareerItemClickListener listener) {
		this.mContext = context;
		this.mEmptyMessage = context.getString(R.string.careerstack_careerlist_noResults);
		this.mListener = listener;
	}	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End initializations
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Resets the career items to be new set. 
	 * @param careerItems The career items to reset to.
	 */
	public void setCareerItems(List<CareerItem> careerItems) {
		//TODO only remove non-present items and add new ones.
		mCareerItems.clear();
		notifyDataSetChanged();
		mCareerItems.addAll(careerItems);
		notifyItemRangeInserted(0, careerItems.size());
	}
	
	//TODO redesign to not use context.	
	/** Sets the search terms to give in the first row	 */
	public void setSearchTerms(String keyword, String location, 
			boolean remote, boolean relocation) {
		mEmptyMessage = "";
		mFirstRowMessage = "";
		mSubTitleMessage = "";
		
		//set keyword
		if (keyword.isEmpty()){
			mEmptyMessage = 
					mContext.getString(R.string.careerstack_careerlist_noResults);
			mFirstRowMessage = 
					mContext.getString(R.string.careerstack_careerlist_matchingResults);
		} else {
			mEmptyMessage = 
					mContext.getString(R.string.careerstack_careerlist_noResults_stub,
							keyword);
			mFirstRowMessage = 
					mContext.getString(R.string.careerstack_careerlist_matchingResults_stub,
							keyword);
		}
		
		String nearLocation = "";
		//set location		
		if (!location.isEmpty()){			
			nearLocation = mContext.getString(R.string.careerstack_careerlist_resultsNear,
					location);
		}
		mEmptyMessage += nearLocation;
		mFirstRowMessage += nearLocation;
		
		
		//set subtitle
		if (remote){
			mSubTitleMessage = 
					mContext.getString(R.string.careerstack_main_check_workRemotely);
		}
		if (relocation){
			if (!mSubTitleMessage.isEmpty()) {
				mSubTitleMessage += " + ";
			}
			mSubTitleMessage += 
					mContext.getString(R.string.careerstack_main_check_offersRelocation);
		}
		if (mSubTitleMessage.isEmpty() == false){
			mSubTitleMessage = "( "+mSubTitleMessage+" )";
		}
	}

	public boolean isEmpty() {
		return mCareerItems.size() <= 0;
	}

	/** Clears all notes + errors and resets the view to both "loading" and "empty" */
	public void clear() {
		final int SIZE = mCareerItems.size();
		mCareerItems.clear();
		notifyItemRangeRemoved(0, SIZE);
	}
	
	
	/**
	 * 
	 * Sets the sort for the adapter.
	 */
	protected void setSort(int mode) {	} //TODO sort


	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Override methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public int getItemCount() {
		return mCareerItems.size() + 1;
	}
	
	@Override
	public void onBindViewHolder(ViewHolder holder, final int position) {
		
		if (position == 0) {
			if (mCareerItems.size() <= 0){
				//if no results, return no results message.
				holder.jobTitle.setText(mEmptyMessage);
			} else {
				//give top row message of : n Matching x
				holder.jobTitle.setText(mCareerItems.size() +" " + mFirstRowMessage);
			}			
			holder.jobTitle.setGravity(Gravity.CENTER);			
			holder.jobTitle.setTypeface(null, Typeface.ITALIC);
			
			if (mSubTitleMessage.isEmpty()){
				//if no message, why show it?
				holder.companyLocationEtc_andDate.setVisibility(View.GONE);
			} else {
				holder.companyLocationEtc_andDate.setVisibility(View.VISIBLE);
				holder.companyLocationEtc_andDate.setText(mSubTitleMessage); 
				holder.companyLocationEtc_andDate.setGravity(Gravity.CENTER);
				holder.companyLocationEtc_andDate.setTypeface(null, Typeface.ITALIC);
			}		
			holder.rootView.setOnClickListener(null);
			
			return; //end early.
			
		} 
		
		//we have an actual career item.		
		final CareerItem item = mCareerItems.get(position - 1);		
			
		holder.jobTitle.setGravity(Gravity.LEFT);
		holder.jobTitle.setTypeface(null, Typeface.BOLD);
		
		holder.companyLocationEtc_andDate.setGravity(Gravity.LEFT);
		holder.companyLocationEtc_andDate.setVisibility(View.VISIBLE);	
		holder.companyLocationEtc_andDate.setTypeface(null, Typeface.NORMAL);
		
		holder.rootView.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				mListener.onCareerItemClick(item);
			}
		});
		
		holder.jobTitle.setText(item.getTitle());
		holder.companyLocationEtc_andDate.setText(
				item.getCompanyLocationEtc() + " - " + 
				Utility.getRelativeTime(holder.companyLocationEtc_andDate.getContext(), item.getUpdateDate())
				);
		
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
	    View v = LayoutInflater.from(parent.getContext())
	    			.inflate(mLayoutResource, parent, false);	    
	    return new ViewHolder(v);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////	


	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Sorting 
	////////////////////////////////////////////////////////////////////////////////////////////////


	/** The sorter that sorts the content according to the current mode. */
	private Comparator<CareerItem> mSortComparator = new Comparator<CareerItem>() {

		/*
		 * Remember: 
		 * 
		 * https://developer.android.com/reference/java/util/Comparator.html#compare%28T,%20T%29
		 * 
		 *  - compare(a,a) returns zero for all a
		 *  - the sign of compare(a,b) must be the opposite of the sign of compare(b,a) for all pairs of (a,b)
		 *  - From compare(a,b) > 0 and compare(b,c) > 0 it must follow compare(a,c) > 0 for all possible combinations of (a,b,c)
		 *  
		 *  
		 *  an integer < 0 if leftHandSide is less than rightHandSide, 
		 *  0 if they are equal, 
		 *  and > 0 if leftHandSide is greater than rightHandSide.
		 *  
		 *  
		 *  (non-Javadoc)
		 */
		@Override
		//compare(StudyNoteItem lhs, StudyNoteItem rhs)
		public int compare(CareerItem leftHS, CareerItem rightHS) {
			//TODO finish sort
			return 0;
		}
		
	};
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal classes
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** View holder for the layout {@link R.layout#row_my_simple_list_item} 
	 * @version 0.1.0-20151007 */
	public static class ViewHolder extends RecyclerView.ViewHolder {
		public final TextView jobTitle;
		public final TextView companyLocationEtc_andDate;
		public final View rootView;
		
		public ViewHolder(View rootView) {
			super(rootView);
			this.rootView = rootView;
			this.jobTitle = (TextView) rootView.findViewById(android.R.id.text1);
			this.companyLocationEtc_andDate = (TextView) rootView.findViewById(android.R.id.text2);
		}
	}
	
	/** @version 0.1.0-20150720 */
	public static interface OnCareerItemClickListener {
		public void onCareerItemClick(CareerItem item);
	}
}
