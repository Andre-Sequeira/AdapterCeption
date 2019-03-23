package com.andresequeira.library;

import androidx.annotation.NonNull;

public interface Unbinder<VW> {
    void unbind(@NonNull VW viewWrapper);
}
