import requests
import time
import random
import json

# Configurare
BASE_URL = "http://localhost:8080/api"
AUTH_URL = f"{BASE_URL}/auth"
USERNAME = "tester"
PASSWORD = "password123"
SYMBOL = "AAPL"

# Culori pentru consolă
GREEN = '\033[92m'
RED = '\033[91m'
YELLOW = '\033[93m'
RESET = '\033[0m'

def log(msg, color=RESET):
    print(f"{color}{msg}{RESET}")

def login():
    # 1. Înregistrare (ignorăm eroarea dacă există deja)
    requests.post(f"{AUTH_URL}/register", json={"username": USERNAME, "password": PASSWORD})

    # 2. Login
    resp = requests.post(f"{AUTH_URL}/login", json={"username": USERNAME, "password": PASSWORD})
    if resp.status_code == 200:
        token = resp.json().get("token")
        log(f"✅ Login reușit! Token obținut.", GREEN)
        return token
    else:
        log(f"❌ Login eșuat: {resp.text}", RED)
        exit(1)

def run_load_test(token):
    headers = {"Authorization": f"Bearer {token}", "Content-Type": "application/json"}
    current_price = 150.00

    log(f"\n🚀 Începem LOAD TESTING și DATA FLOW (20 de iterații)...")

    for i in range(1, 21):
        # Simulare fluctuație normală
        change = random.uniform(-0.5, 0.5)
        current_price += change

        # La iterația 15 generăm o ANOMALIE (Spike)
        if i == 15:
            log(f"⚠️  GENERĂM ANOMALIE (SPIKE) la iterația {i}...", YELLOW)
            current_price = current_price * 1.20 # Creștere bruscă de 20%

        payload = {
            "price": round(current_price, 2),
            "volume": random.randint(1000, 5000),
            "timestamp": time.strftime("%Y-%m-%dT%H:%M:%S")
        }

        try:
            resp = requests.post(f"{BASE_URL}/prices/{SYMBOL}", json=payload, headers=headers)
            if resp.status_code in [200, 201]:
                print(f"[{i}/20] Trimis {SYMBOL}: ${payload['price']} -> Server OK")
            else:
                print(f"[{i}/20] Eroare: {resp.status_code}")
        except Exception as e:
            print(f"Eroare conexiune: {e}")

        # Așteptăm puțin între request-uri (0.5 secunde pentru Load Test rapid)
        time.sleep(0.5)

    log("\n✅ Testare finalizată. Verifică Dashboard-ul!", GREEN)

if __name__ == "__main__":
    jwt = login()
    run_load_test(jwt)