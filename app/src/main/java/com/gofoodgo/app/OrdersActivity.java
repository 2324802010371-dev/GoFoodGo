package com.gofoodgo.app;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.gofoodgo.app.databinding.ActivityListBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class OrdersActivity extends AppCompatActivity {
    private ActivityListBinding b;private final List<DocumentSnapshot> docs=new ArrayList<>();private OrderAdapter adapter;
    @Override public void onCreate(Bundle s){super.onCreate(s);b=ActivityListBinding.inflate(getLayoutInflater());setContentView(b.getRoot());
        b.txtTitle.setText("Đơn hàng của tôi 📦");b.txtTotal.setVisibility(View.GONE);b.btnAction.setText("VỀ TRANG CHỦ");
        b.btnAction.setOnClickListener(v->{Ui.open(this,HomeActivity.class);finish();});
        adapter=new OrderAdapter(docs,d->Ui.toast(this,"Đơn hàng #"+d.getId()));b.rvList.setLayoutManager(new LinearLayoutManager(this));b.rvList.setAdapter(adapter);
        String uid=FirebaseAuth.getInstance().getUid();if(uid==null){finish();return;}
        FirebaseFirestore.getInstance().collection("orders").whereEqualTo("uid",uid).get().addOnSuccessListener(snap->{
            docs.clear();docs.addAll(snap.getDocuments());docs.sort((a,c)->{
                com.google.firebase.Timestamp x=a.getTimestamp("createdAt"),y=c.getTimestamp("createdAt");
                return x==null?1:(y==null?-1:y.compareTo(x));
            });adapter.notifyDataSetChanged();b.txtEmpty.setVisibility(docs.isEmpty()?View.VISIBLE:View.GONE);
        }).addOnFailureListener(e->Ui.toast(this,"Không tải được lịch sử: "+e.getLocalizedMessage()));
    }
}
