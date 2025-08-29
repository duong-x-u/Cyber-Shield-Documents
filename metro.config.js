const {getDefaultConfig, mergeConfig} = require('@react-native/metro-config');
const path = require('path');

/**
 * Metro configuration
 * https://reactnative.dev/docs/metro
 *
 * @type {import('metro-config').MetroConfig}
 */
const config = {
  resolver: {
    sourceExts: ['jsx', 'js', 'ts', 'tsx', 'json', 'node'],
  },
  // watchFolders has been temporarily disabled to debug nodejs-mobile-react-native
  // watchFolders: [
  //   path.resolve(__dirname, 'nodejs-assets'),
  // ],
};

module.exports = mergeConfig(getDefaultConfig(__dirname), config);