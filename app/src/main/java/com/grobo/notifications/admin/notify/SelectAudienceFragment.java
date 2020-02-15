package com.grobo.notifications.admin.notify;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.grobo.notifications.R;
import com.grobo.notifications.account.por.PORItem;
import com.grobo.notifications.utils.utils;

import java.util.ArrayList;
import java.util.List;

import static com.grobo.notifications.utils.Constants.ROLL_NUMBER;

public class SelectAudienceFragment extends Fragment implements AudienceListRecyclerAdapter.OnAudienceItemInteraction {

    public SelectAudienceFragment() {
    }

    private Context context;
    private List<String> selectedAudiences = new ArrayList<>();
    private AudienceListRecyclerAdapter recyclerAdapter;
    private PORItem porItem;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getContext() != null)
            context = getContext();

        if (getArguments() != null && getArguments().containsKey("por"))
            porItem = getArguments().getParcelable("por");

        if (porItem != null) {
            String roll = PreferenceManager.getDefaultSharedPreferences(context).getString(ROLL_NUMBER, "");
            if (!roll.isEmpty()) selectedAudiences.add(roll.toLowerCase());
            selectedAudiences.add(porItem.getClubName().toLowerCase().trim());
        } else
            utils.showSimpleAlertDialog(context, "Alert!!!", "Error in retrieving POR data!");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_select_audience, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() != null)
            getActivity().setTitle("Select Audience");

        RecyclerView recyclerView = view.findViewById(R.id.recycler_audience);
        recyclerAdapter = new AudienceListRecyclerAdapter(context, this);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerAdapter.setItemList(selectedAudiences);

        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.list_hint_audience, android.R.layout.simple_list_item_1);
        final AutoCompleteTextView textView = view.findViewById(R.id.tv_audience);
        textView.setAdapter(adapter);

        ImageView addButton = view.findViewById(R.id.iv_add_audience);
        addButton.setOnClickListener(v -> {
            String t = textView.getText().toString().toLowerCase().trim();
            if ("all".equals(t))
                Toast.makeText(context, "You cannot send notifications to all!", Toast.LENGTH_LONG).show();
            else if (!t.isEmpty()) {
                selectedAudiences.add(t);
                textView.setText("");
                recyclerAdapter.setItemList(selectedAudiences);
            }
        });

        ViewGroup insertPoint = (ViewGroup) view.findViewById(R.id.insert_point);

        new CountDownTimer(5010, 1000){

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

            }
        }.start();

        for (int i = 0; i < 10; i++) {
            TextView v = new TextView(context);
            v.setText("text view" + i);
            insertPoint.addView(v, i, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }


//        KeyboardUtils.showSoftInput(textView, context);
    }

    @Override
    public void onItemSelected(int position) {
        selectedAudiences.remove(position);
        recyclerAdapter.setItemList(selectedAudiences);
    }
}
