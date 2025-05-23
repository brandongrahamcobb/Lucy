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
from lucy.utils.handlers.ai_manager import Completions, BatchProcessor, OpenAIUsageClient

from lucy.utils.handlers.image_manager import add_watermark, create_image, create_image_variation, edit_image
from lucy.utils.handlers.message_manager import Message
from lucy.utils.handlers.predicator import Predicator
from lucy.utils.inc.helpers import *
from random import choice
from typing import Dict, List, Optional

import asyncio
import datetime
import discord
import io
import json
import openai
import os
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

    @commands.hybrid_command(name='colorize', description=f'Usage: between `colorize 0 0 0` and `colorize 255 255 255` or `colorize <color>`')
    async def colorize(self, ctx: commands.Context, *, color: str = commands.parameter(default='blurple', description='Anything between 0 and 255 or a color.')):
        if ctx.interaction:
            async with ctx.typing():
                await ctx.interaction.response.defer(ephemeral=True)
        if not self.predicator.is_release_mode_func(ctx):
            return
        args = shlex.split(color)
        r = args[0]
        if not r.isnumeric():
            input_text_dict = {
                'type': 'text',
                'text': r
            }
            array = [
                {
                    'role': 'user',
                    'content': json.dumps(input_text_dict)
                }
            ]
            async for flagged, reasons in self.handler.completion_prep(array):
                if not flagged:
                    async for completion in self.completions.create_completion(array):
                        color_values = json.loads(completion)
                        r = color_values['r']
                        g = color_values['g']
                        b = color_values['b']
        else:
            g = args[1]
            b = args[2]
        r = int(r)
        g = int(g)
        b = int(b)
        guildroles = await ctx.guild.fetch_roles()
        position = len(guildroles) - 12
        for arg in ctx.author.roles:
            if arg.name.isnumeric():
                await ctx.author.remove_roles(arg)
        for arg in guildroles:
            if arg.name.lower() == f'{r}{g}{b}':
                await ctx.author.add_roles(arg)
                await arg.edit(position=position)
                await self.handler.send_message(ctx, content=f'I successfully changed your role color to {r}, {g}, {b}')
                return
        newrole = await ctx.guild.create_role(name=f'{r}{g}{b}', color=discord.Color.from_rgb(r, g, b), reason='new color')
        await newrole.edit(position=position)
        await ctx.author.add_roles(newrole)
        await self.handler.send_message(ctx, content=f'I successfully changed your role color to {r}, {g}, {b}')

    @commands.hybrid_command(name="imagine")
    async def imagine(self, ctx, *, prompt: str):
        if not self.predicator.is_release_mode_func(ctx):
            return
        async def function():
            try:
                if ctx.message.attachments:
                    image_attachment = ctx.message.attachments[0]
                    image_bytes = await image_attachment.read()
                    image_file = discord.File(io.BytesIO(image_bytes), filename="uploaded_image.png")
                    message = await self.handler.send_message(ctx, content="Choose what to do with the image:", file=image_file)
                    await message.add_reaction("✅")
                    await message.add_reaction("❌")
                    await message.add_reaction("🖼️")
                    await message.add_reaction("🔲")
                    def check(reaction, user):
                        return user == ctx.author and str(reaction.emoji) in ["✅", "❌", "🖼️", "🔲"]
                    reaction, user = await self.bot.wait_for("reaction_add", check=check)
                    if str(reaction.emoji) == "✅":
                        await self.handler.send_message(ctx, content="Please upload a mask for editing, or confirm to use the full image as the mask.")
                        mask_msg = await self.bot.wait_for("message", check=lambda m: m.author == ctx.author)
                        if mask_msg.content.lower() == "confirm":
                            mask_file = image_attachment
                        else:
                            mask_file = mask_msg.attachments[0]
                        edited_image = await edit_image(image_file, mask_file, prompt)
                        if isinstance(edited_image, discord.File):
                            await self.handler.send_message(ctx, content="Here is your edited image with the mask:", file=edited_image)
                        else:
                            await self.handler.send_message(ctx, content=f"Error editing image: {edited_image}")
                    elif str(reaction.emoji) == "❌":
                        await self.handler.send_message(ctx, content="Edit canceled.")
                    elif str(reaction.emoji) == "🖼️":
                        variation = await create_image_variation(image_file, prompt)
                        if isinstance(variation, discord.File):
                            await self.handler.send_message(ctx, content="Here is your image variation:", file=variation)
                        else:
                            await self.handler.send_message(ctx, content=f"Error creating variation: {variation}")
                    elif str(reaction.emoji) == "🔲":
                        await self.handler.send_message(ctx, content="Please upload a mask image to use for editing.")
                        mask_msg = await self.bot.wait_for("message", check=lambda m: m.author == ctx.author)
                        mask_file = mask_msg.attachments[0]
                        edited_image = await edit_image(image_file, mask_file, prompt)
                        if isinstance(edited_image, discord.File):
                            await self.handler.send_message(ctx, content="Here is your edited image with the mask:", file=edited_image)
                        else:
                            await self.handler.send_message(ctx, content=f"Error editing image with mask: {edited_image}")
                else:
                    image_file = await create_image(prompt)
                    if isinstance(image_file, discord.File):
                        await self.handler.send_message(ctx, content="Here is your generated image:", file=image_file)
                    else:
                        await self.handler.send_message(ctx, content=f"Error generating image: {image_file}")
            except openai.OpenAIError as e:
                await self.handler.send_message(ctx, e.http_status)
                await self.handler.send_message(ctx, e.error)
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
