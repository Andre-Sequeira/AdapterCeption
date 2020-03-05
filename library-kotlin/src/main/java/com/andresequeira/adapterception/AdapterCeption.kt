package com.andresequeira.adapterception

import androidx.recyclerview.widget.RecyclerView
import java.lang.RuntimeException
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
fun <VW> RecyclerView.Adapter<*>.asCeption(): AdapterCeption<*>? = this as? AdapterCeption<VW>

val RecyclerView.Adapter<*>.asCeption: AdapterCeption<*>?
    get() = this as? AdapterCeption<*>

operator fun AdapterCeption<*>.get(index: Int): AdapterCeption<*> = getChild(index)

operator fun <T : AdapterCeption<*>> AdapterCeption<*>.get(tag: String): T? = getChild(tag)

operator fun <T : RecyclerView.Adapter<*>> RecyclerView.Adapter<*>.get(adapterClass: KClass<T>): T? =
    asCeption?.getChild(adapterClass.java)

inline fun <reified T : RecyclerView.Adapter<*>> AdapterCeption<*>.getSafe(): T? =
    getChild(T::class.java)

inline fun <reified T : RecyclerView.Adapter<*>> AdapterCeption<*>.get(): T =
    getChild(T::class.java) ?: throw RuntimeException(
        "No child of class: ${T::class.java}, exists."
    )

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

fun RecyclerView.Adapter<*>.pagingAdapter(): PagingAdapter<*>? =
    pagingAdapterH<PagingAdapter.PagingHandler>()

fun <H : PagingAdapter.PagingHandler> RecyclerView.pagingAdapterH(): PagingAdapter<H>? =
    this.adapter?.pagingAdapterH()

fun RecyclerView.pagingAdapter(): PagingAdapter<*>? = pagingAdapterH<PagingAdapter.PagingHandler>()

val RecyclerView.ViewHolder.offsetPosition
    get() = AdapterCeption.offsetAdapterPosition(this) ?: adapterPosition
