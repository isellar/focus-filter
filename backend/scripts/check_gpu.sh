#!/bin/bash
# GPU Detection Script for Focus Filter Backend
# Checks if NVIDIA GPU is available and Docker GPU support is configured

echo "Checking GPU availability..."

# Check if nvidia-smi is available
if command -v nvidia-smi &> /dev/null; then
    echo "✅ nvidia-smi found"
    GPU_NAME=$(nvidia-smi --query-gpu=name --format=csv,noheader | head -n 1)
    echo "   GPU: $GPU_NAME"
    
    # Check if Docker GPU support is available
    if docker run --rm --gpus all nvidia/cuda:12.1.0-base-ubuntu22.04 nvidia-smi &> /dev/null; then
        echo "✅ Docker GPU support is configured"
        echo ""
        echo "💡 Use docker-compose.gpu.yml for GPU-enabled development:"
        echo "   docker-compose -f docker-compose.gpu.yml up -d"
        exit 0
    else
        echo "⚠️  GPU detected but Docker GPU support not configured"
        echo "   Install nvidia-container-toolkit to enable GPU support"
        echo "   Falling back to CPU mode"
        exit 1
    fi
else
    echo "ℹ️  No NVIDIA GPU detected"
    echo "   Using CPU-only mode"
    exit 1
fi

