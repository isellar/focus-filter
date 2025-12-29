#!/bin/bash
# Bash script to set up .env file from user input
# Run this to create your .env file securely

echo "Setting up .env file for local development..."
echo ""

# Check if .env already exists
if [ -f .env ]; then
    read -p ".env file already exists. Overwrite? (y/N) " overwrite
    if [ "$overwrite" != "y" ] && [ "$overwrite" != "Y" ]; then
        echo "Cancelled."
        exit
    fi
fi

# Get Kaggle credentials
echo "Kaggle API Credentials:"
read -p "Enter your Kaggle username: " kaggle_username
read -sp "Enter your Kaggle API key: " kaggle_key
echo ""

# Get Google API key
echo ""
echo "Google API Key (for Gemini):"
read -sp "Enter your Google API key: " google_key
echo ""

# Create .env file
cat > .env << EOF
# Kaggle API Credentials
KAGGLE_USERNAME=$kaggle_username
KAGGLE_KEY=$kaggle_key

# Google API Key (for Gemini)
GOOGLE_API_KEY=$google_key
EOF

echo ""
echo "✅ .env file created successfully!"
echo "⚠️  Remember: .env is in .gitignore and won't be committed to git"

