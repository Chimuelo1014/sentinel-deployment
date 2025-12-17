#!/bin/bash
# Sentinel VPS Setup Script
# Run this script on your VPS to prepare for deployment

set -e

echo "🚀 Sentinel VPS Setup Script"
echo "=============================="
echo ""

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if running as root
if [ "$EUID" -eq 0 ]; then 
    echo -e "${RED}❌ Please do not run as root. Run as sentinel user.${NC}"
    exit 1
fi

echo -e "${YELLOW}Step 1: Installing Docker...${NC}"
if ! command -v docker &> /dev/null; then
    curl -fsSL https://get.docker.com -o get-docker.sh
    sudo sh get-docker.sh
    sudo usermod -aG docker $USER
    echo -e "${GREEN}✅ Docker installed${NC}"
else
    echo -e "${GREEN}✅ Docker already installed${NC}"
fi

echo -e "${YELLOW}Step 2: Installing Docker Compose...${NC}"
if ! command -v docker-compose &> /dev/null; then
    sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
    echo -e "${GREEN}✅ Docker Compose installed${NC}"
else
    echo -e "${GREEN}✅ Docker Compose already installed${NC}"
fi

echo -e "${YELLOW}Step 3: Installing Nginx...${NC}"
if ! command -v nginx &> /dev/null; then
    sudo apt update
    sudo apt install nginx -y
    sudo systemctl enable nginx
    sudo systemctl start nginx
    echo -e "${GREEN}✅ Nginx installed${NC}"
else
    echo -e "${GREEN}✅ Nginx already installed${NC}"
fi

echo -e "${YELLOW}Step 4: Configuring Firewall...${NC}"
sudo ufw allow OpenSSH
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw --force enable
echo -e "${GREEN}✅ Firewall configured${NC}"

echo -e "${YELLOW}Step 5: Installing Certbot...${NC}"
if ! command -v certbot &> /dev/null; then
    sudo apt install certbot python3-certbot-nginx -y
    echo -e "${GREEN}✅ Certbot installed${NC}"
else
    echo -e "${GREEN}✅ Certbot already installed${NC}"
fi

echo -e "${YELLOW}Step 6: Cloning repository...${NC}"
if [ ! -d "$HOME/sentinel-deployment" ]; then
    cd ~
    git clone https://github.com/Chimuelo1014/sentinel-deployment.git
    cd sentinel-deployment
    echo -e "${GREEN}✅ Repository cloned${NC}"
else
    echo -e "${GREEN}✅ Repository already exists${NC}"
    cd ~/sentinel-deployment
    git pull origin main
fi

echo -e "${YELLOW}Step 7: Configuring Nginx...${NC}"
sudo cp nginx-sentinel.conf /etc/nginx/sites-available/sentinel
sudo ln -sf /etc/nginx/sites-available/sentinel /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t
sudo systemctl restart nginx
echo -e "${GREEN}✅ Nginx configured${NC}"

echo -e "${YELLOW}Step 8: Setting up environment file...${NC}"
if [ ! -f ".env.production" ]; then
    cp .env.production.template .env.production
    echo -e "${YELLOW}⚠️  Please edit .env.production with your actual credentials:${NC}"
    echo "nano .env.production"
else
    echo -e "${GREEN}✅ .env.production already exists${NC}"
fi

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}✅ VPS Setup Complete!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "${YELLOW}Next Steps:${NC}"
echo "1. Edit environment file:"
echo "   nano ~/sentinel-deployment/.env.production"
echo ""
echo "2. Obtain SSL certificates:"
echo "   sudo certbot --nginx -d sentinel.crudzaso.com -d service.sentinel.crudzaso.com"
echo ""
echo "3. Start the application:"
echo "   cd ~/sentinel-deployment"
echo "   docker-compose -f docker-compose.prod.yml --env-file .env.production up -d"
echo ""
echo "4. View logs:"
echo "   docker-compose -f docker-compose.prod.yml logs -f"
echo ""
echo -e "${GREEN}Your domains:${NC}"
echo "Frontend: https://sentinel.crudzaso.com"
echo "Backend:  https://service.sentinel.crudzaso.com"
