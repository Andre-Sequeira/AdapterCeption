package com.andresequeira.adapterception

import androidx.recyclerview.widget.RecyclerView

operator fun AdapterCeption<*>.get(index: Int): AdapterCeption<*> = getChild(index)

operator fun <T : AdapterCeption<*>> AdapterCeption<*>.get(tag : String) : T? = getChild(tag)


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