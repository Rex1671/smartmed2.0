# SmartMed 2.0 🏥

SmartMed is a comprehensive AI-powered healthcare assistant application designed to bridge the gap between patients and healthcare providers. It leverages cutting-edge technologies like Google's Gemini AI and Firebase to provide intelligent symptom analysis, patient management, and real-time communication.

## ✨ Key Features

### 🤖 AI-Powered Diagnosis
- **Symptom Analysis**: Users can input symptoms, and the app uses Google's Gemini AI (Vertex AI) to analyze them and suggest potential diagnoses.
- **Smart Recommendations**: Provides preliminary medical insights to assist doctors and inform patients.

### 📋 Patient Management
- **Digital Records**: Securely store and manage patient history and medical records.
- **Patient Lists**: Doctors can easily view and manage their patient list.
- **History Tracking**: Detailed history views for tracking patient progress over time.

### 💊 Drug Discovery
- **Medication Info**: Search and access detailed information about various drugs and medications.
- **Discovery Tool**: Dedicated interface for exploring pharmaceutical information.

### 💬 Communication & Community
- **Live Chat**: Real-time chat functionality for doctor-patient or peer-to-peer communication.
- **Community Posts**: A social platform for medical professionals to share insights, ask questions, and discuss cases.
- **Discussion Forums**: Interactive comment sections on posts.

### 📄 Report Generation
- **PDF Reports**: Automatically generate professional medical reports and prescriptions using iText and PDFBox.
- **Export**: Easy export of patient data and session details.

## 🛠️ Technology Stack

- **Language**: Java
- **Platform**: Android (Min SDK 26, Target SDK 34)
- **Architecture**: MVVM / MVC

### Backend & Cloud Services
- **Firebase Authentication**: Secure user login and signup.
- **Firebase Realtime Database**: Real-time data syncing for chats and posts.
- **Firebase Storage**: Cloud storage for media and documents.
- **Google Vertex AI / Gemini**: Advanced generative AI for medical analysis.

### Libraries & Tools
- **Networking**: Retrofit, OkHttp, Java-WebSocket.
- **Image Loading**: Glide, Android-Gif-Drawable.
- **Document Handling**: Apache PDFBox, iTextG, Apache POI (Word/Excel).
- **UI Components**: Material Design, ConstraintLayout, RecyclerView.
- **Utils**: Gson, Guava, JavaFaker.

## 🚀 Getting Started

### Prerequisites
- Android Studio Iguana or later.
- JDK 1.8.
- A Firebase project with Auth, Database, and Storage enabled.
- Google Cloud Project with Vertex AI API enabled.

### Installation

1.  **Clone the repository**
    ```bash
    git clone https://github.com/yourusername/smartmed.git
    ```

2.  **Setup Firebase**
    - Go to the Firebase Console and create a new project.
    - Add an Android app with package name `com.example.smartmedbeta`.
    - Download `google-services.json` and place it in the `app/` directory.

3.  **Configure API Keys**
    - Ensure your `local.properties` or environment variables contain necessary API keys for Gemini/Vertex AI if not handled via `google-services.json`.

4.  **Build and Run**
    - Open the project in Android Studio.
    - Sync Gradle files.
    - Run the app on an emulator or physical device.

## 🤝 Contribution

Contributions are welcome! Please fork the repository and submit a pull request for any enhancements or bug fixes.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
