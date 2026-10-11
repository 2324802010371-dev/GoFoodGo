package com.gofoodgo.app;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** Demo uploads qua HTTPS/unsigned preset, chỉ dùng cho đồ án.
 * Unsigned preset có thể bị bên ngoài sử dụng; bản production nên có
 * backend xác thực Firebase token và ký upload trước khi gửi Cloudinary.
 */
public final class CloudinaryUploader {
    public interface Callback {
        void onSuccess(String secureUrl);
        void onError(String message);
    }
    private CloudinaryUploader() {}

    public static void uploadImage(Context context, Uri uri, Callback callback) {
        Handler ui = new Handler(Looper.getMainLooper());
        if (!CloudinaryConfig.isConfigured()) {
            ui.post(() -> callback.onError("Chưa nhập CLOUD_NAME trong CloudinaryConfig.java"));
            return;
        }
        Context app = context.getApplicationContext();
        new Thread(() -> {
            try {
                String mime = app.getContentResolver().getType(uri);
                if (mime == null || !(mime.equals("image/jpeg") || mime.equals("image/png") || mime.equals("image/webp"))) {
                    throw new IOException("Chỉ hỗ trợ ảnh JPG, PNG hoặc WebP");
                }
                byte[] bytes = readLimited(app, uri);
                String url = upload(bytes, mime);
                ui.post(() -> callback.onSuccess(url));
            } catch (Exception ex) {
                String msg = ex.getMessage() == null ? "Lỗi kết nối" : ex.getMessage();
                ui.post(() -> callback.onError(msg));
            }
        }, "GoFoodGo-CloudinaryUpload").start();
    }

    private static byte[] readLimited(Context app, Uri uri) throws IOException {
        try (InputStream in = app.getContentResolver().openInputStream(uri)) {
            if (in == null) throw new IOException("Không mở được ảnh đã chọn");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) != -1) {
                if (out.size() + n > CloudinaryConfig.MAX_UPLOAD_BYTES)
                    throw new IOException("Ảnh quá 5 MB, hãy chọn ảnh nhỏ hơn");
                out.write(buffer, 0, n);
            }
            if (out.size() == 0) throw new IOException("Ảnh rỗng");
            return out.toByteArray();
        }
    }

    private static String upload(byte[] image, String mime) throws Exception {
        String cloudName = CloudinaryConfig.CLOUD_NAME;
        // Unsigned API chỉ cần cloud_name và upload_preset, không cần API key/secret.
        String endpoint = "https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload";
        HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
        String boundary = "GoFoodGo" + UUID.randomUUID().toString().replace("-", "");
        try {
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(20000);
            conn.setReadTimeout(45000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            conn.setChunkedStreamingMode(64 * 1024);
            try (OutputStream out = new BufferedOutputStream(conn.getOutputStream())) {
                writeAscii(out, "--" + boundary + "\r\n");
                writeAscii(out, "Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n");
                writeAscii(out, CloudinaryConfig.UPLOAD_PRESET + "\r\n");
                writeAscii(out, "--" + boundary + "\r\n");
                writeAscii(out, "Content-Disposition: form-data; name=\"file\"; filename=\"gofoodgo.jpg\"\r\n");
                writeAscii(out, "Content-Type: " + mime + "\r\n\r\n");
                out.write(image);
                writeAscii(out, "\r\n--" + boundary + "--\r\n");
            }
            int status = conn.getResponseCode();
            InputStream stream = status >= 200 && status < 300 ? conn.getInputStream() : conn.getErrorStream();
            String response = readText(stream);
            JSONObject json = new JSONObject(response);
            if (status < 200 || status >= 300) {
                String reason = json.optJSONObject("error") == null ? response :
                        json.getJSONObject("error").optString("message", response);
                throw new IOException("Cloudinary HTTP " + status + ": " + reason);
            }
            String secureUrl = json.optString("secure_url", "");
            if (!secureUrl.startsWith("https://res.cloudinary.com/"))
                throw new IOException("Không nhận được secure_url hợp lệ");
            return secureUrl;
        } finally {
            conn.disconnect();
        }
    }

    private static void writeAscii(OutputStream out, String value) throws IOException {
        out.write(value.getBytes(StandardCharsets.UTF_8));
    }
    private static String readText(InputStream stream) throws IOException {
        if (stream == null) return "Không có phản hồi từ máy chủ";
        try (InputStream in = new BufferedInputStream(stream)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) != -1) {
                if (out.size() + n > 16384) throw new IOException("Phản hồi máy chủ quá lớn");
                out.write(buffer, 0, n);
            }
            return out.toString(StandardCharsets.UTF_8.name());
        }
    }
}
