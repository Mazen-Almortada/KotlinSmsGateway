# **📱 Kotlin SMS Gateway**

This project is an Android application that turns your phone into an SMS Gateway. It runs a small web server on your device, allowing you to send SMS messages by making simple API requests to it from any other system.

## **✨ Features**

* **Runs 24/7**: Operates as a persistent Foreground Service, ensuring it stays active even if the app is closed. You don't have to worry about it.
* **Dead Simple API**: Sending messages is easier than drinking water. Anyone can use it.
* **Logs Everything**: Keeps a record of all your messages and their statuses (queued, sending, sent, delivered, or failed). No more guesswork\!
* **Clean UI**: The app itself is organized and shows you everything you need: the IP address, your secret token, and a full log of your messages.

## **🚀 How to Get Started \- Let's Go\!**

1. **Run the App**: First, build the project and run it on a real Android device, not an emulator.
2. **Grant Permissions**: The app will ask for a few permissions, like sending SMS and showing notifications. You have to approve them all, or it won't work, lol.
3. **Grab Your Info**: As soon as you open the app, you'll find everything you need right in your face:
   * **Server IP Address**: This is the address you'll send your requests to.
   * **The Token**: This is like your password. Save it and use it with every API request.

## **📡 API Details (The Good Stuff)**

First and most importantly, **all requests** must have the Authorization header with your token in it. Don't forget\!

Authorization: YOUR\_DEVICE\_TOKEN

### **✔️ Wanna send a single message?**

This is for sending a message to one person.

* **Method:** POST
* **Endpoint:** /send
* **Headers:**  
  Content-Type: application/json

* Body:  
  Simply give it the number (to) and the message (message). The messageID is optional if you want to track the message with your own custom ID.  
  {  
  "to": "123456789",  
  "message": "Hey there\! This is a test message\!",  
  "messageID": "your-own-custom-id-if-you-want"  
  }

* **Success Response (200 OK):**  
  {  
  "messageId": "the-id-it-was-saved-with",  
  "status": "queued"  
  }

### **✔️ Sending messages in bulk?**

This is if you have a marketing campaign or want to send a lot of messages at once.

* **Method:** POST
* **Endpoint:** /send-bulk
* **Headers:**  
  Content-Type: application/json

* Body:  
  Give the campaign a name and an ID, then put all the messages you want to send in a list.  
  {  
  "Bulk Name": "Q1 Promotions 2025",  
  "Bulk ID": "promo-q1-2025",  
  "Bulk Messages": \[  
  {  
  "to": "+15551112222",  
  "message": "Hey champ, don't miss our new deals\!"  
  },  
  {  
  "to": "+15553334444",  
  "message": "Our offers are for a limited time, get 'em now\!"  
  }  
  \]  
  }

* **Success Response (202 Accepted):**  
  {  
  "status": "success",  
  "message": "2 messages have been added to the queue.",  
  "bulkId": "promo-q1-2025"  
  }

### **✔️ Wanna see the message log?**

To see all the messages you've sent and their statuses.

* **Method:** GET
* **Endpoint:** /messages
* **Success Response (200 OK):** It will give you a list of all messages.  
  \[  
  {  
  "id": "a1b2c3d4-e5f6-7890-1234-567890abcdef",  
  "recipient": "+15551234567",  
  "content": "This is a test message.",  
  "status": "delivered",  
  "timestamp": 1678886400000,  
  "bulkId": "promo-q1-2025"  
  }  
  \]

## **👥 The Awesome People Who Worked on This Project**

* [Salman Aboholiqah](https://github.com/salman-aboholiqah)
* [Mazen Almortada](https://github.com/Mazen-Almortada)