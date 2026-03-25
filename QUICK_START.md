# 🚀 DEX INSIGHTS MINI - QUICK START GUIDE

## ⚡ FASTEST METHOD (Automated)

### Option 1: One-Click Startup (PowerShell)
1. Open **PowerShell** as Administrator
2. Navigate to the project root:
```powershell
cd D:\dex-insights-mini
```
3. Run the startup script:
```powershell
.\START_APPLICATION.ps1
```
4. Wait for both servers to start (takes ~30 seconds)
5. **Open your browser**: http://localhost:4200

---

## 📋 MANUAL METHOD (Step by Step)

### Step 1: Start Backend (Terminal 1)
```powershell
cd D:\dex-insights-mini
mvn spring-boot:run
```
✅ Wait until you see: **"Tomcat started on port(s): 8080"**

### Step 2: Start Frontend (Terminal 2)
```powershell
cd D:\dex-insights-mini\ui\dex-ui
npm install  # Only needed first time
npm start -- --proxy-config proxy.conf.json
```
✅ Wait until you see: **"Application bundle generation complete."**

### Step 3: Open Browser
🌐 Visit: **http://localhost:4200**

---

## ✅ TESTING CHECKLIST

Once the app is open in your browser:

### 🏪 Stores Tab
- [ ] Stores list loads
- [ ] Shows store names, brand, status
- [ ] Pagination works (Previous/Next buttons)
- [ ] Filters work (Brand, Status)
- [ ] "View" button navigates to store details

### 📊 Insights Tab
- [ ] Overview data loads
- [ ] Shows store metrics and statistics

### 💬 Chat Tab
- [ ] Store list loads in dropdown
- [ ] Can select a store
- [ ] Can ask questions
- [ ] Gets responses from backend

---

## 🔧 API ENDPOINTS TO TEST (Using Postman/curl)

### Get Stores
```
GET http://localhost:8080/v1/stores?page=0&size=10
```

### Get Store Details
```
GET http://localhost:8080/v1/stores/{storeId}
```
Example: `http://localhost:8080/v1/stores/10001`

### Get Insights
```
GET http://localhost:8080/v1/insights/overview
```

### Send Chat Message
```
POST http://localhost:8080/v1/chat
Content-Type: application/json

{
  "storeId": "10001",
  "question": "How many pumps are offline?"
}
```

---

## 🐛 TROUBLESHOOTING

### ❌ "Stores/Insights/Chat not loading"
1. Check backend is running: `http://localhost:8080/v1/stores`
2. Check browser console for errors (F12)
3. Check PowerShell/Terminal for error messages
4. Try refreshing the page (Ctrl+F5)

### ❌ "Port 8080/4200 already in use"
```powershell
# Kill process on port 8080
Get-Process | Where-Object {$_.Port -eq 8080} | Stop-Process -Force

# Kill process on port 4200
Get-Process | Where-Object {$_.Port -eq 4200} | Stop-Process -Force
```

### ❌ "npm install fails"
```powershell
cd D:\dex-insights-mini\ui\dex-ui
npm cache clean --force
npm install
```

### ❌ "Java/Node not found"
- Install Java 21: https://www.oracle.com/java/technologies/downloads/
- Install Node.js 20+: https://nodejs.org/

---

## 📊 PROJECT STRUCTURE

```
dex-insights-mini/
├── Backend (Spring Boot)
│   ├── Controllers: /v1/stores, /v1/insights, /v1/chat
│   ├── Services: StoreService, InsightsService, ChatService
│   └── Data: data/stores.json, transactions.json, incidents.json
│
└── Frontend (Angular)
    ├── Components: Stores, Insights, Chat
    └── Routing: /stores, /insights, /chat
```

---

## 🎯 EXPECTED DATA

The application loads data from JSON files:
- **stores.json**: 254 stores with pump/tank info
- **transactions.json**: Transaction history
- **incidents.json**: Incident logs

All data is loaded on application startup.

---

## 📞 SUPPORT

If you encounter issues:
1. Check the browser console (F12 → Console tab)
2. Check the backend logs in PowerShell
3. Verify both servers are running on correct ports
4. Check that data files exist in `src/main/resources/data/`


