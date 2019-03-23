package com.andresequeira.adapterception;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Binding<VW> {
    Binder<VW> binder;
    Unbinder<VW> unbinder;

    public Binding(@NonNull Binder<VW> binder, @Nullable Unbinder<VW> unbinder) {
        this.binder = binder;
        this.unbinder = unbinder;
    }
}
