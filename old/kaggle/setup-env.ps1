# PowerShell script to set up .env file from user input
# Run this to create your .env file securely

Write-Host "Setting up .env file for local development..." -ForegroundColor Green
Write-Host ""

# Check if .env already exists
if (Test-Path .env) {
    $overwrite = Read-Host ".env file already exists. Overwrite? (y/N)"
    if ($overwrite -ne "y" -and $overwrite -ne "Y") {
        Write-Host "Cancelled." -ForegroundColor Yellow
        exit
    }
}

# Get Kaggle credentials
Write-Host "Kaggle API Credentials:" -ForegroundColor Cyan
$kaggle_username = Read-Host "Enter your Kaggle username"
$kaggle_key = Read-Host "Enter your Kaggle API key" -AsSecureString
$kaggle_key_plain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($kaggle_key)
)

# Get Google API key
Write-Host ""
Write-Host "Google API Key (for Gemini):" -ForegroundColor Cyan
$google_key = Read-Host "Enter your Google API key" -AsSecureString
$google_key_plain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($google_key)
)

# Create .env file
$env_content = @"
# Kaggle API Credentials
KAGGLE_USERNAME=$kaggle_username
KAGGLE_KEY=$kaggle_key_plain

# Google API Key (for Gemini)
GOOGLE_API_KEY=$google_key_plain
"@

$env_content | Out-File -FilePath .env -Encoding utf8 -NoNewline

Write-Host ""
Write-Host "✅ .env file created successfully!" -ForegroundColor Green
Write-Host "⚠️  Remember: .env is in .gitignore and won't be committed to git" -ForegroundColor Yellow

