package cjkim00.imagesharingapplicationfinal.Search;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cjkim00.imagesharingapplicationfinal.R;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class SearchFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private RecyclerView mRecyclerView;
    private EditText mSearch;
    //public MySearchRecyclerViewAdapter mAdapter;
    private List<Member> mMembers;
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SearchFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static SearchFragment newInstance(int columnCount) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMembers = new ArrayList<>();
        mMembers.add(new Member("Username", "desc", "location", 1, 1));
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        Objects.requireNonNull(getActivity()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_list, container, false);
        mSearch = view.findViewById(R.id.editText_search_fragment_search);
        Button searchButton = view.findViewById(R.id.button_search_users);
        mRecyclerView = view.findViewById(R.id.list_search_results_fragment_search);
        Context context = view.getContext();

        if (mColumnCount <= 1) {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            mRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }

        searchButton.setOnClickListener(v -> onButtonPress());
        return view;
    }

    private void onButtonPress() {
        if(mSearch.getText().toString().length() != 0) {
            hideSoftKeyboard();
            try {
                mSearch.clearFocus();
                String str = mSearch.getText().toString();
                getMembers(str);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void hideSoftKeyboard() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) Objects.requireNonNull(getActivity())
                        .getSystemService(Activity.INPUT_METHOD_SERVICE);
        View focusedView = getActivity().getCurrentFocus();
        if(focusedView != null) {
            inputMethodManager.hideSoftInputFromWindow(getActivity()
                    .getCurrentFocus().getWindowToken(), 0);
        }
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void getMembers(String text) throws InterruptedException {
        List<Member> tempArray = new ArrayList<>();
        Thread thread = new Thread(() -> {

            try {
                Uri uri = new Uri.Builder()
                        .scheme("https")
                        .appendPath("cjkim00-image-sharing-app.herokuapp.com")
                        .appendPath("find_member")
                        .build();

                URL url = new URL(uri.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                conn.setUseCaches(false);
                conn.setAllowUserInteraction(false);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                conn.connect();

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("User", text);
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(jsonParam.toString());
                os.flush();
                os.close();

                int status = conn.getResponseCode();
                Log.i("MSG", "STATUS: " + os.toString());
                switch (status) {
                    case 200:
                    case 201:
                        BufferedReader br = new BufferedReader(
                                new InputStreamReader(conn.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }
                        br.close();
                        Log.i("MSG", sb.toString());
                        getResults(sb.toString() , tempArray);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
        thread.join();
        mMembers = tempArray;
        mRecyclerView.setAdapter(new MySearchRecyclerViewAdapter(tempArray, mListener));
    }

    private void getResults(String result, List<Member> arr) {
        try {
            JSONObject root = new JSONObject(result);
            if (root.has("success") && root.getBoolean("success") ) {
                //JSONObject response = root.getJSONObject("success");
                JSONArray data = root.getJSONArray("data");
                for(int i = 0; i < data.length(); i++) {
                    JSONObject jsonPost = data.getJSONObject(i);
                    Member tempMember = new Member(jsonPost.getString("username")
                            , jsonPost.getString("profiledescription")
                            , jsonPost.getString("profileimagelocation")
                            , jsonPost.getInt("followingtotal")
                            , jsonPost.getInt("followerstotal"));
                    arr.add(tempMember);

                }
            } else {
                Log.i("MSG", "No response");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i("MSG", e.getMessage());
        }
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
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Member mItem);
    }
}
