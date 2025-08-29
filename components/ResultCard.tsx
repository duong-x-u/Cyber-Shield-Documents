import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

export default function ResultCard({ result }: { result: any }) {
  if (!result) return null;

  const getScoreColor = (score: number) => {
    if (score >= 4) return '#D32F2F';
    if (score >= 3) return '#FBC02D';
    return '#388E3C';
  };

  const isScam = result.is_scam === true;
  const hasUrlMatches = result.url_analysis && result.url_analysis.length > 0;

  return (
    <View style={[styles.card, { borderColor: isScam ? '#D32F2F' : '#4CAF50' }]}>
      <Text style={styles.title}>Kết Quả Phân Tích 🧠</Text>
      <View style={styles.row}>
        <Text style={styles.label}>Trạng thái:</Text>
        <Text style={[styles.value, { color: isScam ? '#D32F2F' : '#388E3C', fontWeight: 'bold' }]}>
          {isScam ? 'CÓ DẤU HIỆU LỪA ĐẢO' : 'TRÔNG CÓ VẺ AN TOÀN'}
        </Text>
      </View>
      {isScam && (
        <>
          <View style={styles.row}>
            <Text style={styles.label}>Loại hình:</Text>
            <Text style={styles.value}>{result.types || 'Chưa xác định'}</Text>
          </View>
          <View style={styles.row}>
            <Text style={styles.label}>Lý do:</Text>
            <Text style={styles.value}>{result.reason || 'Không có'}</Text>
          </View>
          <View style={styles.row}>
            <Text style={styles.label}>Mức độ nguy hiểm:</Text>
            <Text style={[styles.value, { color: getScoreColor(result.score), fontWeight: 'bold' }]}>
              {result.score || 'N/A'} / 5
            </Text>
          </View>
          
          {/* Phần hiển thị URL nguy hiểm */}
          {hasUrlMatches && (
            <View style={styles.urlSection}>
              <Text style={styles.urlSectionTitle}>Phân Tích Đường Link Không An Toàn</Text>
              {result.url_analysis.map((match: any, index: number) => (
                <View key={index} style={styles.urlMatch}>
                  <Text style={styles.urlText} numberOfLines={1} ellipsizeMode="middle">- {match.threat.url}</Text>
                  <Text style={styles.threatType}>{match.threatType}</Text>
                </View>
              ))}
            </View>
          )}

          <View style={[styles.row, styles.recommendation]}>
            <Text style={styles.label}>👉 Khuyến nghị:</Text>
            <Text style={styles.value}>{result.recommend || 'Hãy luôn cẩn trọng.'}</Text>
          </View>
        </>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    marginVertical: 20,
    elevation: 4,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 6,
    borderWidth: 2,
  },
  title: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 12,
    color: '#1c1e21',
    textAlign: 'center',
    borderBottomWidth: 1,
    borderBottomColor: '#e4e6eb',
    paddingBottom: 8,
  },
  row: {
    flexDirection: 'row',
    marginBottom: 8,
  },
  label: {
    fontSize: 15,
    color: '#606770',
    fontWeight: 'bold',
    marginRight: 8,
    flexShrink: 0,
  },
  value: {
    fontSize: 15,
    color: '#1c1e21',
    flex: 1,
  },
  recommendation: {
    marginTop: 10,
    paddingTop: 10,
    borderTopWidth: 1,
    borderTopColor: '#e4e6eb',
    flexDirection: 'column',
  },
  // Styles cho phần URL
  urlSection: {
    marginTop: 10,
    paddingTop: 10,
    borderTopWidth: 1,
    borderTopColor: '#e4e6eb',
  },
  urlSectionTitle: {
    fontSize: 15,
    fontWeight: 'bold',
    color: '#D32F2F',
    marginBottom: 5,
  },
  urlMatch: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 3,
  },
  urlText: {
    fontSize: 14,
    color: '#333',
    flex: 1,
    marginRight: 10,
  },
  threatType: {
    fontSize: 12,
    color: '#fff',
    backgroundColor: '#D32F2F',
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 8,
    fontWeight: 'bold',
    overflow: 'hidden',
  },
});