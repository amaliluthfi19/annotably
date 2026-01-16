# Firestore Security Rules Setup

## Issue
You're getting a `PERMISSION_DENIED` error when trying to write to Firestore because the security rules are blocking writes.

## Solution

### Option 1: Update Rules in Firebase Console (Quickest)

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: **annotably**
3. Navigate to **Firestore Database** → **Rules** tab
4. Replace the existing rules with:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Books collection - allow read and write for all users (development only)
    match /books/{bookId} {
      allow read, write: if true;
    }
    
    // Default: deny all other access
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

5. Click **Publish**

### Option 2: Deploy Rules Using Firebase CLI

If you have Firebase CLI installed:

1. Install Firebase CLI (if not already installed):
   ```bash
   npm install -g firebase-tools
   ```

2. Login to Firebase:
   ```bash
   firebase login
   ```

3. Initialize Firebase in your project (if not already done):
   ```bash
   firebase init firestore
   ```

4. Deploy the rules:
   ```bash
   firebase deploy --only firestore:rules
   ```

### Option 3: Production-Ready Rules (Requires Authentication)

For production, you should require authentication. Update the rules to:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Books collection - require authentication
    match /books/{bookId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }
    
    // Default: deny all other access
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

**Note:** If you use authentication-based rules, you'll need to implement Firebase Authentication in your app first.

## Current Rules File

A `firestore.rules` file has been created in the project root. You can use this with Firebase CLI or copy its contents to the Firebase Console.

## Testing

After updating the rules, try adding a book again. The permission error should be resolved.
