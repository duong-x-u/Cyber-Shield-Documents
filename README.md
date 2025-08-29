# CyberShield

> CyberShield: Ứng dụng Android (React Native) giúp phát hiện tin nhắn lừa đảo bằng AI, bảo vệ người dùng khỏi các mối nguy trên mạng.

## Về dự án

CyberShield là một công cụ bảo mật cho người dùng Android, được thiết kế để chủ động phát hiện và cảnh báo về các tin nhắn có nội dung lừa đảo. Ứng dụng có khả năng quét các tin nhắn đến từ nhiều nguồn khác nhau và phân tích chúng trong nền để đưa ra cảnh báo kịp thời.

## Tính năng chính

- **Phân tích tự động:** Tự động quét và phân tích tin nhắn đến từ các ứng dụng phổ biến (Zalo, Messenger, SMS...) và từ clipboard.
- **Phân tích thủ công:** Cho phép người dùng nhập trực tiếp văn bản đáng ngờ để kiểm tra.
- **Tích hợp menu ngữ cảnh:** Phân tích nhanh bất kỳ đoạn văn bản nào bằng cách bôi đen và chọn "Phân tích với CyberShield".
- **Chế độ chơi game:** Tự động tạm dừng các tác vụ quét nền khi người dùng đang chơi game để đảm bảo hiệu năng.
- **Cảnh báo tức thì:** Gửi thông báo hệ thống ngay lập-tức khi phát hiện nội dung có dấu hiệu nguy hiểm.

## Công nghệ sử dụng

- **Frontend:** React Native
- **Native Modules (Android):** Kotlin
- **Backend:** Python (Flask/FastAPI)

## Bắt đầu

Để chạy dự án, hãy đảm bảo bạn đã cài đặt môi trường cho React Native.

1.  **Cài đặt các gói phụ thuộc:**
    ```bash
    npm install
    ```
2.  **Khởi động Metro Bundler:**
    ```bash
    npm start
    ```
3.  **Chạy ứng dụng trên Android:**
    ```bash
    npm run android
    ```