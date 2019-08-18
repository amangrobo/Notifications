package com.grobo.notifications.clubs;

import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.grobo.notifications.R;
import com.grobo.notifications.admin.clubevents.ClubEventFragment;
import com.grobo.notifications.network.OtherRoutes;
import com.grobo.notifications.network.RetrofitClientInstance;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.noties.markwon.Markwon;
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.image.glide.GlideImagesPlugin;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.grobo.notifications.utils.Constants.USER_TOKEN;


public class ClubDetailsFragment extends Fragment {

    public ClubDetailsFragment() {
    }

    private ClubViewModel clubViewModel;
    private PorAdapter porAdapter;
    private View porListParent;
    private boolean refreshed = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clubViewModel = ViewModelProviders.of(this).get(ClubViewModel.class);
    }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_club_details, container, false);

        ImageView cover = view.findViewById(R.id.event_detail_header_bg);
        TextView name = view.findViewById(R.id.tv_event_detail_title);
        FloatingActionButton followingFab = view.findViewById(R.id.fab_event_detail_interested);
        TextView bio = view.findViewById(R.id.tv_event_detail_time);
        ImageView website = view.findViewById(R.id.fab_website);
        TextView description = view.findViewById(R.id.tv_event_detail_description);
        View events = view.findViewById(R.id.club_card_events);

        Bundle b = getArguments();
        if (b != null) {
            String transitionName = b.getString("transitionName");
            cover.setTransitionName(transitionName);
            String id = b.getString("clubId");

            final ClubItem current = clubViewModel.getClubById(id);

            if (current != null) {

                Glide.with(this)
                        .load(current.getImage())
                        .centerInside()
                        .placeholder(R.drawable.baseline_dashboard_24)
                        .into(cover);

                name.setText(current.getName());

                porListParent = view.findViewById(R.id.por_cv_clubs);
                RecyclerView porRecyclerView = view.findViewById(R.id.por_recycler_clubs);
                porRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                porAdapter = new PorAdapter(getContext(), (PorAdapter.OnPORSelectedListener) getActivity());
                porRecyclerView.setAdapter(porAdapter);

                if (!refreshed) {
                    getClubPOR(current.getId());
                }

                if (current.getDescription() == null) {
                    current.setDescription("No Description");
                }
                final Markwon markwon = Markwon.builder(getContext())
                        .usePlugin(GlideImagesPlugin.create(getContext()))
                        .usePlugin(HtmlPlugin.create())
                        .build();
                final Spanned spanned = markwon.toMarkdown(current.getDescription());
                markwon.setParsedMarkdown(description, spanned);

                bio.setText(current.getBio());

                if (current.isFollowed()) {
                    followingFab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.light_blue)));
                } else {
                    followingFab.getBackground().setTint(getResources().getColor(R.color.dark_gray));
                }

                followingFab.setOnClickListener(v -> toggleStar(current));

                website.setOnClickListener(v -> openWebsite(current.getWebsite()));

                if (current.getEvents() != null && current.getEvents().size() > 1) {

                    events.setVisibility(View.VISIBLE);
                    events.setOnClickListener(v -> {

                        Fragment eventsFragment = new ClubEventFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("club_id", current.getId());
                        eventsFragment.setArguments(bundle);

                        if (getActivity() != null)
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_main, eventsFragment)
                                    .addToBackStack("later_fragment")
                                    .commit();
                    });
                }

                if (current.getPages() != null) {
                    for (String page : current.getPages()) {
                        if (page.contains("facebook")) {
                            ImageView fb = view.findViewById(R.id.club_facebook);
                            fb.setVisibility(View.VISIBLE);
                            fb.setOnClickListener(view1 -> openWebsite(page));
                        } else if (page.contains("instagram")) {
                            ImageView inst = view.findViewById(R.id.club_instagram);
                            inst.setVisibility(View.VISIBLE);
                            inst.setOnClickListener(view1 -> openWebsite(page));
                        } else if (page.contains("twitter")) {
                            ImageView tw = view.findViewById(R.id.club_twitter);
                            tw.setVisibility(View.VISIBLE);
                            tw.setOnClickListener(view1 -> openWebsite(page));
                        }
                    }
                }

            }
        }

        return view;
    }

    private void toggleStar(ClubItem item) {
        if (item.isFollowed()) {
            item.setFollowed( false );
        } else {
            item.setFollowed( true );
        }
        clubViewModel.insert( item );
        detach();
    }
    private void detach()
    {

        if (getActivity() != null)
            getActivity().getSupportFragmentManager().beginTransaction().detach(this).attach(this).commit();
    }

    private void getClubPOR(String clubId) {
        String token = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(USER_TOKEN, "0");

        OtherRoutes service = RetrofitClientInstance.getRetrofitInstance().create(OtherRoutes.class);

        Call<ResponseBody> call = service.getPorByClub(token, clubId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        List<PorItem> allPors = new ArrayList<>();

                        try {
                            JSONObject object = new JSONObject(response.body().string());

                            JSONArray array = object.getJSONArray("pors");

                            for (int i = 0; i < array.length(); i++) {

                                PorItem newPor = new PorItem();
                                JSONObject singlePor = array.getJSONObject(i);

                                if (singlePor.has("_id"))
                                    newPor.setPorId(singlePor.getString("_id"));
                                if (singlePor.has("club"))
                                    newPor.setClubId(singlePor.getString("club"));
                                if (singlePor.has("access"))
                                    newPor.setAccess(singlePor.getInt("access"));
                                if (singlePor.has("position"))
                                    newPor.setPosition(singlePor.getString("position"));

                                if (singlePor.has("user")) {
                                    JSONObject user = singlePor.getJSONObject("user");
                                    if (user.has("_id"))
                                        newPor.setUserId(user.getString("_id"));
                                    if (user.has("name"))
                                        newPor.setName(user.getString("name"));
                                    if (user.has("instituteId"))
                                        newPor.setInstituteId(user.getString("instituteId"));
                                    if (user.has("image"))
                                        newPor.setImage(user.getString("image"));
                                }

                                allPors.add(newPor);
                            }


                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (!allPors.isEmpty()) {
                            porListParent.setVisibility(View.VISIBLE);
                            porAdapter.setItemList(allPors);
                        }
                    }

//                    Toast.makeText(getActivity(), "PORs updated", Toast.LENGTH_SHORT).show();
                    refreshed = true;
                } else {
                    Toast.makeText(getContext(), "Failed to get PORs!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e("failure", t.getMessage());
                Toast.makeText(getContext(), "PORs fetch failure!!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openWebsite(String url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(getResources().getColor(R.color.colorPrimary));

        String website1 = url;
        if (!website1.startsWith("http") && !website1.startsWith("https")) {
            website1 = "https://" + website1;
        }

        CustomTabsIntent customTabsIntent = builder.build();
        if (getContext() != null)
            customTabsIntent.launchUrl(getContext(), Uri.parse(website1));
    }
}
