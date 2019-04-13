package com.andresequeira.adapterception

import androidx.recyclerview.widget.RecyclerView

operator fun AdapterCeption<*>.get(index: Int): AdapterCeption<*> = getChild(index)

operator fun <T : AdapterCeption<*>> AdapterCeption<*>.get(tag: String): T? = getChild(tag)

val AdapterCeption<*>.lastChild: AdapterCeption<*>?
    get() {
        if (childrenSize == 0) {
            return null
        }
        return get(childrenSize - 1)
    }

//TODO optimize adapter objects creation
infix operator fun RecyclerView.Adapter<*>.plus(other: RecyclerView.Adapter<*>): AdapterCeption<*> {
    return AdapterCeption<Any>(this, other)
}

operator fun AdapterCeption<*>.plusAssign(other: RecyclerView.Adapter<*>) {
    add(other)
}

inline operator fun AdapterCeption<*>.invoke(apply: AdapterCeption<*>.() -> Unit): AdapterCeption<*> {
    apply()
    return this
}

fun <VH : RecyclerView.ViewHolder> RecyclerView.Adapter<VH>.adapt(): AdapterCeption<VH> = AdapterCeption.adapt(this)

fun RecyclerView.Adapter<*>.addPaging(): PagingAdapter<*> =
    PagingAdapter.addPaging(this)

infix fun RecyclerView.Adapter<*>.addPaging(loadingViewProvider: AdapterCeption.ViewProvider<*>): PagingAdapter<*> =
    PagingAdapter.addPaging(this, null, loadingViewProvider)

infix fun <H : PagingAdapter.PagingHandler> RecyclerView.Adapter<*>.addPaging(h: H): PagingAdapter<H> =
    PagingAdapter.addPaging(this, h)

fun <H : PagingAdapter.PagingHandler> RecyclerView.Adapter<*>.addPaging(
    handler: H, loadingViewProvider: AdapterCeption.ViewProvider<*>
): PagingAdapter<H> = PagingAdapter.addPaging(this, handler, loadingViewProvider)

val AdapterCeption<*>.pagingAdapter: PagingAdapter<*>?
    get() = get(PagingAdapter.ADAPTER_TAG)
