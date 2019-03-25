package com.andresequeira.adapterception;

import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

class AdapterCeptionAdapter<VW extends RecyclerView.ViewHolder> extends AdapterCeption<VW> {

    private RecyclerView.Adapter<VW> delegate;

    AdapterCeptionAdapter(RecyclerView.Adapter<VW> delegate) {
        this.delegate = delegate;
        delegate.registerAdapterDataObserver(new AdapterDataObserver());
    }

    @Override
    protected int count() {
        return delegate.getItemCount();
    }

    @Override
    protected void onAttach(@NonNull RecyclerView recyclerView) {
        delegate.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    protected void onDetach(@NonNull RecyclerView recyclerView) {
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
        return new ViewHolderViewProvider();
    }

    private class ViewHolderViewProvider extends ViewProvider<VW> {

        @NonNull
        @Override
        protected VW newViewWrapper(@NonNull ViewGroup parent, int viewType) {
            final VW vw = delegate.onCreateViewHolder(parent, viewType);
            vw.itemView.setTag(R.id.view_holder_tag, AdapterCeptionAdapter.this);
            return vw;
        }

        @NonNull
        @Override
        protected View getView(@NonNull VW vw) {
            return vw.itemView;
        }

        @Override
        protected void onAttachToWindow(@NonNull VW vw) {
            delegate.onViewAttachedToWindow(vw);
        }

        @Override
        protected void onDetachFromWindow(@NonNull VW vw) {
            delegate.onViewDetachedFromWindow(vw);
        }
    }

    private class AdapterDataObserver extends RecyclerView.AdapterDataObserver {
        @Override
        public void onChanged() {
            AdapterCeptionAdapter.this.notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            AdapterCeptionAdapter.this.notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            AdapterCeptionAdapter.this.notifyItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            AdapterCeptionAdapter.this.notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            AdapterCeptionAdapter.this.notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            for (int i = 0; i < itemCount; i++) {
                AdapterCeptionAdapter.this.notifyItemMoved(fromPosition + i, toPosition + i);
            }
        }
    }
}
