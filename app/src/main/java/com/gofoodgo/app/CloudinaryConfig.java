package com.gofoodgo.app;

/**
 * CHỈ nhập Cloud name và UNSIGNED upload preset từ Cloudinary.
 * KHÔNG đặt API secret, private key hoặc mật khẩu trong ứng dụng Android.
 */
public final class CloudinaryConfig {
    private CloudinaryConfig() {}

    public static final String CLOUD_NAME = "lddo9bsy";
    public static final String UPLOAD_PRESET = "gofoodgo_unsigned";
    public static final int MAX_UPLOAD_BYTES = 5 * 1024 * 1024;

    public static boolean isConfigured() {
        return !CLOUD_NAME.isEmpty() && !CLOUD_NAME.equals("DIEN_CLOUD_NAME")
                && !UPLOAD_PRESET.isEmpty();
    }
}
