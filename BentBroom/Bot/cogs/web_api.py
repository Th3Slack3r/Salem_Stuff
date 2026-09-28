import os
import discord
from discord.ext import commands
from flask import Flask, request, jsonify
import asyncio
import logging

# Set up logging for this file
logger = logging.getLogger('discord.web_api')

# --- Flask App Initialization ---
app = Flask(__name__)

# Read port from environment variable
# NOTE: We're adding a log to confirm the value read from the environment
FLASK_PORT_ENV = os.environ.get('SERVER_PORT')
FLASK_PORT = int(FLASK_PORT_ENV) if FLASK_PORT_ENV else 5000
API_HOST = '0.0.0.0'

print(f"--- DEBUG --- FLASK_PORT read from ENV: {FLASK_PORT_ENV}")
print(f"--- DEBUG --- FLASK_PORT actual value: {FLASK_PORT}")


class WebAPICog(commands.Cog):
    """Hosts a Flask web server to receive external API calls and send DMs."""
    
    def __init__(self, bot):
        self.bot = bot
        self.web_server_task = None

    async def run_flask_app(self):
        """Runs the Flask web server asynchronously."""
        print(f"--- DEBUG --- Attempting to bind Flask to {API_HOST}:{FLASK_PORT}")
        logger.info(f"Starting Flask web server on {API_HOST}:{FLASK_PORT}")
        try:
            # Running the blocking Flask server in a separate thread
            # We explicitly pass the host and port we determined
            await asyncio.to_thread(app.run, host=API_HOST, port=FLASK_PORT, debug=False)
        except Exception:
            logger.exception("Flask server FAILED TO START. Check environment variable and binding.")


    # --- Flask Handler (Executed by Flask) ---
    def handle_send_dm(self):
        # This part remains the same, assuming we can get the bot context
        data = request.get_json()
        user_id = data.get('user_id')
        message_content = data.get('message')
        
        # Access the bot object from the app config
        bot_instance = app.config.get("bot")
        
        if not user_id or not message_content or not bot_instance:
            return jsonify({"error": "Missing data or bot context"}), 400

        try:
            # Schedule the asynchronous Discord task onto the bot's event loop
            bot_instance.loop.create_task(
                # Use the existing bot instance to get the cog and method
                bot_instance.get_cog("WebAPICog").send_discord_message(int(user_id), message_content)
            )
            return jsonify({"status": "DM task scheduled successfully"}), 200
        except Exception:
            logger.exception("Error scheduling DM task.")
            return jsonify({"error": "Internal server error scheduling task"}), 500


    async def send_discord_message(self, user_id: int, content: str):
        # ... (same as before) ...
        await self.bot.wait_until_ready() 
        
        try:
            user = self.bot.get_user(user_id) or await self.bot.fetch_user(user_id)
            if user:
                await user.send(content)
                logger.info(f"Successfully sent DM to {user.name} ({user_id})")
            else:
                logger.warning(f"User with ID {user_id} not found.")
        except discord.Forbidden:
            logger.warning(f"Failed to send DM to {user_id}. User may have DMs disabled.")
        except Exception:
            logger.exception(f"Error during DM attempt to {user_id}")
            
    # --- Cog Lifecycle Hooks ---
    @commands.Cog.listener()
    async def on_ready(self):
        """Start the Flask server when the bot is ready."""
        print("--- DEBUG --- WebAPICog: on_ready event triggered.") # <-- CRITICAL DEBUG
        if self.web_server_task is None:
            self.web_server_task = self.bot.loop.create_task(self.run_flask_app())
            
    async def cog_unload(self):
        # ... (same as before) ...
        if self.web_server_task:
            self.web_server_task.cancel()
            logger.info("Flask web server task cancelled.")

async def setup(bot):
    """Adds the WebAPICog to the bot."""
    print("--- DEBUG --- Running WebAPICog setup.")
    # Store the bot instance for global Flask access
    app.config["bot"] = bot
    # Add the cog
    await bot.add_cog(WebAPICog(bot))

# --- Global Flask Route ---
@app.route('/api/send_dm', methods=['POST'])
def api_send_dm():
    """Wrapper function to call the Cog's handler."""
    # Get the cog instance from the bot object stored in app.config
    bot_instance = app.config.get("bot")
    if bot_instance:
        # Get the instance of the WebAPICog
        cog = bot_instance.get_cog("WebAPICog") 
        if cog:
            return cog.handle_send_dm()
    
    return jsonify({"error": "Bot context not available"}), 500