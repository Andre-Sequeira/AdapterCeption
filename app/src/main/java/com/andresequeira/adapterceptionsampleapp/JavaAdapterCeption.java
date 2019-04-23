package com.andresequeira.adapterceptionsampleapp;

import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.andresequeira.adapterception.AdapterCeption;

/**
 * TODO
 */
public class JavaAdapterCeption extends AdapterCeption<TextView> {

    @Override
    protected int viewType(int position) {
        return super.viewType(position);
    }

    @Override
    protected int count() {
        return 0;
    }

    @Nullable
    @Override
    protected ViewProvider<TextView> newViewProvider() {
        return null;
    }

    @Override
    public void bind(@NonNull TextView viewWrapper, int position) {

    }
}
