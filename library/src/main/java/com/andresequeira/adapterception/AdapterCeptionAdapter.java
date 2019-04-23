package com.andresequeira.adapterception;

import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

class AdapterCeptionAdapter<VW extends RecyclerView.ViewHolder> extends AdapterCeption<VW> {

    private RecyclerView.Adapter<VW> delegate;

    AdapterCeptionAdapter(RecyclerView.Adapter<VW> delegate) {
        this.delegate = delegate;
        DelegateDataObserver.apply(this, delegate);
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

    private static class DelegateDataObserver extends RecyclerView.AdapterDataObserver {

        private final RecyclerView.Adapter adapter;
        private DelegateDataObserver other;

        private boolean flag;

        static void apply(RecyclerView.Adapter adapter1, RecyclerView.Adapter adapter2) {

            final DelegateDataObserver observer1 = new DelegateDataObserver(adapter1);
            final DelegateDataObserver observer2 = new DelegateDataObserver(adapter2);

            observer1.other = observer2;
            observer2.other = observer1;

            adapter1.registerAdapterDataObserver(observer2);
            adapter2.registerAdapterDataObserver(observer1);
        }

        public DelegateDataObserver(RecyclerView.Adapter adapter) {
            this.adapter = adapter;
        }

        private boolean preNotify() {
            if (flag) {
                return false;
            }
            other.flag = true;
            return true;
        }

        private void posNotify() {
            other.flag = false;
        }

        @Override
        public void onChanged() {
            if (preNotify()) {
                adapter.notifyDataSetChanged();
                posNotify();
            }
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            if (preNotify()) {
                adapter.notifyItemRangeChanged(positionStart, itemCount);
                posNotify();
            }
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            if (preNotify()) {
                adapter.notifyItemRangeChanged(positionStart, itemCount, payload);
                posNotify();
            }
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            if (preNotify()) {
                adapter.notifyItemRangeInserted(positionStart, itemCount);
                posNotify();
            }
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            if (preNotify()) {
                adapter.notifyItemRangeRemoved(positionStart, itemCount);
                posNotify();
            }
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            if (preNotify()) {
                for (int i = 0; i < itemCount; i++) {
                    adapter.notifyItemMoved(fromPosition + i, toPosition + i);
                }
                posNotify();
            }
        }
    }
}
