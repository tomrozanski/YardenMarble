package com.FinalProject.TomAlon.YardenShaish.Listeners;

import android.graphics.Bitmap;

public interface UploadImageListener {
    void uploadImage(Bitmap bitmap, final boolean isSignature);
}
