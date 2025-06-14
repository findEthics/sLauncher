# sLauncher

A minimalist Android launcher focused on simplicity and essential functionality.

## Features

- **Minimalist Design**: Clean interface with a 3x2 grid layout for 6 customizable app shortcuts
- **Persistent App Selection**: Selected apps remain saved across app restarts and force closes
- **Date Display**: Shows current date in a readable format (e.g., "Friday, June 13")
- **Smart Calendar Integration**: Tap the date to open Mudita Calendar (if installed) or default calendar app
- **App Selection**: Long press or tap empty slots to choose apps from all installed applications
- **All Apps Access**: Quick access to all installed apps via the hamburger menu button
- **Home Screen Integration**: Functions as a proper Android launcher with home button support
- **Lightweight Performance**: Optimized for minimal memory usage and fast startup times

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

### v1.1.0 (Current)
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