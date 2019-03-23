package com.andresequeira.library;

import androidx.annotation.NonNull;

public interface Binder<VW> {
    void bind(@NonNull VW viewWrapper, int position);
}
