package com.andresequeira.libraryKotlin

import androidx.recyclerview.widget.RecyclerView
import com.andresequeira.library.AdapterCeption

operator fun AdapterCeption<*>.get(index: Int): AdapterCeption<*> = getChild(index)

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
