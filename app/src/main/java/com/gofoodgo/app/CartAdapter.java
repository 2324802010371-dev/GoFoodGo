package com.gofoodgo.app;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.gofoodgo.app.databinding.ItemCartBinding;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.Holder>{
    public interface OnUpdate {void change();}
    private final List<CartStore.Line> items;private final OnUpdate onUpdate;
    public CartAdapter(List<CartStore.Line> i,OnUpdate cb){items=i;onUpdate=cb;}
    static class Holder extends RecyclerView.ViewHolder{ItemCartBinding b;Holder(ItemCartBinding v){super(v.getRoot());b=v;}}
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup p,int t){return new Holder(ItemCartBinding.inflate(LayoutInflater.from(p.getContext()),p,false));}
    @Override public void onBindViewHolder(@NonNull Holder h,int pos){
        CartStore.Line l=items.get(pos);h.b.txtName.setText(l.name);h.b.txtPrice.setText(Ui.money(l.price));h.b.txtQuantity.setText(String.valueOf(l.quantity));
        Glide.with(h.itemView).load(l.imageUrl).placeholder(R.drawable.bg_pale).into(h.b.imgFood);
        h.b.btnPlus.setOnClickListener(v->{if(l.quantity<99){l.quantity++;onUpdate.change();}});
        h.b.btnMinus.setOnClickListener(v->{l.quantity--;if(l.quantity<=0)items.remove(l);onUpdate.change();});
    }
    @Override public int getItemCount(){return items.size();}
}
