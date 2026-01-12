import requests

# Configurare
BASE_URL = "http://localhost:8080/api"
AUTH_URL = f"{BASE_URL}/auth"

# 1. Cream un user nou (ca sa fim siguri)
username = "admin_nou"
password = "password123"

print(f"1. Inregistrare user: {username}...")
requests.post(f"{AUTH_URL}/register", json={"username": username, "password": password})

# 2. Ne logam si luam token-ul
print("2. Login...")
resp = requests.post(f"{AUTH_URL}/login", json={"username": username, "password": password})

if resp.status_code != 200:
    print(f"❌ Login esuat: {resp.text}")
    exit()

token = resp.json().get("token")
print("✅ Token obtinut!")

# 3. Cream simbolurile
headers = {"Authorization": f"Bearer {token}", "Content-Type": "application/json"}

symbols = [
    {"symbolCode": "AAPL", "name": "Apple Inc.", "type": "STOCK"},
    {"symbolCode": "BTC", "name": "Bitcoin", "type": "CRYPTO"},
    {"symbolCode": "GOOGL", "name": "Alphabet Inc.", "type": "STOCK"}
]

print("3. Creare simboluri...")
for sym in symbols:
    r = requests.post(f"{BASE_URL}/symbols", json=sym, headers=headers)
    if r.status_code in [200, 201]:
        print(f"  ✅ {sym['symbolCode']} creat.")
    elif r.status_code == 403:
        print(f"  ❌ 403 Forbidden la {sym['symbolCode']} (Token respins).")
    else:
        print(f"  ⚠️ {sym['symbolCode']}: {r.status_code} - {r.text}")

print("\n🚀 Gata! Acum ruleaza 'python e2e_test.py' pentru date si fa screenshot-ul.")