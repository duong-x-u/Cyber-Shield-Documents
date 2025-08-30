import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, Switch, Button, Alert, NativeModules, DeviceEventEmitter, ScrollView } from 'react-native';
import InputBox from '../components/InputBox';
import ResultCard from '../components/ResultCard';
import LoadingOverlay from '../components/LoadingOverlay';

const { ControlModule } = NativeModules;
const API_URL = 'https://cybershield-backend-renderserver.onrender.com/api/analyze';
const ANALYSIS_TIMEOUT = 20000; // 20 giây

const URL_REGEX = /(https?:\/\/[^\s]+)/g;

const openAppSettings = async (settingType: 'notification' | 'usage') => {
    try {
        await ControlModule.openSpecificAppSettings(settingType);
    } catch (e: any) {
        if (e.code === 'E_USAGE_ACCESS_NOT_FOUND') {
            Alert.alert(
                'Hướng Dẫn Cấp Quyền Thủ Công',
                `Không thể mở trực tiếp. Vui lòng cấp quyền thủ công theo các đường dẫn phổ biến dưới đây:

                📱 OPPO / Realme:
                Cài đặt > Quản lý ứng dụng > Truy cập ứng dụng đặc biệt > Truy cập dữ liệu sử dụng

                📱 Samsung:
                Cài đặt > Ứng dụng > (dấu 3 chấm ⋮) > Truy cập đặc biệt > Quyền truy cập sử dụng

                📱 Xiaomi:
                Cài đặt > Bảo vệ quyền riêng tư > Quyền đặc biệt > Truy cập dữ liệu sử dụng

                📱 Android Gốc (Pixel, ...):
                Cài đặt > Ứng dụng > Quyền truy cập đặc biệt của ứng dụng > Truy cập dữ liệu sử dụng

                (Tên các mục có thể thay đổi một chút tùy phiên bản)`,
                [{ text: 'Đã hiểu' }]
            );
        } else {
            Alert.alert('Lỗi', `Không thể mở màn hình Cài đặt. Vui lòng thử cấp quyền thủ công trong Cài đặt của máy.\nLỗi: ${e.message}`);
        }
    }
};

export default function HomeScreen() {
    const [inputText, setInputText] = useState('');
    const [analysisResult, setAnalysisResult] = useState(null);
    const [isLoading, setIsLoading] = useState(false);
    const [loadingMessage, setLoadingMessage] = useState('Đang phân tích...');
    const [error, setError] = useState<string | null>(null);
    const [isProtectionEnabled, setIsProtectionEnabled] = useState(false);

    useEffect(() => {
        const processTextSubscription = DeviceEventEmitter.addListener('onProcessText', async (event) => {
            const text = event.text;
            if (text) {
                setInputText(text);
                // Automatically trigger analysis for text received from the context menu
                handleAnalysis(text, true);
            }
        });

        return () => {
            processTextSubscription.remove();
        };
    }, []);

    const handleAnalysis = async (text: string, isAuto: boolean = false) => {
        if (!text.trim()) {
            if (!isAuto) Alert.alert("Lỗi", "Vui lòng nhập nội dung cần phân tích.");
            return;
        }
        
        setLoadingMessage(isAuto ? 'Tự động quét văn bản...' : 'Đang phân tích...');
        setIsLoading(true);
        setError(null);
        setAnalysisResult(null);

        const urls = text.match(URL_REGEX) || [];

        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), ANALYSIS_TIMEOUT);

        try {
                  const response = await fetch('https://cybershield-backend-renderserver.onrender.com/api/analyze', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },

            clearTimeout(timeoutId);

            if (!response.ok) {
                const errData = await response.json();
                throw new Error(errData.error || `Lỗi máy chủ: ${response.status}`);
            }

            const result = await response.json();
            setAnalysisResult(result.result);

            // For text from context menu, also show a notification
            if (isAuto) {
                ControlModule.showAnalysisNotification(result.result);
            }

        } catch (e: any) {
            clearTimeout(timeoutId);
            let errorMessage = 'Đã có lỗi xảy ra khi kết nối tới máy chủ.';
            if (e.name === 'AbortError') {
                errorMessage = 'Yêu cầu phân tích đã hết thời gian. Vui lòng thử lại.';
            }
            setError(errorMessage);
            if (!isAuto) {
                Alert.alert("Lỗi Phân Tích", errorMessage);
            }
        } finally {
            setIsLoading(false);
        }
    };

    const toggleProtection = async (value: boolean) => {
        if (value) {
            try {
                const permissions = await ControlModule.checkPermissions();
                if (!permissions.notificationAccess || !permissions.usageAccess) {
                    let missingPermissions = [];
                    if (!permissions.notificationAccess) missingPermissions.push("Truy cập Thông báo");
                    if (!permissions.usageAccess) missingPermissions.push("Truy cập Dữ liệu Sử dụng");
                    
                    Alert.alert(
                        'Thiếu quyền quan trọng',
                        `Để bật bảo vệ, bạn cần cấp quyền: ${missingPermissions.join(' và ')}.\n\nVui lòng vào mục cấp quyền bên dưới để bật.`,
                        [{ text: 'Đã hiểu' }]
                    );
                    return;
                }

                Alert.alert(
                    'Xác nhận Bật Bảo vệ',
                    'Bạn có chắc chắn muốn bật tính năng quét thông báo tự động không?',
                    [
                        { text: 'Hủy', style: 'cancel' },
                        { 
                            text: 'OK, Bật', 
                            onPress: () => {
                                ControlModule.startControlService();
                                setIsProtectionEnabled(true);
                            } 
                        }
                    ]
                );

            } catch (err) {
                Alert.alert("Lỗi", "Không thể kiểm tra quyền của ứng dụng.");
            }
        } else {
            ControlModule.stopControlService();
            setIsProtectionEnabled(false);
        }
    };

    return (
        <ScrollView style={styles.container}>
            <LoadingOverlay visible={isLoading} message={loadingMessage} />
            <Text style={styles.title}>CyberShield</Text>
            <Text style={styles.description}>
                Nhập hoặc dán tin nhắn, đường link đáng ngờ vào ô dưới đây để kiểm tra.
            </Text>
            <InputBox value={inputText} onChange={setInputText} />
            <View style={styles.buttonContainer}>
                 <Button title="KIỂM TRA" onPress={() => handleAnalysis(inputText)} disabled={isLoading} />
            </View>
            
            {error && <Text style={styles.errorText}>{error}</Text>}
            <ResultCard result={analysisResult} />

            <View style={styles.card}>
                <Text style={styles.cardTitle}>Bảo Vệ Tự Động</Text>
                <Text style={styles.cardDescription}>
                    Tự động quét tin nhắn từ Zalo, Messenger... và cảnh báo nếu có nguy hiểm.
                </Text>
                <View style={styles.switchContainer}>
                    <Text style={[styles.status, { color: isProtectionEnabled ? '#4CAF50' : '#F44336' }]}>
                        {isProtectionEnabled ? 'ĐANG BẬT' : 'ĐANG TẮT'}
                    </Text>
                    <Switch
                        trackColor={{ false: '#767577', true: '#81b0ff' }}
                        thumbColor={isProtectionEnabled ? '#1976D2' : '#f4f3f4'}
                        onValueChange={toggleProtection}
                        value={isProtectionEnabled}
                    />
                </View>
                 <Text style={styles.permissionDescription}>
                    Nếu tính năng không hoạt động, hãy cấp đủ quyền cho ứng dụng:
                </Text>
                <View style={styles.permissionButtonContainer}>
                    <Button title="Quyền Thông Báo" onPress={() => openAppSettings('notification')} />
                    <Button title="Quyền Sử Dụng" onPress={() => openAppSettings('usage')} />
                </View>
            </View>
        </ScrollView>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        padding: 16,
        backgroundColor: '#f0f2f5',
    },
    title: {
        fontSize: 28,
        fontWeight: 'bold',
        textAlign: 'center',
        marginBottom: 8,
        color: '#1c1e21',
    },
    description: {
        fontSize: 16,
        color: '#606770',
        textAlign: 'center',
        lineHeight: 22,
        marginBottom: 20,
    },
    buttonContainer: {
        marginVertical: 10,
    },
    errorText: {
        color: 'red',
        textAlign: 'center',
        marginVertical: 10,
    },
    card: {
        backgroundColor: '#fff',
        borderRadius: 12,
        padding: 20,
        marginTop: 20,
        elevation: 3,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.1,
        shadowRadius: 4,
    },
    cardTitle: {
        fontSize: 20,
        fontWeight: 'bold',
        marginBottom: 8,
        color: '#1c1e21',
    },
    cardDescription: {
        fontSize: 15,
        color: '#606770',
        lineHeight: 22,
        marginBottom: 16,
    },
    permissionDescription: {
        fontSize: 14,
        color: '#606770',
        lineHeight: 20,
        marginTop: 16,
        textAlign: 'center',
    },
    switchContainer: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        paddingTop: 10,
        borderTopWidth: 1,
        borderTopColor: '#e4e6eb',
    },
    status: {
        fontSize: 16,
        fontWeight: 'bold',
    },
    permissionButtonContainer: {
        flexDirection: 'row',
        justifyContent: 'space-around',
        marginTop: 10,
    },
});