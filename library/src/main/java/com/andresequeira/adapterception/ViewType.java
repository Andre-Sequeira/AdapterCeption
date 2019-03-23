package com.andresequeira.adapterception;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ViewType<VW> {
    final int viewType;
    final Binding<VW> binding;
    final AdapterCeption.ViewProvider<VW> viewProvider;

    public ViewType(int viewType, @NonNull AdapterCeption.ViewProvider<VW> viewProvider, @NonNull Binder<VW> binder) {
        this(viewType, viewProvider, binder, null);
    }

    public ViewType(int viewType, @NonNull AdapterCeption.ViewProvider<VW> viewProvider, @NonNull Binder<VW> binder, @Nullable Unbinder<VW> unbinder) {
        this(viewType, viewProvider, new Binding<>(binder, unbinder));
    }

    public ViewType(int viewType, @NonNull AdapterCeption.ViewProvider<VW> viewProvider, @NonNull Binding<VW> binding) {
        if (viewType == 0) {
            throw new RuntimeException("viewType must be different from 0");
        }
        this.viewType = viewType;
        this.viewProvider = viewProvider;
        this.binding = binding;
    }
}
