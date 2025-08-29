// frontend_react_native/components/InputBox.jsx
import React from 'react';
import { TextInput, StyleSheet, View } from 'react-native';

export default function InputBox({ value, onChange }) {
  return (
    <View style={styles.container}>
      <TextInput
        style={styles.input}
        placeholder="Dán nội dung cần kiểm tra..."
        placeholderTextColor="#999"
        value={value}
        onChangeText={onChange}
        multiline
        numberOfLines={4}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { marginVertical: 10 },
  input: {
    backgroundColor: '#f0f0f0',
    borderRadius: 10,
    padding: 12,
    fontSize: 16,
    color: '#000',
    textAlignVertical: 'top'
  },
});
