#!/bin/bash

###############################################################################
# My Finances BFF - Docker Deployment Script
# Quick deployment script for Orange Pi Ubuntu
###############################################################################

set -e  # Exit on error

echo "=================================="
echo "My Finances BFF - Docker Deploy"
echo "=================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if docker-compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}Error: docker-compose is not installed${NC}"
    echo "Install with: sudo apt install docker-compose"
    exit 1
fi

# Check if we're in the right directory
if [ ! -f "docker-compose.yml" ]; then
    echo -e "${RED}Error: docker-compose.yml not found${NC}"
    echo "Please run this script from the project root directory"
    exit 1
fi

echo -e "${YELLOW}Step 1: Stopping existing container...${NC}"
docker-compose down 2>/dev/null || true
echo -e "${GREEN}✓ Done${NC}"
echo ""

echo -e "${YELLOW}Step 2: Creating directories...${NC}"
mkdir -p logs uploads
chmod 755 logs uploads
echo -e "${GREEN}✓ Directories created${NC}"
echo ""

echo -e "${YELLOW}Step 3: Building Docker image...${NC}"
docker-compose build --no-cache
echo -e "${GREEN}✓ Image built${NC}"
echo ""

echo -e "${YELLOW}Step 4: Starting container...${NC}"
docker-compose up -d
echo -e "${GREEN}✓ Container started${NC}"
echo ""

echo -e "${YELLOW}Step 5: Waiting for application to start...${NC}"
sleep 5
echo -e "${GREEN}✓ Done${NC}"
echo ""

echo -e "${YELLOW}Step 6: Checking container status...${NC}"
docker-compose ps
echo ""

echo -e "${YELLOW}Step 7: Testing application...${NC}"
if curl -s http://localhost:9090/api/test > /dev/null; then
    echo -e "${GREEN}✓ Application is responding!${NC}"
else
    echo -e "${RED}✗ Application is not responding${NC}"
    echo "Check logs with: docker-compose logs -f"
fi
echo ""

echo -e "${YELLOW}Step 8: Checking logs directory...${NC}"
if [ -f "logs/app.log" ]; then
    echo -e "${GREEN}✓ Log file created!${NC}"
    echo "Last 5 lines of log:"
    tail -n 5 logs/app.log
else
    echo -e "${YELLOW}⚠ Log file not created yet (may take a few seconds)${NC}"
fi
echo ""

echo "=================================="
echo -e "${GREEN}Deployment Complete!${NC}"
echo "=================================="
echo ""
echo "Useful commands:"
echo "  - View logs (file):   tail -f logs/app.log"
echo "  - View logs (docker): docker-compose logs -f"
echo "  - Stop:               docker-compose down"
echo "  - Restart:            docker-compose restart"
echo "  - Status:             docker-compose ps"
echo ""
echo "Application URLs:"
echo "  - API Test:    http://192.168.0.60:9090/api/test"
echo "  - Swagger UI:  http://192.168.0.60:9090/swagger-ui/index.html"
echo ""
