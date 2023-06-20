package com.example.absensi;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements View.OnClickListener {

    private final static String TAG = "HomeFragment";

    //    init komponent
    private AppCompatTextView tv_date_today, tv_count_suhu, tv_condition_suhu, tv_location, tv_jokse;
    private CardView menu_hadir, menu_izin, menu_sakit;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        tv_date_today = v.findViewById(R.id.tv_date_today);
        tv_count_suhu = v.findViewById(R.id.tv_count_suhu);
        tv_location = v.findViewById(R.id.tv_location);
        tv_condition_suhu = v.findViewById(R.id.tv_condition_suhu);
        tv_jokse = v.findViewById(R.id.tv_jokes);
        menu_hadir = v.findViewById(R.id.menu_hadir);
        menu_izin = v.findViewById(R.id.menu_izin);
        menu_sakit = v.findViewById(R.id.menu_sakit);
        getTodayDate();
        getWheateher();
        getJokes();

        menu_hadir.setOnClickListener(this);
        return v;
    }

    public void getTodayDate() {
        Calendar calendar = Calendar.getInstance();
        Locale id = new Locale("in", "ID");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", id);
        tv_date_today.setText(simpleDateFormat.format(calendar.getTime()));
    }

    public void getWheateher() {
        AndroidNetworking.get("https://weatherapi-com.p.rapidapi.com/current.json")
                .addHeaders("X-RapidAPI-Key", "929c441b19msh0f1552d44e995a1p191b95jsn651f9069c737")
                .addHeaders("X-RapidAPI-Host", "weatherapi-com.p.rapidapi.com")
                .addQueryParameter("q", "-5.39,105.25")
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            tv_location.setText(response.getJSONObject("location").getString("name"));
                            tv_count_suhu.setText(response.getJSONObject("current").getString("temp_c"));
                            tv_condition_suhu.setText(response.getJSONObject("current").getJSONObject("condition").getString("text"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.d("responEdit", "gagal " + anError.getErrorBody());
                    }
                });
    }

    public void getJokes() {
        AndroidNetworking.get("https://candaan-api.vercel.app/api/text/random")
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, "onResponse: " + response.getString("status"));
                            if(response.getString("status").equals("200")){
                                tv_jokse.setText(response.getString("data"));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.d("responEdit", "gagal " + anError.getErrorBody());
                    }
                });
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.menu_hadir:
                intent = new Intent(getActivity(), UserAttendenceActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}