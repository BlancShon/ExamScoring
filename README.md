# ExamScoring
android-based exam answer sheet mark sum up tool

This project is a mobile application designed to automatically score multiple-choice exams using Optical Character Recognition (OCR) technology. The app captures images of exam answer sheets, processes the images, recognizes the filled circles, and calculates the exam scores as csv file.

## Getting Started

These instructions will help you set up the project on your local machine for development and testing purposes.

### Prerequisites

Ensure you have the following software and libraries installed on your local machine:

- Android Studio (version 2020.3.1 or newer)
- Java Development Kit (JDK 11 or newer)
- OpenCV (version 4.5.0 or newer)
- Tesseract OCR (version 4.0.0 or newer)

### Installation

Follow these steps to set up the project on your local machine:

1. Clone the repository using `git clone https://github.com/yourusername/automated-exam-scoring.git`.
2. Open the project in Android Studio.
3. Install the required dependencies using the built-in Gradle system.
4. Configure the environment variables for OpenCV and Tesseract OCR following their respective documentation.

## Usage

To use the application:

1. Build and run the app on an Android device or emulator.
2. Capture an image of a multiple-choice exam answer sheet using the in-app camera functionality.
3. Review the detected answers and make any necessary corrections.
4. View the calculated exam score and export the results as a CSV file.
