#!/bin/bash
# Script to run Kaggle notebook environment locally with Docker

# Set your Kaggle API credentials (you'll need to get these from Kaggle)
export KAGGLE_USERNAME="your_username"
export KAGGLE_KEY="your_api_key"

# Run the Kaggle Docker image
# This uses the official Kaggle Docker image
docker run -it --rm \
  -p 8888:8888 \
  -v "$(pwd):/home/jovyan/work" \
  -e KAGGLE_USERNAME="$KAGGLE_USERNAME" \
  -e KAGGLE_KEY="$KAGGLE_KEY" \
  -w /home/jovyan/work \
  gcr.io/kaggle-images/python:latest \
  jupyter lab --NotebookApp.token='' --NotebookApp.password='' --ip=0.0.0.0 --port=8888 --allow-root --no-browser

