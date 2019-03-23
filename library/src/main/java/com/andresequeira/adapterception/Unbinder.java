package com.andresequeira.adapterception;

import androidx.annotation.NonNull;

public interface Unbinder<VW> {
    void unbind(@NonNull VW viewWrapper);
}
