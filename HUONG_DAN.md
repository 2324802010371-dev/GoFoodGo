# GoFoodGo Java + XML: hướng dẫn từ A đến Z

> Mã nguồn Android Java, view XML, Firebase Authentication + Firestore + Cloudinary Free (không cần gắn thẻ).
> Đây là đồ án học tập COD mô phỏng. Không phải hệ thống thanh toán hay định giá đơn hàng đủ bảo mật cho sản phẩm thật.

## 1. Tạo project mới trong Android Studio

1. File > New > New Project > **Empty Views Activity** (không chọn Empty Activity Compose).
2. Name: `GoFoodGo`; Package name: `com.gofoodgo.app`; Language: **Java**; Minimum SDK: **API 24**.
3. Chọn Groovy Gradle DSL nếu Android Studio có tùy chọn.
4. Đóng Android Studio và sao lưu thư mục project mới tạo.
5. Giải nén toàn bộ các thư mục/tệp trong gói này vào gốc project `GoFoodGo` và **ghi đè** tệp trùng.
6. **GIỮ LẠI** tệp `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, `local.properties` của Android Studio. Gói mã nguồn không chứa Gradle wrapper binary / Android SDK.
7. Nếu project mới tạo có `build.gradle.kts`, `settings.gradle.kts` hoặc `app/build.gradle.kts`, **xóa các tệp `.kts` tương ứng**, vì gói này dùng `build.gradle` Groovy.
8. Làm bước Firebase bên dưới, đặt đúng `app/google-services.json`, rồi mở lại project, giữ JDK 17 hoặc 21 đã Sync thành công, cài Android SDK API 35 nếu được yêu cầu và Sync Project.

### Nếu trước đó đã tạo Firebase cho FoodHuanGo

Không bắt buộc tạo Firebase project mới. Trong Firebase project hiện có, vào **Project settings → Your apps → Add app → Android** và đăng ký ứng dụng mới bằng package **`com.gofoodgo.app`**; tải tệp `google-services.json` tương ứng về thư mục **`app/`**. Tệp cấu hình cũ của `com.foodhuango.app` không thể dùng nguyên trạng sau khi đổi `applicationId`, nếu chưa đăng ký Android app mới. Trên máy, mở Android Studio rồi **Sync Project with Gradle Files**.

## 2. Firebase console (https://console.firebase.google.com)

1. Add project > `GoFoodGo`. Analytics tùy chọn.
2. Project settings > Your apps > Add app Android.
3. Android package name **chính xác**: `com.gofoodgo.app`.
4. Tải `google-services.json` và chép vào `GoFoodGo/app/google-services.json` (đừng để ở thư mục gốc).
5. Build > Authentication > Sign-in method > bật **Email/Password** (không chỉ Email link).
6. Build > Firestore Database > Create database > chọn **Standard edition / Native mode**, chọn location. Chọn production mode hoặc dán rules trước khi app truy cập.
7. Trong Firestore Database > Rules, dán **toàn bộ** `firestore.rules` rồi Publish.
8. **Bỏ qua Firebase Storage**: KHÔNG nâng cấp Blaze, KHÔNG nhập thẻ. Gói này đã đổi upload món/ảnh đại diện sang Cloudinary Free.
9. Vào https://cloudinary.com/users/register_free, đăng ký tài khoản Free (không cần thẻ) và xác minh email nếu được yêu cầu.
10. Trong Cloudinary Console, tìm **Cloud name** ở Dashboard/API Keys (tên menu có thể thay đổi); ghi lại Cloud name.
11. Mở **Settings > Upload > Upload presets** (hoặc đường dẫn Upload Presets ở Console), tạo preset **gofoodgo_unsigned**, chọn **Signing Mode: Unsigned**; trong preset cấu hình thư mục `gofoodgo`, tài nguyên hình ảnh, Allowed formats **jpg, png, webp**; tắt tùy chọn cho người tải đặt public_id nếu có; Save.
12. Trong Android Studio, mở `app/src/main/java/com/gofoodgo/app/CloudinaryConfig.java`. Thay `DIEN_CLOUD_NAME` bằng Cloud name của bạn. Nếu đặt tên preset khác `gofoodgo_unsigned`, thay cả `UPLOAD_PRESET` cho khớp. KHÔNG nhập API Secret hoặc private key vào code.
13. Sync, Build, Run. CloudinaryUploader sẽ upload JPG/PNG/WebP dưới 5 MB lên Cloudinary qua HTTPS và ghi `secure_url` vào Firestore. Mã nguồn không còn phụ thuộc `firebase-storage`.
14. Lưu ý: **Unsigned preset có thể bị người khác sử dụng nếu biết tên**; giới hạn định dạng/thư mục và theo dõi quota. Bản production nên dùng backend ký upload sau khi kiểm tra Firebase Auth + quyền Admin.

## 3. Tạo tài khoản Admin an toàn

1. Chạy app, đăng ký một tài khoản mới bằng email và mật khẩu.
2. Firebase Console > Authentication > Users > copy **UID** của tài khoản vừa tạo.
3. Firebase Console > Firestore Database > Data > **Start collection**: `admins`.
4. Document ID: dán chính xác UID vừa copy. Thêm field ví dụ `createdBy` kiểu string giá trị `firebase-console`.
5. Save. Đăng xuất và đăng nhập lại; nút **+ Món** ở trang chủ và **Quản trị đơn hàng** trong hồ sơ sẽ xuất hiện.
6. **Không** thêm field `role: admin` vào `users` bằng code client để tự phân quyền. Bộ rules chỉ cho phép tài khoản quản trị khi có document trong `admins` do bạn tạo qua Console.

## 4. Tạo dữ liệu món ăn

- Đăng nhập với tài khoản Admin.
- Màn hình trang chủ > nút **+ Món**.
- Chọn ảnh từ thiết bị; nhập tên, mô tả, danh mục (Cơm, Mì, Gà rán, Ăn vặt, Đồ uống), giá dạng số nguyên VND, bật "Đang bán".
- Bấm **LƯU MÓN ĂN**. Ảnh tải lên Cloudinary, link HTTPS và thông tin món lưu trong Firestore `foods`.
- Chạm giữ thẻ món ăn ở trang chủ để chỉnh sửa / tạm ẩn / xóa.
- Tài khoản User bình thường xem món, tìm kiếm, chọn danh mục, xem chi tiết, thêm giỏ, thay đổi số lượng, đặt hàng COD giả lập.

## 5. Các màn hình

| Activity | Vai trò |
| --- | --- |
| LoginActivity | Đăng nhập, chuyển Đăng ký / Quên mật khẩu |
| RegisterActivity | Tạo Firebase Auth, thêm `users/{uid}` |
| ForgotPasswordActivity | Email khôi phục mật khẩu |
| HomeActivity | Danh mục, tìm kiếm, hiển thị món và nút Admin |
| FoodDetailActivity | Chi tiết món, thêm vào giỏ |
| CartActivity | Giỏ hàng lưu local SharedPreferences theo UID |
| CheckoutActivity | Địa chỉ/số điện thoại, đặt COD giả lập |
| OrdersActivity | Lịch sử đơn hàng của người dùng |
| ProfileActivity | Sửa tên, chọn/upload avatar, đăng xuất |
| EditFoodActivity | Admin tạo/sửa/xóa món, upload hình |
| AdminOrdersActivity | Admin xem mọi đơn và cập nhật trạng thái |

## 6. Cấu trúc Firestore

```
users/{uid}        { uid, name, email, avatarUrl, createdAt }
admins/{uid}       { createdBy }
foods/{foodId}     { name, description, category, price, imageUrl, active, createdAt }
orders/{orderId}   { uid, items: [{foodId,name,price,quantity}], total, status, phone, address, createdAt }
```

Cloudinary: ảnh được lưu trong thư mục/preset cấu hình trên dashboard. URL HTTPS ở `users/{uid}.avatarUrl` hoặc `foods/{id}.imageUrl`.

## 7. Sơ đồ thao tác

`Login/Register -> Home -> Food detail -> Cart -> Checkout -> Orders`

`Home -> Profile -> (Save name / upload avatar / Orders / Logout)`

`Admin -> Home (+Món) -> EditFood` hoặc `Profile -> AdminOrders`.

## 8. Lưu ý rất quan trọng về bảo mật và chi phí

- Dự án giữ Firebase Spark và Cloudinary Free, không yêu cầu nhập thẻ. Cloudinary Free có hạn mức hàng tháng; kiểm tra phần Usage/Billing để tránh vượt quota.
- **Không tải lên** `google-services.json`, private keys, service-account JSON vào repo công khai. `google-services.json` không phải khóa quản trị, nhưng vẫn nên để ngoài repo trong dự án học tập.
- **Giá và tổng tiền trong đơn được tính trên client**, vì đây là COD mô phỏng cho môn học. Client có thể sửa request. Sản phẩm thật phải tính lại giá/khuyến mãi ở backend tin cậy (Cloud Functions hoặc Spring Boot), xác thực danh sách món, giới hạn số lượng và chống spam trước khi tạo đơn.
- Xóa món hiện chỉ xóa Firestore document, ảnh trên Cloudinary chưa tự xóa. Bản production cần cleanup qua backend có kiểm soát.
- Sử dụng `GetContent` chọn ảnh, không yêu cầu quyền đọc toàn bộ bộ nhớ trên Android.
- Nếu Cloudinary báo `Upload preset not found`: kiểm tra đúng tên preset, chế độ Unsigned và Cloud name. Nếu `Invalid image file`: dùng JPG/PNG/WebP dưới 5 MB. Không upload API Secret.
- Nếu `PERMISSION_DENIED` Firestore: kiểm tra đã Publish rules, UID tài khoản đăng nhập và `admins/{UID}` đã tạo.
- Nếu `No matching client found for package name`: kiểm tra package name và `google-services.json` có tương ứng.
- Nếu lỗi không có gradle-wrapper.jar: bạn chưa giữ lại tệp wrapper của project mới được tạo bằng Android Studio ở bước 1.

## 9. Giới hạn demo / hướng phát triển

Đây là app mẫu hoàn chỉnh các chức năng được giao: auth, danh mục, food list/detail, cart, profile, food/avatar upload và thêm order/admin. Chưa có thanh toán thật, bản đồ người giao hàng, giao vận, hệ thống nhiều nhà hàng độc lập hoặc kiểm soát tồn kho đồng thời. Muốn mở rộng thành nền tảng nhiều nhà hàng: bổ sung `restaurants`, `restaurantId` vào foods/orders, membership người quản lý và backend kiểm tra giá/owner.
