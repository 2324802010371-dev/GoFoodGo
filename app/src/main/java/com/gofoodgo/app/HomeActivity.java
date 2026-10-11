package com.gofoodgo.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import com.gofoodgo.app.databinding.ActivityHomeBinding;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding b;private final List<Food> all=new ArrayList<>(),shown=new ArrayList<>();
    private final List<String> cats=Arrays.asList("Tất cả","Cơm","Mì","Gà rán","Ăn vặt","Đồ uống","Khác");
    private String selected="Tất cả";private boolean admin=false;private FoodAdapter adapter;
    private ListenerRegistration foodsListener;
    private final FirebaseFirestore db=FirebaseFirestore.getInstance();
    @Override public void onCreate(Bundle s){
        super.onCreate(s);
        if(FirebaseAuth.getInstance().getCurrentUser()==null){Ui.open(this,LoginActivity.class);finish();return;}
        b=ActivityHomeBinding.inflate(getLayoutInflater());setContentView(b.getRoot());
        adapter=new FoodAdapter(shown,new FoodAdapter.Callback(){
            @Override public void open(Food f){Intent i=new Intent(HomeActivity.this,FoodDetailActivity.class);i.putExtra("foodId",f.id);startActivity(i);}
            @Override public void add(Food f){if(!f.active){Ui.toast(HomeActivity.this,"Món này đang tạm ngừng bán");return;}CartStore.add(HomeActivity.this,f);Ui.toast(HomeActivity.this,"Đã thêm "+f.name+" vào giỏ");}
            @Override public void edit(Food f){if(!admin)return;Intent i=new Intent(HomeActivity.this,EditFoodActivity.class);i.putExtra("foodId",f.id);startActivity(i);}
        },true);
        b.rvFoods.setLayoutManager(new GridLayoutManager(this,2));b.rvFoods.setAdapter(adapter);
        b.btnAdmin.setOnClickListener(v->Ui.open(this,EditFoodActivity.class));
        b.etSearch.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){filter();}public void afterTextChanged(Editable e){} });
        String uid=FirebaseAuth.getInstance().getUid();
        db.collection("users").document(uid).get().addOnSuccessListener(d->{if(d.exists())b.txtGreeting.setText("Ăn gì hôm nay, "+d.getString("name")+"?");});
        db.collection("admins").document(uid).get().addOnSuccessListener(d->{admin=d.exists();b.btnAdmin.setVisibility(admin?View.VISIBLE:View.GONE);startFoods();drawCategories();})
            .addOnFailureListener(e->{Ui.toast(this,"Không kiểm tra được quyền quản trị: "+e.getLocalizedMessage());startFoods();drawCategories();});
        drawCategories();
    }
    private void startFoods(){
        if(foodsListener!=null)foodsListener.remove();
        com.google.firebase.firestore.Query q=admin?db.collection("foods"):db.collection("foods").whereEqualTo("active",true);
        foodsListener=q.addSnapshotListener((snap,ex)->{
            if(ex!=null){Ui.toast(this,"Lỗi tải thực đơn: "+ex.getLocalizedMessage());return;}
            all.clear();if(snap!=null)for(DocumentSnapshot d:snap.getDocuments())all.add(Food.from(d));filter();
        });
    }
    private void drawCategories(){
        b.categoryRow.removeAllViews();for(String category:cats){
            MaterialButton chip=new MaterialButton(this);chip.setText(category);chip.setAllCaps(false);chip.setCornerRadius(dp(22));chip.setTextSize(12);
            chip.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(category.equals(selected)?R.color.coral:R.color.white)));
            chip.setTextColor(getColor(category.equals(selected)?R.color.white:R.color.ink));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,dp(43));lp.setMargins(dp(5),0,dp(5),0);b.categoryRow.addView(chip,lp);
            chip.setOnClickListener(v->{selected=category;drawCategories();filter();});
        }
    }
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density);}
    private void filter(){
        shown.clear();String query=Ui.value(b.etSearch).toLowerCase(new java.util.Locale("vi","VN"));
        for(Food f:all)if((selected.equals("Tất cả")||selected.equals(f.category))&&(f.name.toLowerCase(new java.util.Locale("vi","VN")).contains(query)||f.category.toLowerCase(new java.util.Locale("vi","VN")).contains(query)))shown.add(f);
        adapter.notifyDataSetChanged();b.txtEmpty.setVisibility(shown.isEmpty()?View.VISIBLE:View.GONE);
    }
    public void homeTab(View v){} // Đang ở trang chủ
    public void cartTab(View v){Ui.open(this,CartActivity.class);}
    public void profileTab(View v){Ui.open(this,ProfileActivity.class);}
    @Override protected void onDestroy(){if(foodsListener!=null)foodsListener.remove();super.onDestroy();}
}
