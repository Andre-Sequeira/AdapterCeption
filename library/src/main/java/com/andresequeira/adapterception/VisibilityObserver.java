package com.andresequeira.adapterception;

import androidx.annotation.NonNull;

/**
 * //TODO Create data change observables and extend this one to it
 * @param <A> Observable adapter
 */
public interface VisibilityObserver<A extends AdapterCeption<?>> {

    void onVisibilityChanged(@NonNull A adapter, boolean visible);

}
