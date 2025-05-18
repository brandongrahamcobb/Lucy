''' hybrid.py The purpose of this program is to be an extension to a Discord
    bot to provide the command functionality to Vyrtuous.
    Copyright (C) 2024  github.com/brandongrahamcobb

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
'''
from discord import Embed, File, app_commands
from discord.ext import commands
from googletrans import Translator, LANGUAGES
from lucy.utils.handlers.ai_manager import Completions, BatchProcessor, OpenAIUsageClient
from lucy.utils.handlers.chemistry_manager import construct_helm_from_peptide, draw_fingerprint, draw_watermarked_molecule, get_mol, get_molecule_name, get_proximity, gsrs, manual_helm_to_smiles
from lucy.utils.handlers.game_manager import Game
from lucy.utils.handlers.image_manager import add_watermark, combine_gallery, create_image, create_image_variation, edit_image, stable_cascade
from lucy.utils.handlers.message_manager import Message
from lucy.utils.handlers.predicator import Predicator
from lucy.utils.handlers.tag_manager import TagManager
from lucy.utils.inc.helpers import *
from lucy.utils.inc.frames import extract_random_frames
from lucy.utils.inc.google import google
from lucy.utils.inc.script import script
from lucy.utils.inc.unique_pairs import unique_pairs
from rdkit import Chem
from rdkit.Chem import AllChem, Crippen
from random import choice
from typing import Dict, List, Optional

import asyncio
import datetime
import discord
import io
import json
import openai
import os
import pubchempy as pcp
import re
import shlex
import time
import traceback
import uuid
import requests

class Hybrid(commands.Cog):
    def __init__(self, bot):
        self.bot = bot
        self.config = bot.config
        self.completions = Completions()
        self.batch_processor = BatchProcessor(bot)
        self.predicator = Predicator(self.bot)
        self.handler = Message(self.bot, self.config, self.completions, self.bot.db_pool)

    @commands.hybrid_command(name="chat", description="Usage: chat <model> <prompt>")
    async def chat(
        self,
        ctx: commands.Context,
        model: str,
        prompt: str,
        new: bool = True,
        max_tokens: int = None,
        response_format: str = None,
        stop: str = None,
        store: bool = None,
        stream: bool = None,
        sys_input: str = None,
        temperature: float = None,
        top_p: float = None,
        use_history: bool = None,
        add_completion_to_history: bool = None,
    ):
        if not self.predicator.is_release_mode_func(ctx):
            return
        async def function():
            array = await self.handler.process_array(prompt, attachments=ctx.message.attachments)
            custom_id = f"{ctx.author.id}-{uuid.uuid4()}"
            request_data = {
                "completions": 1,
                "custom_id": custom_id,
                "input_array": array,
                "max_tokens": max_tokens if max_tokens is not None else OPENAI_MODEL_OUTPUT_LIMITS[model],
                "model": model,
                "response_format": response_format if response_format is not None else OPENAI_CHAT_RESPONSE_FORMAT,
                "stop": stop if stop is not None else self.config.get("openai_chat_stop", None),
                "store": store if store is not None else self.config.get("openai_chat_store", False),
                "stream": stream if stream is not None else self.config.get("openai_chat_stream", False),
                "sys_input": sys_input if sys_input is not None else self.config.get("openai_chat_sys_input", None),
                "temperature": temperature if temperature is not None else self.config.get("openai_chat_temperature", 0.7),
                "top_p": top_p if top_p is not None else self.config.get("openai_chat_top_p", 1.0),
                "use_history": use_history if use_history is not None else self.config.get("openai_chat_use_history", True),
                "add_completion_to_history": add_completion_to_history if add_completion_to_history is not None else self.config.get("openai_chat_add_completion_to_history", True),
            }
            if new:
                async for chat_completion in self.completions.create_https_completion(**request_data):
                    if len(chat_completion) > 2000:
                        unique_filename = f'temp_{uuid.uuid4()}.txt'
                        with open(unique_filename, 'w') as f:
                            f.write(chat_completion)
                        await self.handler.send_message(ctx, content=None, file=discord.File(unique_filename))
                        os.remove(unique_filename)
                    else:
                        await self.handler.handle_large_response(ctx, chat_completion)
            else:
                with open(PATH_OPENAI_REQUESTS, "a") as f:
                    f.write(json.dumps(request_data) + "\n")
                await self.handler.send_message(ctx, content="✅ Your request has been queued for weekend batch processing.")
        if ctx.interaction:
            await ctx.interaction.response.defer(ephemeral=True)
            await function()
        else:
            if ctx.channel and isinstance(ctx.channel, discord.abc.GuildChannel):
                permissions = ctx.channel.permissions_for(ctx.guild.me)
                if permissions.send_messages:
                    async with ctx.typing():
                        await function()
                else:
                    await function()
            else:
                async with ctx.typing():
                    await function()


async def setup(bot: commands.Bot):
    await bot.add_cog(Hybrid(bot))
