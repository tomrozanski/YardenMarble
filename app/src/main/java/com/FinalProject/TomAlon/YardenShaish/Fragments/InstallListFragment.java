package com.FinalProject.TomAlon.YardenShaish.Fragments;

import android.app.ActivityOptions;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.FinalProject.TomAlon.YardenShaish.AdminActivity;
import com.FinalProject.TomAlon.YardenShaish.Install.Install;
import com.FinalProject.TomAlon.YardenShaish.Install.StatusEnum;
import com.FinalProject.TomAlon.YardenShaish.LoginActivity;
import com.FinalProject.TomAlon.YardenShaish.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Objects;

public class InstallListFragment extends Fragment {
    private static final String TAG = InstallListFragment.class.getSimpleName();

    private static final String INSTALLATIONS_COLLECTION_TAG = "installations";
    private static final String TIME_FIELD_TAG = "time";
    private static final String STATUS_FIELD_TAG = "status";

    // date
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEEE dd/MM", Locale.ENGLISH);
    private final GregorianCalendar calendar = resetDay(new GregorianCalendar());

    // widgets
    private Button dateBtn;

    // dialogs
    private DatePickerDialog datePickerDialog;

    // firebase and recycle view
    private final FirebaseFirestore mFireStoreDB = FirebaseFirestore.getInstance();
    private FirestoreRecyclerAdapter<Install, ContactViewHolder> contactsAdapter;
    private RecyclerView contactRecyclerView;

    private LinearLayout noMessageLayout;

    //  shiftFragments listener
    private OnInstallUpdated mListener;

    public interface OnInstallUpdated {
        void updateHolderViaParentActivity(ContactViewHolder holder, final Install install, final int position);
    }

    public InstallListFragment() {
    }

    public static InstallListFragment newInstance() {
        return new InstallListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_install_list, container, false);

        dateBtn = view.findViewById(R.id.date_text);

        // initialize logout button
        FloatingActionButton logoutBtn = view.findViewById(R.id.logout_btn);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent, Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle() : null);
                Objects.requireNonNull(getActivity()).finish();
            }
        });

        updateDate();

        noMessageLayout = view.findViewById(R.id.no_data_layout);

        datePickerDialog = new DatePickerDialog(view.getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Log.d(TAG, "date picked");
                        calendar.set(year, monthOfYear, dayOfMonth);
                        updateDate();
                        Query queryByDate = getInstallsQueryByDate();
                        setInstallationsAdapter(queryByDate);
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        dateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });

        contactRecyclerView = view.findViewById(R.id.installers_list_recycler_view);
        contactRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        Query contactsQuery = getInstallsQueryByDate();
        setInstallationsAdapter(contactsQuery);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnInstallUpdated) {
            mListener = (OnInstallUpdated) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnInstallUpdated");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (contactsAdapter != null) {
            contactsAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        if (contactsAdapter != null) {
            contactsAdapter.stopListening();
        }
        super.onStop();
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    public void updateDate() {
        dateBtn.setText(DATE_FORMAT.format(calendar.getTime()));
    }

    public Install getInstallByPos(int pos) {
        return contactsAdapter.getItem(pos);
    }

    public Query getInstallsQueryByDate() {
        return mFireStoreDB.collection(INSTALLATIONS_COLLECTION_TAG)
                .whereGreaterThanOrEqualTo(TIME_FIELD_TAG, calendar.getTime())
                .whereLessThanOrEqualTo(TIME_FIELD_TAG, getTomorrowDate(calendar))
                .orderBy(TIME_FIELD_TAG, Query.Direction.ASCENDING);
    }

    public Date getTomorrowDate(GregorianCalendar calendar) {
        int today = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH, today + 1);
        Date date = calendar.getTime();
        calendar.set(Calendar.DAY_OF_MONTH, today);
        return date;
    }

    private static GregorianCalendar resetDay(GregorianCalendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    public void updateInstallStatus(StatusEnum status, int position) {
        mFireStoreDB.collection(INSTALLATIONS_COLLECTION_TAG)
                .document(getInstallByPos(position).getUid())
                .update(STATUS_FIELD_TAG, status.toString());
    }

    public void notifyAdapter() {
        contactsAdapter.notifyDataSetChanged();
    }


    public void setInstallationsAdapter(Query query) {
        FirestoreRecyclerOptions<Install> options = new FirestoreRecyclerOptions.Builder<Install>()
                .setQuery(query, Install.class)
                .setLifecycleOwner(this)
                .build();

        final FirestoreRecyclerAdapter<Install, ContactViewHolder> adapter = new FirestoreRecyclerAdapter<Install, ContactViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactViewHolder holder, final int position, @NonNull Install model) {
                holder.setContact(model);
                if (getActivity() instanceof AdminActivity) {
                    holder.setStatusTextVisible();
                }
                mListener.updateHolderViaParentActivity(holder, model, position);
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                if (getItemCount() == 0) {
                    contactRecyclerView.setVisibility(View.GONE);
                    noMessageLayout.setVisibility(View.VISIBLE);
                } else {
                    noMessageLayout.setVisibility(View.GONE);
                    contactRecyclerView.setVisibility(View.VISIBLE);
                }
            }

            @NonNull
            @Override
            public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_design, parent, false);
                return new ContactViewHolder(view);
            }
        };

        contactRecyclerView.setAdapter(adapter);
        contactsAdapter = adapter;
        contactsAdapter.notifyDataSetChanged();
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {
        private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("E HH:mm", Locale.ENGLISH);

        private TextView nameText;
        private TextView timeText;
        private TextView addressText;
        private TextView marbleSizeText;
        private TextView colonText;
        private ImageView phoneImage;
        private ImageView navigationImage;

        // for admin activity
        private TextView statusText;

        ContactViewHolder(View itemView) {
            super(itemView);
            RelativeLayout installLayout = itemView.findViewById(R.id.card_view);
            nameText = installLayout.findViewById(R.id.name_text);
            timeText = installLayout.findViewById(R.id.time_text);
            addressText = installLayout.findViewById(R.id.text_address);
            marbleSizeText = installLayout.findViewById(R.id.marble_size_text);
            colonText = installLayout.findViewById(R.id.colon_text);
            statusText = installLayout.findViewById(R.id.status_text);
            phoneImage = installLayout.findViewById(R.id.phone_image);
            navigationImage = installLayout.findViewById(R.id.navigate_image);
            installLayout.findViewById(R.id.status_text).setVisibility(TextView.INVISIBLE);
            installLayout.findViewById(R.id.colon_text).setVisibility(TextView.INVISIBLE);
        }

        void setContact(final Install install) {
            nameText.setText(install.getName());
            timeText.setText(DATE_FORMAT.format(install.getTime()));
            addressText.setText(install.getAddress());
            marbleSizeText.setText(install.getMarbleSize());
            phoneImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + install.getPhone()));
                    startActivity(callIntent);
                }
            });
            navigationImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent navigateIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + install.getAddress()));
                    startActivity(navigateIntent);
                }
            });
        }

        public void setColors(int backgroundColor, int textColor) {
            itemView.setBackgroundColor(backgroundColor);
            addressText.setTextColor(textColor);
            marbleSizeText.setTextColor(textColor);
            nameText.setTextColor(textColor);
            timeText.setTextColor(textColor);
            navigationImage.setColorFilter(textColor);
            phoneImage.setColorFilter(textColor);
        }

        void setStatusTextVisible() {
            colonText.setVisibility(View.VISIBLE);
            statusText.setVisibility(View.VISIBLE);
        }

        public void setStatusTextAndColor(StatusEnum status) {
            int color = 0;
            switch (status) {
                case WAITING:
                    color = Color.parseColor("#FFC300");
                    break;
                case REACHED:
                    color = Color.parseColor("#00BF57");
                    break;
                case FINISHED:
                    color = Color.parseColor("#641112");
                    break;
            }
            statusText.setText(status.toString());
            statusText.setTextColor(color);
        }
    }
}