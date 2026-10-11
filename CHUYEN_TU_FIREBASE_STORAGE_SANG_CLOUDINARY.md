# Chuyển GoFoodGo sang Cloudinary Free (không liên kết thẻ)

## Bạn đang ở màn hình Firebase Storage yêu cầu Upgrade project?

BỎ QUA màn hình đó. Giữ Firebase Spark, Firebase Auth, Firestore và `app/google-services.json` hiện có.

## 1. Đăng ký Cloudinary Free

1. Mở https://cloudinary.com/users/register_free và chọn Free. Cloudinary công bố gói Free **không yêu cầu thẻ**.
2. Xác minh email/đăng nhập; mở Console.
3. Sao chép **Cloud name** từ Console. Không dùng API key/secret.
4. Mở Settings > Upload > Upload presets (hoặc mục Upload Presets tương ứng).
5. Tạo preset: tên **gofoodgo_unsigned**; Signing mode **Unsigned**.
6. Trong preset: chỉ cho các dạng ảnh **jpg, png, webp**, đặt folder **gofoodgo**, hạn chế đặt `public_id` từ phía client nếu có, rồi Save. Chế độ Unsigned không có phân quyền thực sự theo Firebase UID; phù hợp bản demo hạn chế người dùng.

## 2. Chép bản mã nguồn Cloudinary vào GoFoodGo

1. Sao lưu toàn bộ thư mục AndroidStudioProjects/GoFoodGo trước.
2. Giải nén zip mới, copy **nội dung bên trong** GoFoodGo_Java vào gốc project, ghi đè file thuộc mã nguồn và Gradle.
3. **GIỮ LẠI** `app/google-services.json`, `local.properties`, `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar` đã chạy tốt và mọi thiết lập JDK riêng.
4. Giữ Groovy `build.gradle` đã cung cấp, tránh để các file `build.gradle.kts` tương ứng cùng tồn tại.

## 3. Sửa duy nhất hai giá trị nếu dùng preset mặc định

File: `app/src/main/java/com/gofoodgo/app/CloudinaryConfig.java`

```java
public static final String CLOUD_NAME = "DIEN_CLOUD_NAME";
public static final String UPLOAD_PRESET = "gofoodgo_unsigned";
```

Thay `DIEN_CLOUD_NAME` bằng Cloud name trong tài khoản. Nếu bạn đã đặt tên preset khác, thay tiếp `UPLOAD_PRESET`.

**TUYỆT ĐỐI KHÔNG** đưa API Secret, private key hoặc tài khoản thanh toán vào mã nguồn Java.

## 4. Sync - Run - thử tải ảnh

1. Android Studio: File > Sync Project with Gradle Files; Build > Make Project.
2. Chạy app, đăng ký/đăng nhập; nếu chưa có quyền Admin, Firebase Console > Authentication > Users > copy UID; Firestore > Data > tạo `admins/{uid}` như trong HUONG_DAN.md.
3. Admin: + Món > chọn ảnh JPG/PNG/WebP dưới 5 MB > Lưu món. App sẽ upload bằng HTTPS lên Cloudinary rồi tạo document Firestore `foods` có `imageUrl`.
4. User: Profile > chọn ảnh đại diện > Lưu. App sẽ ghi `avatarUrl` trong `users/{uid}`.
5. Xem Media Library của Cloudinary và Data của Firestore để đối chiếu.

## File đã thay đổi so với bản dùng Firebase Storage

- `EditFoodActivity.java`: dùng `CloudinaryUploader` thay `FirebaseStorage`.
- `ProfileActivity.java`: dùng `CloudinaryUploader` thay `FirebaseStorage`.
- `CloudinaryUploader.java`: lớp upload HTTPS không cần SDK bổ sung.
- `CloudinaryConfig.java`: hai tham số cloud/preset cần nhập.
- `app/build.gradle`: bỏ Firebase Storage dependency.
- `storage.rules`: không còn cần nên không có trong bản mới.

## Bảo mật và hạn chế

- Preset unsigned được nhúng trong app: bất cứ ai biết preset có thể gửi file lên Cloudinary. Client chỉ giới hạn 5 MB để tối ưu trải nghiệm, KHÔNG ngăn được client giả mạo. Gói miễn phí có quota. Hãy giám sát dashboard, thu hồi preset nếu bị lạm dụng.
- Với triển khai thật, dùng server/Cloud Function/Spring Boot xác thực Firebase token, kiểm tra quyền Admin và ký upload, đồng thời giới hạn request.
- Xóa ảnh cũ chưa có tính năng dọn dẹp trên Cloudinary. Chức năng đặt hàng COD trong app là mô phỏng học tập.
