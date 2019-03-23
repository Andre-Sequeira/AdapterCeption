package com.andresequeira.adapterception;

import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class AdapterCeptionAdapter<VW extends RecyclerView.ViewHolder> extends AdapterCeption<VW>{

    private RecyclerView.Adapter<VW> delegate;

    public AdapterCeptionAdapter(RecyclerView.Adapter<VW> delegate) {
        this.delegate = delegate;
    }

    @Override
    protected int count() {
        return delegate.getItemCount();
    }

    @Override
    protected void onAttach(RecyclerView recyclerView) {
        delegate.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    protected void onDetach(RecyclerView recyclerView) {
        delegate.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public void bind(@NonNull VW viewWrapper, int position) {
        delegate.onBindViewHolder(viewWrapper, position);
    }

    @Override
    public void unbind(@NonNull VW viewWrapper) {
        delegate.onViewRecycled(viewWrapper);
    }

    @Override
    protected int viewType(int position) {
        return delegate.getItemViewType(position);
    }

    @Override
    protected ViewProvider<VW> newViewProvider() {
        return new ViewProvider<VW>() {

            @NonNull
            @Override
            protected VW newViewWrapper(@NonNull ViewGroup parent, int viewType) {
                return delegate.onCreateViewHolder(parent, viewType);
            }

            @NonNull
            @Override
            protected View getView(@NonNull VW vw) {
                return vw.itemView;
            }

            @Override
            protected void onAttachToWindow(VW vw) {
                delegate.onViewAttachedToWindow(vw);
            }

            @Override
            protected void onDetachFromWindow(VW vw) {
                delegate.onViewDetachedFromWindow(vw);
            }
        };
    }
}
