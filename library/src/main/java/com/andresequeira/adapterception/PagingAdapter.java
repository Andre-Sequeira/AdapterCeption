package com.andresequeira.adapterception;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PagingAdapter<H extends PagingAdapter.PagingHandler> extends AdapterCeption {

    public static final String ADAPTER_TAG = "com.andresequeira.adapterception.PagingAdapter";

    @Nullable
    public static <H extends PagingAdapter.PagingHandler> PagingAdapter<H> getPagingAdapter(
            @NonNull RecyclerView.Adapter<?> adapter) {
        if (!(adapter instanceof AdapterCeption)) {
            return null;
        }
        return ((AdapterCeption<?>) adapter).getChildAtEnd(ADAPTER_TAG);
    }

    public static AdapterCeption<?> addPaging(@NonNull RecyclerView.Adapter<?> root) {
        return addPaging(root, null, null);
    }

    public static AdapterCeption<?> addPaging(@NonNull RecyclerView.Adapter<?> root,
                                              @NonNull ViewProvider<?> loadingViewProvider) {
        return addPaging(
                root,
                null,
                loadingViewProvider
        );
    }

    public static AdapterCeption<?> addPaging(@NonNull RecyclerView.Adapter<?> root, @NonNull PagingHandler handler) {
        return addPaging(
                root,
                handler,
                null
        );
    }

    public static AdapterCeption<?> addPaging(@NonNull RecyclerView.Adapter<?> root,
                                              @Nullable PagingHandler handler,
                                              @Nullable ViewProvider<?> loadingViewProvider) {
        AdapterCeption adapter;
        if (root instanceof AdapterCeption) {
            adapter = (AdapterCeption) root;
            if (!adapter.isRoot()) {
                throw new RuntimeException("Provided AdapterCeption must be root");
            }
        } else {
            adapter = AdapterCeption.adapt(root);
        }

        if (loadingViewProvider == null) {
            loadingViewProvider = new DefaultViewProvider();
        }

        final PagingAdapter pagingAdapter = new PagingAdapter<>(loadingViewProvider, handler);

        adapter.add(pagingAdapter);

        //when adapter's relative position is set to last, switch it to second from last
        //so the paging adapter always shows last
        if (adapter.getRelativePosition() == adapter.getChildrenSize()) {
            adapter.setRelativePosition(adapter.getRelativePosition() - 1);
        }

        return adapter;
    }

    private H pagingHandler;
    private LoadingOnScrollListener scrollListener;
    private final ViewProvider<?> viewProvider;
    private boolean isLoading;
    private Handler handler = new Handler();
    private Runnable r = new Runnable() {
        @Override
        public void run() {
            PagingAdapter.this.update();
        }
    };

    private PagingAdapter(@NonNull ViewProvider<?> viewProvider, @Nullable H handler) {
        this.viewProvider = viewProvider;
        this.pagingHandler = handler;

        setTag(ADAPTER_TAG);

        if (handler != null) {

            handler.adapter = this;

            scrollListener = new LoadingOnScrollListener();
            attachScrollToRv(getRecyclerView());
        }
    }

    @Override
    protected void onAttach(@NonNull RecyclerView recyclerView) {
        attachScrollToRv(recyclerView);
    }

    @Override
    protected void onDetach(@NonNull RecyclerView recyclerView) {
        detachScrollToRv(recyclerView);
    }

    @Override
    protected ViewProvider<?> newViewProvider() {
        return viewProvider;
    }

    @Override
    public void bind(@NonNull Object viewWrapper, int position) {
        //empty
    }

    @Override
    public int count() {
        return isLoading ? 1 : 0;
    }

    @Override
    protected DiffUtil.Callback onUpdate() {
        return new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return isLoading ? 0 : 1;
            }

            @Override
            public int getNewListSize() {
                return isLoading ? 1 : 0;
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return true;
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return true;
            }
        };
    }

    public void finishLoading() {
        setLoading(false);
    }

    public void setLoading(boolean loading) {
        if (isLoading == loading) {
            return;
        }
        isLoading = loading;
        handler.post(r);
    }

    public boolean isLoading() {
        return isLoading;
    }

    @Nullable
    public H getHandler() {
        return pagingHandler;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void attachScrollToRv(RecyclerView recyclerView) {
        if (scrollListener == null) {
            return;
        }

        recyclerView.setOnTouchListener(new View.OnTouchListener() {

            private float mStartY = -1;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        if (mStartY == -2) {
                            return false;
                        }
                        if (mStartY == -1) {
                            mStartY = event.getY();
                            break;
                        }
                        if (event.getY() > mStartY) {
                            mStartY = -2;
                            break;
                        }
                        RecyclerView rv = (RecyclerView) v;
                        if (!rv.canScrollVertically(1)) {
                            mStartY = -2;
                            checkLoad();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        mStartY = -1;
                        break;
                }
                return false;
            }
        });

        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (!(layoutManager instanceof LinearLayoutManager)) {
            throw new RuntimeException("RecyclerView's LayoutManager isn't a LinearLayoutManager");
        }

        scrollListener.setLayoutManager((LinearLayoutManager) layoutManager);

        recyclerView.addOnScrollListener(scrollListener);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void detachScrollToRv(RecyclerView recyclerView) {
        if (scrollListener == null) {
            return;
        }

        recyclerView.setOnTouchListener(null);
        recyclerView.removeOnScrollListener(scrollListener);

        scrollListener.setLayoutManager(null);
    }

    private void checkLoad() {
        if (scrollListener != null) {
            final RecyclerView recyclerView = getRecyclerView();
            boolean b = recyclerView != null && recyclerView.canScrollVertically(1);
            if (getItemCount() == 0) {
                return;
            }
            if (b) {
                return;
            }
            scrollListener.checkLoad();
        }
    }

    public static abstract class PagingHandler {

        int prefetchDistance;
        PagingAdapter adapter;

        public PagingHandler() {

        }

        public PagingHandler(int prefetchDistance) {
            this.prefetchDistance = prefetchDistance;
        }

        /**
         * @return true if it possible to load more
         */
        public boolean canLoad() {
            return true;
        }

        /**
         * @return true if it starts loading
         */
        public abstract boolean load();

        public final void finishLoading() {
            adapter.finishLoading();
        }

    }


    class LoadingOnScrollListener extends RecyclerView.OnScrollListener {

        private LinearLayoutManager layoutManager;

        LoadingOnScrollListener() {
        }

        void setLayoutManager(LinearLayoutManager layoutManager) {
            this.layoutManager = layoutManager;
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            if (dy <= 0) {
                return;
            }

            checkLoad();
        }

        private void checkLoad() {
            if (isLoading() || !pagingHandler.canLoad()) {
                return;
            }
            int count = layoutManager.getItemCount();
            int last = layoutManager.findLastVisibleItemPosition();

            if ((count - last - 1) <= pagingHandler.prefetchDistance) {
                if (pagingHandler.load()) {
                    setLoading(true);
                }
            }
        }
    }

    public static class DefaultViewProvider extends ViewProvider<View> {

        @NonNull
        @Override
        protected View newViewWrapper(@NonNull ViewGroup parent, int type) {
            return LayoutInflater.from(parent.getContext()).inflate(R.layout.view_default_page_loading, parent, false);
        }
    }

}
