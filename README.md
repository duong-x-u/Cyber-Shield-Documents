
# README - Phân tích chuyên sâu Ứng dụng NewCyberShield

Tài liệu này phân tích chi tiết dự án "NewCyberShield", một ứng dụng bảo mật cho Android. Nội dung được trình bày dưới hai góc độ: góc độ người dùng phổ thông và góc độ kỹ thuật dành cho nhà phát triển.

---

## 1. Tổng quan (Dành cho mọi người dùng)

### Ứng dụng này làm gì?
**NewCyberShield** là một "người vệ sĩ" kỹ thuật số cho điện thoại Android của bạn. Chức năng chính của nó là tự động đọc và phân tích các tin nhắn đến từ các ứng dụng chat phổ biến (như Zalo, Messenger, Telegram, SMS...) để phát hiện và cảnh báo bạn về các nội dung nguy hiểm như link lừa đảo (phishing), tin nhắn rác, hoặc các chiêu trò lừa đảo khác.

Nói một cách đơn giản: Thay vì bạn phải tự hỏi "tin nhắn này có an toàn không?", NewCyberShield sẽ làm việc đó cho bạn một cách tự động và âm thầm.

### Lợi ích chính
- **An tâm hơn:** Giảm thiểu rủi ro bị lừa đảo qua mạng, mất tài khoản mạng xã hội, hoặc mất tiền trong tài khoản ngân hàng.
- **Tiết kiệm thời gian:** Không cần phải đắn đo, suy nghĩ về mỗi tin nhắn hay đường link lạ bạn nhận được.
- **Bảo vệ tự động:** Ứng dụng hoạt động nền, bảo vệ bạn 24/7 mà không cần bạn phải mở lên thường xuyên.

### Cách hoạt động (phiên bản đơn giản)
1.  **Nhận tin nhắn:** Khi bạn có tin nhắn mới từ Zalo, Messenger...
2.  **Lặng lẽ kiểm tra:** NewCyberShield sẽ đọc nội dung tin nhắn đó trong nền.
3.  **Gửi đi phân tích:** Nội dung được gửi đến một "trung tâm phân tích" (server chạy trên Raspberry Pi) để kiểm tra độ an toàn.
4.  **Cảnh báo (nếu cần):** Nếu tin nhắn bị phát hiện là nguy hiểm, ứng dụng sẽ ngay lập tức gửi một thông báo cảnh báo "Nguy hiểm!" trên điện thoại của bạn. Nếu tin nhắn an toàn, nó sẽ không làm gì cả.

---

## 2. Phân tích từ góc độ Người dùng & Giao diện (UI/UX)

### Giao diện người dùng (GUI)
Giao diện của NewCyberShield được thiết kế tối giản và tập trung vào một nhiệm vụ duy nhất, thể hiện qua các thành phần chính trên màn hình `HomeScreen`:
- **Nút bật/tắt bảo vệ:** Một công tắc lớn, rõ ràng cho phép người dùng kích hoạt hoặc vô hiệu hóa chế độ bảo vệ tự động bất cứ lúc nào.
- **Ô nhập liệu thủ công:** Cho phép người dùng dán (paste) bất kỳ đoạn văn bản hoặc đường link nào họ nghi ngờ để ứng dụng phân tích ngay lập tức.
- **Thẻ kết quả:** Khi một phân tích hoàn tất (dù là tự động hay thủ công), kết quả sẽ được hiển thị trên một thẻ (`ResultCard`) cung cấp thông tin chi tiết về mối nguy hiểm (nếu có).

### Trải nghiệm người dùng (UX)

#### Ưu điểm:
- **Dễ sử dụng:** Chỉ với một nút bấm, người dùng không rành công nghệ cũng có thể tự bảo vệ mình.
- **Phản hồi tức thì:** Cảnh báo nguy hiểm xuất hiện dưới dạng thông báo đẩy quen thuộc, giúp người dùng hành động kịp thời.
- **"Set it and forget it":** Sau khi bật, người dùng gần như không cần tương tác thêm, ứng dụng tự lo phần còn lại.

#### Nhược điểm & Lo ngại:
- **Yêu cầu quyền truy cập nhạy cảm:** Để hoạt động, ứng dụng cần được cấp quyền "Truy cập thông báo" (Notification Access). Đây là một quyền rất mạnh, có thể khiến người dùng lo ngại về quyền riêng tư vì ứng dụng có thể đọc toàn bộ thông báo của họ. Cần có một chính sách bảo mật rất rõ ràng và minh bạch.
- **Pin:** Việc một dịch vụ chạy nền liên tục có thể gây ảnh hưởng đến thời lượng pin của thiết bị.
- **Sự tin tưởng:** Người dùng phải đặt niềm tin tuyệt đối vào ứng dụng và máy chủ phân tích, tin rằng tin nhắn của họ sẽ không bị lạm dụng hoặc rò rỉ.

---

## 3. Phân tích Kỹ thuật (Dành cho Nhà phát triển)

### 3.1. Kiến trúc tổng thể & Luồng dữ liệu

Kiến trúc của NewCyberShield là một mô hình Hybrid thông minh, kết hợp điểm mạnh của nhiều công nghệ:

`Client (Điện thoại Android)` <--> `Server (Raspberry Pi)`

**Luồng dữ liệu phân tích tự động:**

1.  **Thu thập (Kotlin):** `NotificationListener.kt` (một `Service` của Android) chạy nền, lắng nghe sự kiện `onNotificationPosted`. Nếu thông báo đến từ một trong các app mục tiêu (Zalo, Messenger...), nó sẽ trích xuất nội dung text.
2.  **Yêu cầu Phân tích (Kotlin):** `AnalysisHandler.kt` nhận text từ `NotificationListener`, sau đó tạo một `POST request` đến endpoint `/api/analyze` của server.
3.  **Xử lý (Python/Flask):** Server trên Raspberry Pi nhận yêu cầu và thực hiện quy trình phân tích đa tầng:
    - **Tầng 0 (Lọc trước):** Bỏ qua các tin nhắn vô nghĩa, quá ngắn.
    - **Tầng 1 (Quét URL):** Dùng API của **VirusTotal** để quét tất cả các URL có trong tin nhắn. Nếu có URL độc hại, trả về kết quả nguy hiểm ngay.
    - **Tầng 2 (Phân tích AI):** Nếu không có URL độc hại, toàn bộ nội dung text được gửi đến AI tạo sinh (**Google Gemini**, có dự phòng **ChatGPT**) để phân tích sâu về ngữ cảnh, phát hiện các dấu hiệu lừa đảo tinh vi.
4.  **Cảnh báo (Kotlin):** `AnalysisHandler.kt` nhận phản hồi JSON từ server. Nếu kết quả là "nguy hiểm", nó sẽ tạo một thông báo đẩy (local notification) trên thiết bị với tiêu đề cảnh báo. Dữ liệu phân tích chi tiết được cache lại.
5.  **Hiển thị (React Native):** Nếu người dùng bấm vào thông báo cảnh báo, ứng dụng sẽ mở ra màn hình `HomeScreen`. `HomeScreen.tsx` sẽ gọi qua `ControlModule.kt` (Native Bridge) để lấy dữ liệu phân tích đã cache và hiển thị chi tiết cho người dùng.

### 3.2. Client (React Native & Android Native)

- **React Native (`.tsx`):**
  - Chịu trách nhiệm chính về giao diện người dùng và quản lý trạng thái phía người dùng.
  - `HomeScreen.tsx` là trung tâm, điều khiển việc bật/tắt dịch vụ nền và hiển thị kết quả.
  - Tương tác với lớp Native qua một cầu nối (Bridge) là `ControlModule.kt`.

- **Android Native (Kotlin):**
  - Đây là "bộ não" của client, thực hiện các tác vụ nặng và yêu cầu quyền hệ thống.
  - `ControlService.kt`: Là `Service` chạy nền, đảm bảo `NotificationListener` luôn hoạt động kể cả khi app đã bị đóng.
  - `NotificationListener.kt`: Thành phần cốt lõi, lắng nghe và trích xuất dữ liệu từ thông báo hệ thống.
  - `AnalysisHandler.kt`: Đóng gói logic gọi API đến server và tạo thông báo cảnh báo.
  - `ControlModule.kt`: Là một `ReactContextBaseJavaModule`, nó "phơi" các hàm của Kotlin (như `startControlService`, `stopControlService`, `getAndClearAnalysisResultById`) để phía JavaScript có thể gọi được.

### 3.3. Server (Python & Raspberry Pi)

- **Công nghệ:**
  - **Framework:** Python + Flask.
  - **API:** Cung cấp endpoint chính là `/api/analyze` (`analyze.py`).
  - **Phân tích:** Tích hợp với các dịch vụ bên thứ ba: VirusTotal API, Google Gemini API, OpenAI (ChatGPT) API.

- **Hạ tầng:**
  - **Thiết bị:** Chạy trên bo mạch **Raspberry Pi 5**. Lựa chọn này cho thấy định hướng xây dựng một giải pháp tự lưu trữ (self-hosted), chi phí thấp, và đề cao quyền riêng tư (dữ liệu được xử lý trên thiết bị của chính người dùng).
  - **Live Server:** Sử dụng **Cloudflare Tunnel** để public endpoint của server chạy trên mạng nội bộ ra Internet một cách an toàn. Giải pháp này rất thông minh, không cần IP tĩnh, không cần mở port trên router, đồng thời được hưởng lợi từ các lớp bảo vệ của Cloudflare.

---

## 4. Đánh giá Ưu điểm và Nhược điểm (Góc nhìn Kỹ thuật)

### Ưu điểm (Strengths)
- **Kiến trúc tách biệt (Decoupled):** Client và Server hoàn toàn độc lập, giúp dễ dàng bảo trì, nâng cấp từng phần. Server có thể được cập nhật logic phân tích mà không cần người dùng phải cập nhật ứng dụng.
- **Hiệu quả:** Tận dụng thế mạnh của từng nền tảng: Kotlin xử lý các tác vụ hệ thống sâu của Android, React Native cho UI/UX nhanh và hiện đại, Python cho xử lý logic và AI mạnh mẽ phía server.
- **Logic phân tích đa tầng:** Việc kết hợp giữa quét URL và phân tích AI tạo ra một hệ thống phòng thủ có chiều sâu, có khả năng phát hiện nhiều loại mối đe dọa khác nhau.
- **Chi phí thấp & Riêng tư:** Mô hình self-host trên Raspberry Pi giúp giảm đáng kể chi phí vận hành và tăng cường quyền riêng tư khi người dùng có thể tự kiểm soát server của mình.
- **Sử dụng Cloudflare Tunnel:** Một lựa chọn kỹ thuật xuất sắc để triển khai server một cách an toàn, ổn định và đơn giản.

### Nhược điểm & Rủi ro (Weaknesses & Risks)
- **Single Point of Failure:** Server trên Raspberry Pi là một điểm lỗi duy nhất. Nếu bo mạch hỏng, mất điện, hoặc mất kết nối mạng, toàn bộ hệ thống sẽ ngừng hoạt động.
- **Khả năng mở rộng (Scalability):** Giải pháp hiện tại phù hợp cho người dùng cá nhân hoặc nhóm nhỏ. Nếu số lượng người dùng tăng đột biến, một chiếc Raspberry Pi sẽ không thể xử lý kịp lượng request.
- **Bảo mật của Server:** Server chạy trên Raspberry Pi cần được cấu hình và bảo mật cẩn thận. Dù có Cloudflare Tunnel, các lỗ hổng trong mã nguồn Flask hoặc hệ điều hành vẫn có thể bị khai thác.
- **Quyền riêng tư:** Mặc dù là self-hosted, nhưng việc gửi toàn bộ nội dung tin nhắn (kể cả tin nhắn nhạy cảm) đến server dưới dạng clear-text (văn bản thuần) là một rủi ro tiềm tàng nếu kênh truyền không được mã hóa end-to-end một cách cẩn thận hoặc server bị xâm nhập.
- **Bảo trì mô hình AI:** Logic phân tích của AI có thể bị "lỗi thời" trước các chiêu trò lừa đảo mới. Cần có cơ chế để cập nhật prompt hoặc tinh chỉnh mô hình.

---

## 5. Đề xuất & Hướng phát triển

1.  **Phân tích trên thiết bị (On-device Analysis):**
    - Tích hợp một mô hình Machine Learning nhỏ (ví dụ: dùng TensorFlow Lite) ngay trên điện thoại để thực hiện một bước lọc sơ bộ. Các tin nhắn rõ ràng an toàn sẽ không cần gửi đến server, giúp giảm tải cho server, tăng tốc độ và cải thiện đáng kể quyền riêng tư.
2.  **Mã hóa End-to-End:**
    - Triển khai một cặp khóa Public/Private cho mỗi client. Tin nhắn nên được mã hóa trên điện thoại bằng khóa công khai của server trước khi gửi đi, và chỉ server mới có thể giải mã. Điều này đảm bảo ngay cả khi dữ liệu bị chặn trên đường truyền, nó vẫn vô dụng.
3.  **Giao diện Dashboard:**
    - Xây dựng một trang trên app để người dùng xem lại lịch sử các lần quét và cảnh báo, giúp họ có cái nhìn tổng quan về các mối nguy đã bị chặn.
4.  **Cơ chế Whitelist/Blacklist:**
    - Cho phép người dùng thêm các số điện thoại hoặc từ khóa mà họ tin tưởng (whitelist) để không phân tích, hoặc các từ khóa họ luôn muốn chặn (blacklist).
5.  **Cải thiện cơ chế fallback:**
    - Nếu server không phản hồi (do mất kết nối), ứng dụng nên có một cơ chế cảnh báo người dùng rằng "Chế độ bảo vệ đang tạm thời gián đoạn".
