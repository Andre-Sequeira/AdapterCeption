package com.andresequeira.adapterception;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class InvisibleAdapterCeption extends AdapterCeption<Void> {

    public InvisibleAdapterCeption(@NonNull List<RecyclerView.Adapter> children) {
        super(children);
    }

    public InvisibleAdapterCeption(@NonNull RecyclerView.Adapter... children) {
        super(children);
    }

    @Override
    protected int count() {
        return 0;
    }

    @Nullable
    @Override
    protected ViewProvider<Void> newViewProvider() {
        return null;
    }

    @Override
    public void bind(@NonNull Void viewWrapper, int position) {

    }
}
