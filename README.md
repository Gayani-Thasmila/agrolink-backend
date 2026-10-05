# agrolink-backend
Full-featured backend API for AgroLink using Spring Boot, MySQL, Spring Security, JPA and Firebase.

## Run with the Android app on a phone

Start the backend, then use the computer's Wi-Fi IP address in the Android app base URL. Do not use `localhost` from a real phone.

Current local backend URL:

```text
http://192.168.8.118:8080/api/products
```

If the Android app uses plain HTTP, make sure its manifest allows cleartext traffic for development:

```xml
<application
    android:usesCleartextTraffic="true">
```

## PayHere configuration

Set these environment variables before starting the backend. Do not put a Merchant Secret in source control.

```text
PAYHERE_SANDBOX=true
PAYHERE_MERCHANT_ID=<your sandbox merchant ID>
PAYHERE_MERCHANT_SECRET=<secret for this Android app package>
```

In the PayHere Merchant Portal, add and approve the Android app package under **Domains and Credentials** and use the Merchant Secret issued for that app. The Merchant ID, Merchant Secret, `sandbox` value, and the hash sent to the SDK must all belong to the same environment.
