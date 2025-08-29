// frontend_react_native/styles/main.js
import { StyleSheet } from 'react-native';

const styles = StyleSheet.create({
  container: {
    padding: 16,
    backgroundColor: '#f4f4f4',
    flex: 1,
  },
  inputBox: {
    borderWidth: 1,
    borderColor: '#ccc',
    borderRadius: 8,
    padding: 12,
    backgroundColor: '#fff',
    marginBottom: 12,
  },
  resultCard: {
    padding: 16,
    borderRadius: 10,
    backgroundColor: '#fff',
    elevation: 3,
    marginBottom: 12,
  },
  resultText: {
    fontSize: 16,
    color: '#333',
  },
  riskScore: {
    fontWeight: 'bold',
    color: 'red',
  },
});

export default styles;
