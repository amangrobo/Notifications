package com.grobo.notifications.services;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.grobo.notifications.R;
import com.grobo.notifications.services.lostandfound.LostAndFoundActivity;
import com.grobo.notifications.utils.ImageViewerActivity;

public class ServicesFragment extends Fragment implements View.OnClickListener {


    public ServicesFragment() {}


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated( view, savedInstanceState );
        if(getActivity()!=null)
            getActivity().setTitle( "Services" );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_services, container, false);

        CardView maintenance = view.findViewById(R.id.maintenance_cv);
        CardView sharingPortal = view.findViewById(R.id.sharing_portal_cv);
        CardView lostFound = view.findViewById(R.id.lost_found_cv);
        CardView cabSharing = view.findViewById(R.id.cab_sharing_cv);
        CardView busSharing = view.findViewById(R.id.bus_sharing_cv);

        ImageView lost=view.findViewById( R.id.layout2 );
        Glide.with( getContext() ).load( "https://amwaycenter.s3.amazonaws.com/img/CONTACTUS-lostandfound.jpeg" ).into( lost );
        ImageView bus=view.findViewById( R.id.layout1 );
        Glide.with( getContext() ).load( "https://firebasestorage.googleapis.com/v0/b/timetable-grobo.appspot.com/o/bus.png?alt=media&token=a695cc67-8877-4abe-a770-e6c05f82e1c6" ).into( bus );

        ImageView sharing=view.findViewById( R.id.layout3 );
        Glide.with( getContext() ).load( "https://cdn.givingcompass.org/wp-content/uploads/2017/10/15103015/Giving-Compass-Magazine-Coming-soon.jpg" ).into( sharing );


        ImageView cabsharing=view.findViewById( R.id.layout4 );
        Glide.with( getContext() ).load( "https://cdn.givingcompass.org/wp-content/uploads/2017/10/15103015/Giving-Compass-Magazine-Coming-soon.jpg" ).into( cabsharing );

        ImageView maintenanceimage=view.findViewById( R.id.layout5 );
        Glide.with( getContext() ).load( "https://cdn.givingcompass.org/wp-content/uploads/2017/10/15103015/Giving-Compass-Magazine-Coming-soon.jpg" ).into( maintenanceimage );


        maintenance.setOnClickListener(this);
        sharingPortal.setOnClickListener(this);
        lostFound.setOnClickListener(this);
        cabSharing.setOnClickListener(this);
        busSharing.setOnClickListener( this );

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.sharing_portal_cv:
                Toast.makeText(getContext(), "Coming Soon...", Toast.LENGTH_SHORT).show();;
                break;

            case R.id.cab_sharing_cv:
                Toast.makeText(getContext(), "Coming Soon...", Toast.LENGTH_SHORT).show();;
                break;

            case R.id.maintenance_cv:
                Toast.makeText(getContext(), "Coming Soon...", Toast.LENGTH_SHORT).show();;
                break;

            case R.id.lost_found_cv:
                startActivity(new Intent(getActivity(), LostAndFoundActivity.class));
                break;
            case R.id.bus_sharing_cv:
                Intent intent=new Intent( getActivity(), ImageViewerActivity.class );
                intent.putExtra( "image_url","https://firebasestorage.googleapis.com/v0/b/timetable-grobo.appspot.com/o/bus.png?alt=media&token=a695cc67-8877-4abe-a770-e6c05f82e1c6" );
                startActivity( intent );
                break;


        }
    }

//    private void transactFragment(Fragment frag){
//        FragmentTransaction fragmentManager = getActivity().getSupportFragmentManager().beginTransaction();
//
//        fragmentManager.setCustomAnimations(R.anim.right_in, R.anim.left_out, R.anim.left_in, R.anim.right_out)
//                .replace(R.id.frame_layout_main, frag, frag.getTag())
//                .addToBackStack("later_fragment")
//                .commit();
//    }
}
