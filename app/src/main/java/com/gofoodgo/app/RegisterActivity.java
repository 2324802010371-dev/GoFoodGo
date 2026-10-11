package com.gofoodgo.app;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.gofoodgo.app.databinding.ActivityAuthBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private ActivityAuthBinding b;
    @Override public void onCreate(Bundle s){
        super.onCreate(s);b=ActivityAuthBinding.inflate(getLayoutInflater());setContentView(b.getRoot());
        b.txtTitle.setText("Tạo tài khoản");b.txtSub.setText("Tham gia GoFoodGo và khám phá món ngon");b.boxName.setVisibility(View.VISIBLE);
        b.btnSubmit.setText("ĐĂNG KÝ");b.txtSecondary.setText("Đã có tài khoản? Đăng nhập");b.txtTertiary.setVisibility(View.GONE);
        b.txtSecondary.setOnClickListener(v->finish());b.btnSubmit.setOnClickListener(v->register());
    }
    private void register(){
        String name=Ui.value(b.etName),email=Ui.value(b.etEmail),pass=Ui.value(b.etPassword);
        if(name.isEmpty()||email.isEmpty()||pass.length()<6){Ui.toast(this,"Nhập tên, email và mật khẩu ít nhất 6 ký tự");return;}
        b.btnSubmit.setEnabled(false);
        FirebaseAuth auth=FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(t->{
            if(!t.isSuccessful()) {b.btnSubmit.setEnabled(true);Ui.toast(this,"Đăng ký lỗi: "+t.getException().getLocalizedMessage());return;}
            String uid=auth.getUid(); if(uid==null)return;
            Map<String,Object> data=new HashMap<>();
            data.put("uid",uid);data.put("name",name);data.put("email",email);data.put("avatarUrl","");data.put("createdAt",FieldValue.serverTimestamp());
            FirebaseFirestore.getInstance().collection("users").document(uid).set(data).addOnCompleteListener(saved->{
                b.btnSubmit.setEnabled(true);
                if(saved.isSuccessful()){Ui.open(this,HomeActivity.class);finishAffinity();}
                else Ui.toast(this,"Đã tạo tài khoản nhưng chưa lưu hồ sơ: "+saved.getException().getLocalizedMessage());
            });
        });
    }
}
