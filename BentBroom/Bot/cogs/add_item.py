import discord
from discord.ext import commands
import pysql
from pysql import Error # Assuming pysql exposes an Error class

# --- Database Connection Details ---
# Use your new database credentials here
DB_CONFIG = {
    'host': '2.56.246.128',
    'port': 3306,
    'user': 'u14751_gGrr4LahO7',
    'password': 'yl6q7+GcCY6+PWILdf^W0vsF',
    'database': 's14751_BentBroom'
}

# --- Target Configuration ---
TARGET_CHANNEL_ID = 1441515555994472602
DB_TABLE = 'CureentItems'
DB_ITEM_COLUMN = 'Item' # The column name where the item names are stored

class ItemManagement(commands.Cog):
    """A Cog for managing items in the 'CureentItems' table using pysql."""
    
    def __init__(self, bot):
        self.bot = bot

    def get_db_connection(self):
        """Attempts to establish and return a database connection using pysql."""
        try:
            # pysql.connect should handle host and port as separate arguments, 
            # or combined in the host string depending on the implementation.
            # Assuming it takes kwargs like a standard library.
            return pysql.connect(**DB_CONFIG)
        except Error as err:
            print(f"Database connection error (pysql): {err}")
            return None
        except Exception as e:
            print(f"An unexpected error occurred during DB connection: {e}")
            return None

    @commands.command(name='additem')
    async def add_item_to_list(self, ctx, *, item_name: str):
        """
        Adds an item to the CureentItems table if it doesn't already exist, 
        but only when run in the specified channel.
        """
        
        # 1. Channel Check
        if ctx.channel.id != TARGET_CHANNEL_ID:
            # Silently ignore or send a private warning, as desired.
            # We'll just ignore it to avoid spam in wrong channels.
            return

        # Sanitize and prepare item name
        safe_item_name = item_name.strip()
        
        # Guard against empty item names
        if not safe_item_name:
            await ctx.send("Please provide an item name to add (e.g., `!additem Bat Wing`).")
            return

        conn = self.get_db_connection()
        if conn is None:
            await ctx.send("❌ Error: Could not connect to the database. Please contact an admin.")
            return

        try:
            cursor = conn.cursor()
            
            # 2. Check if the item already exists
            # Parameterized query to prevent SQL Injection
            select_query = f"SELECT {DB_ITEM_COLUMN} FROM {DB_TABLE} WHERE {DB_ITEM_COLUMN} = %s"
            cursor.execute(select_query, (safe_item_name,))
            
            result = cursor.fetchone()
            
            if result:
                # Item already exists
                await ctx.send(f"⚠️ **{safe_item_name}** is already in the list.")
            else:
                # 3. Add the item as a new row
                insert_query = f"INSERT INTO {DB_TABLE} ({DB_ITEM_COLUMN}) VALUES (%s)"
                cursor.execute(insert_query, (safe_item_name,))
                conn.commit() # Commit the transaction
                
                # 4. Success message
                await ctx.send(f"✅ **{safe_item_name}** Added to list.")

        except Error as err:
            print(f"pysql Database Error: {err}")
            await ctx.send("❌ An error occurred while adding the item. Please check the bot console.")
            
        except Exception as e:
            print(f"An unexpected Python error occurred: {e}")
            await ctx.send("❌ An unexpected error occurred. Please try again.")
            
        finally:
            # Always close the connection
            if conn:
                try:
                    conn.close()
                except Exception as e:
                    print(f"Error closing pysql connection: {e}")

# --- Cog Setup Function ---
async def setup(bot):
    """Function required to load the Cog into the bot."""
    await bot.add_cog(ItemManagement(bot))