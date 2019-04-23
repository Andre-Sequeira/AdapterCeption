package com.andresequeira.adapterception;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

public class AdapterVisibility implements VisibilityObserver<AdapterCeption<?>> {

    public static AdapterVisibility sync(@NonNull AdapterCeption<?> adapter) {
        return new AdapterVisibility(adapter);
    }

    private static Predicate defaultPredicate;

    private static Predicate getDefaultPredicate() {
        if (defaultPredicate == null) {
            defaultPredicate = new VisibleWithOnePredicate();
        }
        return defaultPredicate;
    }

    private AdapterCeption<?> syncAdapter;
    private LinkedHashSet<AdapterCeption<?>> observableAdapters = new LinkedHashSet<>();

    private Predicate predicate = getDefaultPredicate();

    public AdapterVisibility(@NonNull AdapterCeption<?> syncAdapter) {
        this.syncAdapter = syncAdapter;
    }

    public AdapterVisibility withChildren() {
        if (!syncAdapter.hasChildren()) {
            throw new RuntimeException(syncAdapter + " does not have children.");
        }
        with(syncAdapter.getChildren());
        return this;
    }

    public AdapterVisibility withBranches() {
        if (!syncAdapter.hasChildren()) {
            throw new RuntimeException(syncAdapter + " does not have children.");
        }
        with(syncAdapter.getBranches());
        return this;
    }

    /**
     * Sync every tree node with its corresponding children
     */
    public AdapterVisibility syncTree() {
        withChildren();
        syncAdapter.applyToChildren(new AdapterCeption.Action() {
            @Override
            public void apply(AdapterCeption<?> adapter) {
                if (!adapter.hasChildren()) {
                    return;
                }
                sync(adapter)
                        .visibleWhen(predicate)
                        .with(adapter.getChildren());
            }
        });
        return this;
    }

    public AdapterVisibility with(@NonNull Collection<AdapterCeption<?>> adapters) {
        if (adapters.isEmpty()) {
            throw new RuntimeException("Adapters cannot be empty.");
        }

        for (AdapterCeption<?> adapter : adapters) {
            if (observableAdapters.add(adapter)) {
                adapter.registerVisibilityObserver(this);
            }
        }
        updateVisibility();
        return this;
    }

    public AdapterVisibility with(@NonNull AdapterCeption<?>... adapters) {
        with(Arrays.asList(adapters));
        return this;
    }

    /**
     * Visible when every synced node is visible
     */
    public AdapterVisibility visibleWhenAll() {
        visibleWhen(new VisibleWithAllPredicate());
        return this;
    }

    /**
     * Visible when at least one of the synced nodes is visible
     */
    public AdapterVisibility visibleWhenOne() {
        visibleWhen(defaultPredicate);
        return this;
    }

    public AdapterVisibility visibleWhen(Predicate predicate) {
        this.predicate = predicate;
        updateVisibility();
        return this;
    }

    @Override
    public void onVisibilityChanged(@NonNull AdapterCeption<?> adapter, boolean visible) {
        updateVisibility();
    }

    private void updateVisibility() {
        syncAdapter.setVisibility(
                predicate.isVisible(observableAdapters)
        );
    }

    private static class VisibleWithOnePredicate implements Predicate {

        @Override
        public boolean isVisible(@NonNull Collection<AdapterCeption<?>> observableAdapters) {
            for (AdapterCeption<?> observableAdapter : observableAdapters) {
                if (observableAdapter.isVisible()) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class VisibleWithAllPredicate implements Predicate {

        @Override
        public boolean isVisible(@NonNull Collection<AdapterCeption<?>> observableAdapters) {
            for (AdapterCeption<?> observableAdapter : observableAdapters) {
                if (!observableAdapter.isVisible()) {
                    return false;
                }
            }
            return true;
        }
    }

    public interface Predicate {

        boolean isVisible(@NonNull Collection<AdapterCeption<?>> observableAdapters);

    }
}
