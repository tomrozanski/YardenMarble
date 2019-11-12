package com.FinalProject.TomAlon.YardenShaish.Fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.FinalProject.TomAlon.YardenShaish.Listeners.UploadImageListener;
import com.FinalProject.TomAlon.YardenShaish.R;
import com.kyanogen.signatureview.SignatureView;

public class SignatureFragment extends Fragment {
    private SignatureView signatureView;

    private UploadImageListener mListener;

    public SignatureFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signature, container, false);
        signatureView = view.findViewById(R.id.view_signature);
        Button clearBtn = view.findViewById(R.id.button_clear);
        Button saveBtn = view.findViewById(R.id.button_save);

        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signatureView.clearCanvas();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = signatureView.getSignatureBitmap();
                mListener.uploadImage(bitmap, true);
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof UploadImageListener) {
            mListener = (UploadImageListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement UploadImageListener");
        }
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }
}
