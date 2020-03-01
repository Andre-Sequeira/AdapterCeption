package com.andresequeira.adapterception

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.lang.RuntimeException
import java.lang.RuntimeException
import kotlin.reflect.KClass


fun <VW> adapterCeption(block : DslAdapterCeptionBuilder<VW>.() -> Unit) : AdapterCeption<VW> {
    return DslAdapterCeptionBuilder<VW>()
        .apply(block)
        .build()
}

fun <VW> DslAdapterCeptionBuilder<VW>.viewProvider(block : DslViewProviderBuilder<VW>.() -> Unit)
        : AdapterCeption.ViewProvider<VW> {
    return DslViewProviderBuilder<VW>()
        .apply(block)
        .build()
}

fun test() {
    adapterCeption<TextView> {
        viewProvider {
            dslNewViewWrapper = { viewGroup, i ->
                TextView(viewGroup.context)
            }
        }
    }
}

fun <T> viewProvider1(
    getView: ((T) -> View)? = null,
    newViewWrapper: (ViewGroup, Int) -> T
) =
    object : AdapterCeption.ViewProvider<T>() {
        override fun newViewWrapper(parent: ViewGroup, viewType: Int): T {
            return newViewWrapper(parent, viewType)
        }

        override fun getView(viewWrapper: T): View {
            return getView?.invoke(viewWrapper) ?: super.getView(viewWrapper)
        }
    }

typealias DslGetView<VW> = (VW) -> View
typealias DslNewViewWrapper<VW> = (ViewGroup, Int) -> VW

class DslViewProviderBuilder<VW> {

    private var dslGetView: DslGetView<VW>? = null
    private var dslNewViewWrapper: DslNewViewWrapper<VW>? = null

    fun build() = DslViewProvider(
        dslGetView,
        dslNewViewWrapper ?: throw RuntimeException()
    )
}

class DslViewProvider<VW>(
    val dslGetView: DslGetView<VW>? = null,
    val dslNewViewWrapper: DslNewViewWrapper<VW>
) : AdapterCeption.ViewProvider<VW>(){

    override fun getView(viewWrapper: VW) =
        dslGetView?.invoke(viewWrapper) ?: super.getView(viewWrapper)

    override fun newViewWrapper(parent: ViewGroup, viewType: Int) =
        dslNewViewWrapper(parent, viewType)
}

typealias DslCount = () -> Int
typealias DslViewProvider1<VW> = () -> AdapterCeption.ViewProvider<VW>
typealias DslBinding<VW> = () -> Binding<VW>
typealias DslViewType = (Int) -> Int
typealias DslViewTypes<VW> = () -> Array<ViewType<VW>>

class DslAdapterCeptionBuilder<VW> {

    private var dslCount: DslCount? = null
    private var dslViewProvider: DslViewProvider1<VW>? = null
    private var dslBinding: DslBinding<VW>? = null
    private var dslViewType: DslViewType? = null
    private var dslViewTypes: DslViewTypes<*>? = null

    fun build() = DslAdapterCeption(
        dslCount ?: throw RuntimeException(),
        dslViewProvider ?: throw RuntimeException(),
        dslBinding ?: throw RuntimeException(),
        dslViewType,
        dslViewTypes
    )
}

open class DslAdapterCeption<VW>(
    val dslCount: DslCount,
    val dslViewProvider: DslViewProvider1<VW>,
    val dslBinding: DslBinding<VW>,
    val dslViewType: DslViewType? = null,
    val dslViewTypes: DslViewTypes<*>? = null
) : AdapterCeption<VW>() {

    override fun count() = dslCount()

    override fun newViewProvider() = dslViewProvider()

    override fun bind(viewWrapper: VW, position: Int) =
        dslBinding().binder.bind(viewWrapper, position)

    override fun unbind(viewWrapper: VW) = dslBinding().unbinder?.unbind(viewWrapper) ?: Unit

    override fun viewType(position: Int) = dslViewType?.invoke(position) ?: super.viewType(position)

    override fun viewTypes() = dslViewTypes?.invoke()
}

//fun <VW> adapterBuilder(): AdapterCeption<VW> {
//
//    return object : AdapterCeption<VW>() {
//        override fun count(): Int {
//            TODO("not implemented")
//        }
//
//        override fun newViewProvider(): ViewProvider<VW>? {
//
//        }
//
//        override fun bind(viewWrapper: VW, position: Int) {
//            TODO("not implemented")
//        }
//
//    }
//
//}

@Suppress("UNCHECKED_CAST")
fun <VW> RecyclerView.Adapter<*>.asCeption(): AdapterCeption<*>? = this as? AdapterCeption<VW>

val RecyclerView.Adapter<*>.asCeption: AdapterCeption<*>?
    get() = this as? AdapterCeption<*>

operator fun AdapterCeption<*>.get(index: Int): AdapterCeption<*> = getChild(index)

operator fun <T : AdapterCeption<*>> AdapterCeption<*>.get(tag: String): T? = getChild(tag)

operator fun <T : RecyclerView.Adapter<*>> AdapterCeption<*>.get(adapterClass: KClass<T>): T? =
    getChild(adapterClass.java)

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

class AggregatorAdapter(vararg children: RecyclerView.Adapter<*>) :
    InvisibleAdapterCeption(*children)

operator fun AdapterCeption<*>.plusAssign(other: RecyclerView.Adapter<*>) {
    add(other)
}

inline operator fun AdapterCeption<*>.invoke(apply: AdapterCeption<*>.() -> Unit): AdapterCeption<*> {
    apply()
    return this
}

fun <VH : RecyclerView.ViewHolder> RecyclerView.Adapter<VH>.adapt(): AdapterCeption<VH> =
    AdapterCeption.adapt(this)

fun AdapterCeption<*>.syncVisibility(): AdapterVisibility = AdapterVisibility.sync(this)

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

val RecyclerView.ViewHolder.offsetPosition
    get() = AdapterCeption.offsetAdapterPosition(this) ?: adapterPosition
