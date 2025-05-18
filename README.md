lucy  
====  

**lucy** is a Discord bot built in Python. It supports  
- OpenAI-powered chat & image generation  
- Moderation (auto-mod, etc.)  
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

Chat Interface  
--------------  

lucy provides both a prefix-based and a slash `/chat` command powered by OpenAI.  

Usage:  
• `<prefix>chat <model> <prompt>`  
• `/chat` via Discord’s slash-command menu  
• Mention or reply to lucy (e.g. `@lucy What’s the weather?`) for a quick completion using the default model  

Required Inputs  
• model (string) – OpenAI model name (e.g. `gpt-4`, `gpt-3.5-turbo`)  
• prompt (string) – your query or instruction  

Slash-Command Options  
All of these appear as editable fields in the `/chat` form:  
• new (bool, default true) – start a fresh context or continue history  
• max_tokens (int) – cap on generated tokens  
• response_format (string) – e.g. `text`, `json`  
• stop (string) – sequence to halt generation  
• store (bool) – save this exchange in history  
• stream (bool) – receive tokens in real time  
• sys_input (string) – system-level instruction prefixed to your prompt  
• temperature (float, 0.0–2.0) – controls randomness  
• top_p (float, 0.0–1.0) – nucleus-sampling parameter  
• use_history (bool) – include prior conversation turns  
• add_completion_to_history (bool) – append the AI’s response to history  

Example (prefix):  
```  
!chat gpt-4 “Write a haiku about autumn”  
```  
Example (slash):  
• `/chat model:gpt-3.5-turbo prompt:"Summarize this article" temperature:0.5 stream:yes`  

Configuration Flags  
These are set in `.config/config.yml` (created on first run):  
• OPENAI_MODERATION: true  
    – must be true to allow any completions  
• OPENAI_CHAT_COMPLETION: true  
    – enables both prefix and `/chat` commands  
• DISCORD_RELEASE_MODE: true  
    – when false, only the primary server will send user data to OpenAI; test servers are muted  

All flags default as shown. You can reconfigure interactively or by editing `.config/config.yml`.

Image Generation  
---------------  

lucy also supports image generation using OpenAI’s DALL·E 3.  

Usage (prefix)  
• `<prefix>imagine <prompt>`  
  • Generate a DALL·E 3 image from your prompt and post it in-channel.  

Usage (slash)  
• `/imagine prompt:"<your prompt here>"`  

Example  
• `!imagine a serene mountain landscape at sunrise in watercolor style`  

Response  
• lucy will attach the generated image(s) directly in the Discord channel.  
