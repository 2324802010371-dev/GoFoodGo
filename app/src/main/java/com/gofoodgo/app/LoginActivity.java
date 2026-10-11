package com.gofoodgo.app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.gofoodgo.app.databinding.ActivityAuthBinding;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private ActivityAuthBinding b;
    @Override public void onCreate(Bundle s) {
        super.onCreate(s);
        if(FirebaseAuth.getInstance().getCurrentUser()!=null) { Ui.open(this,HomeActivity.class);finish();return; }
        b=ActivityAuthBinding.inflate(getLayoutInflater());setContentView(b.getRoot());
        b.btnSubmit.setOnClickListener(v -> login());
        b.txtSecondary.setOnClickListener(v -> Ui.open(this,RegisterActivity.class));
        b.txtTertiary.setOnClickListener(v -> Ui.open(this,ForgotPasswordActivity.class));
    }
    private void login() {
        String email=Ui.value(b.etEmail), password=Ui.value(b.etPassword);
        if(email.isEmpty()||password.isEmpty()){ Ui.toast(this,"Vui lòng nhập email và mật khẩu");return; }
        b.btnSubmit.setEnabled(false);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password).addOnCompleteListener(t -> {
            b.btnSubmit.setEnabled(true);
            if(t.isSuccessful()){ Ui.open(this,HomeActivity.class);finish(); }
            else Ui.toast(this,"Đăng nhập thất bại: "+ (t.getException()==null?"Không rõ lý do":t.getException().getLocalizedMessage()));
        });
    }
}
