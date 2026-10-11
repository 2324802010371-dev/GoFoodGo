package com.gofoodgo.app;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.gofoodgo.app.databinding.ItemOrderBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.Holder> {
    public interface Click {void open(DocumentSnapshot d);}
    private final List<DocumentSnapshot> items;private final Click click;
    OrderAdapter(List<DocumentSnapshot> data,Click c){items=data;click=c;}
    static class Holder extends RecyclerView.ViewHolder {ItemOrderBinding b;Holder(ItemOrderBinding x){super(x.getRoot());b=x;}}
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup p,int t){return new Holder(ItemOrderBinding.inflate(LayoutInflater.from(p.getContext()),p,false));}
    @Override public void onBindViewHolder(@NonNull Holder h,int pos){
        DocumentSnapshot d=items.get(pos);Long sum=d.getLong("total");
        String state=d.getString("status");h.b.txtId.setText("#"+d.getId().substring(0,Math.min(8,d.getId().length())));
        h.b.txtInfo.setText("Tổng: "+Ui.money(sum==null?0:sum)+"\n"+d.getString("phone")+" · "+d.getString("address"));
        h.b.txtStatus.setText("Trạng thái: "+state);h.itemView.setOnClickListener(v->click.open(d));
    }
    @Override public int getItemCount(){return items.size();}
}
