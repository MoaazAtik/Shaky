# Shaky 📳

Your Vibration Detection Companion.<br>
By activating **Shaky** as your Earthquake Detector, you can sleep peacefully, knowing that it's vigilantly Detecting and will Alert you to any tremors.

Get it on **Play Store** [here](https://play.google.com/store/apps/details?id=com.thewhitewings.shaky)<br>
Supported by **The White Wings**🪽. Play Store [link](https://play.google.com/store/apps/dev?id=6456450686494659010)

<br>

<div align="center">
  <img src="https://github.com/user-attachments/assets/8b999548-bfb3-4735-b578-735884d2389f" alt="Home Screen" width="220"/>
  <img src="https://github.com/user-attachments/assets/ed77a3f3-751a-4574-b709-d387a33bfc6f" alt="Battery Optimization Dialog" width="220"/>
  <img src="https://github.com/user-attachments/assets/d3a0f097-9905-46c9-99f7-797db5bd7449" alt="More Actions Screen" width="220"/>
</div>
<div align="center">
  <img src="https://github.com/user-attachments/assets/3c2abcec-3f77-4084-90de-559570d70253" alt="Info Screen" width="220"/>
  <img src="https://github.com/user-attachments/assets/1f79fa80-d486-4eba-a5b7-8da3a34d1eb9" alt="Feedback" width="220"/>
</div>

<div align="center">
  Screenshots
</div>

<br>

## Navigate Your Journey of exploring Shaky 🗺️
1. [Inspiration & Goal - The Story Behind Shaky](#inspiration--goal---the-story-behind-shaky-)
2. [Quick Start](#quick-start-)
3. [Usage](#usage-)
4. [Demonstration](#demonstration-)
5. [Utilized Technologies](#utilized-technologies-)
6. [Core Files](#core-files-)
7. [Links to Check Out](#links-to-check-out-)

<br>

## Inspiration & Goal - The Story Behind Shaky 💡
In 2023, Turkey and Syria were rocked by a devastating earthquake, followed by over 30,000 aftershocks. The catastrophe affected more than 14 million people, resulting in widespread damage and loss of life.<br>
My brother shared stories of some friends too frightened to sleep, worried they wouldn't have time to respond to potential tremors.

To assist them facing such situations, he inspired me to create an app. This app should provide peace of mind by detecting vibrations and alerting the user to take precautions.<br>
So, I set out to create **Shaky**. My goal was to design an app that anyone could use effortlessly anywhere, ensuring that everyone could benefit from its capabilities.

<br>

## Quick Start 🚀
**Clone** the Repository or **Download Shaky** from Play Store, and **Run** the app on your device or emulator.

After that:
1. **Start Detection:** The detector will start monitoring **automatically**.
2. **Enable Notifications:** Upon opening the app, grant permission for notifications.<br>
This allows Shaky to alert you when vibrations are detected.
- **To Stop Detection:** Tap the *OFF* button at the middle of the screen.
- **Set Sensitivity:** Adjust the sensitivity level of the vibration detector with the *Sensitivity slider*. More instructions in the [Usage](#usage-) section.
- **Adjust Alarm Volume:** Set the volume level for the alert alarm.<br>
Choose a volume that ensures you'll wake up or be alerted during vibrations.

<br>

## Usage 📱
- **Multilingual support:** Available in **English**, **Arabic**, and **Turkish**.<br>
It corresponds to your device language.
- **Monitoring Vibrations:** After opening Shaky, it will start monitoring vibrations **automatically**.<br>
Keep the app running in the background, and it will continuously detect any vibrations.
- You can **return** to Shaky effortlessly through its **Notification**.

<br>

Interacting with the Main Screen:
   - **Status Indicator:** Check the status indicator on the main screen to see if the app is actively monitoring vibrations.
   - **Start/Stop Detection:** Use the *ON/OFF* buttons to start or stop vibration monitoring.
   - **Adjust Sensitivity:** Slide the Sensitivity slider to customize the detector's sensitivity level.<br>
   Start with the maximum sensitivity and adjust it gradually to suit your preference. You can increase sensitivity to detect weaker vibrations or decrease it to avoid false alarms.
   - **Adjust Alarm Volume:** Slide the *Alarm volume slider* to set your preferred alarm volume level.
   - **Access Additional Functions:** Tap the button on the bottom right corner of the main screen to access more functions.
 
<br>
  
Accessing Additional Features:
   - **Changing Alarm Tone:** Customize the alarm tone by tapping on the corresponding button and selecting from available options.
   - **Providing Feedback:** Share your thoughts or report issues by tapping on the feedback button, which will direct you to your preferred email app.
   - **Accessing Important Notes:** Navigate to the Notes Screen by tapping on the corresponding button.

<br>
 
On the Notes Screen:
- You will find essential notes about using the app. Here, you'll discover instructions on overcoming **common obstacles** you may face, such as background operation disruptions.
- Additionally you'll find a reference to the app's **Privacy Policy**.

<br>

Ensuring proper app functionality when the **screen turns off** ❕
- Apps running in the background may be halted by the Android system for battery preservation. This could lead to disruptions in app functionality shortly after your screen turns off.
- To prevent such occurrences, you should adjust specific settings based on your *device's manufacturer* and *Android version*. While this process may seem challenging in some cases, unfortunately, these settings can only be modified manually by the user.
- To overcome this obstacle, click the *"Let's fix it"* button on the dialog that appears upon app launch, or refer to the **"Background Operation Explanation"** note in the **"Important Notes"** section. You'll find **step-by-step guides** to assist you in rectifying this situation.

<br>

Shaky operates offline and features a straightforward UI, making it accessible for **Everyone Anytime, Anywhere.**

<br>

## Demonstration 📸
Click the image below to watch the full app demo on YouTube ⬇️

[![Full demo](https://github.com/TheMaestroCo/Shaky/assets/75887565/88cff34e-dd5d-4988-947e-444d530a74d7)](https://youtu.be/-XRmLw_ChLI)

## Utilized Technologies 🔧
- **Programming Language:** Java

- **UI**: Android Views with XML

- **Frameworks/Libraries:**
  - **Foreground Service**: For consistent vibration monitoring.
  - **Wake Lock**: For increased vibration monitoring reliability by surviving App Stanby States and Doze Mode.
  - **Accelerometer**: For vibrations detection.
  - **Motion Layout**: For creating smooth animations.
  - **Media Player**: For playing the triggered alarm.
  - **View Binding**: For better interactions with the Android Views.
  - **SharedPreferences**: For storing and retrieving user's preferred detector sensitivity, and alarm tone.

- **Development Tools:** Figma, Lottie Animations, Git, and Android Studio

<br>

## Core Files 📁
- [MainActivity.java](app/src/main/java/com/thewhitewings/shaky/ui/main/MainActivity.java)
- [MediaAndSensorViewModel.java](app/src/main/java/com/thewhitewings/shaky/ui/main/MediaAndSensorViewModel.java)
- [activity_main.xml](app/src/main/res/layout/activity_main.xml)
- [activity_main_scene.xml](app/src/main/res/xml/activity_main_scene.xml)<br><br>
- [MediaAndSensorService.java](app/src/main/java/com/thewhitewings/shaky/service/MediaAndSensorService.java)
- [SensorHandler.java](app/src/main/java/com/thewhitewings/shaky/SensorHandler.java)
- [MediaHandler.java](app/src/main/java/com/thewhitewings/shaky/MediaHandler.java)
- [NotificationHandler.java](app/src/main/java/com/thewhitewings/shaky/NotificationHandler.java)
- [AndroidManifest.xml](app/src/main/AndroidManifest.xml)<br><br>
- [MoreFragment.java](app/src/main/java/com/thewhitewings/shaky/ui/more/MoreFragment.java)
- [NotesFragment.java](app/src/main/java/com/thewhitewings/shaky/ui/notes/NotesFragment.java)<br><br>
- [ShakyApplication.java](app/src/main/java/com/thewhitewings/shaky/ShakyApplication.java)
- [ShakyPreferences.java](app/src/main/java/com/thewhitewings/shaky/data/ShakyPreferences.java)
- [Util.java](app/src/main/java/com/thewhitewings/shaky/Util.java)

<br>

## Links to Check Out: 👍
- My Developer Page [on](https://play.google.com/store/apps/dev?id=6456450686494659010) **Play Store**
- Shaky [on](https://play.google.com/store/apps/details?id=com.thewhitewings.shaky) **Play Store**
- Shaky [on](https://www.youtube.com/watch?v=DE-bHpHHT2Q) **YouTube**

<br></br>
When the ground gets shaky, **Shaky** has got you 🛡️
