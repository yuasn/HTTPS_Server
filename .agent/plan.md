# Project Plan

An Android application that acts as an HTTPS server. Users can specify a local directory as the web folder, customize the port, and have Start/Stop controls. The app should allow the user to confirm assumptions about SSL/TLS certificates (e.g., self-signed vs. provided). Background execution with Foreground Service, IP display, and a browser shortcut button are now required.

## Project Brief

# Project Brief: HttpsServerYuasn

## Features
- **Server Dashboard & IP Monitoring:** A central control hub to start/stop the HTTPS server, featuring a live display of the device's local IP addresses and a shortcut button to open the server URL in the device's browser.
- **Persistent Background Service:** Utilizes an Android Foreground Service with a persistent notification to ensure the HTTPS server remains active even when the app is in the background or the device is locked.
- **Customizable Web Root & Port:** Allows users to select any local directory as the web folder and define a custom network port for the server to listen on.
- **SSL/TLS Configuration:** Integrated support for managing certificates, allowing users to toggle between auto-generated self-signed certificates for quick setup or providing their own custom SSL/TLS files.


## High-Level Technical Stack
- **Kotlin:** The primary language for robust and safe Android development.
- **Jetpack Compose:** The modern toolkit for building a reactive Material Design 3 user interface.
- **Jetpack Navigation 3:** A state-driven navigation framework for managing application transitions.
- **Compose Material Adaptive:** Ensures a consistent and optimized layout across different form factors (phones, foldables, tablets).
- **Android Foreground Services:** Core component used to maintain server execution and system priority during background operation.
- **Kotlin Coroutines:** Used for non-blocking I/O operations and server lifecycle management.
- **Ktor Server:** A lightweight, asynchronous framework to handle the HTTPS server engine and SSL/TLS termination.

## Implementation Steps

### Task_1_Dependencies_ServerEngine: Add Ktor server dependencies and implement the core HTTPS server manager with SSL/TLS support and file serving logic.
- **Status:** COMPLETED
- **Updates:** Added Ktor 3.5.0 dependencies (core, netty, tls-certificates, status-pages). Implemented HttpsServerManager with SSL/TLS, self-signed certificate generation, and static file serving. Updated build.gradle.kts and AndroidManifest.xml. Project builds successfully.
- **Acceptance Criteria:**
  - Ktor dependencies (netty, tls) added to build.gradle.kts
  - HttpsServerManager implemented with start/stop and logging capabilities
  - Self-signed certificate generation logic included

### Task_2_UI_Implementation: Develop the Material 3 UI using Jetpack Compose, including the Dashboard and Configuration screens with Navigation 3.
- **Status:** COMPLETED
- **Updates:** Implemented vibrant Material 3 theme. Created Dashboard with Start/Stop controls and Live Activity Log. Created Configuration screen with Port and Directory settings. Implemented Navigation 3 for screen transitions. UI is responsive and follows M3 guidelines.
- **Acceptance Criteria:**
  - Material 3 UI implemented with vibrant theme
  - Dashboard contains start/stop controls and activity log
  - Configuration screen includes port and directory settings
  - Navigation 3 handles screen transitions

### Task_3_Integration_and_FilePicker: Connect the UI to the server manager, implement directory/certificate file picking using SAF, and persist settings with DataStore.
- **Status:** COMPLETED
- **Updates:** Integrated HttpsServerManager with UI and DataStore. Implemented SAF for directory and certificate picking. Settings (port, directory, SSL preference) are persisted and used to initialize the server. Activity logs are streamed to the Dashboard. App is fully functional.
- **Acceptance Criteria:**
  - Server starts/stops based on UI interaction
  - Activity logs stream to the UI console
  - Storage Access Framework used for directory and certificate selection
  - Settings (port, path) persisted via DataStore

### Task_4_Final_Polish_and_Run: Implement edge-to-edge display, create an adaptive app icon, apply final M3 theming, and verify app stability.
- **Status:** COMPLETED
- **Updates:** Implemented full Edge-to-Edge display. Created a secure-server themed adaptive app icon. Refined the vibrant M3 theme and moved strings to strings.xml. Final code cleanup and build verification completed.
- **Acceptance Criteria:**
  - Full Edge-to-Edge display implemented
  - Adaptive app icon matching the server function created
  - Vibrant energetic color scheme applied
  - App builds and runs without crashes
  - HTTPS server verified as accessible

### Task_5_ForegroundService_and_IP_Logic: Implement an Android Foreground Service to host the Ktor server and add logic to monitor and retrieve the device's local IP addresses.
- **Status:** COMPLETED
- **Updates:** Implemented ServerForegroundService with persistent notification to host the Ktor server in the background. Added IP address detection logic via NetworkUtils and displayed local IPs on the Dashboard. Added an "Open in Browser" button to the UI. Updated AndroidManifest.xml with FOREGROUND_SERVICE_DATA_SYNC and POST_NOTIFICATIONS permissions. Refactored HttpsServerManager to a singleton for log sharing.
- **Acceptance Criteria:**
  - Foreground Service implemented with persistent notification
  - Ktor server lifecycle managed within the service
  - IP address detection logic for local network implemented
  - Required permissions (POST_NOTIFICATIONS, FOREGROUND_SERVICE) added

### Task_6_UI_Update_and_Final_Verify: Update the Dashboard UI with IP display and browser shortcuts, integrate service controls, and perform final run and verify.
- **Status:** COMPLETED
- **Updates:** Fixed Android 14+ crash by adding foreground service type to startForeground. Resolved webpage access issues by implementing MANAGE_EXTERNAL_STORAGE permission request. Updated SSL logic to use PKCS12 for better Android compatibility. Refined SAF path resolution.
- **Acceptance Criteria:**
  - Dashboard displays local IP and server URL
  - 'Open in Browser' button correctly launches the URL
  - Server remains active in background via Foreground Service
  - Build pass, app does not crash, and critical UI alignment verified

