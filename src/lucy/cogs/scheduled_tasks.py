from collections import defaultdict
from discord.ext import commands, tasks
from lucy.utils.handlers.ai_manager import BatchProcessor
from lucy.utils.handlers.sql_manager import perform_backup, setup_backup_directory
from lucy.utils.inc.helpers import *

import asyncio
import datetime
import discord
import os
import pytz
import traceback

class Ruderalis(commands.Cog):

    def __init__(self, bot):
        self.backup_database.start()
        self.bot = bot
        self.config = bot.config
        self.batch_processor = BatchProcessor(self.bot)
        self.batch_task.start()

    @commands.after_invoke
    async def after_invoke(self, ctx):
        if hasattr(bot, 'db_pool'):
            await bot.db_pool.close()

    @tasks.loop(hours=168)  # Runs once a week
    async def batch_task(self):
        now = datetime.datetime.utcnow()
        if now.weekday() in [5, 6]:  # Saturday or Sunday
            print("Running batch processing...")
            result_message = await self.batch_processor.process_batches()
            print(result_message)

    @tasks.loop(hours=24)
    async def backup_database(self):
        try:
            backup_dir = setup_backup_directory('./backups')
            backup_file = perform_backup(
                db_user='postgres',
                db_name='lucy',
                db_host='localhost',
                backup_dir=backup_dir
            )
            logger.info(f'Backup completed successfully: {backup_file}')
        except Exception as e:
            logger.error(f'Error during database backup: {e}')

    @backup_database.before_loop
    async def before_backup(self):
        await self.bot.wait_until_ready()

async def setup(bot: commands.Bot):
    await bot.add_cog(Ruderalis(bot))

