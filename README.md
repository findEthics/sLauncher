# sLauncher

A modern minimalist Android launcher with advanced customization features and essential information at your fingertips.

## Features

### 🎨 **Dual Layout Modes**
- **List View**: Clean text-based app names in vertical list (24sp bold, center-aligned)
- **Grid View**: Traditional icon-based grid layout with flexible configurations (1×2, 2×2, 2×3, 2×4)

### ⚡ **Essential Information Display** 
- **Time Display**: Prominent 24-hour format time (26sp bold) with tap-to-open clock functionality
- **Date Display**: Current date in readable format (22sp) with smart calendar integration  
- **Battery Indicator**: Real-time battery percentage (18sp) with automatic updates
- **Smart Hierarchy**: Information organized by importance for quick at-a-glance viewing

### 🌙 **Advanced Theming**
- **Dark Mode Support**: Complete light/dark theme switching with instant application
- **Theme Persistence**: Your theme preference saved and restored across app sessions
- **Theme-Aware Icons**: All UI elements including settings icon adapt to current theme
- **System Integration**: Proper status bar styling for both light and dark modes

### ⚙️ **Flexible Configuration**
- **Dynamic App Count**: Choose 2, 4, 6, or 8 apps for home screen (grid view only)
- **Grid Configurations**: 1×2, 2×2, 2×3, 2×4 layouts with automatic column/row adjustment
- **Persistent Settings**: All customizations saved and restored automatically
- **Settings Integration**: Unified settings dialog accessible from All Apps screen

### 📱 **Smart App Management**
- **Persistent App Selection**: Selected apps remain saved across restarts and force closes
- **Long Press Selection**: Easy app changing via long press on any slot
- **All Apps Access**: Full app list with real-time search functionality
- **Intelligent App Detection**: Smart clock app detection with multiple fallbacks

### 🔧 **System Integration**
- **Proper Launcher**: Functions as complete Android launcher with home button support
- **Boot Integration**: Starts automatically on device boot
- **Memory Optimized**: Efficient app loading and minimal resource usage
- **Global Application**: Theme persistence handled at application level

## Screenshots

*Screenshots coming soon...*

## Installation

### From Source

1. Clone the repository:
   ```bash
   git clone https://github.com/findEthics/sLauncher.git
   cd sLauncher
   ```

2. Open the project in Android Studio

3. Build and install:
   ```bash
   ./gradlew assembleDebug
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

### Setting as Default Launcher

1. After installation, press the home button
2. Select "sLauncher" from the launcher options
3. Choose "Always" to set as default

## Usage

### Setting Up App Shortcuts
- **Empty slots**: Tap any empty app slot to select an app from the installed apps list
- **Changing apps**: Long press any app icon to change the selected app
- **Launching apps**: Single tap on configured app icons to launch them
- **Persistent storage**: Your selected apps are automatically saved and restored when the launcher restarts

### Accessing All Apps
- Tap the hamburger menu button (three horizontal lines) in the bottom right corner
- Select any app from the list to launch it

### Calendar Access
- Tap the date display at the top to open your calendar app
- Prioritizes Mudita Calendar if installed, otherwise opens the default calendar

## Permissions

The app requires the following permissions:

- `RECEIVE_BOOT_COMPLETED`: Start launcher on device boot
- `WRITE_SETTINGS`: Modify system settings (launcher functionality)

The app also uses `<queries>` declaration to access information about installed apps with launcher intents.

## Technical Details

- **Minimum SDK**: API 21 (Android 5.0)
- **Target SDK**: API 34 (Android 14)
- **Language**: Kotlin
- **Architecture**: Single Activity with XML layouts
- **Data Storage**: SharedPreferences for lightweight app selection persistence
- **Memory Optimization**: Efficient app loading and icon caching

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/new-feature`)
3. Commit your changes (`git commit -am 'Add new feature'`)
4. Push to the branch (`git push origin feature/new-feature`)
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Changelog

### v2.0.0 (Current - Grid View)
- **Grid Layout**: Traditional icon-based grid with flexible configurations (1×2, 2×2, 2×3, 2×4)
- **App Icons**: Display actual app icons instead of text names
- **Dynamic Grid**: Automatic row/column adjustment based on app count
- **Enhanced Settings**: Grid-specific options with visual configuration info
- **Icon Optimization**: 80dp icons with proper spacing and theme-aware backgrounds

### v1.5.0 (List View)  
- **List Layout**: Clean vertical list of app names with center alignment
- **Time Display**: 24-hour format time with clock app integration
- **Battery Indicator**: Real-time battery percentage with auto-updates  
- **Dark Mode**: Complete theme switching with theme-aware UI elements
- **Enhanced Typography**: Improved font sizes and styling hierarchy
- **Settings Dialog**: Unified settings for app count and theme selection
- **Smart Integration**: Calendar and clock app detection with fallbacks

### v1.1.0
- Added SharedPreferences persistence for app selections
- App choices now survive force closes and restarts
- Improved memory management for lightweight performance
- Enhanced user experience with persistent app configuration

### v1.0.0
- Initial release
- 3x2 app grid layout
- Date display with calendar integration
- All apps menu
- Basic launcher functionality

## Support

If you encounter any issues or have suggestions, please [open an issue](https://github.com/findEthics/sLauncher/issues) on GitHub.

## Acknowledgments

- Inspired by minimalist launcher designs
- Built with Android's modern development practices