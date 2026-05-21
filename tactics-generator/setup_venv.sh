#!/bin/bash

# Exit on error
set -e

# Color output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}Setting up Python virtual environment...${NC}"

# Check if Python 3 is installed
if ! command -v python3 &> /dev/null; then
    echo -e "${RED}Error: python3 is not installed${NC}"
    exit 1
fi

# Virtual environment directory name
VENV_DIR="venv"

# Remove existing virtual environment if it exists
if [ -d "$VENV_DIR" ]; then
    echo -e "${YELLOW}Removing existing virtual environment...${NC}"
    rm -rf "$VENV_DIR"
fi

# Create virtual environment
echo -e "${GREEN}Creating virtual environment...${NC}"
python3 -m venv "$VENV_DIR"

# Activate virtual environment
echo -e "${GREEN}Activating virtual environment...${NC}"
source "$VENV_DIR/bin/activate"

# Upgrade pip
echo -e "${GREEN}Upgrading pip...${NC}"
pip install --upgrade pip

# Install requirements
if [ -f "requirements.txt" ]; then
    echo -e "${GREEN}Installing requirements from requirements.txt...${NC}"
    pip install -r requirements.txt
    echo -e "${GREEN}✓ Dependencies installed successfully${NC}"
else
    echo -e "${RED}Error: requirements.txt not found${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Setup complete!${NC}"
echo ""
echo -e "${YELLOW}To activate the virtual environment, run:${NC}"
echo -e "  source $VENV_DIR/bin/activate"
echo ""
echo -e "${YELLOW}To deactivate, run:${NC}"
echo -e "  deactivate"
