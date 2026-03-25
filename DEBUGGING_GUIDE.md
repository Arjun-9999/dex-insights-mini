# 🔧 DEBUGGING GUIDE - Data Loading Issues

## ✅ QUICK CHECKLIST

Before debugging, verify:

```
☐ Backend running on http://localhost:8080
☐ Frontend running on http://localhost:4200
☐ Can access http://localhost:8080/v1/stores (without "http://localhost:4200" prefix)
☐ Browser DevTools open (F12) to check console for errors
☐ No other app using ports 8080 or 4200
```

---

## 🎯 STEP-BY-STEP DEBUGGING

### 1️⃣ Check if Backend is Working

**In Browser Console:**
```javascript
// Test backend API
fetch('http://localhost:8080/v1/stores?page=0&size=10')
  .then(r => r.json())
  .then(d => console.log('Success:', d))
  .catch(e => console.error('Error:', e))
```

**Expected Result:**
```
Success: {
  content: [array of 10 stores],
  totalElements: 254,
  ...
}
```

**If you see CORS error:**
```
Access to XMLHttpRequest at 'http://localhost:8080/...' from origin 'http://localhost:4200' 
has been blocked by CORS policy
```

**Solution:** Add CORS to Spring Boot controller (see section below)

---

### 2️⃣ Check if Frontend Can Access Backend

**In Browser DevTools (F12):**
1. Go to **Network** tab
2. Click on **Stores** tab
3. Look for `/v1/stores` request
4. Check Status Code:
   - ✅ **200** = Success
   - ❌ **0** = Request blocked (CORS)
   - ❌ **404** = Endpoint not found
   - ❌ **500** = Backend error

**Check Request URL:**
- Should be: `http://localhost:8080/v1/stores?...` (via proxy)
- NOT: `http://localhost:4200/v1/stores...`

---

### 3️⃣ Check Browser Console for Errors

**Press F12 → Console tab**

**Look for errors like:**
```
⚠️ Error loading stores: 0 - 
❌ GET http://localhost:8080/v1/stores 404 (Not Found)
❌ Unexpected token < in JSON at position 0
```

**Our Enhanced Logging:**
You'll see messages like:
```
✅ StoresComponent initialized
✅ Loading stores from: /v1/stores?page=0&size=10
✅ Stores loaded successfully: Object {content: Array(10), ...}
```

---

## 🚀 SOLUTIONS FOR COMMON ISSUES

### ❌ Stores/Insights/Chat page shows "No stores found" or "Unable to load"

**Solution 1: Verify Backend is Running**
```powershell
# In PowerShell, check if port 8080 is listening
netstat -ano | findstr "8080"

# Or test the API directly
Invoke-WebRequest http://localhost:8080/v1/stores
```

**Solution 2: Check Data Files Exist**
```powershell
# Verify data files
ls D:\dex-insights-mini\src\main\resources\data\
# Should show: stores.json, transactions.json, incidents.json
```

**Solution 3: Rebuild and Restart**
```powershell
cd D:\dex-insights-mini
mvn clean install -DskipTests
mvn spring-boot:run
```

---

### ❌ CORS Error in Console

**Error Message:**
```
Access to XMLHttpRequest at 'http://localhost:8080/v1/stores' 
from origin 'http://localhost:4200' has been blocked by CORS policy
```

**Solution: Add CORS Headers to Spring Boot**

Open `D:\dex-insights-mini\src\main\java\com\dex\dex_insights_mini\controller\StoreController.java`

Add `@CrossOrigin` annotation:
```java
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/v1/stores")
@CrossOrigin(origins = "http://localhost:4200")
public class StoreController {
    // ... existing code
}
```

Do the same for:
- `InsightsController.java` 
- `ChatController.java`

Then rebuild and restart:
```powershell
mvn clean install -DskipTests
mvn spring-boot:run
```

---

### ❌ 404 Error - Endpoint Not Found

**Error in Console:**
```
GET /v1/stores 404 (Not Found)
```

**Check:**
1. Backend is actually running (check logs in PowerShell)
2. URL is correct: `/v1/stores` (not `/v1/store` or `/stores`)
3. Check `StoreController.java` has `@RequestMapping("/v1/stores")`

**Backend Log Should Show:**
```
Mapped to c.d.d.c.StoreController#getStores(...)
```

---

### ❌ 500 Error - Backend Error

**Error in Console:**
```
POST /v1/chat 500 (Internal Server Error)
```

**Check Backend Logs:**
Look in the PowerShell window running `mvn spring-boot:run`

**Common Issues:**
- Missing `storeId` in chat request
- Invalid JSON in request body
- Data files not loaded properly

**Debug the Chat Request:**
```javascript
// In browser console
fetch('http://localhost:8080/v1/chat', {
  method: 'POST',
  headers: {'Content-Type': 'application/json'},
  body: JSON.stringify({
    storeId: '10001',
    question: 'How many pumps are online?'
  })
})
.then(r => r.json())
.then(d => console.log('Response:', d))
.catch(e => console.error('Error:', e))
```

---

## 📊 ENABLE DETAILED LOGGING

### Backend Logging

Edit `D:\dex-insights-mini\src\main\resources\application.yaml`:
```yaml
server:
  port: 8080
spring:
  application:
    name: dex-insights-mini

logging:
  level:
    root: INFO
    com.dex.dex_insights_mini: DEBUG
```

**Backend will now log:**
```
[DEBUG] event=load_stores brand=null status=null sortByOfflinePumps=false page=0 size=10
[DEBUG] Loaded 254 stores from JSON
[DEBUG] Returning paginated result: 10 stores
```

### Frontend Logging

We've already enhanced all components with `console.log()` statements.

**Expected Console Output:**
```
✅ StoresComponent initialized
✅ Loading stores from: /v1/stores?page=0&size=10
✅ Stores loaded successfully: {content: Array(10), totalElements: 254, ...}
```

---

## 🧪 TEST EACH ENDPOINT

### Test Stores API
```bash
curl http://localhost:8080/v1/stores?page=0&size=5
```

### Test Insights API
```bash
curl http://localhost:8080/v1/insights/overview
```

### Test Chat API
```bash
curl -X POST http://localhost:8080/v1/chat \
  -H "Content-Type: application/json" \
  -d '{"storeId":"10001","question":"How many pumps are online?"}'
```

---

## 🎬 COMPLETE TROUBLESHOOTING FLOW

1. **Open DevTools (F12)** → Console tab
2. **Check for error messages** from our logging
3. **Look at Network tab** → Find failing request
4. **Read the error message carefully** (URL, Status Code)
5. **Check backend logs** in PowerShell
6. **Test endpoint directly** with curl/Postman
7. **Verify data files** exist and have content
8. **Check proxy configuration** in `proxy.conf.json`
9. **Restart both servers** if still not working
10. **Check browser console** for any JavaScript errors

---

## 📞 QUICK REFERENCE

| Issue | Check | Fix |
|-------|-------|-----|
| Stores not loading | Network tab status code | Check backend is running |
| 404 Error | URL in request | Verify endpoint mapping |
| CORS Error | Browser console | Add @CrossOrigin to controller |
| 500 Error | Backend logs in PowerShell | Check JSON format, storeId |
| Blank page | Open DevTools F12 | Check for JavaScript errors |
| Slow loading | Network tab timing | Check backend response time |


