package com.andresequeira.adapterceptionsampleapp

import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andresequeira.adapterception.AdapterCeption
import com.andresequeira.adapterception.ViewType
import com.andresequeira.adapterception.get
import com.andresequeira.adapterception.plus
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.button_custom.view.*
import kotlinx.android.synthetic.main.item_country_capital.view.*
import kotlinx.android.synthetic.main.layout_search.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG_SEARCH_ADAPTER = "SearchAdapterTag"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recycler.layoutManager = LinearLayoutManager(this)
        val headerAdapter = HeaderAdapter()
        val countryCapitalAdapter = CountryCapitalAdapter(headerAdapter.listSize())
        val buttonAdapter = ButtonAdapter(
            listOf(
                "My view type is: ",
                ", mine is: ",
                "and mine: "
            )
        )
        val regularAdapter = RegularButtonAdapter(
            listOf(
                "I'm a view from a regular Adapter",
                "So am I, but with a different view type",
                "Cool right?"
            )
        )
        val searchAdapter = SearchAdapter(countryCapitalAdapter.filter())
            .setTag<SearchAdapter>(TAG_SEARCH_ADAPTER)
        recycler.adapter = headerAdapter + searchAdapter + countryCapitalAdapter +
                HeaderAdapter("--- Separator --- Different example bellow --- Separator ---") +
                buttonAdapter + regularAdapter
    }

    override fun onBackPressed() {
        val adapterCeption = recycler.adapter as AdapterCeption<*>
        val searchAdapter : SearchAdapter = adapterCeption[TAG_SEARCH_ADAPTER] ?: return super.onBackPressed()
        if (searchAdapter.c == 0) {
            return searchAdapter.toggleC()
        }
        super.onBackPressed()
    }
}

private fun CountryCapitalAdapter.filter(): (s: String) -> Unit = { s ->
    old.addAll(new)
    new.clear()
    new.addAll(list.filter { it.country.contains(s, true) || it.capital.contains(s, true) })
    update()
    old.clear()
}

class CountryCapitalAdapter(val listSize: (s: Int) -> Unit) : AdapterCeption<View>() {

    val list = CountryCapitalProvider.capitalList
    val old = mutableListOf<CountryCapital>()
    val new = mutableListOf<CountryCapital>()

    init {
        new.addAll(list)
        listSize(new.size)
    }

    override fun bind(view: View, position: Int) {
        view.textCountry.text = new[position].country
        view.textCity.text = new[position].capital
        view.textValue.text = "${(position + 1)}"
        view.setBackgroundColor(Color.TRANSPARENT)
    }

    override fun viewType(position: Int): Int = when {
        position == 0 -> R.id.view_type_country_capital_adapter_3
        position % 10 == 0 -> R.id.view_type_country_capital_adapter_2
        position % 5 == 0 -> R.id.view_type_country_capital_adapter_1
        else -> 0
    }

    val vp = object : ViewProvider<View>() {

        override fun newViewWrapper(parent: ViewGroup, viewType: Int): View {
            return newCapitalView(parent)
        }

    }

    override fun viewTypes(): Array<ViewType<*>> {
        return arrayOf(
            ViewType(R.id.view_type_country_capital_adapter_1, vp) { viewWrapper, position ->
                bind(viewWrapper, position)
                viewWrapper.setBackgroundColor(Color.LTGRAY)
            },
            ViewType(R.id.view_type_country_capital_adapter_2, vp) { viewWrapper, position ->
                bind(viewWrapper, position)
                viewWrapper.setBackgroundColor(ContextCompat.getColor(viewWrapper.context, R.color.colorPrimary))
            },
            ViewType(R.id.view_type_country_capital_adapter_3,
                object : ViewProvider<View>() {

                    override fun newViewWrapper(parent: ViewGroup, viewType: Int): View {
                        return newCapitalView(parent)
                    }

                },
                { viewWrapper, position ->
                    bind(viewWrapper, position)
                    viewWrapper.setBackgroundColor(
                        ContextCompat.getColor(
                            viewWrapper.context,
                            R.color.notification_icon_bg_color
                        )
                    )
                    viewWrapper.setOnClickListener {
                        Toast.makeText(viewWrapper.context, "Toast", Toast.LENGTH_SHORT).show()
                    }
                }
                , { viewWrapper ->
                    viewWrapper.setOnClickListener(null)
                }
            )
        )
    }

    override fun count(): Int = new.size

    override fun newViewProvider(): ViewProvider<View> {
        return object : ViewProvider<View>() {

            override fun newViewWrapper(parent: ViewGroup, viewType: Int): View {
                return newCapitalView(parent)
            }

        }
    }

    override fun onUpdate(): DiffUtil.Callback? {
        listSize(new.size)
        return object : DiffUtil.Callback() {

            override fun getOldListSize(): Int = old.size

            override fun getNewListSize(): Int = new.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                old[oldItemPosition] === new[newItemPosition]

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                old[oldItemPosition] == new[newItemPosition]

        }
    }

    private fun newCapitalView(parent: ViewGroup): View {
        return LayoutInflater.from(parent.context)
            .inflate(R.layout.item_country_capital, parent, false)
    }
}

class HeaderAdapter(val text: String? = null) : AdapterCeption<TextView>() {

    var listSize: Int = 0

    fun listSize(): (s: Int) -> Unit = { size ->
        listSize = size
        update()
    }

    override fun bind(viewWrapper: TextView, position: Int) {
        viewWrapper.text = text ?: "Country | Capital | Count: $listSize"
    }

    override fun count(): Int = 1

    override fun newViewProvider(): ViewProvider<TextView> {
        return object : ViewProvider<TextView>() {
            override fun newViewWrapper(parent: ViewGroup, viewType: Int): TextView {
                val textView = TextView(parent.context)
                textView.setPadding(16.px, 16.px, 16.px, 8.px)
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                return textView
            }

        }
    }

    override fun onUpdate(): DiffUtil.Callback? {
        return object : DiffUtil.Callback() {
            override fun getOldListSize() = 1

            override fun getNewListSize() = 1

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = true

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = false

        }
    }

}

class SearchAdapter(val filter: (String) -> Unit) : AdapterCeption<FilterLayout>() {

    var c = 1

    fun toggleC() {
        c = !c
        update()
    }

    override fun bind(viewWrapper: FilterLayout, position: Int) {
        viewWrapper.bind(filter)
        viewWrapper.textFilter.setOnClickListener {
            toggleC()
        }
    }

    operator fun Int.not(): Int = if (this != 0) 0 else 1

    override fun unbind(viewWrapper: FilterLayout) {
        viewWrapper.unbind()
        viewWrapper.textFilter.setOnClickListener(null)
    }

    override fun count(): Int = c

    override fun newViewProvider(): ViewProvider<FilterLayout> {
        return object : ViewProvider<FilterLayout>() {
            override fun newViewWrapper(parent: ViewGroup, viewType: Int) = FilterLayout.inflate(parent)

            override fun getView(viewWrapper: FilterLayout) = viewWrapper.containerView

        }
    }

}

class FilterLayout(override var containerView: View) : LayoutContainer {

    companion object {
        fun inflate(parent: ViewGroup): FilterLayout {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_search, parent, false)
            return FilterLayout(view)
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            filter?.invoke(s.toString())
        }
    }

    var filter: ((String) -> Unit)? = null

    fun bind(filter: (String) -> Unit) {
        editTextFilter.addTextChangedListener(textWatcher)
        this.filter = filter
    }

    fun unbind() {
        editTextFilter.removeTextChangedListener(textWatcher)
        this.filter = null
    }
}

class RegularButtonAdapter(var texts: List<String>) : RecyclerView.Adapter<RegularButtonViewHolder>() {

    override fun getItemCount() = texts.size

    override fun getItemViewType(position: Int): Int = when (position) {
        0 -> R.id.view_type_regular_button_adapter_1
        1 -> R.id.view_type_regular_button_adapter_2
        2 -> R.id.view_type_regular_button_adapter_3
        else -> 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RegularButtonViewHolder.new(parent, viewType)

    override fun onBindViewHolder(holder: RegularButtonViewHolder, position: Int) {
        holder.update(texts[position])
    }

}

class RegularButtonViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {
        fun new(parent: ViewGroup, viewType: Int): RegularButtonViewHolder {
            var color: Int = Color.BLACK
            when (viewType) {
                R.id.view_type_regular_button_adapter_1 -> {
                    color = Color.parseColor("#beffa4")
                }
                R.id.view_type_regular_button_adapter_2 -> {
                    color = Color.parseColor("#8ff2af")
                }
                R.id.view_type_regular_button_adapter_3 -> {
                    color = Color.parseColor("#14e3b4")
                }
            }
            val view = LayoutInflater.from(parent.context).inflate(R.layout.button_custom, parent, false)
            view.buttonCustom.setBackgroundColor(color)
            return RegularButtonViewHolder(view)
        }
    }

    fun update(text: String) {
        itemView.buttonCustom.text = text
    }
}

class ButtonAdapter(var texts: List<String>) : AdapterCeption<Button>() {

    override fun count() = texts.size

    override fun bind(viewWrapper: Button, position: Int) {
        val text = texts[position] + viewType(position)
        viewWrapper.text = text
    }

    override fun viewType(position: Int): Int = when (position) {
        0 -> R.id.view_type_button_adapter_1
        1 -> R.id.view_type_button_adapter_2
        2 -> R.id.view_type_button_adapter_3
        else -> 0
    }

    override fun newViewProvider() = object : ViewProvider<Button>() {
        override fun newViewWrapper(parent: ViewGroup, viewType: Int): Button {
            var color: Int = Color.BLACK
            when (viewType) {
                R.id.view_type_button_adapter_1 -> {
                    color = Color.WHITE
                }
                R.id.view_type_button_adapter_2 -> {
                    color = Color.LTGRAY
                }
                R.id.view_type_button_adapter_3 -> {
                    color = Color.GRAY
                }
            }
            val view = LayoutInflater.from(parent.context).inflate(R.layout.button_custom, parent, false)
            view.buttonCustom.setBackgroundColor(color)
            return view.buttonCustom
        }

    }

}

val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()