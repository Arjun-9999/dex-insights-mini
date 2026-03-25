# 🚀 DEX INSIGHTS MINI - COMPLETE SETUP & TESTING GUIDE

## ✨ IMPROVEMENTS MADE

✅ **Added CORS Support** - Frontend can now communicate with backend  
✅ **Enhanced Error Logging** - Console shows clear error messages  
✅ **Better Loading States** - UI shows when data is loading  
✅ **Improved Error Messages** - Tells you exactly what went wrong  
✅ **Backend Rebuilt** - All changes compiled and tested  

---

## 🎯 START THE APPLICATION

### Method 1: Using Batch Script (EASIEST)

Double-click the script file:
```
D:\dex-insights-mini\START_APPLICATION.ps1
```

Wait ~30 seconds for both servers to start.

### Method 2: Manual Steps (RECOMMENDED)

**Terminal 1 - Start Backend:**
```powershell
cd D:\dex-insights-mini
mvn spring-boot:run
```

Wait for:
```
Tomcat started on port(s): 8080
```

**Terminal 2 - Start Frontend:**
```powershell
cd D:\dex-insights-mini\ui\dex-ui
npm install  # Only if you haven't run this before
npm start
```

Wait for:
```
Application bundle generation complete
```

### Method 3: Using JAR File

```powershell
cd D:\dex-insights-mini\target
java -jar dex-insights-mini-0.0.1-SNAPSHOT.jar
```

Then start frontend as shown in Method 2.

---

## 🌐 OPEN IN BROWSER

Once both servers are running:

🔗 **http://localhost:4200**

You should now see:
- Navigation bar with: **Stores | Insights | Chat**
- Click each tab to load data
- No error messages should appear

---

## ✅ COMPLETE TESTING CHECKLIST

### 🏪 Stores Page
- [ ] Stores list loads (shows multiple stores)
- [ ] Can see store IDs, brands, status, city names
- [ ] Pagination works (Previous/Next buttons)
- [ ] Filters work (Brand, Status dropdowns)
- [ ] "Sort by offline pumps" checkbox works
- [ ] Click "View" button → goes to store detail page

### 📊 Insights Page  
- [ ] Overview loads
- [ ] Shows 5 categories:
  - [ ] topOfflineStores
  - [ ] lowTankLevelStores
  - [ ] incidentCountsBySeverity
  - [ ] transactionVolumeByStore
  - [ ] revenueByStore

### 💬 Chat Page
- [ ] Store dropdown loads with store options
- [ ] Can select a store
- [ ] Can type a question
- [ ] Ask button works
- [ ] Gets a response from backend
- [ ] Response shows answer and citations

---

## 🔍 VERIFY EACH API ENDPOINT

### Test in Browser Console (F12)

**Stores API:**
```javascript
fetch('http://localhost:8080/v1/stores?page=0&size=5')
  .then(r => r.json())
  .then(d => console.log('✅ Stores:', d.content.length, 'found'))
  .catch(e => console.error('❌ Error:', e.message))
```

**Insights API:**
```javascript
fetch('http://localhost:8080/v1/insights/overview')
  .then(r => r.json())
  .then(d => console.log('✅ Insights keys:', Object.keys(d)))
  .catch(e => console.error('❌ Error:', e.message))
```

**Chat API:**
```javascript
fetch('http://localhost:8080/v1/chat', {
  method: 'POST',
  headers: {'Content-Type': 'application/json'},
  body: JSON.stringify({storeId: '10001', question: 'How many pumps are online?'})
})
  .then(r => r.json())
  .then(d => console.log('✅ Chat response:', d.answer))
  .catch(e => console.error('❌ Error:', e.message))
```

**Expected Output:**
```
✅ Stores: 10 found
✅ Insights keys: (5) ['topOfflineStores', 'lowTankLevelStores', 'incidentCountsBySeverity', 'transactionVolumeByStore', 'revenueByStore']
✅ Chat response: Store 10001 (Austin, TX) is ONLINE with 0 offline pumps. In the last 24h, ...
```

---

## 🐛 TROUBLESHOOTING

### Problem: "Unable to load stores" Message

**Check 1: Backend Running?**
```powershell
# In another PowerShell window
Invoke-WebRequest http://localhost:8080/v1/stores
```
Should return JSON with store data, not an error.

**Check 2: Browser Console (F12)**
Look for error messages. You should see:
```
✅ StoresComponent initialized
✅ Loading stores from: /v1/stores?page=0&size=10
✅ Stores loaded successfully: Object {...}
```

**Check 3: Network Tab (F12)**
- Click on `/v1/stores` request
- Status should be **200** (green)
- Response should show JSON array

**Fix:**
```powershell
# Kill any existing processes
Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force
Get-Process node -ErrorAction SilentlyContinue | Stop-Process -Force

# Restart both servers
cd D:\dex-insights-mini
mvn spring-boot:run

# In another terminal
cd D:\dex-insights-mini\ui\dex-ui
npm start
```

---

### Problem: "CORS" Error in Console

**Error Message:**
```
Access to XMLHttpRequest at 'http://localhost:8080/...' has been blocked by CORS policy
```

**Why:** We already fixed this! CORS is enabled in all controllers.

**Make sure:**
- You rebuilt with: `mvn clean install`
- You restarted the backend: `mvn spring-boot:run`
- The JAR file is new (from the recent build)

**If still happening:**
```powershell
# Clean and rebuild
cd D:\dex-insights-mini
mvn clean install -DskipTests
```

---

### Problem: 404 Error on /v1/insights/overview

**Backend Log Shows:**
```
No mapping for GET /v1/insights/overview
```

**Cause:** Controller not found

**Fix:**
1. Verify `InsightsController.java` exists:
   ```powershell
   ls D:\dex-insights-mini\src\main\java\com\dex\dex_insights_mini\controller\
   ```
   Should include: `InsightsController.java`

2. Verify endpoint is mapped:
   ```
   @GetMapping("/overview")
   ```

3. Rebuild and restart:
   ```powershell
   mvn clean install -DskipTests
   ```

---

### Problem: Stores Load But Chat/Insights Don't

**Check Backend Logs** in PowerShell window where you ran `mvn spring-boot:run`

**Common Issues:**
- Missing Spring annotations
- Data files not found
- JSON parsing error
- Exception in service

**Check Data Files:**
```powershell
ls D:\dex-insights-mini\src\main\resources\data\

# Verify files have content
(Get-Content D:\dex-insights-mini\src\main\resources\data\stores.json | Measure-Object -Line).Lines
# Should show: 254
```

---

### Problem: Application Runs But No Data Shows

**Check:**
1. **Network Tab (F12)** - Request completed but response is empty
2. **Backend Logs** - Any error messages?
3. **Browser Console** - Any JavaScript errors?

**Test:**
```powershell
# Check data file is valid JSON
python -m json.tool D:\dex-insights-mini\src\main\resources\data\stores.json

# Or
$data = Get-Content D:\dex-insights-mini\src\main\resources\data\stores.json | ConvertFrom-Json
$data.Count
# Should show: 254
```

---

## 📊 PROJECT STRUCTURE

```
D:\dex-insights-mini\
├── ✅ Backend (Spring Boot)
│   ├── Controllers/
│   │   ├── StoreController.java (GET /v1/stores, GET /v1/stores/{id})
│   │   ├── InsightsController.java (GET /v1/insights/overview)
│   │   └── ChatController.java (POST /v1/chat)
│   ├── Services/
│   │   ├── StoreService.java (load, filter, paginate stores)
│   │   ├── InsightsService.java (generate overview)
│   │   └── ChatService.java (answer questions)
│   ├── Repository/
│   │   └── JsonRepository.java (load from JSON files)
│   └── Resources/
│       └── data/
│           ├── stores.json (254 stores)
│           ├── transactions.json
│           └── incidents.json
│
├── ✅ Frontend (Angular)
│   ├── Components/
│   │   ├── StoresComponent (list, filter, paginate)
│   │   ├── InsightsComponent (show overview)
│   │   └── ChatComponent (send questions)
│   └── Services/
│       └── HttpClient (communicate with backend)
│
└── Configuration/
    ├── proxy.conf.json (proxy /v1/* to localhost:8080)
    ├── QUICK_START.md (quick reference)
    └── DEBUGGING_GUIDE.md (troubleshooting)
```

---

## 📞 QUICK HELP

| Issue | Solution |
|-------|----------|
| Can't start backend | Check Java is installed: `java -version` |
| Can't start frontend | Check Node.js: `node --version` |
| Port 8080 in use | `Get-Process -Port 8080 \| Stop-Process -Force` |
| Port 4200 in use | `Get-Process -Port 4200 \| Stop-Process -Force` |
| Data not loading | Check: Browser console (F12), Backend logs, Network tab |
| CORS error | Rebuild: `mvn clean install -DskipTests` |
| 404 error | Check endpoint mapping in controller |
| 500 error | Check backend logs in PowerShell window |

---

## 🎉 YOU'RE ALL SET!

The application is now ready for testing. All components should load data from the backend and display it in the UI.

**If you encounter any issues:**
1. Open browser DevTools (F12)
2. Check Console tab for our detailed error messages
3. Check Network tab for HTTP responses
4. Review DEBUGGING_GUIDE.md for detailed troubleshooting


