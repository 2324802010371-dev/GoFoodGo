package com.gofoodgo.app;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.gofoodgo.app.databinding.ItemFoodBinding;
import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.Holder> {
    public interface Callback { void open(Food f);void add(Food f);void edit(Food f); }
    private final List<Food> data; private final Callback callback; private final boolean admin;
    public FoodAdapter(List<Food> d,Callback cb,boolean a){data=d;callback=cb;admin=a;}
    static class Holder extends RecyclerView.ViewHolder { ItemFoodBinding b;
        Holder(ItemFoodBinding x){super(x.getRoot());b=x;}}
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup p,int type){return new Holder(ItemFoodBinding.inflate(LayoutInflater.from(p.getContext()),p,false));}
    @Override public void onBindViewHolder(@NonNull Holder h,int pos){
        Food f=data.get(pos);h.b.txtName.setText(f.name);h.b.txtCategory.setText(f.category+(f.active?"":" • Đã ẩn"));h.b.txtPrice.setText(Ui.money(f.price));
        Glide.with(h.itemView).load(f.imageUrl.isEmpty()?null:f.imageUrl).placeholder(R.drawable.bg_pale).error(R.drawable.bg_pale).into(h.b.imgFood);
        h.itemView.setOnClickListener(v->callback.open(f));
        h.b.btnAdd.setOnClickListener(v->callback.add(f));
        h.itemView.setOnLongClickListener(v->{if(admin){callback.edit(f);return true;}return false;});
    }
    @Override public int getItemCount(){return data.size();}
}
