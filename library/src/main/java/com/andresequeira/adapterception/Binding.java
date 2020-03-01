package com.andresequeira.adapterception;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Binding<VW> {
    @NonNull
    private Binder<VW> binder;
    @Nullable
    private Unbinder<VW> unbinder;

    public Binding(@NonNull Binder<VW> binder, @Nullable Unbinder<VW> unbinder) {
        this.binder = binder;
        this.unbinder = unbinder;
    }

    @NonNull
    public Binder<VW> getBinder() {
        return binder;
    }

    @Nullable
    public Unbinder<VW> getUnbinder() {
        return unbinder;
    }
}
