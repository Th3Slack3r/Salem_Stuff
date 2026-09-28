import sqlite3
conn = sqlite3.connect(r"C:\Users\MikeM\AppData\Local\Haven Launcher\cache\https\game.salemthegame.com\java\items.db")
cur = conn.execute("SELECT COUNT(*) FROM Skills WHERE Description = '?'")
count = cur.fetchone()[0]
conn.execute("UPDATE Skills SET Description = NULL WHERE Description = '?'")
conn.commit()
print(f"Reset {count} failed skills back to NULL (will retry)")
conn.close()
