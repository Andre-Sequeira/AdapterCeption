# AdapterCeption

<p>
A RecyclerView.Adapter that allows itself to be "mixed" together with other adapters. Like an adapter with different view types but where every view type can be turned into an independent adapter.
  <br>
  This can be done when the view types are sequencial, i.e., when a view type A always comes before a view type B, which always         comes before a type C. Although dynamic view types are also supported inside the same adapter.
  </br>
</p>
<p>
How is this done?
</p>
  <p>The AdapterCeption adapter is composed by a tree with other adapters as nodes. The items of nodes with positive count are displayed in the order defined by the tree and a node offset is created to be applied to every item position. The root node is attached to the RecyclerView and every Adapter callback is propagated to correct node given its position.
</p>
<p>
  For example, the following tree shows every node with its item count and position relative to its children:
</p>

[!Tree](readme/AdapterCeption_Tree.png)

<p>
  This results in the following items, each with its position in the RecyclerView, node offset and item position in the adapter:
</p>

[!Items](readme/AdapterCeption_Items.png)

Usage
--------

Creating an AdapterCeption can be done in 2 different ways:
 * Write a regular Adapter or grab an existing one and it gets wrapped into a special adapter:
 ```java
 //Java
 RecyclerView.Adapter<VH> adapter = new MyAdapter();
 AdapterCeption<VH> adapterCeption = AdapterCeption.adapt(adapter);
 recyclerView.setAdapter(adapterCeption);
 ```
  ```kotlin
  //Kotlin
  //With the extensions library
val adapter : RecyclerView.Adapter<VH> = MyAdapter()
recyclerView.adapter = adapter.adapt()
 ```
 - Create a subclass of AdapterCeption and override the following methods similarly to a RecyclerView.Adapter:

 ```java
 //Java
class MyAdapter extends AdapterCeption<VW> {
    
    @Override
    protected int viewType(int position) {...}

    @Override
    protected int count()  {...}

    @Nullable
    @Override
    protected ViewProvider<TextView> newViewProvider()  {...}

    @Override
    public void bind(@NonNull TextView viewWrapper, int position)  {...}
}
 ```
A [sample project](app/) is available that demonstrates some of the core capabilities of this library.
