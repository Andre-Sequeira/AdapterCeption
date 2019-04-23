package com.andresequeira.adapterception;

import android.util.Log;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class LoggingObserver extends RecyclerView.AdapterDataObserver {

    public static LoggingObserver apply(RecyclerView.Adapter adapter) {
        final LoggingObserver observer = new LoggingObserver(adapter.toString());
        adapter.registerAdapterDataObserver(observer);
        return observer;
    }

    private String tag = "LoggingObserver";

    public LoggingObserver() {
    }

    public LoggingObserver(String tag) {
        this.tag = tag;
    }

    @Override
    public void onChanged() {
        Log.d(tag, "onChanged()");
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount) {
        Log.d(tag, "onItemRangeChanged(positionStart = [" + positionStart + "], itemCount = [" + itemCount + "])");
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
        Log.d(tag, "onItemRangeChanged(positionStart = [" + positionStart + "], itemCount = [" + itemCount + "], payload = [" + payload + "])");
    }

    @Override
    public void onItemRangeInserted(int positionStart, int itemCount) {
        Log.d(tag, "onItemRangeInserted(positionStart = [" + positionStart + "], itemCount = [" + itemCount + "])");
    }

    @Override
    public void onItemRangeRemoved(int positionStart, int itemCount) {
        Log.d(tag, "onItemRangeRemoved(positionStart = [" + positionStart + "], itemCount = [" + itemCount + "])");
    }

    @Override
    public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        Log.d(tag, "onItemRangeMoved(fromPosition = [" + fromPosition + "], toPosition = [" + toPosition + "], itemCount = [" + itemCount + "])");
    }
}
