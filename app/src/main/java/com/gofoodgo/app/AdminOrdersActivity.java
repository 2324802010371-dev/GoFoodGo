package com.gofoodgo.app;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.gofoodgo.app.databinding.ActivityListBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class AdminOrdersActivity extends AppCompatActivity {
    private ActivityListBinding b;private final List<DocumentSnapshot> docs=new ArrayList<>();private OrderAdapter adapter;
    @Override public void onCreate(Bundle s){super.onCreate(s);b=ActivityListBinding.inflate(getLayoutInflater());setContentView(b.getRoot());
        b.txtTitle.setText("Quản trị đơn hàng");b.txtTotal.setVisibility(View.GONE);b.btnAction.setText("TRỞ VỀ");b.btnAction.setOnClickListener(v->finish());
        adapter=new OrderAdapter(docs,this::editStatus);b.rvList.setLayoutManager(new LinearLayoutManager(this));b.rvList.setAdapter(adapter);
        String uid=FirebaseAuth.getInstance().getUid();if(uid==null){finish();return;}
        FirebaseFirestore.getInstance().collection("admins").document(uid).get().addOnSuccessListener(d->{if(d.exists())load();else{Ui.toast(this,"Bạn không có quyền quản trị");finish();}})
            .addOnFailureListener(e->{Ui.toast(this,e.getLocalizedMessage());finish();});
    }
    private void load(){FirebaseFirestore.getInstance().collection("orders").get().addOnSuccessListener(s->{
        docs.clear();docs.addAll(s.getDocuments());docs.sort((a,c)->{
            com.google.firebase.Timestamp x=a.getTimestamp("createdAt"),y=c.getTimestamp("createdAt");return x==null?1:(y==null?-1:y.compareTo(x));});
        adapter.notifyDataSetChanged();b.txtEmpty.setVisibility(docs.isEmpty()?View.VISIBLE:View.GONE);
    }).addOnFailureListener(e->Ui.toast(this,"Không tải được đơn: "+e.getLocalizedMessage()));}
    private void editStatus(DocumentSnapshot d){String[] states={"PENDING","CONFIRMED","DELIVERING","COMPLETED","CANCELLED"};
        new AlertDialog.Builder(this).setTitle("Cập nhật trạng thái #"+d.getId().substring(0,6)).setItems(states,(dialog,which)->{
            FirebaseFirestore.getInstance().collection("orders").document(d.getId()).update("status",states[which]).addOnSuccessListener(v->load())
                .addOnFailureListener(e->Ui.toast(this,"Không cập nhật được: "+e.getLocalizedMessage()));
        }).setNegativeButton("Hủy",null).show();
    }
}
