# 🕳️ Fillpit – Report Potholes, Fix Roads!

A citizen-centric platform that empowers users to report road potholes with just a few taps – making streets safer, one report at a time.

<p align="center">
  <img src="https://github.com/user-attachments/assets/db9f912f-f9e1-4bf9-9ba1-20f833d9ae1d" alt="fillpitlogo" width="200"/>
</p>

---

## 🚀 Overview

**Fillpit** bridges the gap between citizens and local authorities by providing a seamless way to report potholes. With features like real-time location tracking, image uploads, and admin management tools, Fillpit enhances civic engagement and road maintenance efficiency.

Available as a full-featured **Android app** built in **Java & XML**, backed by **Firebase Authentication** and **Cloudinary** for seamless image uploads.

---

## 📸 Screenshots

<p align="center">
  <img src="https://github.com/user-attachments/assets/2579bb2f-f0f5-4404-921c-5f20a0342a8d" alt="Android App – Home" width="250" style="margin-left: 25px;"/>
  <img src="https://github.com/user-attachments/assets/5ca61d01-b2cb-4edc-8217-792272523c7a" alt="Upload Screen" width="250" style="margin-right: 25px;"/>
  <img src="https://github.com/user-attachments/assets/0fa072a6-27ae-4558-a171-6aa4c833519c" alt="History Tab" width="250"/>
</p>

<p align="center">
  <b>Android App – Home</b> &nbsp;&nbsp;&nbsp; <b>Upload Screen</b> &nbsp;&nbsp;&nbsp; <b>History Tab</b>
</p>



## 📱 Key Features

### 🌐 Web Platform
- 📸 Upload pothole image via file or live camera
- 🌍 Auto-detect location using **Leaflet.js**
- 📝 Add description and nearby landmarks
- 🧑‍💼 Admin panel for managing reports
- 🔍 Dashboard with search, filters, and history tracking

### 📱 Mobile App (Android)
- 🔐 Login via Email (**Firebase Authentication**)
- 🖼 Upload pothole images to **Cloudinary**
- 📍 Auto-fetch GPS coordinates
- 🕘 View personal submission history
- 👤 Unique user IDs for tracking reports
- 🧾 Clean, responsive native UI

---

## 🧱 Tech Stack

| Platform     | Technology                            |
|--------------|----------------------------------------|
| Frontend     | HTML, CSS, JavaScript                 |
| Backend      | PHP                                    |
| Database     | MySQL                                  |
| Mobile App   | Java, XML                              |
| Authentication | Firebase Authentication            |
| Image Storage| Cloudinary                             |
| User Storage | Firebase Realtime Database            |
| Mapping      | Leaflet.js (Web), osmdroid (Android)   |
| Design       | Figma                                  |

---

## 🗺️ Map Integration

### 🌐 Web:
- Uses **Leaflet.js** to fetch and display user location
- Auto-mark user position on submission form

### 📱 Android:
- Uses **osmdroid** for displaying OpenStreetMap
- Auto-captures GPS coordinates with marker placement
- Allows manual adjustment of location

> osmdroid is an open-source Android library that supports offline map tiles and smooth OpenStreetMap rendering.
<p align="center">
  <img src="https://github.com/user-attachments/assets/b1094326-b462-452d-854b-0f9f11097e53" alt="Android App – Home" width="200"/>
</p>

---

## 🛣️ Roadmap

### ✅ Completed
- Web form with image & location
- Admin panel for report management
- User dashboard with history & search
- Android app with native UI (Java & XML)
- Firebase Authentication integration
- Firebase Realtime DB for user data
- Cloudinary for image uploads
- osmdroid-based map in Android

### 🔄 In Progress
- Push notifications for report status updates
- Admin verification & tagging workflow
- Time-limited report editing

---

## 📦 Project Structure

```
FillPit-App/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/fill_pit/
│   │   │   │   ├── MainActivity.java
│   │   │   │   ├── SubmissionActivity.java
│   │   │   │   └── ... (other classes)
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   ├── drawable/
│   │   │   │   ├── mipmap/
│   │   │   │   └── values/
│   │   │   ├── AndroidManifest.xml
│   │   │   └── google-services.json
│   ├── build.gradle
│   └── proguard-rules.pro
├── gradle/
├── .gitignore
├── build.gradle
├── settings.gradle
└── README.md
```
## 🤝 Contribution
We welcome all contributors! Here’s how to get started:
```
git fork https://github.com/akshit-singhh/fillpit.git
cd fillpit
```
## 📄 License
This project is licensed under the MIT License.

## 🙋‍♂️ Contact
Made with ❤ by Akshit Singh

📧 Email: akshitsingh658@gmail.com

🔗 LinkedIn: linkedin.com/in/akshit-singhh

## ⭐ Support
If you found this project useful, don’t forget to ⭐ the repository!
