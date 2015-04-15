package com.handytasks.handytasks;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DateTimePicker.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DateTimePicker#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DateTimePicker extends Fragment {
// --Commented out by Inspection START (4/15/2015 11:24 PM):
//    // TODO: Rename parameter arguments, choose names that match
//    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
// --Commented out by Inspection STOP (4/15/2015 11:24 PM)
    // --Commented out by Inspection (4/15/2015 11:24 PM):private static final String ARG_PARAM2 = "param2";

// --Commented out by Inspection START (4/15/2015 11:24 PM):
//    // TODO: Rename and change types of parameters
//    private String mParam1;
// --Commented out by Inspection STOP (4/15/2015 11:24 PM)
    // --Commented out by Inspection (4/15/2015 11:24 PM):private String mParam2;

    private IDateTimePicked mListener;

    public DateTimePicker() {
        // Required empty public constructor
    }

// --Commented out by Inspection START (4/15/2015 11:24 PM):
//    public static DateTimePicker newInstance() {
//        DateTimePicker fragment = new DateTimePicker();
//        return fragment;
//    }
// --Commented out by Inspection STOP (4/15/2015 11:24 PM)

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_date_time_picker, container, false);
        ((Button) view.findViewById(R.id.set)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSet(v);
            }
        });
        return view;
    }

    void onSet(View view) {
        if (mListener != null) {
            Calendar cal = Calendar.getInstance();
            DatePicker datePicker = ((DatePicker) getView().findViewById(R.id.date_picker));
            TimePicker timePicker = ((TimePicker) getView().findViewById(R.id.time_picker));
            cal.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), timePicker.getCurrentHour(), timePicker.getCurrentMinute());
            mListener.onDateTimePicked(cal.getTime());
            DateTimePicker.this.onDetach();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (IDateTimePicked) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface IDateTimePicked {
        // TODO: Update argument type and name
        public void onDateTimePicked(Date date);
    }

}
