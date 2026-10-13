#!/bin/bash
set -e

echo "================================================================="
echo "   StudyHub Production Cloud VPS Auto-Deployment Script"
echo "================================================================="

# 1. Detect OS and ensure Docker & Docker Compose are installed
if ! command -v docker &> /dev/null; then
    echo "[1/4] Docker not found. Installing Docker..."
    curl -fsSL https://get.docker.com -o get-docker.sh
    sudo sh get-docker.sh
    sudo usermod -aG docker $USER || true
    echo "Docker installed successfully."
else
    echo "[1/4] Docker is already installed."
fi

if ! docker compose version &> /dev/null; then
    echo "Installing Docker Compose plugin..."
    sudo apt-get update && sudo apt-get install -y docker-compose-plugin
fi

# 2. Setup Environment Variables
echo "[2/4] Setting up environment configuration..."
if [ ! -f .env ]; then
    cp .env.example .env
    # Generate random secure passwords for production
    DB_PASS=$(openssl rand -hex 16)
    DB_ROOT_PASS=$(openssl rand -hex 16)
    JWT_KEY=$(openssl rand -hex 32)
    sed -i "s/studyhub_pass_secure/$DB_PASS/g" .env
    sed -i "s/root_secret_change_me/$DB_ROOT_PASS/g" .env
    sed -i "s/your-256-bit-secret-key-must-be-at-least-32-chars-long-here/$JWT_KEY/g" .env
    echo ".env created with cryptographically secure random credentials."
fi

# Detect Server Public IP
PUBLIC_IP=$(curl -s https://api.ipify.org || echo "localhost")
echo "Detected Server Public IP: $PUBLIC_IP"

# Update CORS_ALLOWED_ORIGINS to include public IP
if ! grep -q "$PUBLIC_IP" .env; then
    echo "CORS_ALLOWED_ORIGINS=http://$PUBLIC_IP,http://localhost" >> .env
fi

# 3. Build & Run Containers
echo "[3/4] Building and launching containers via Docker Compose..."
docker compose down || true
docker compose up -d --build

# 4. Wait and Verify Health
echo "[4/4] Verifying services health..."
sleep 15
docker compose ps

echo "================================================================="
echo "   🎉 StudyHub Deployment Completed Successfully!"
echo "================================================================="
echo "   Frontend Web UI : http://$PUBLIC_IP"
echo "   Backend Health  : http://$PUBLIC_IP:8080/actuator/health"
echo "   Admin Login     : admin@studyhub.edu / AdminStudyHub#2026"
echo "================================================================="
