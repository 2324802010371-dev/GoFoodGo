package com.gofoodgo.app;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.gofoodgo.app.databinding.ActivityProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding b;private Uri pendingAvatar;private String existingAvatar="";
    private final FirebaseFirestore db=FirebaseFirestore.getInstance();
    private final ActivityResultLauncher<String> picker=registerForActivityResult(new ActivityResultContracts.GetContent(),uri->{
        if(uri!=null){pendingAvatar=uri;Glide.with(this).load(uri).circleCrop().into(b.imgAvatar);}
    });
    @Override public void onCreate(Bundle state){super.onCreate(state);b=ActivityProfileBinding.inflate(getLayoutInflater());setContentView(b.getRoot());
        String uid=FirebaseAuth.getInstance().getUid();if(uid==null){finish();return;}
        b.txtEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        db.collection("users").document(uid).get().addOnSuccessListener(d->{
            if(!d.exists())return;
            String name=d.getString("name");if(name!=null)b.etName.setText(name);
            existingAvatar=d.getString("avatarUrl")==null?"":d.getString("avatarUrl");
            if(!existingAvatar.isEmpty()&&pendingAvatar==null)Glide.with(this).load(existingAvatar).circleCrop().into(b.imgAvatar);
        });
        db.collection("admins").document(uid).get().addOnSuccessListener(d->b.btnAdminOrders.setVisibility(d.exists()?View.VISIBLE:View.GONE));
        b.btnPickAvatar.setOnClickListener(v->picker.launch("image/*"));
        b.btnSave.setOnClickListener(v->save(uid));
        b.btnOrders.setOnClickListener(v->Ui.open(this,OrdersActivity.class));
        b.btnAdminOrders.setOnClickListener(v->Ui.open(this,AdminOrdersActivity.class));
        b.btnLogout.setOnClickListener(v->{FirebaseAuth.getInstance().signOut();Ui.open(this,LoginActivity.class);finishAffinity();});
    }
    private void save(String uid){
        String name=Ui.value(b.etName);if(name.isEmpty()){Ui.toast(this,"Vui lòng nhập tên hiển thị");return;}
        b.btnSave.setEnabled(false);
        if(pendingAvatar==null){write(uid,name,existingAvatar);return;}
        CloudinaryUploader.uploadImage(this, pendingAvatar, new CloudinaryUploader.Callback() {
            @Override public void onSuccess(String secureUrl) {
                if (!isFinishing() && !isDestroyed()) write(uid, name, secureUrl);
            }
            @Override public void onError(String message) {
                if (!isFinishing() && !isDestroyed()) failed("Upload avatar thất bại: " + message);
            }
        });
    }
    private void write(String uid,String name,String avatar){
        Map<String,Object> patch=new HashMap<>();patch.put("name",name);patch.put("avatarUrl",avatar);
        db.collection("users").document(uid).update(patch).addOnSuccessListener(v->{b.btnSave.setEnabled(true);pendingAvatar=null;existingAvatar=avatar;Ui.toast(this,"Đã cập nhật hồ sơ");})
            .addOnFailureListener(e->failed("Không lưu được hồ sơ: "+e.getLocalizedMessage()));
    }
    private void failed(String message){b.btnSave.setEnabled(true);Ui.toast(this,message);}
}
