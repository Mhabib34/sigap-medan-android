<div align="center">

<img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white"/>
<img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white"/>
<img src="https://img.shields.io/badge/Architecture-MVVM%20Clean-FF6B35?style=for-the-badge"/>
<img src="https://img.shields.io/badge/Status-In%20Development-FFA500?style=for-the-badge"/>
<img src="https://img.shields.io/badge/Min%20API-26-green?style=for-the-badge"/>

<br/><br/>

# 🏙️ SIGAP MEDAN
### *Sistem Informasi Gerakan Aksi Peduli Medan*

**A gamified civic reporting app that turns Medan citizens into city heroes.**  
Report urban problems, earn points, unlock badges, and redeem real rewards — all while making Medan a better city.

<br/>

> 🏆 Built for the **Mobile App Competition** — Subtema: Kebencanaan & Lingkungan  
> 📍 Universitas Potensi Utama Medan · Rekayasa Perangkat Lunak · 2026

</div>

---

## 📌 Table of Contents

- [Overview](#-overview)
- [The Problem](#-the-problem)
- [Key Features](#-key-features)
- [Tech Stack](#-tech-stack)
- [System Architecture](#-system-architecture)
- [Getting Started](#-getting-started)
- [Mission Categories](#-mission-categories)
- [How It Works](#-how-it-works)
- [UAT Results](#-uat-results)
- [Roadmap](#-roadmap)
- [Team](#-team)

---

## 📖 Overview

**SIGAP MEDAN** (Sistem Informasi Gerakan Aksi Peduli Medan) is an Android mobile application that empowers citizens of Medan City to actively participate in disaster mitigation and environmental preservation through a **gamification-based approach**.

Citizens can report urban issues (potholes, floods, garbage overflow, illegal parking, broken street lights), use public transport, and perform environmental actions — all while earning **points, badges, and redeemable voucher rewards**.

Every report is automatically pinned to a **Live Heat Map**, giving city authorities and the public a real-time visual overview of problem hotspots across Medan.

---

## 🚨 The Problem

Medan, as the capital of North Sumatra with a population of over 2.5 million, faces persistent urban challenges:

- Hundreds of **damaged road spots** scattered across districts
- **Recurring floods** in areas like Medan Maimun and Medan Deli every rainy season
- **Unstructured reporting** — citizens still rely on WhatsApp and social media to report issues with no guarantee of reaching authorities
- **No dedicated local platform** for structured civic participation, despite 81% smartphone penetration in Medan (Kominfo Sumut, 2025)

SIGAP MEDAN bridges this gap by providing a structured, rewarding, and data-driven civic reporting platform.

---

## ✨ Key Features

### 📋 Task System (Mission System)
Citizens are presented with a list of missions across three categories. Each task has a specific point value and requires photo confirmation or GPS-based verification.

### 🪙 Point System
Every validated task awards points in real-time. Points are displayed on the home dashboard and act as the in-app currency for reward redemption.

### 🏅 Badge System
Milestone-based digital badges are automatically awarded:
| Badge | Requirement |
|-------|-------------|
| 🦸 **City Hero** | Complete 10 disaster reports |
| 🌿 **Eco Warrior** | Complete 10 environmental actions |
| 🚌 **Bus Rider Pro** | Use Trans Metro 10 times |
| *(and more...)* | |

### 🎁 Reward System
Accumulated points can be redeemed for **shopping vouchers and discounts from local Medan partners**. The system includes point-balance validation and transaction confirmation for full transparency.

### 🗺️ Live Heat Map
An interactive map that displays all citizen reports as location markers in real-time. Citizens and authorities can tap any marker to see details about the reported issue, making it easy to identify problem hotspots across the city.

### 📊 Impact Dashboard (City Impact Score)
A collective contribution counter showing the cumulative impact of all citizens — total reports submitted, daily goals, and the city's overall "City Impact Score."

---

## 🛠️ Tech Stack

| Component | Technology | Purpose |
|-----------|-----------|---------|
| Language | **Kotlin** | Native Android development |
| UI Framework | **XML (Android View)** | Declarative modern UI |
| Architecture | **MVVM + Clean Architecture** | Separation of concerns |
| Local Database | **SQLite via Room Database** | Offline storage for reports, tasks, points, badges |
| Navigation | **Navigation Component** | Screen-to-screen flow management |
| Design Tool | **Figma** | Wireframes & UI/UX mockups |

---

## 🏗️ System Architecture

SIGAP MEDAN follows **MVVM Clean Architecture** across four main layers:

```
┌─────────────────────────────────────────┐
│           UI Layer (View)               │
│     Activities · Fragments · XML        │
├─────────────────────────────────────────┤
│        ViewModel Layer                  │
│   Handles UI logic & state management   │
├─────────────────────────────────────────┤
│        Repository Pattern               │
│  Single source of truth for all data   │
├─────────────────────────────────────────┤
│     Local Database (Room / SQLite)      │
│  Users · Tasks · Reports · Points ·    │
│         Rewards · Badges                │
└─────────────────────────────────────────┘
```

> ⚡ **Offline-first**: Core features work without internet, making the app reliable across all areas of Medan.

---

## 🚀 Getting Started

### Prerequisites

- Android Studio **Hedgehog** or later
- Android SDK **API 26+** (Android 8.0 Oreo minimum)
- Kotlin **1.9+**

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/Mhabib34/sigap-medan-android.git
   cd sigap-medan-android
   ```

2. **Open in Android Studio**
   ```
   File → Open → select the project folder
   ```

3. **Sync Gradle**
   ```
   Let Android Studio automatically sync dependencies
   ```

4. **Run the app**
   ```
   Connect an Android device or start an emulator, then click Run ▶
   ```

> 📦 No external API keys required for the current version — all data is stored locally via Room Database.

---

## 🗂️ Mission Categories

Citizens can choose from **8 mission types**:

| # | Mission | Type | Points |
|---|---------|------|--------|
| 1 | 🕳️ Report Pothole (Jalan Berlubang) | Report | ⭐ |
| 2 | 🚗 Report Traffic Jam (Kemacetan) | Report | ⭐ |
| 3 | 💧 Report Blocked Drainage (Drainase Tersumbat) | Report | ⭐ |
| 4 | 🗑️ Report Overloaded Garbage Site (TPS Overload) | Report | ⭐ |
| 5 | 🚫 Report Illegal Parking (Parkir Liar) | Report | ⭐ |
| 6 | 💡 Report Broken Street Light (Lampu Jalan Mati) | Report | ⭐ |
| 7 | 🚌 Use Trans Metro (Scan QR Barcode) | Transport | ⭐ |
| 8 | ♻️ Deposit Plastic Waste (Setor Sampah Plastik) | Environment | ⭐ |

> Point values per mission are managed in the app's task configuration.

---

## ⚙️ How It Works

```
Open App
   ↓
Login / Register
   ↓
Browse Missions (Home Dashboard)
   ↓
Choose a Mission → Complete in Real Life → Take Photo / Scan QR
   ↓
System Validates Submission
   ↓
✅ Points Awarded + Report Pinned on Live Heat Map
   ↓
Collect Badges (Milestones) → Redeem Rewards (Vouchers)
```

---

## 📊 UAT Results

User Acceptance Testing was conducted with **30 respondents** (students and Medan residents) across 6 usability dimensions:

| Usability Aspect | Score (1–5) |
|-----------------|------------|
| Ease of Navigation | **4.6** |
| Clarity of Points & Tasks | **4.5** |
| App Response Speed | **4.4** |
| Ease of Reporting | **4.3** |
| Satisfaction with Rewards & Badges | **4.7** |
| Likelihood to Use Again | **4.8** |
| **Overall Average** | **✅ 4.55 / 5.00 — Very Good** |

Black Box Testing was also performed on **14 functional scenarios** including authentication, reporting, QR barcode scanning, reward redemption, auto-badge awarding, and Live Heat Map — **all scenarios passed without critical failures**.

---

## 🗺️ Roadmap

### 🔜 Near-term
- [ ] Push notifications via **Firebase Cloud Messaging** for new tasks
- [ ] Community-based report verification (citizens validate each other's reports)
- [ ] Official integration with **BPBD Kota Medan** for formal follow-up

### 📅 Mid-term
- [ ] Migration from local SQLite to **cloud backend server** for cross-device data access
- [ ] Real-time data dashboard for city government

### 🌏 Long-term
- [ ] iOS expansion via **Kotlin Multiplatform Mobile (KMM)**
- [ ] Expansion to other North Sumatra cities: **Binjai**, **Pematangsiantar**, and beyond

---

## 👥 Team

**Team Name:** Mobile Gang (MBG)  
**Institution:** Universitas Potensi Utama Medan — Rekayasa Perangkat Lunak

| Role | Name |
|------|------|
| 👑 Team Lead | Muhammad Habib |
| 👨‍💻 Member | Ahmad Fauzi |
| 👨‍💻 Member | Muhammad Fahmi Syah Putra |

---

## 📎 Links

| Resource | Link |
|----------|------|
| 📱 Download APK | *Coming soon* |
| 🎬 Demo Video | *Coming soon* |
| 🎨 Figma Design | *Coming soon* |
| 📄 Full Proposal | Available in repo |

---

## 📄 License

This project is developed for academic and competition purposes under **Universitas Potensi Utama Medan**.

---

<div align="center">

Made with ❤️ for the people of Medan City

**SIGAP MEDAN** · *Platform Kota Cerdas Medan*

</div>
