
package com.gofoodgo.app;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.gofoodgo.app.databinding.ActivityEditFoodBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditFoodActivity extends AppCompatActivity {

    private ActivityEditFoodBinding b;

    private Uri pendingImage;
    private String imageUrl = "";
    private String editId;

    private boolean isBusy = true;

    private final FirebaseFirestore db =
            FirebaseFirestore.getInstance();

    // Trình chọn hình ảnh
    private final ActivityResultLauncher<String> picker =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null && b != null && !isBusy) {
                            pendingImage = uri;

                            Glide.with(this)
                                    .load(uri)
                                    .into(b.imgFood);
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        b = ActivityEditFoodBinding.inflate(
                getLayoutInflater()
        );

        setContentView(b.getRoot());

        editId = getIntent().getStringExtra("foodId");

        if (editId != null && editId.trim().isEmpty()) {
            editId = null;
        }

        // Mặc định ẩn nút xóa
        b.btnDelete.setVisibility(View.GONE);

        // Chưa được thao tác khi chưa xác minh Admin
        setBusy(true);

        b.btnPickImage.setOnClickListener(v -> {
            if (!isBusy) {
                picker.launch("image/*");
            }
        });

        b.btnSave.setOnClickListener(v -> save());

        b.btnDelete.setOnClickListener(
                v -> confirmDelete()
        );

        verifyAdmin();
    }

    // Kiểm tra quyền Admin
    private void verifyAdmin() {

        String uid = FirebaseAuth.getInstance().getUid();

        if (uid == null) {
            Ui.toast(this, "Vui lòng đăng nhập");
            finish();
            return;
        }

        db.collection("admins")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {

                    if (isFinishing() || isDestroyed()) return;

                    if (!doc.exists()) {
                        Ui.toast(this,
                                "Tài khoản không có quyền Admin");
                        finish();
                        return;
                    }

                    loadEdit();
                })
                .addOnFailureListener(e -> {

                    if (isFinishing() || isDestroyed()) return;

                    Ui.toast(this,
                            "Lỗi kiểm tra Admin: "
                                    + e.getLocalizedMessage());

                    finish();
                });
    }

    // Phân biệt thêm món và sửa món
    private void loadEdit() {

        if (editId == null) {

            b.txtTitle.setText("Thêm món ăn");
            b.btnDelete.setVisibility(View.GONE);

            setBusy(false);
            return;
        }

        b.txtTitle.setText("Chỉnh sửa món ăn");

        db.collection("foods")
                .document(editId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (isFinishing() || isDestroyed()) return;

                    if (!doc.exists()) {
                        Ui.toast(this,
                                "Món ăn không tồn tại");
                        finish();
                        return;
                    }

                    Food food = Food.from(doc);

                    b.etName.setText(food.name);
                    b.etDescription.setText(food.description);
                    b.etCategory.setText(food.category);
                    b.etPrice.setText(
                            String.valueOf(food.price)
                    );

                    b.switchActive.setChecked(food.active);

                    imageUrl = food.imageUrl == null
                            ? "" : food.imageUrl;

                    if (!imageUrl.isEmpty()) {
                        Glide.with(this)
                                .load(imageUrl)
                                .into(b.imgFood);
                    }

                    b.btnDelete.setVisibility(View.VISIBLE);
                    setBusy(false);
                })
                .addOnFailureListener(e -> {

                    if (isFinishing() || isDestroyed()) return;

                    Ui.toast(this,
                            "Không tải được món: "
                                    + e.getLocalizedMessage());

                    finish();
                });
    }

    // Hiện hộp thoại xác nhận xóa
    private void confirmDelete() {

        if (editId == null || isBusy) return;

        new AlertDialog.Builder(this)
                .setTitle("Xóa món ăn?")
                .setMessage(
                        "Bạn có chắc chắn muốn xóa món này?\n\n"
                                + "Món sẽ bị xóa khỏi Firestore.\n"
                                + "Ảnh trên Cloudinary vẫn được giữ lại."
                )
                .setNegativeButton("HỦY", null)
                .setPositiveButton("XÓA", (dialog, which) -> {
                    deleteFood();
                })
                .show();
    }

    // Xóa món khỏi Firestore
    private void deleteFood() {

        if (editId == null || isBusy) return;

        setBusy(true);

        db.collection("foods")
                .document(editId)
                .delete()
                .addOnSuccessListener(unused -> {

                    if (isFinishing() || isDestroyed()) return;

                    Ui.toast(this,
                            "Đã xóa món ăn thành công");

                    finish();
                })
                .addOnFailureListener(e -> {

                    if (isFinishing() || isDestroyed()) return;

                    setBusy(false);

                    Ui.toast(this,
                            "Xóa thất bại: "
                                    + e.getLocalizedMessage());
                });
    }

    // Lưu món ăn
    private void save() {

        if (isBusy) return;

        String name = Ui.value(b.etName);
        String desc = Ui.value(b.etDescription);
        String category = Ui.value(b.etCategory);
        String rawPrice = Ui.value(b.etPrice);

        long price;

        try {
            price = Long.parseLong(rawPrice);
        } catch (NumberFormatException e) {

            Ui.toast(this,
                    "Giá bán phải là số nguyên VND");
            return;
        }

        if (name.isEmpty()
                || desc.isEmpty()
                || category.isEmpty()
                || price < 1000
                || price > 10000000) {

            Ui.toast(this,
                    "Nhập đầy đủ thông tin. "
                            + "Giá từ 1.000 đến 10 triệu VND");
            return;
        }

        if (editId == null && pendingImage == null) {

            Ui.toast(this,
                    "Vui lòng chọn hình ảnh món ăn");
            return;
        }

        boolean active = b.switchActive.isChecked();

        setBusy(true);

        // Có ảnh mới thì upload lên Cloudinary
        if (pendingImage != null) {

            CloudinaryUploader.uploadImage(
                    this,
                    pendingImage,
                    new CloudinaryUploader.Callback() {

                        @Override
                        public void onSuccess(String secureUrl) {

                            runOnUiThread(() -> {

                                if (isFinishing() || isDestroyed()) {
                                    return;
                                }

                                if (secureUrl == null
                                        || secureUrl.isEmpty()) {
                                    fail("Cloudinary không trả URL ảnh");
                                    return;
                                }

                                saveDocument(
                                        name,
                                        desc,
                                        category,
                                        price,
                                        secureUrl,
                                        active
                                );
                            });
                        }

                        @Override
                        public void onError(String message) {

                            runOnUiThread(() -> {

                                if (!isFinishing() && !isDestroyed()) {
                                    fail("Upload ảnh lỗi: " + message);
                                }
                            });
                        }
                    }
            );

        } else {

            // Không chọn ảnh mới thì giữ ảnh cũ
            saveDocument(
                    name,
                    desc,
                    category,
                    price,
                    imageUrl,
                    active
            );
        }
    }

    // Thêm hoặc cập nhật document món ăn
    private void saveDocument(
            String name,
            String desc,
            String category,
            long price,
            String url,
            boolean active
    ) {

        Map<String, Object> payload = new HashMap<>();

        payload.put("name", name);
        payload.put("description", desc);
        payload.put("category", category);
        payload.put("price", price);
        payload.put("imageUrl", url);
        payload.put("active", active);

        if (editId == null) {

            // THÊM MÓN MỚI
            payload.put(
                    "createdAt",
                    FieldValue.serverTimestamp()
            );

            db.collection("foods")
                    .add(payload)
                    .addOnSuccessListener(ref -> {

                        if (isFinishing() || isDestroyed()) return;

                        Ui.toast(this,
                                "Thêm món ăn thành công");

                        finish();
                    })
                    .addOnFailureListener(e ->
                            fail(e.getLocalizedMessage())
                    );

        } else {

            // CHỈNH SỬA MÓN CŨ
            db.collection("foods")
                    .document(editId)
                    .update(payload)
                    .addOnSuccessListener(unused -> {

                        if (isFinishing() || isDestroyed()) return;

                        Ui.toast(this,
                                "Cập nhật món ăn thành công");

                        finish();
                    })
                    .addOnFailureListener(e ->
                            fail(e.getLocalizedMessage())
                    );
        }
    }

    // Khóa nút khi đang xử lý
    private void setBusy(boolean busy) {

        isBusy = busy;

        b.btnSave.setEnabled(!busy);
        b.btnPickImage.setEnabled(!busy);
        b.btnDelete.setEnabled(!busy);
    }

    // Thông báo lỗi và mở lại nút
    private void fail(String reason) {

        if (isFinishing() || isDestroyed()) return;

        setBusy(false);

        Ui.toast(this,
                "Không lưu được món: " + reason);
    }
}
