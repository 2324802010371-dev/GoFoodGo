package com.gofoodgo.app;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.gofoodgo.app.databinding.ActivityAuthBinding;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    @Override public void onCreate(Bundle s){
        super.onCreate(s);ActivityAuthBinding b=ActivityAuthBinding.inflate(getLayoutInflater());setContentView(b.getRoot());
        b.txtTitle.setText("Khôi phục mật khẩu");b.txtSub.setText("Nhập email để nhận liên kết đặt lại mật khẩu");
        b.boxPassword.setVisibility(View.GONE);b.txtTertiary.setVisibility(View.GONE);b.btnSubmit.setText("GỬI EMAIL KHÔI PHỤC");
        b.txtSecondary.setText("Quay về đăng nhập");b.txtSecondary.setOnClickListener(v->finish());
        b.btnSubmit.setOnClickListener(v->{
            String email=Ui.value(b.etEmail);
            if(email.isEmpty()){Ui.toast(this,"Hãy nhập email");return;}
            b.btnSubmit.setEnabled(false);
            FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener(t->{
                b.btnSubmit.setEnabled(true);
                if(t.isSuccessful()) Ui.toast(this,"Nếu email hợp lệ, hãy kiểm tra hộp thư để đặt lại mật khẩu.");
                else Ui.toast(this,"Không thể gửi email: "+t.getException().getLocalizedMessage());
            });
        });
    }
}
