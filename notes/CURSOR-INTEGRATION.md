# Using Cursor with Docker Jupyter

There are several ways to work with your Docker-based Jupyter notebook from Cursor:

## Option 1: Use Jupyter Lab in Browser (Current Setup) ✅

This is what you have now and it works great:
- Open http://localhost:8888 in your browser
- Edit notebooks directly in Jupyter Lab
- Changes are saved to your local filesystem (mounted volume)
- Cursor can still edit the `.ipynb` file directly

**Pros:**
- ✅ Already working
- ✅ Full Jupyter Lab features
- ✅ Interactive widgets, outputs, etc.
- ✅ Can edit in Cursor and see changes in browser

**Cons:**
- ⚠️  Need to switch between Cursor and browser

## Option 2: Connect Cursor to Remote Jupyter Kernel

You can configure Cursor to use the Jupyter kernel running in Docker:

### Step 1: Get the Jupyter Connection Info

In your Docker container, Jupyter is running. You need to get the connection token:

```powershell
# Get the Jupyter token from container logs
docker logs kaggle-notebook 2>&1 | Select-String "token"
```

Or check the container directly:
```powershell
docker exec kaggle-notebook jupyter lab list
```

### Step 2: Configure Cursor

1. In Cursor, open Command Palette (`Ctrl+Shift+P`)
2. Search for "Jupyter: Specify Jupyter Server for Connections"
3. Enter: `http://localhost:8888?token=YOUR_TOKEN`
   - Replace `YOUR_TOKEN` with the token from step 1
   - Or if you set `--NotebookApp.token=''`, use: `http://localhost:8888`

### Step 3: Select Kernel

1. Open your notebook in Cursor
2. Click on the kernel selector (top right of notebook)
3. Select "Existing Jupyter Server"
4. Enter: `http://localhost:8888`

**Note:** This might not work perfectly because Cursor's Jupyter integration expects a local kernel, not a remote one.

## Option 3: Run Jupyter Locally (Recommended for Cursor) 🎯

If you want the best Cursor integration, run Jupyter locally:

### Setup:

1. **Install Python and dependencies locally:**
   ```powershell
   # Make sure you have Python 3.8+
   python --version
   
   # Install dependencies
   pip install -r requirements.txt
   ```

2. **Set environment variables:**
   ```powershell
   # Load from .env
   $env:GOOGLE_API_KEY = (Get-Content .env | Select-String "GOOGLE_API_KEY").ToString().Split('=')[1]
   ```

   Or use python-dotenv:
   ```python
   from dotenv import load_dotenv
   load_dotenv()
   ```

3. **Install Jupyter extension in Cursor:**
   - Cursor should have Jupyter support built-in
   - If not, install the Jupyter extension

4. **Open notebook in Cursor:**
   - Open `submission.ipynb`
   - Cursor should automatically detect it's a notebook
   - Select your local Python kernel

**Pros:**
- ✅ Full Cursor integration
- ✅ Syntax highlighting, IntelliSense
- ✅ Run cells directly in Cursor
- ✅ No browser needed

**Cons:**
- ⚠️  Need to install packages locally
- ⚠️  Might have different environment than Docker

## Option 4: Hybrid Approach (Best of Both Worlds) 🌟

Use both:
- **Docker for running** (consistent environment)
- **Cursor for editing** (better editor experience)

**Workflow:**
1. Edit notebook in Cursor
2. Save file
3. Refresh browser (Jupyter Lab auto-reloads)
4. Run cells in browser
5. See outputs in browser

**Tip:** You can even edit the notebook JSON directly in Cursor if needed, but Jupyter Lab is better for that.

## Recommendation

For now, stick with **Option 1** (browser-based Jupyter Lab) because:
- ✅ It's already working
- ✅ You have the exact Kaggle environment
- ✅ All packages are pre-installed
- ✅ Cursor can still edit the file

If you want better Cursor integration later, try **Option 3** (local Jupyter) for editing, but use Docker for final testing.

## Quick Tips

- **Auto-reload:** Jupyter Lab watches for file changes, so edits in Cursor appear automatically
- **Git:** You can commit changes made in either Cursor or Jupyter Lab
- **Debugging:** Use Jupyter Lab's debugger in the browser
- **Extensions:** Install Jupyter Lab extensions in the browser interface


