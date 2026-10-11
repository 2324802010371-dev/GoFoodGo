package com.gofoodgo.app;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.gofoodgo.app.databinding.ActivityListBinding;
import java.util.List;

public class CartActivity extends AppCompatActivity {
    private ActivityListBinding b;private List<CartStore.Line> items;private CartAdapter adapter;
    @Override protected void onCreate(Bundle s){super.onCreate(s);b=ActivityListBinding.inflate(getLayoutInflater());setContentView(b.getRoot());
        b.txtTitle.setText("Giỏ hàng của bạn 🛒");items=CartStore.get(this);
        adapter=new CartAdapter(items,()->{CartStore.save(this,items);adapter.notifyDataSetChanged();update();});
        b.rvList.setLayoutManager(new LinearLayoutManager(this));b.rvList.setAdapter(adapter);
        b.btnAction.setText("TIẾN HÀNH ĐẶT HÀNG");b.btnAction.setOnClickListener(v->Ui.open(this,CheckoutActivity.class));update();
    }
    private void update(){b.txtTotal.setText("Tổng tiền: "+Ui.money(CartStore.total(items)));b.txtEmpty.setVisibility(items.isEmpty()?View.VISIBLE:View.GONE);b.btnAction.setEnabled(!items.isEmpty());}
    @Override protected void onResume(){super.onResume();if(b!=null){items.clear();items.addAll(CartStore.get(this));adapter.notifyDataSetChanged();update();}}
}
