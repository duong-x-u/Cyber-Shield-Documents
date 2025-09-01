import 'react-native-gesture-handler';
import React from 'react';
import type {PropsWithChildren} from 'react';
import {
  SafeAreaView,
  StatusBar,
  StyleSheet,
  useColorScheme,
  View,
} from 'react-native';
import {Colors} from 'react-native/Libraries/NewAppScreen';
import {GestureHandlerRootView} from 'react-native-gesture-handler';
import {SafeAreaProvider} from 'react-native-safe-area-context';
import {NavigationContainer} from '@react-navigation/native';
import {createStackNavigator} from '@react-navigation/stack';
import HomeScreen from './screens/HomeScreen';

// Define param list cho stack navigator
export type RootStackParamList = {
  'CyberShield 🛡️': undefined;
};

const Stack = createStackNavigator<RootStackParamList>();

function MainStack(): React.JSX.Element {
  return (
    <Stack.Navigator
      screenOptions={{
        headerStyle: {backgroundColor: '#1e1e2f'},
        headerTintColor: '#fff',
        headerTitleAlign: 'center',
        headerTitleStyle: {fontWeight: 'bold'},
      }}>
      <Stack.Screen name="CyberShield 🛡️" component={HomeScreen} />
    </Stack.Navigator>
  );
}

function App(): React.JSX.Element {
  const isDarkMode = useColorScheme() === 'dark';

  const backgroundStyle = {
    flex: 1,
    backgroundColor: isDarkMode ? Colors.darker : '#f0f2f5',
  };

  return (
    <GestureHandlerRootView style={styles.flex}>
      <SafeAreaProvider>
        <SafeAreaView style={backgroundStyle}>
          <StatusBar
            barStyle={isDarkMode ? 'light-content' : 'dark-content'}
            backgroundColor="#1e1e2f"
          />
          <View style={styles.flex}>
            <NavigationContainer>
              <MainStack />
            </NavigationContainer>
          </View>
        </SafeAreaView>
      </SafeAreaProvider>
    </GestureHandlerRootView>
  );
}

const styles = StyleSheet.create({
  flex: {
    flex: 1,
  },
});

export default App;
