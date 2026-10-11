package com.gofoodgo.app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.gofoodgo.app.databinding.ActivityCheckoutBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckoutActivity extends AppCompatActivity {
    private ActivityCheckoutBinding b;
    @Override public void onCreate(Bundle s){super.onCreate(s);b=ActivityCheckoutBinding.inflate(getLayoutInflater());setContentView(b.getRoot());
        List<CartStore.Line> items=CartStore.get(this);if(items.isEmpty()){Ui.toast(this,"Giỏ hàng đang trống");finish();return;}
        b.txtTotal.setText("Tổng cộng: "+Ui.money(CartStore.total(items)));b.btnPlaceOrder.setOnClickListener(v->place());
    }
    private void place(){
        String phone=Ui.value(b.etPhone),address=Ui.value(b.etAddress),uid=FirebaseAuth.getInstance().getUid();
        List<CartStore.Line> lines=CartStore.get(this);
        if(uid==null||lines.isEmpty()){Ui.toast(this,"Phiên đăng nhập hoặc giỏ hàng không hợp lệ");return;}
        if(phone.length()<9||address.length()<8){Ui.toast(this,"Hãy điền đúng số điện thoại và địa chỉ giao hàng");return;}
        long total=CartStore.total(lines);if(total<=0||total>100000000L){Ui.toast(this,"Tổng tiền không hợp lệ");return;}
        List<Map<String,Object>> items=new ArrayList<>();
        for(CartStore.Line l:lines){Map<String,Object> m=new HashMap<>();m.put("foodId",l.id);m.put("name",l.name);m.put("price",l.price);m.put("quantity",l.quantity);items.add(m);}
        Map<String,Object> order=new HashMap<>();order.put("uid",uid);order.put("items",items);order.put("total",total);
        order.put("status","PENDING");order.put("phone",phone);order.put("address",address);order.put("createdAt",FieldValue.serverTimestamp());
        b.btnPlaceOrder.setEnabled(false);
        FirebaseFirestore.getInstance().collection("orders").add(order).addOnSuccessListener(doc->{
            CartStore.clear(this);Ui.toast(this,"Đặt hàng COD thành công (mô phỏng)! Mã: "+doc.getId().substring(0,6));
            Ui.open(this,OrdersActivity.class);finish();
        }).addOnFailureListener(e->{b.btnPlaceOrder.setEnabled(true);Ui.toast(this,"Không tạo được đơn: "+e.getLocalizedMessage());});
    }
}
