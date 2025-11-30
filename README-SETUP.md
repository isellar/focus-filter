# Local Development Setup Guide

This guide will help you set up the Focus Filter notebook to run locally with proper credential management.

## Quick Start

### 1. Set Up Environment Variables

**Option A: Use the setup script (Recommended)**

**Windows (PowerShell):**
```powershell
.\setup-env.ps1
```

**Linux/Mac/WSL:**
```bash
chmod +x setup-env.sh
./setup-env.sh
```

**Option B: Manual Setup**

Create a `.env` file in the project root:
```env
# Kaggle API Credentials
KAGGLE_USERNAME=your_kaggle_username
KAGGLE_KEY=your_kaggle_api_key

# Google API Key (for Gemini)
GOOGLE_API_KEY=your_google_api_key
```

### 2. Get Your API Keys

**Kaggle API Key:**
1. Go to https://www.kaggle.com/settings
2. Scroll to "API" section
3. Click "Create New Token"
4. Download `kaggle.json`
5. Extract `username` and `key` values

**Google API Key (Gemini):**
1. Go to https://aistudio.google.com/app/api-keys
2. Click "Create API Key"
3. Copy the key

### 3. Run with Docker (Recommended)

**Using Docker Compose:**
```powershell
docker-compose up
```

Then open http://localhost:8888 in your browser.

**Using Docker Run:**
```powershell
# PowerShell
.\docker-run.ps1

# Or WSL/Bash
./docker-run.sh
```

### 4. Run Locally (Without Docker)

**Install dependencies:**
```bash
pip install -r requirements.txt
```

**Set environment variables:**
```powershell
# PowerShell
$env:GOOGLE_API_KEY = "your_key_here"

# Or load from .env
python -c "from dotenv import load_dotenv; load_dotenv()"
```

**Run Jupyter:**
```bash
jupyter notebook submission.ipynb
# or
jupyter lab submission.ipynb
```

## What Changed to Fix the Hanging Issue

1. **Added `nest_asyncio`**: Allows async code to run properly in Jupyter notebooks
2. **Created helper function**: `run_notification_test()` wraps async operations
3. **Simplified test cells**: All test cells now use the helper function

## Troubleshooting

### Still Hanging?

1. **Check if `nest_asyncio` is installed:**
   ```python
   import nest_asyncio
   nest_asyncio.apply()
   ```

2. **Verify your API key is set:**
   ```python
   import os
   print("GOOGLE_API_KEY set:", "GOOGLE_API_KEY" in os.environ)
   ```

3. **Test the connection:**
   ```python
   from google.genai import Client
   client = Client(api_key=os.environ["GOOGLE_API_KEY"])
   # Should not hang
   ```

### Environment Variables Not Loading?

- Make sure `.env` file is in the project root
- Check that `python-dotenv` is installed: `pip install python-dotenv`
- Verify the `.env` file format (no spaces around `=`)

### Docker Issues?

- Make sure Docker Desktop is running
- Check port 8888 is available: `netstat -an | findstr 8888` (Windows)
- Try a different port: Change `8888:8888` to `8889:8888` in docker-compose.yml

## Security Notes

- ✅ `.env` is in `.gitignore` - your credentials won't be committed
- ✅ Never commit API keys to git
- ✅ Use `.env.example` as a template (without real keys)
- ⚠️  Don't share your `.env` file

## Next Steps

Once everything is set up:
1. Open `submission.ipynb` in Jupyter
2. Run cells in order
3. The test cells should now work without hanging!

