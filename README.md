# 📱 Kotlin SMS Gateway

This project is an Android application that turns your phone into an SMS Gateway. It runs a small web server on your device, allowing you to send SMS messages by making simple API requests to it from any other system.

---

## ✨ Features

* **Runs in the Background**: Operates as a persistent Foreground Service, ensuring it stays active even if the app is closed.
* **Simple API**: Provides a straightforward API for sending messages.
* **Local Logging**: Records all messages and their statuses (`queued`, `sending`, `sent`, `delivered`, `failed`) in a local database.
* **Clear UI**: Displays the server's IP address, Authorization Token, and a log of all processed messages.

---

## 🚀 How to Use

1. **Run the App**: Build and run the project on a real Android device using Android Studio.
2. **Grant Permissions**: When you first launch the app, it will ask for `SEND_SMS` and Notification permissions. You must grant these for the app to function correctly.
3. **Get Info**: The main screen will display:
    * **Server IP Address**: Use this IP to make requests to the gateway.
    * **Authorization Token**: This token must be included in every API request for authorization.

---

## 📡 API Endpoints

### ✔️ Send a New Message

**Method:** `POST`  
**Endpoint:** `/send`

#### Headers

```http
Content-Type: application/json
Authorization: YOUR_DEVICE_TOKEN
```

#### Body

```json
{
    "to": "123456789",
    "message": "This is a test message from my gateway!"
}
```

---

### ✔️ Get Message Log

**Method:** `GET`  
**Endpoint:** `/messages`

#### Headers

```http
Authorization: YOUR_DEVICE_TOKEN
```
