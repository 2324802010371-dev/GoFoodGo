
package com.gofoodgo.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.gofoodgo.app.databinding.ActivityFoodDetailBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class FoodDetailActivity extends AppCompatActivity {

    private ActivityFoodDetailBinding b;

    private final FirebaseFirestore db =
            FirebaseFirestore.getInstance();

    private String foodId;
    private boolean isAdmin = false;
    private boolean foodLoaded = false;
    private Food currentFood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        b = ActivityFoodDetailBinding.inflate(
                getLayoutInflater()
        );
        setContentView(b.getRoot());

        // Nhận ID món ăn từ màn hình trước
        foodId = getIntent().getStringExtra("foodId");

        if (foodId == null || foodId.trim().isEmpty()) {
            Ui.toast(this, "Không tìm thấy ID món ăn");
            finish();
            return;
        }

        // Mặc định ẩn nút chỉnh sửa
        b.btnEdit.setVisibility(View.GONE);
        b.btnAdd.setEnabled(false);

        // Thêm món vào giỏ hàng
        b.btnAdd.setOnClickListener(v -> {

            if (currentFood == null || !currentFood.active) {
                return;
            }

            CartStore.add(this, currentFood);

            Ui.toast(this, "Đã thêm món vào giỏ hàng");
        });

        // Admin chuyển sang màn hình chỉnh sửa
        b.btnEdit.setOnClickListener(v -> {

            if (!isAdmin || !foodLoaded) {
                return;
            }

            Intent intent = new Intent(
                    FoodDetailActivity.this,
                    EditFoodActivity.class
            );

            intent.putExtra("foodId", foodId);

            startActivity(intent);
        });

        // Kiểm tra quyền Admin
        checkAdmin();
    }

    // Kiểm tra tài khoản hiện tại
    private void checkAdmin() {

        String uid = FirebaseAuth.getInstance().getUid();

        if (uid == null) {
            isAdmin = false;
            updateEditButton();
            return;
        }

        db.collection("admins")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {

                    if (isFinishing() || isDestroyed()) return;

                    isAdmin = doc.exists();

                    updateEditButton();
                })
                .addOnFailureListener(e -> {

                    if (isFinishing() || isDestroyed()) return;

                    isAdmin = false;
                    updateEditButton();
                });
    }

    // Chỉ Admin mới thấy nút chỉnh sửa
    private void updateEditButton() {

        if (isAdmin && foodLoaded) {
            b.btnEdit.setVisibility(View.VISIBLE);
        } else {
            b.btnEdit.setVisibility(View.GONE);
        }
    }

    // Tải thông tin món ăn
    private void loadFood() {

        foodLoaded = false;
        currentFood = null;

        b.btnAdd.setEnabled(false);
        updateEditButton();

        db.collection("foods")
                .document(foodId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (isFinishing() || isDestroyed()) return;

                    if (!doc.exists()) {

                        Ui.toast(this,
                                "Món ăn không còn tồn tại");

                        finish();
                        return;
                    }

                    Food food = Food.from(doc);
                    currentFood = food;

                    b.txtName.setText(food.name);
                    b.txtPrice.setText(
                            Ui.money(food.price)
                    );
                    b.txtCategory.setText(food.category);
                    b.txtDescription.setText(
                            food.description
                    );

                    // Hiển thị ảnh từ Cloudinary
                    Glide.with(this)
                            .load(food.imageUrl)
                            .placeholder(R.drawable.bg_pale)
                            .into(b.imgFood);

                    // Chỉ cho thêm món đang bán
                    b.btnAdd.setEnabled(food.active);

                    foodLoaded = true;
                    updateEditButton();
                })
                .addOnFailureListener(e -> {

                    if (isFinishing() || isDestroyed()) return;

                    Ui.toast(this,
                            "Lỗi tải món: "
                                    + e.getLocalizedMessage());
                });
    }

    // Tự tải lại khi quay về từ màn hình sửa/xóa
    @Override
    protected void onResume() {
        super.onResume();

        if (foodId != null && !foodId.trim().isEmpty()) {
            loadFood();
        }
    }
}
