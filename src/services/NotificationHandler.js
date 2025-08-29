import PushNotification from 'react-native-push-notification';

// Hàm mới để gọi API backend trên Render
const analyzeTextWithAPI = async (text) => {
  try {
    const response = await fetch('https://cybershield-backend-renderserver.onrender.com/api/analyze', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ text: text }),
    });

    if (!response.ok) {
      // Nếu server trả về lỗi (status code không phải 2xx)
      const errorBody = await response.text();
      throw new Error(`Lỗi từ server: ${response.status} - ${errorBody}`);
    }

    const result = await response.json();
    return result;
  } catch (error) {
    console.error('[API Call] Lỗi khi gọi backend:', error);
    // Trả về null hoặc một object lỗi để hàm gọi có thể xử lý
    return null;
  }
};

PushNotification.configure({
    onRegister: function (token) { console.log("TOKEN:", token); },
    onNotification: function (notification) { console.log("NOTIFICATION:", notification); },
    permissions: { alert: true, badge: true, sound: true },
    popInitialNotification: true,
    requestPermissions: true,
});

PushNotification.createChannel({
    channelId: "cybershield-alerts",
    channelName: "Cảnh báo An ninh",
    channelDescription: "Kênh nhận cảnh báo từ CyberShield",
    playSound: true,
    soundName: "default",
    importance: 4, // Tương đương với NotificationManager.IMPORTANCE_HIGH
    vibrate: true,
}, (created) => console.log(`Kênh 'cybershield-alerts' đã được tạo: ${created}`));

export const handleNotification = async (taskData) => {
    console.log('[Headless Task] Nhận được thông báo:', taskData);
    const { text, title } = taskData;
    if (!text) return;

    try {
        // Thay thế hàm cũ bằng hàm gọi API mới
        const finalResult = await analyzeTextWithAPI(text);

        // Kiểm tra nếu finalResult không phải null và is_scam là true
        if (finalResult?.is_scam === true) {
            PushNotification.localNotification({
                channelId: "cybershield-alerts",
                title: `⚠️ Cảnh báo lừa đảo (${finalResult.types || 'N/A'})`,
                message: finalResult.reason || 'Phát hiện nội dung đáng ngờ.', // Fallback message
                bigText: `Phát hiện trong tin nhắn từ "${title}".\n\nLý do: ${finalResult.reason || 'Không có lý do cụ thể.'}\n\n👉 Khuyến nghị: ${finalResult.recommend || 'Cẩn thận với tin nhắn này.'}`,
                priority: "high",
                vibrate: true,
            });
        }
    } catch (error) {
        console.error('[Headless Task] Phân tích thất bại:', error.message);
    }
};