# Cyber Shield: Ứng dụng bảo vệ người dùng khỏi tin nhắn lừa đảo và độc hại

## 🛡️ Giới thiệu

**Cyber Shield** là một ứng dụng di động thông minh được thiết kế để bảo vệ người dùng khỏi các tin nhắn lừa đảo, đe dọa, quấy rối, kích động bạo lực hoặc chống phá Nhà nước Việt Nam. Ứng dụng sử dụng công nghệ Trí tuệ nhân tạo (AI) tiên tiến (Google Gemini) kết hợp với các dịch vụ kiểm tra an toàn URL (Google Safe Browsing) để phân tích nội dung tin nhắn theo thời gian thực và cảnh báo người dùng về các mối nguy hiểm tiềm ẩn.

## ✨ Tính năng nổi bật

*   **Phân tích tin nhắn thủ công:** Người dùng có thể nhập hoặc dán bất kỳ đoạn văn bản đáng ngờ nào vào ứng dụng để nhận được phân tích chi tiết về mức độ an toàn, lý do và khuyến nghị.
*   **Bảo vệ tự động theo thời gian thực:**
    *   **Quét thông báo:** Tự động quét các tin nhắn đến từ các ứng dụng nhắn tin phổ biến như Zalo, Messenger, Telegram và SMS.
    *   **Quét Clipboard:** Tự động phân tích nội dung được sao chép vào clipboard.
    *   **Phân tích từ menu ngữ cảnh:** Cho phép người dùng chọn văn bản từ bất kỳ ứng dụng nào và gửi trực tiếp đến Cyber Shield để phân tích.
*   **Cảnh báo thông minh:** Hiển thị thông báo rõ ràng về mức độ nguy hiểm của tin nhắn, kèm theo lý do và lời khuyên hữu ích.
*   **Chế độ chơi game:** Tự động tạm dừng quét nền khi người dùng đang chơi game nặng để đảm bảo hiệu suất thiết bị.
*   **Tích hợp AI mạnh mẽ:** Sử dụng mô hình ngôn ngữ lớn Google Gemini để hiểu ngữ cảnh và ý định của tin nhắn.
*   **Kiểm tra an toàn URL:** Tích hợp Google Safe Browsing để phát hiện các liên kết độc hại.

## 🚀 Bắt đầu

### Yêu cầu hệ thống

*   **Node.js:** Phiên bản 16 trở lên (khuyến nghị phiên bản LTS).
*   **npm** hoặc **Yarn:** Trình quản lý gói cho Node.js.
*   **Java Development Kit (JDK):** Phiên bản 17 trở lên.
*   **Android SDK:** Với các phiên bản API 24 (Android 7.0) đến 35 (Android 15).
*   **Python:** Phiên bản 3.8 trở lên.
*   **pip:** Trình quản lý gói cho Python.
*   **Git:** Để clone repository.

### Thiết lập môi trường

1.  **Cài đặt Node.js và npm/Yarn:** Tải từ trang chủ Node.js.
2.  **Cài đặt JDK:** Tải từ Oracle hoặc OpenJDK.
3.  **Cài đặt Android Studio:** Bao gồm Android SDK. Đảm bảo bạn đã cài đặt các phiên bản API cần thiết (24-35).
4.  **Cài đặt Python và pip:** Tải từ trang chủ Python.
5.  **Cài đặt Git:** Tải từ trang chủ Git.

## 📦 Cài đặt dự án

1.  **Clone Repository:**
    ```bash
    git clone https://github.com/pham-thai-duong-2010-vietnam/Cyber_Shield_backup.git
    cd Cyber_Shield_backup
    ```

2.  **Cài đặt Dependencies cho Frontend (React Native):**
    ```bash
    npm install # hoặc yarn install
    ```

3.  **Cài đặt Dependencies cho Backend (Python):**
    ```bash
    cd RenderBackend
    pip install -r requirements.txt
    cd ..
    ```

## ⚙️ Cấu hình

### API Keys

Ứng dụng yêu cầu các API Keys để hoạt động:

*   **Google Gemini API Key:**
    *   Truy cập [Google AI Studio](https://aistudio.google.com/app/apikey) để tạo API Key.
    *   Thiết lập biến môi trường `GOOGLE_API_KEYS` trên server backend của bạn (ví dụ: trong Render Dashboard hoặc file `.env` nếu chạy cục bộ). Nếu có nhiều key, phân tách bằng dấu phẩy (`,`).
*   **Google Safe Browsing API Key:**
    *   Truy cập [Google Cloud Console](https://console.cloud.google.com/) và kích hoạt Safe Browsing API.
    *   Tạo API Key và thiết lập biến môi trường `SAFE_BROWSING_API_KEY` trên server backend.

### URL Backend

Ứng dụng được cấu hình để kết nối với server backend triển khai trên Render. URL mặc định là:
`https://cybershield-backend-renderserver.onrender.com/api/analyze`

Nếu bạn triển khai backend ở một nơi khác, hãy cập nhật URL này trong:
*   `screens/HomeScreen.tsx` (biến `API_URL`)
*   `android/app/src/main/java/com/thaiduong/cybershield/analysis/AnalysisHandler.kt` (biến `ANALYSIS_API_URL`)

### Redis URL (Tùy chọn)

Nếu bạn muốn sử dụng Redis để caching kết quả phân tích (khuyến nghị cho production), hãy thiết lập biến môi trường `REDIS_URL` trên server backend của bạn.

## ▶️ Chạy ứng dụng (Môi trường phát triển)

1.  **Khởi động Backend Server (Python):**
    ```bash
    cd RenderBackend
    python app.py # hoặc gunicorn -c gunicorn.conf.py app:app
    cd ..
    ```
    *Lưu ý:* Để backend có thể truy cập được từ thiết bị di động trong mạng cục bộ, bạn có thể cần cấu hình `app.py` để lắng nghe trên `0.0.0.0` và đảm bảo tường lửa cho phép kết nối.

2.  **Khởi động ứng dụng React Native:**
    *   **Trên thiết bị Android:**
        ```bash
        npm run android # hoặc yarn android
        ```
    *   **Trên iOS Simulator/Thiết bị (nếu có):**
        ```bash
        npm run ios # hoặc yarn ios
        ```

## 📦 Build bản Release (Android)

Để tạo bản Release APK/AAB, bạn cần đảm bảo JavaScript bundle được tạo và ứng dụng được ký.

1.  **Đóng gói JavaScript Bundle:**
    Chạy script `.bat` đã được tạo để tự động hóa bước này:
    ```bash
    bundle_release_js.bat
    ```
    Script này sẽ tạo `index.android.bundle` và đặt các assets vào thư mục `android/app/src/main/assets` và `android/app/src/main/res/`.

2.  **Build Release APK/AAB:**
    *   **Cấu hình ký ứng dụng:** Đảm bảo bạn đã cấu hình file keystore và thông tin ký ứng dụng trong `android/app/build.gradle` (mục `signingConfigs { release { ... } }`).
    *   **Chạy lệnh build:**
        ```bash
        cd android
        ./gradlew assembleRelease # để build APK
        # hoặc
        ./gradlew bundleRelease # để build AAB (khuyến nghị cho Google Play)
        cd ..
        ```

## 💡 Hướng dẫn sử dụng ứng dụng

*   **Phân tích thủ công:** Mở ứng dụng, nhập/dán văn bản vào ô và nhấn "KIỂM TRA".
*   **Bảo vệ tự động:**
    *   Bật "Bảo Vệ Tự Động" trong ứng dụng.
    *   Cấp quyền "Truy cập Thông báo" và "Truy cập Dữ liệu Sử dụng" khi được yêu cầu (trên Android 13+, ứng dụng sẽ tự động hỏi quyền thông báo khi khởi chạy).
    *   Ứng dụng sẽ tự động quét tin nhắn đến từ Zalo, Messenger, Telegram và SMS, cũng như nội dung clipboard.
*   **Phân tích từ menu ngữ cảnh:** Chọn một đoạn văn bản trong bất kỳ ứng dụng nào, chọn "Chia sẻ" hoặc "Thêm" (tùy thiết bị), sau đó chọn "Cyber Shield" để phân tích.

## 🤝 Đóng góp

Mọi đóng góp để cải thiện Cyber Shield đều được hoan nghênh! Vui lòng mở một issue hoặc gửi pull request.

## 📧 Liên hệ

Nếu có bất kỳ câu hỏi hoặc cần hỗ trợ, vui lòng liên hệ [Địa chỉ email của bạn hoặc thông tin liên hệ khác].
