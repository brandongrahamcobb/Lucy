lucy  
====  

**lucy** is a Discord bot built in Python. It supports  
- OpenAI-powered chat & image generation  
- Moderation (message wipes, auto-mod, etc.)  
- AI utilities (summaries, embeddings, more to come)  

Installation  
------------  

Prerequisites  
• Python 3.13+ (`python3 --version`)  
• pip (`pip --version`)  

1. Clone the repository
```bash
git clone https://github.com/brandongrahamcobb/lucy.git
   cd lucy
```
2. Create and activate a virtual environment
```bash
python3 -m venv .venv
   # macOS / Linux
   source .venv/bin/activate
   # Windows (PowerShell)
   .venv\Scripts\Activate.ps1
```
3. Inside the venv, install Poetry
```bash
pip install poetry
```
4. Build the wheel
```bash
poetry build --format wheel
```
5. Install the built wheel
```bash
pip install dist/lucy-1.0.0-py3-none-any.whl
```
Configuration  
-------------  

On first run, `lucy` will interactively prompt you for:  
• Discord Bot Token  
• Command prefix (e.g. `!`)  
• OpenAI API key (for chat, images, embeddings)  
• Database URL (for logs, settings)  

Those answers are saved in:
```txt
<installation_directory>/.config/config.yml
```
All future runs load settings from this file—no environment variables needed.  

Running  
-------  

With your venv active, simply run:
```bash
lucy
```
The bot will read (or create) its config, connect to Discord, and register its commands.
