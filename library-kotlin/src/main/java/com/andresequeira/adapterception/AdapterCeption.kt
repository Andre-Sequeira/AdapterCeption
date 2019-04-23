package com.andresequeira.adapterception

import androidx.recyclerview.widget.RecyclerView

@Suppress("UNCHECKED_CAST")
fun <VW> RecyclerView.Adapter<*>.asCeption(): AdapterCeption<*>? = this as? AdapterCeption<VW>

val RecyclerView.Adapter<*>.asCeption: AdapterCeption<*>?
    get() = this as? AdapterCeption<*>

operator fun AdapterCeption<*>.get(index: Int): AdapterCeption<*> = getChild(index)

operator fun <T : AdapterCeption<*>> AdapterCeption<*>.get(tag: String): T? = getChild(tag)

val AdapterCeption<*>.lastChild: AdapterCeption<*>?
    get() {
        if (childrenSize == 0) {
            return null
        }
        return get(childrenSize - 1)
    }

infix operator fun RecyclerView.Adapter<*>.plus(other: RecyclerView.Adapter<*>): AdapterCeption<*> {
    if (this is AggregatorAdapter) {
        return this.add(other)
    }
    return AggregatorAdapter(this, other)
}

class AggregatorAdapter(vararg children: RecyclerView.Adapter<*>) : InvisibleAdapterCeption(*children)

operator fun AdapterCeption<*>.plusAssign(other: RecyclerView.Adapter<*>) {
    add(other)
}

inline operator fun AdapterCeption<*>.invoke(apply: AdapterCeption<*>.() -> Unit): AdapterCeption<*> {
    apply()
    return this
}

fun <VH : RecyclerView.ViewHolder> RecyclerView.Adapter<VH>.adapt(): AdapterCeption<VH> = AdapterCeption.adapt(this)

fun AdapterCeption<*>.syncVisibility() : AdapterVisibility = AdapterVisibility.sync(this)

fun RecyclerView.Adapter<*>.addPaging(): AdapterCeption<*> =
    PagingAdapter.addPaging(this)

infix fun RecyclerView.Adapter<*>.addPaging(loadingViewProvider: AdapterCeption.ViewProvider<*>): AdapterCeption<*> =
    PagingAdapter.addPaging(this, null, loadingViewProvider)

infix fun RecyclerView.Adapter<*>.addPaging(h: PagingAdapter.PagingHandler): AdapterCeption<*> =
    PagingAdapter.addPaging(this, h)

fun RecyclerView.Adapter<*>.addPaging(
    handler: PagingAdapter.PagingHandler, loadingViewProvider: AdapterCeption.ViewProvider<*>
): AdapterCeption<*> = PagingAdapter.addPaging(this, handler, loadingViewProvider)

fun <H : PagingAdapter.PagingHandler> RecyclerView.Adapter<*>.pagingAdapterH(): PagingAdapter<H>? =
    PagingAdapter.getPagingAdapter(this)

fun RecyclerView.Adapter<*>.pagingAdapter(): PagingAdapter<*>? = pagingAdapterH<PagingAdapter.PagingHandler>()
