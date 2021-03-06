package com.grobo.notifications.account.por;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.grobo.notifications.R;
import com.grobo.notifications.clubs.ClubItem;
import com.grobo.notifications.clubs.ClubViewModel;
import com.grobo.notifications.network.ClubRoutes;
import com.grobo.notifications.network.PorRoutes;
import com.grobo.notifications.network.RetrofitClientInstance;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.grobo.notifications.utils.Constants.USER_TOKEN;

public class ClaimPORFragment extends Fragment implements ClaimPorClubAdapter.OnClaimClubSelListener {

    public ClaimPORFragment() {
    }

    private ClubViewModel clubViewModel;
    private List<ClubItem> allClubs;
    private ClaimPorClubAdapter adapter;
    private ClubItem selectedClub = null;
    private Context context;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clubViewModel = new ViewModelProvider(this).get(ClubViewModel.class);
        if (getContext() != null)
            this.context = getContext();

        progressDialog = new ProgressDialog(context);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_claim_por, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateClubsData();

        RecyclerView recyclerView = view.findViewById(R.id.recycler_clubs);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        adapter = new ClaimPorClubAdapter(context, this);
        recyclerView.setAdapter(adapter);
        clubViewModel.getAllClubs().observe(getViewLifecycleOwner(), clubItems -> {
            allClubs = clubItems;
            adapter.setClubList(allClubs);
        });

        SearchView searchView = view.findViewById(R.id.sv_club_for_por);
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                if (s.length() > 0) {

                    List<ClubItem> temp = new ArrayList<>();

                    for (ClubItem ci : allClubs) {
                        if (ci.getName().toLowerCase().contains(s.toLowerCase()))
                            temp.add(ci);
                    }

                    adapter.setClubList(temp);
                } else {
                    adapter.setClubList(allClubs);
                }

                return false;
            }
        });

    }

    private void updateClubsData() {
        String token = PreferenceManager.getDefaultSharedPreferences(context).getString(USER_TOKEN, "0");

        ClubRoutes service = RetrofitClientInstance.getRetrofitInstance().create(ClubRoutes.class);

        Call<List<ClubItem>> call = service.getAllClubs(token);
        call.enqueue(new Callback<List<ClubItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<ClubItem>> call, @NonNull Response<List<ClubItem>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        clubViewModel.delete();

                        List<ClubItem> allItems = response.body();
                        for (ClubItem newItem : allItems) {
                            clubViewModel.insert(newItem);
                        }

                    }
                    Toast.makeText(context, "Updated.", Toast.LENGTH_SHORT).show();
                    PreferenceManager.getDefaultSharedPreferences(context).edit().putLong("last_club_update_time", System.currentTimeMillis()).apply();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ClubItem>> call, @NonNull Throwable t) {
                if (t.getMessage() != null)
                    Log.e("failure", t.getMessage());
                Toast.makeText(getContext(), "Update failed!! Please check internet connection!", Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public void onClubSelected(ClubItem clubItem) {
        selectedClub = clubItem;

        if (getView() != null) {
            getView().findViewById(R.id.recycler_clubs).setVisibility(View.GONE);
            getView().findViewById(R.id.sv_club_for_por).setVisibility(View.GONE);

            LinearLayout sel = getView().findViewById(R.id.ll_claim_por_2);
            sel.setVisibility(View.VISIBLE);

            View selectedCard = getView().findViewById(R.id.selected_club_card);

            TextView clubName = selectedCard.findViewById(R.id.club_name);
            TextView clubBio = selectedCard.findViewById(R.id.club_bio);

            clubName.setTextColor(Color.BLACK);
            clubBio.setTextColor(Color.DKGRAY);

            clubName.setText(clubItem.getName());
            clubBio.setText(clubItem.getBio());

            Glide.with(context)
                    .load(clubItem.getImage())
                    .placeholder(R.drawable.baseline_dashboard_24)
                    .into((ImageView) selectedCard.findViewById(R.id.club_image));

            EditText position = getView().findViewById(R.id.input_position_por);
            EditText description = getView().findViewById(R.id.input_description_por);
            Button submit = getView().findViewById(R.id.submit_por_for_approval);
            submit.setOnClickListener(v -> {
                if (position.getText().toString().length() == 0) {
                    position.setError("Enter a valid position");
                } else
                    showConfirmation(position.getText().toString(), description.getText().toString());
            });
        }
    }

    private void showConfirmation(String positionText, String descriptionText) {
        new AlertDialog.Builder(context)
                .setTitle("Confirmation Dialog")
                .setMessage("Submitting POR for approval...\nPlease confirm!!")
                .setPositiveButton("Confirm", (dialog, which) -> postPor(positionText, descriptionText))
                .setNegativeButton("Cancel", (dialog, id) -> {
                    if (dialog != null) dialog.dismiss();
                }).show();
    }

    private void postPor(String positionText, String descriptionText) {
        progressDialog.setMessage("Processing...");
        progressDialog.show();

        String token = PreferenceManager.getDefaultSharedPreferences(context).getString(USER_TOKEN, "");

        Map<String, Object> data = new HashMap<>();
        data.put("club", selectedClub.getId());
        data.put("position", positionText);
        data.put("description", descriptionText);

        RequestBody body = RequestBody.create((new JSONObject(data)).toString(), okhttp3.MediaType.parse("application/json; charset=utf-8"));

        PorRoutes service = RetrofitClientInstance.getRetrofitInstance().create(PorRoutes.class);
        Call<Void> call = service.claimPor(token, body);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "POR sent for approval.", Toast.LENGTH_SHORT).show();
                    if (getActivity() != null)
                        getActivity().getSupportFragmentManager().popBackStackImmediate();
                } else {
                    Log.e("failure", String.valueOf(response.code()));
                    Toast.makeText(context, "Upload failed", Toast.LENGTH_LONG).show();
                }
                if (progressDialog.isShowing()) progressDialog.dismiss();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (t.getMessage() != null) Log.e("failure", t.getMessage());
                if (progressDialog.isShowing()) progressDialog.dismiss();

                Toast.makeText(context, "Post failed, please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
