FROM python:3.11-slim

WORKDIR /workspace

# Install system dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    git \
    curl \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

# Set Python to unbuffered mode for better logging
ENV PYTHONUNBUFFERED=1

# Python dependencies will be installed via postCreateCommand
# or manually: pip install -r requirements.txt

