# PowerShell script to run Kaggle notebook environment locally with Docker
# Run this from PowerShell

# Set your Kaggle API credentials (you'll need to get these from Kaggle)
$env:KAGGLE_USERNAME = "your_username"
$env:KAGGLE_KEY = "your_api_key"

# Get current directory
$currentDir = (Get-Location).Path

# Run the Kaggle Docker image
docker run -it --rm `
  -p 8888:8888 `
  -v "${currentDir}:/home/jovyan/work" `
  -e KAGGLE_USERNAME="$env:KAGGLE_USERNAME" `
  -e KAGGLE_KEY="$env:KAGGLE_KEY" `
  -w /home/jovyan/work `
  gcr.io/kaggle-images/python:latest `
  jupyter lab --NotebookApp.token='' --NotebookApp.password='' --ip=0.0.0.0 --port=8888 --allow-root --no-browser

