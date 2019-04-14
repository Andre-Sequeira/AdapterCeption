package com.andresequeira.adapterception;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.*;


/**
 * Created by andre on 1/26/18.
 * <p>
 * TODO: full coverage tests
 * TODO: Write proper documentation
 * TODO: Create Exception classes
 */

public class AdapterCeption<VW> extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements Binder<VW>, Unbinder<VW> {

    private static final String TAG = AdapterCeption.class.getSimpleName();

    private static final boolean DEBUG = false;

    public static final int POSITION_FIRST = 0;
    public static final int POSITION_LAST = -1;

    /**
     * This pointer is used to avoid creating new Lists for leafs.
     * This should never be populated, nor can it be.
     */
    private static final List<AdapterCeption<?>> EMPTY = Collections.emptyList();

    private static final Action attachAction = new Action() {
        @Override
        public void apply(AdapterCeption<?> adapter) {
            adapter.attach(adapter.getRecyclerView());
        }
    };
    private static final Action detachAction = new Action() {
        @Override
        public void apply(AdapterCeption<?> adapter) {
            adapter.detach(adapter.getRecyclerView());
        }
    };

    //region ADAPTER FIELDS
    private int relativePosition = POSITION_FIRST;
    private OffsetAdapterDataObserver observer;
    private ViewProvider<VW> viewProvider;
    private List<AdapterCeption<?>> children = EMPTY;
    private AdapterCeption parent;
    private int itemCount;
    private int offset;
    private String tag;
    private Map<Integer, ViewType<?>> viewTypes;
    private Binding<VW> binding = new Binding<>(new Binder<VW>() {
        @Override
        public void bind(@NonNull VW viewWrapper, int position) {
            AdapterCeption.this.bind(viewWrapper, position);
        }
    }, new Unbinder<VW>() {
        @Override
        public void unbind(@NonNull VW viewWrapper) {
            AdapterCeption.this.unbind(viewWrapper);
        }
    });
    //endregion

    //region ROOT FIELDS
    /**
     * The following fields are only used by the root Adapter.
     */
    private int rootCount = -1;
    private RecyclerView rv;
    private AdapterCeption[] binderPositions;
    private AdapterCeption[] binders;
    private SparseArray<ViewProvider> viewProviders;
    private RootAdapterDataObserver rootObserver;
    private NotifyWrapper notifier;
    //endregion

    //region CONSTRUCTOR

    public AdapterCeption(@NonNull List<RecyclerView.Adapter> children) {
        setAdapters(children);
    }

    public AdapterCeption(@NonNull RecyclerView.Adapter... children) {
        setAdapters(children);
    }
    //endregion

    //region ADAPTER

    @Override
    public final int getItemViewType(int position) {
        return rootGetViewProviderType(position);
    }

    @NonNull
    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        final ViewProvider viewProvider = viewProviders.get(type);
        return newViewHolder(parent, viewProvider, type);
    }

    @Override
    public final void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = getViewHolder(holder);
        viewHolder.unbinder = rootBind(viewHolder.viewWrapper, position);
    }

    //TODO
    @Override
    public final void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public final void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        getViewHolder(holder).unbind();
    }

    @Override
    public final int getItemCount() {
        return rootCount;
    }

    @Override
    public final void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        if (!isRoot()) {
            throw new RuntimeException(getThisAdapterCeptionMessage("is already attached."));
        }
        rootAttach(recyclerView);
    }

    @Override
    public final void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setRecyclerListener(
                new RecyclerView.RecyclerListener() {
                    @Override
                    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
                        if (holder instanceof AdapterCeption.ViewHolder) {
                            ((ViewHolder) holder).destroy();
                        }
                    }
                }
        );
        rootDetach(recyclerView);
    }

    @Override
    public final void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        getViewHolder(holder).attachToWindow();
    }

    @Override
    public final void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        getViewHolder(holder).detachFromWindow();
    }
    //endregion

    //region PUBLIC

    @NonNull
    public static RecyclerView.ViewHolder getViewHolder(@NonNull View view) {

        final Object tag = view
                .getTag(R.id.view_tag);

        if (!(tag instanceof RecyclerView.ViewHolder)) {
            throw new RuntimeException("Invalid view: " + view);
        }
        return (RecyclerView.ViewHolder) tag;
    }

    @NonNull
    public static <VW extends RecyclerView.ViewHolder> AdapterCeption<VW> adapt(@NonNull RecyclerView.Adapter<VW> adapter) {
        return new AdapterCeptionAdapter<>(adapter);
    }

    @Nullable
    public static Integer offsetAdapterPosition(RecyclerView.ViewHolder vh) {
        final AdapterCeption tag = (AdapterCeption) vh.itemView.getTag(R.id.view_holder_tag);
        if (tag == null) {
            return null;
        }
        return vh.getAdapterPosition() - tag.offset;
    }

    @Nullable
    public final RecyclerView getRecyclerView() {
        return getRoot().rv;
    }

    @NonNull
    public final List<AdapterCeption<?>> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public final int getChildrenSize() {
        return children.size();
    }

    public final boolean isRoot() {
        return getParent() == null;
    }

    @NonNull
    public final AdapterCeption getRoot() {
        final AdapterCeption<?> parent = getParent();
        if (parent == null) {
            return this;
        }
        return parent.getRoot();
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public final <VB extends AdapterCeption<?>> VB getChild(int index) {
        return (VB) children.get(index);
    }

    public final boolean isRootAttached() {
        return getRoot().isAttached();
    }

    public final int getRelativePosition() {
        return relativePosition;
    }

    public final int getStartingPosition() {
        return offset;
    }

    public final int getEndingPosition() {
        return offset + itemCount - 1;
    }

    public final int getPosition(int offsettedPosition) {
        if (offsettedPosition < 0 || offsettedPosition > itemCount) {
            throw new IndexOutOfBoundsException();
        }
        return offset + offsettedPosition;
    }

    public final int getLevel() {
        if (isRoot()) {
            return 0;
        }
        return getParent().getLevel() + 1;
    }

    public final boolean hasChildren() {
        return children != EMPTY;
    }

    @Nullable
    public final AdapterCeption<?> getParent() {
        return parent;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public final <T extends AdapterCeption<VW>> T setTag(@NonNull String tag) {
        this.tag = tag;
        return (T) this;
    }

    @Nullable
    public final String getTag() {
        return tag;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public final <T extends AdapterCeption<?>> T getChild(@NonNull String tag) {
        if (this.tag != null && this.tag.equals(tag)) {
            return (T) this;
        }
        for (AdapterCeption<?> child : children) {
            final AdapterCeption<?> child1 = child.getChild(tag);
            if (child1 != null) {
                return (T) child1;
            }
        }
        return null;
    }

    public final void setAdapters(@NonNull RecyclerView.Adapter... adapters) {
        setAdapters(Arrays.asList(adapters));
    }

    public final void setAdapters(@Nullable Collection<? extends RecyclerView.Adapter> adapters) {
        this.children.clear();
        if (adapters == null) {
            this.children = EMPTY;
            getRoot().rootRecount();
            return;
        }
        add(adapters);
    }

    @NonNull
    public final AdapterCeption<VW> add(@NonNull RecyclerView.Adapter... adapters) {
        add(Arrays.asList(adapters));
        return this;
    }

    public final void add(@NonNull Collection<? extends RecyclerView.Adapter> adapters) {
        for (RecyclerView.Adapter AdapterCeption : adapters) {
            addAdapterCeption(AdapterCeption);
        }
        if (relativePosition == POSITION_LAST) {
            relativePosition = children.size();
        }
        getRoot().rootRecount();
    }

    /**
     * @param position
     * @return
     */
    @NonNull
    public final AdapterCeption<VW> setRelativePosition(int position) {
        final int size = children.size();
        if (position > size) {
            throw new RuntimeException(
                    String.format(
                            "Cannot set relative position to %d because AdapterCeption: %s has children size of %d."
                            , position,
                            getClass().getName(),
                            size
                    )
            );
        }
        this.relativePosition = position < 0
                ? (size > 0 ? size : POSITION_LAST)
                : position;

        getRoot().rootRecount();

        return this;
    }

    @NonNull
    public final String getInfo() {
        return getInfo(new StringBuilder());
    }

    public final void update() {
        if (!isRootAttached()) {
            return;
        }

        DiffUtil.Callback update = onUpdate();
        final AdapterCeption root = getRoot();
        if (update == null) {
            root.rootRecount();
            root.notifyDataSetChanged();
            return;
        }

        final DiffUtil.DiffResult diffResult = processUpdates(update);
        updateBinders(update.getOldListSize(), update.getNewListSize());
        getRoot().unregisterAdapterDataObserver(getRoot().rootObserver);
        dispatchUpdates(diffResult);
        getRoot().registerAdapterDataObserver(getRoot().rootObserver);
    }
    //endregion

    //region INTERNAL

    /**
     * Sets the count and offset of the given AdapterCeption node and all its children.
     *
     * @param adapter The AdapterCeption to be recounted;
     * @param offset  The base offset of the given AdapterCeption;
     * @return The count of the AdapterCeption.
     */
    private static void recount(AdapterCeption<?> adapter, int offset, LinkedList<AdapterCeption> adapters) {
        int count = 0;
        if (!adapter.hasChildren()) {
            count += updateCount(adapter, count, offset, adapters);
        } else {
            for (int i = 0; i < adapter.children.size(); i++) {
                if (adapter.relativePosition == i) {
                    adapter.offset = count + offset;
                    count = updateCount(adapter, count, offset, adapters);
                }
                AdapterCeption child = adapter.children.get(i);
                recount(child, count + offset, adapters);
                count += child.rootCount;
            }
            if (adapter.relativePosition == adapter.children.size()) {
                count = updateCount(adapter, count, offset, adapters);
            }
        }
        adapter.rootCount = count;
    }

    private static int updateCount(AdapterCeption<?> adapter, int count, int offset, LinkedList<AdapterCeption> adapters) {
        adapters.add(adapter);
        adapter.offset = count + offset;
        adapter.itemCount = adapter.count();
        return count + adapter.itemCount;
    }

    /**
     * Recounts the root AdapterCeption.
     */
    private void rootRecount() {
        if (!isAttached()) {
            return;
        }

        final LinkedList<AdapterCeption> binders = new LinkedList<>();

        recount(this, 0, binders);

        this.binderPositions = new AdapterCeption[rootCount];
        this.binders = binders.toArray(new AdapterCeption[0]);

        final Iterator<AdapterCeption> iterator = binders.iterator();
        for (int i = 0; iterator.hasNext(); ) {
            final AdapterCeption next = iterator.next();
            for (int j = 0; j < next.itemCount; j++) {
                this.binderPositions[i++] = next;
            }
        }

        for (AdapterCeption<?> adapter : binders) {
            final ViewProvider<?> viewProvider = adapter.getViewProvider();
            if (viewProvider != null) {
                viewProviders.put(viewProvider.defaultViewType(), viewProvider);
            }
            final Map<Integer, ViewType<?>> viewTypes = adapter.getViewTypes();
            if (viewTypes != null) {
                for (ViewType<?> value : viewTypes.values()) {
                    viewProviders.put(value.viewType, value.viewProvider);
                }
            }
        }
    }

    private void addAdapterCeption(RecyclerView.Adapter adapter) {
        if (this.children == EMPTY) {
            this.children = new LinkedList<>();
        }
        AdapterCeption adapterCeption;
        if (adapter instanceof AdapterCeption) {
            adapterCeption = (AdapterCeption) adapter;
        } else {
            adapterCeption = adapt(adapter);
        }
        adopt(adapterCeption);
    }

    private void adopt(AdapterCeption adapter) {
        if (!adapter.isRoot()) {
            throw new RuntimeException(getThisAdapterCeptionMessage("already has a parent."));
        }
        if (adapter.isAttached()) {
            throw new RuntimeException(getThisAdapterCeptionMessage("already is attached to a RecyclerView."));
        }
        adapter.registerAdapterDataObserver(observer = new OffsetAdapterDataObserver(adapter));
        this.children.add(adapter);
        adapter.setParent(this);
    }

    private void setParent(AdapterCeption binder) {
        this.parent = binder;
    }

    private void rootAttach(RecyclerView recyclerView) {
        this.rv = recyclerView;
        viewProviders = new SparseArray<>();
        attach(rv);
        rootRecount();
        registerAdapterDataObserver(rootObserver = new RootAdapterDataObserver());
        notifier = new NotifyWrapper();
    }

    private void attach(RecyclerView recyclerView) {
        applyToChildren(attachAction);
        onAttach(recyclerView);
    }

    private void rootDetach(RecyclerView recyclerView) {
        detach(recyclerView);
        unregisterAdapterDataObserver(rootObserver);
        rv = null;

    }

    private void detach(RecyclerView recyclerView) {
        applyToChildren(detachAction);
        onDetach(recyclerView);
    }

    private boolean isAttached() {
        return rv != null;
    }

    private DiffUtil.DiffResult processUpdates(@NonNull DiffUtil.Callback callback) {
        return calculateDiff(callback);
    }

    private void dataSetChanged() {
        getRoot().rootRecount();
    }

    private void itemRangeInserted(int itemCount, AdapterCeption adapter) {
        adapter.updateBinders(adapter.itemCount, adapter.itemCount + itemCount);
    }

    private void itemRangeRemoved(int positionStart, int itemCount) {
        final AdapterCeption adapter = binderPositions[positionStart];
        adapter.updateBinders(adapter.itemCount, adapter.itemCount - itemCount);
    }

    private void updateBinders(int oldSize, int newSize) {
        int diff = newSize - oldSize;
        if (diff == 0) {
            return;
        }
        final AdapterCeption root = getRoot();
        final int newRootCount = root.rootCount + diff;
        AdapterCeption[] binderPositions = new AdapterCeption[newRootCount];

        final int bottomIndex = offset + newSize;
        final int oldBottomIndex = offset + oldSize;

        System.arraycopy(root.binderPositions, 0, binderPositions, 0, offset);
        Arrays.fill(binderPositions, offset, bottomIndex, this);
        System.arraycopy(root.binderPositions, oldBottomIndex, binderPositions, bottomIndex, root.rootCount - oldBottomIndex);

        //find this binder index
        int index = -1;
        for (int i = 0; i < root.binders.length; i++) {
            if (root.binders[i] == this) {
                index = i + 1;
                break;
            }
        }

        //update every binder offset to the right of this
        for (int i = index; i < root.binders.length; i++) {
            root.binders[i].offset += diff;
        }

        itemCount += diff;
        root.rootCount += diff;

        root.binderPositions = binderPositions;
    }

    private Map<Integer, ViewType<?>> getViewTypes() {
        if (viewTypes != null) {
            return viewTypes;
        }
        ViewType<?>[] types = viewTypes();
        if (types == null) {
            return null;
        }
        if (viewTypes == null) {
            viewTypes = new HashMap<>();
        }
        for (ViewType<?> viewType : types) {
            viewTypes.put(viewType.viewType, viewType);
        }
        return viewTypes;
    }

    final <T> Unbinder<T> rootBind(T viewWrapper, int position) {
        final AdapterCeption<?> adapter = rootGetAdapter(position);
        final int offsettedPosition = position - adapter.offset;
        final Binding<T> binding = adapter.rootGetBinding(adapter, offsettedPosition);
        binding.binder.bind(viewWrapper, offsettedPosition);
        return binding.unbinder;
    }

    @SuppressWarnings("unchecked")
    private <T> AdapterCeption<T> rootGetAdapter(int position) {
        return binderPositions[position];
    }

    @SuppressWarnings({"unchecked"})
    private <T> Binding<T> rootGetBinding(AdapterCeption<?> adapter, int position) {
        if (adapter.viewTypes != null) {
            final int type = adapter.viewType(position);
            if (type != 0) {
                final ViewType<?> viewType = adapter.viewTypes.get(type);
                if (viewType != null) {
                    return (Binding<T>) viewType.binding;
                }
            }
        }
        return (Binding<T>) adapter.binding;
    }

    private int rootGetViewProviderType(int position) {
        final AdapterCeption<Object> adapter = rootGetAdapter(position);
        final int type = adapter.viewType(position - adapter.offset);
        if (type == 0) {
            return adapter.getViewProvider().defaultViewType();
        }
        final ViewProvider vp = viewProviders.get(type);
        if (vp == null) {
            viewProviders.put(type, adapter.getViewProvider());
        }
        return type;
    }

    private ViewHolder getViewHolder(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof ViewHolder) {
            return (ViewHolder) viewHolder;
        }
        return (ViewHolder) viewHolder.itemView.getTag(R.id.view_tag);
    }

    private <T> RecyclerView.ViewHolder newViewHolder(ViewGroup parent, ViewProvider<T> provider, int type) {
        T viewWrapper = provider.newViewWrapper(parent, type);

        final ViewHolder<T> vh = new ViewHolder<>(
                viewWrapper,
                provider
        );

        vh.itemView.setTag(R.id.view_tag, vh);

        if (viewWrapper instanceof RecyclerView.ViewHolder) {
            ((RecyclerView.ViewHolder) viewWrapper).itemView.setTag(R.id.view_tag, vh);
            return (RecyclerView.ViewHolder) viewWrapper;
        }

        return vh;
    }

    private String getThisAdapterCeptionMessage(String s2) {
        return "This AdapterCeption: " + getClass().getName() + ", " + s2;
    }

    //region FOR TESTING
    private String getInfo(final StringBuilder builder) {
        final Action action = new Action() {
            @Override
            public void apply(AdapterCeption<?> vb) {
                final int level = vb.getLevel();
                for (int i = 0; i < level; i++) {
                    builder.append("    ");
                }
                builder.append("----\n");
            }
        };
        applyRelative(
                new Action() {
                    @Override
                    public void apply(AdapterCeption<?> vb) {
                        final int level = vb.getLevel();
                        for (int i = 0; i < level; i++) {
                            builder.append("    ");
                        }
                        builder.append("L");
                        builder.append(level);
                        builder.append(" - ");
                        builder.append(vb.toString());
                        builder.append("\n");
                    }
                },
                action,
                action
        );
        return builder.toString();
    }

    int getDynamicCount() {
        int count = 0;
        for (AdapterCeption<?> child : children) {
            count += child.getDynamicCount();
        }
        return count + itemCount;
    }

    int getDynamicOffset() {
        return getDynamicOffset(null);
    }

    int getDynamicOffset(AdapterCeption child) {
        int offset = 0;

        final int childIndex = child != null
                ? children.indexOf(child)
                : -2;

        final int to = childIndex >= 0
                ? childIndex
                : relativePosition;

        for (int i = 0; i < to; i++) {
            AdapterCeption c = children.get(i);
            offset += c.getDynamicCount();
        }

        if (relativePosition <= childIndex) {
            offset += itemCount;
        }

        if (!isRoot()) {
            offset += parent.getDynamicOffset(this);
        }
        return offset;
    }
    //endregion

    //endregion

    //region OVERRIDABLE
    protected int viewType(int position) {
        return 0;
    }

    @Nullable
    protected ViewType<?>[] viewTypes() {
        return null;
    }

    protected int count() {
        return 0;
    }

    @Nullable
    protected ViewProvider<VW> getViewProvider() {
        if (viewProvider == null) {
            viewProvider = newViewProvider();
        }
        return viewProvider;
    }

    @Nullable
    protected ViewProvider<VW> newViewProvider() {
        return null;
    }

    @Override
    public void bind(@NonNull VW viewWrapper, int position) {

    }

    @Override
    public void unbind(@NonNull VW viewWrapper) {

    }

    protected void onAttach(@NonNull RecyclerView recyclerView) {

    }

    protected void onDetach(@NonNull RecyclerView recyclerView) {

    }

    @Nullable
    protected DiffUtil.Callback onUpdate() {
        return null;
    }

    protected void dispatchUpdates(@NonNull DiffUtil.DiffResult diffResult) {
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    protected DiffUtil.DiffResult calculateDiff(@NonNull DiffUtil.Callback callback) {
        return DiffUtil.calculateDiff(callback);
    }

    @NonNull
    @Override
    public String toString() {
        return getClass().getSimpleName() +
                ", tag: " + tag +
                ", total count: " + rootCount +
                ", itemCount: " + count() +
                ", relativePosition: " + relativePosition +
                ", offset: " + offset
                ;
    }
    //endregion

    //region ACTION
    void apply(Action action) {
        action.apply(this);
        applyToChildren(action);
    }

    private void applyRelative(Action action, Action before, Action after) {
        if (before != null) {
            before.apply(this);
        }

        int to = relativePosition;
        final int size = children.size();
        if (to > size) {
            to = size;
        }
        for (int i = 0; i < to; i++) {
            getChild(i).applyRelative(action, before, after);
        }

        if (action != null) {
            action.apply(this);
        }

        int from = relativePosition;
        if (from < 0) {
            from = 0;
        }
        for (int i = from; i < size; i++) {
            getChild(i).applyRelative(action, before, after);
        }

        if (after != null) {
            after.apply(this);
        }
    }

    private void applyToChildren(Action action) {
        applyToChildrenLeft(action, null);
    }

    private void applyToChildrenLeft(Action action, @Nullable AdapterCeption toTheLeftOf) {
        if (!hasChildren()) {
            return;
        }

        int to = toTheLeftOf != null
                ? children.indexOf(toTheLeftOf)
                : children.size();

        applyToChildrenLeft(action, to);
    }

    private void applyToChildrenLeft(Action action, int to) {
        if (to > children.size()) {
            to = children.size();
        }
        for (int i = 0; i < to; i++) {
            getChild(i).apply(action);
        }
    }

    //endregion

    @SuppressWarnings("unchecked")
    private NotifyWrapper startNotify() {
        final AdapterCeption root = getRoot();
        root.notifier.notifiedAdapter = this;
        return root.notifier;
    }

    /**
     * WIP: trying somethings out.
     * Should set the same strategy for every notify eventually.
     * Possibly merge OffsetDataObserver and RootDataObserver with this class.
     */
    private class NotifyWrapper {

        private AdapterCeption notifiedAdapter;

        private NotifyWrapper notifyItemRangeInserted(int positionStart, int itemCount) {
            AdapterCeption.this.notifyItemRangeInserted(positionStart + notifiedAdapter.offset, itemCount);
            return this;
        }

        private void finish() {
            notifiedAdapter = null;
        }

    }

    public static abstract class ViewProvider<ViewWrapper> {

        private int type;

        public int defaultViewType() {
            if (type != 0) {
                return type;
            }
            return type = ViewCompat.generateViewId();
        }

        @NonNull
        protected abstract ViewWrapper newViewWrapper(@NonNull ViewGroup parent, int viewType);

        @NonNull
        protected View getView(@NonNull ViewWrapper viewWrapper) {
            if (viewWrapper instanceof View) {
                return (View) viewWrapper;
            }
            throw new RuntimeException("If ViewWrapper is not a View then getView(ViewWrapper) must be implemented.");
        }

        protected void onAttachToWindow(@NonNull ViewWrapper viewWrapper) {

        }

        protected void onDetachFromWindow(@NonNull ViewWrapper viewWrapper) {

        }

        protected void onDestroy(@NonNull ViewWrapper viewWrapper) {

        }

        public int getAdapterPosition(AdapterCeption<ViewWrapper> adapter, ViewWrapper viewWrapper) {
            return AdapterCeption.getViewHolder(getView(viewWrapper)).getAdapterPosition() - adapter.offset;
        }
    }

    private static class ViewHolder<VW> extends RecyclerView.ViewHolder {

        private VW viewWrapper;
        private ViewProvider<VW> provider;
        private Unbinder<VW> unbinder;

        public ViewHolder(VW viewWrapper, ViewProvider<VW> provider) {
            super(provider.getView(viewWrapper));
            this.viewWrapper = viewWrapper;
            this.provider = provider;
        }

        void unbind() {
            if (unbinder == null) {
                return;
            }
            unbinder.unbind(viewWrapper);
            unbinder = null;
        }

        void detachFromWindow() {
            provider.onDetachFromWindow(viewWrapper);
        }

        void attachToWindow() {
            provider.onAttachToWindow(viewWrapper);
        }

        public void destroy() {
            provider.onDestroy(viewWrapper);
            provider = null;
        }
    }

    interface Action {
        void apply(AdapterCeption<?> adapter);
    }

    private static class OffsetAdapterDataObserver extends RecyclerView.AdapterDataObserver {

        private AdapterCeption adapter;

        public OffsetAdapterDataObserver(AdapterCeption adapter) {
            this.adapter = adapter;
        }

        @Override
        public void onChanged() {
            adapter.getRoot().notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            adapter.getRoot().notifyItemRangeChanged(positionStart + adapter.offset, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            adapter.getRoot().notifyItemRangeChanged(positionStart + adapter.offset, itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            adapter.startNotify()
                    .notifyItemRangeInserted(positionStart, itemCount)
                    .finish();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            adapter.getRoot().notifyItemRangeRemoved(positionStart + adapter.offset, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            for (int i = 0; i < itemCount; i++) {
                adapter.getRoot().notifyItemMoved(fromPosition + i + adapter.offset, toPosition + i + adapter.offset);
            }
        }
    }

    private class RootAdapterDataObserver extends RecyclerView.AdapterDataObserver {

        @Override
        public void onChanged() {
            dataSetChanged();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            itemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            itemRangeInserted(itemCount, notifier.notifiedAdapter != null ? notifier.notifiedAdapter : AdapterCeption.this);
        }
    }

//    private static class OffsetListUpdateCallback implements ListUpdateCallback {
//
//        ListUpdateCallback callback;
//        int offset;
//
//        OffsetListUpdateCallback(RecyclerView.Adapter adapter) {
//            callback = new AdapterListUpdateCallback(adapter);
//        }
//
//        OffsetListUpdateCallback setOffset(int offset) {
//            this.offset = offset;
//            return this;
//        }
//
//        @Override
//        public void onInserted(int position, int count) {
//            callback.onInserted(position + offset, count);
//        }
//
//        @Override
//        public void onRemoved(int position, int count) {
//            callback.onRemoved(position + offset, count);
//        }
//
//        @Override
//        public void onMoved(int fromPosition, int toPosition) {
//            callback.onMoved(fromPosition + offset, toPosition + offset);
//        }
//
//        @Override
//        public void onChanged(int position, int count, Object payload) {
//            callback.onChanged(position + offset, count, payload);
//        }
//    }
//

}