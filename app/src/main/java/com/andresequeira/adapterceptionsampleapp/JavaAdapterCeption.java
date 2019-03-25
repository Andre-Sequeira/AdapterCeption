package com.andresequeira.adapterceptionsampleapp;

import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.andresequeira.adapterception.AdapterCeption;

public class JavaAdapterCeption extends AdapterCeption<TextView> {



    @Override
    protected int viewType(int position) {
        return super.viewType(position);
    }

    @Override
    protected int count() {
        return super.count();
    }

    @Nullable
    @Override
    protected ViewProvider<TextView> newViewProvider() {
        return super.newViewProvider();
    }

    @Override
    public void bind(@NonNull TextView viewWrapper, int position) {
        super.bind(viewWrapper, position);
    }
}
