''' indica.py The purpose of the program is to be an extension for a Discord bot for listeners.
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
from discord.ext import commands
from lucy.utils.handlers.ai_manager import Completions, Moderator
from lucy.utils.handlers.message_manager import Message
from lucy.utils.handlers.predicator import Predicator
from lucy.utils.inc.helpers import *
from lucy.utils.inc.setup_logging import logger

import discord
import json
import os
import shutil
import time
import traceback
import uuid

class Indica(commands.Cog):

    def __init__(self, bot):
        self.bot = bot
        self.config = bot.config
        self.completions = Completions()
        self.db_pool = bot.db_pool
        self.handler = Message(self.bot, self.config, self.completions, self.db_pool)
        self.moderator = Moderator()
        self.predicator = Predicator(self.bot)
        self.user_messages = {}

    @commands.after_invoke
    async def after_invoke(ctx):
        if hasattr(bot, 'db_pool'):
            await bot.db_pool.close()

    @commands.Cog.listener()
    async def on_message_edit(self, before, after):
        if before.content != after.content:
            ctx = await self.bot.get_context(after)
            if ctx.command:
                await self.bot.invoke(ctx)

    async def moderate_name(self, member: discord.Member, faction_name) -> bool:
        if not self.config.get('openai_chat_moderation', False) or self.predicator.is_developer(member):
            return False
        try:
            async for moderation_completion in self.moderator.create_moderation(input_array=[faction_name]):
                try:
                    full_response = json.loads(moderation_completion)
                    results = full_response.get('results', [])
                    if results and results[0].get('flagged', False):
                        await self.moderator.handle_moderation(ctx.message)
                        return True
                except Exception as e:
                    logger.error(traceback.format_exc())
                    print(f'An error occurred: {e}')
        except Exception as e:
            logger.error(traceback.format_exc())
            print(f'An error occurred: {e}')
        return False

    @commands.Cog.listener()
    async def on_member_update(self, before: discord.Member, after: discord.Member):
        if before.nick == after.nick:
            return
        try:
            flagged = await moderate_name(after.nick or after.name)
            if flagged:
                try:
                    await after.edit(nick=None, reason="Nickname reverted due to moderation violation.")
                except discord.Forbidden:
                    logger.error(f"[moderation] Missing permissions to revert nickname for {after}.")
                except discord.HTTPException as e:
                    logger.error(f"[moderation] HTTPException while reverting nickname for {after}: {e}")
                return
            user_data = await self.game.get_user(after.id)
            if not user_data:
                return
            faction_name = user_data["faction_name"]
            if not faction_name:
                return
            expected_nick = f"[{faction_name}] {after.name}"
            if after.nick != expected_nick:
                try:
                    await after.edit(nick=expected_nick, reason="Enforcing faction nickname format.")
                except discord.Forbidden:
                    logger.error(f"[faction] Cannot change nickname for {after} due to missing permissions.")
                except discord.HTTPException as e:
                    logger.error(f"[faction] HTTPException while changing nickname for {after}: {e}")
        except Exception as e:
            logger.error(traceback.format_exc())
        finally:
            try:
                shutil.rmtree(DIR_TEMP)
                os.makedirs(DIR_TEMP, exist_ok=True)
            except Exception as cleanup_error:
                logger.error(f"Error cleaning up temporary files: {cleanup_error}")

    @commands.Cog.listener()
    async def on_message(self, message):
        try:
            if message.author.bot or message.is_system():
                return
            ctx = await self.bot.get_context(message)
            author = ctx.author.name
            current_time = time.time()
            ctx = await self.bot.get_context(message)
            author = ctx.author.name
            user_id = ctx.author.id
            await self.handler.ai_handler(ctx)
        except Exception as e:
            logger.error(traceback.format_exc())
        finally:
            try:
                shutil.rmtree(DIR_TEMP)
                os.makedirs(DIR_TEMP, exist_ok=True)
            except Exception as cleanup_error:
                logger.error(f"Error cleaning up temporary files: {cleanup_error}")

    @commands.Cog.listener()
    async def on_ready(self):
        bot_user = self.bot.user
        bot_name = bot_user.name
        bot_id = bot_user.id
        guild_count = len(self.bot.guilds)
        info = (
            f'\n=============================\n'
            f'bot Name: {bot_name}\n'
            f'bot ID: {bot_id}\n'
            f'Connected Guilds: {guild_count}\n'
            f'============================='
        )
        guild_info = '\n'.join(
            [f'- {guild.name} (ID: {guild.id})' for guild in self.bot.guilds]
        )
        stats_message = f'{info}\n\nGuilds:\n{guild_info}'
        print(stats_message)

async def setup(bot: commands.Bot):
    await bot.add_cog(Indica(bot))
