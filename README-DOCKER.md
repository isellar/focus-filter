# Running Kaggle Notebooks Locally with Docker

This guide will help you run your Kaggle notebook environment locally using Docker Desktop on Windows.

## Prerequisites

1. **Docker Desktop** installed and running
2. **Kaggle API credentials** (get from https://www.kaggle.com/settings)
3. **WSL 2** (recommended for better performance, but not required)

## Quick Start

### Option 0: Using Dev Container (Most Seamless - VS Code/Cursor)

This is the **recommended** option for the best development experience in VS Code or Cursor.

1. **Set up your `.env` file** (if not already done):
   ```env
   KAGGLE_USERNAME=your_kaggle_username
   KAGGLE_KEY=your_kaggle_api_key
   GOOGLE_API_KEY=your_google_api_key
   ```

2. **Open in Dev Container:**
   - In VS Code/Cursor: Press `F1` or `Ctrl+Shift+P`
   - Type "Dev Containers: Reopen in Container"
   - Select it and wait for the container to build/start

3. **You're ready!**
   - The container will start automatically
   - Jupyter Lab will be available at http://localhost:8888
   - All Python extensions are pre-configured
   - Your workspace is mounted and ready to use

**Benefits:**
- ✅ Seamless integration with VS Code/Cursor
- ✅ Automatic Python environment setup
- ✅ Pre-installed Jupyter extensions
- ✅ Port forwarding configured automatically
- ✅ Terminal access to the container environment

### Option 1: Using Docker Compose (Recommended for CLI)

1. **Set up your Kaggle credentials:**
   ```powershell
   # In PowerShell
   $env:KAGGLE_USERNAME = "your_kaggle_username"
   $env:KAGGLE_KEY = "your_kaggle_api_key"
   ```

2. **Start the container:**
   ```powershell
   docker-compose up
   ```

3. **Access Jupyter Lab:**
   - Open your browser to: http://localhost:8888
   - Your notebook files will be in the `/home/jovyan/work` directory

### Option 2: Using Docker Run (PowerShell)

1. **Edit `docker-run.ps1`** and add your Kaggle credentials:
   ```powershell
   $env:KAGGLE_USERNAME = "your_username"
   $env:KAGGLE_KEY = "your_api_key"
   ```

2. **Run the script:**
   ```powershell
   .\docker-run.ps1
   ```

### Option 3: Using Docker Run (WSL/Bash)

1. **Set environment variables:**
   ```bash
   export KAGGLE_USERNAME="your_username"
   export KAGGLE_KEY="your_api_key"
   ```

2. **Run the container:**
   ```bash
   chmod +x docker-run.sh
   ./docker-run.sh
   ```

## Getting Your Kaggle API Key

1. Go to https://www.kaggle.com/settings
2. Scroll down to "API" section
3. Click "Create New Token"
4. Download the `kaggle.json` file
5. Extract the `username` and `key` values

## Troubleshooting

### Port 8888 already in use
If port 8888 is already in use, change it in the docker command:
```powershell
-p 8889:8888  # Use port 8889 instead
```

### Permission issues on Windows
Make sure Docker Desktop has access to your drive. Go to:
- Docker Desktop → Settings → Resources → File Sharing
- Add your drive (usually C:)

### Slow performance
- Use WSL 2 backend in Docker Desktop settings
- Ensure Docker Desktop has enough resources allocated (Settings → Resources)

### Can't access Jupyter
- Check that the container is running: `docker ps`
- Check the logs: `docker logs kaggle-notebook`
- Try accessing http://127.0.0.1:8888 instead of localhost

## Stopping the Container

- **Docker Compose:** Press `Ctrl+C` or run `docker-compose down`
- **Docker Run:** Press `Ctrl+C` in the terminal

## Notes

- Your local files are mounted to `/home/jovyan/work` in the container
- Changes made in the container are reflected on your local filesystem
- The Kaggle image includes all necessary packages (google-adk, etc.)
- No GPU support needed for this notebook (CPU-only)

## Alternative: Using Jupyter Notebook Directly

If you prefer to run Jupyter locally without Docker:

1. Install Python 3.8+
2. Install dependencies:
   ```bash
   pip install jupyter google-adk google-genai kaggle
   ```
3. Set up Kaggle credentials:
   ```bash
   mkdir -p ~/.kaggle
   # Copy your kaggle.json to ~/.kaggle/kaggle.json
   ```
4. Run: `jupyter notebook submission.ipynb`

