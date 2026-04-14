# StadiumFlow 🏟️

The **StadiumFlow (Physical Event Experience System)** is an intelligent, real-time solution designed to enhance attendee experiences at large-scale sporting events. It tackles key challenges such as crowd congestion, long waiting times, and lack of real-time coordination by leveraging data-driven insights, automation, and seamless communication.

---

## 📌 Overview
StadiumFlow is a next-generation venue operating system designed to solve the three hardest problems of high-density physical events:
- **Crowd Safety**
- **Network Reliability**
- **Commercial Efficiency**

---

## 🚀 Key Features

### 1. Gemini-Powered Smart Assistant ✨
- Context-aware assistant using real-time venue data
- Answers queries like: *"Is Gate A crowded?"*
- Suggests alternate routes dynamically

---

### 2. Smart Reroute & Live Wayfinding
- Real-time crowd-aware navigation
- Dynamic route adjustments to avoid congestion
- Interactive venue maps

---

### 3. Queue & Crowd Optimization
- Live wait-time tracking
- Suggests less crowded food stalls and restrooms
- Prevents bottlenecks proactively

---

### 4. Dynamic Pricing (Reverse Surge)
- Reduces prices at under-utilized stalls
- Balances crowd distribution across the venue

---

### 5. Resilient Connectivity (Mesh Simulation)
- BLE Mesh fallback when network fails
- Ensures uninterrupted communication and transactions

---

## 🏗️ Architecture & Approach

### Backend
- Java 11, Spring Boot
- Microservices-ready architecture
- Spring Data JPA + H2 Database
- Kafka/Event-driven compatible

### Frontend
- Mobile-first responsive UI
- Real-time updates using SSE/WebSockets
- Accessibility-friendly design

---

## ⚖️ Assumptions
- IoT sensors continuously send crowd data
- Most users have BLE/UWB-enabled devices
- Safety-first routing over revenue

---

## 💻 How to Run

```bash
./gradlew clean test
./gradlew bootRun