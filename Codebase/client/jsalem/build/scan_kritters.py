import zipfile, re, sqlite3, os

jar_path = r"C:\Users\MikeM\AppData\Local\Haven Launcher\cache\https\game.salemthegame.com\java\salem-res.jar"
db_path = r"C:\Users\MikeM\AppData\Local\Haven Launcher\cache\https\game.salemthegame.com\java\items.db"

if not os.path.exists(jar_path):
    print(f"salem-res.jar not found at {jar_path}")
    exit(1)

# Scan for kritter directories
kritters = set()
with zipfile.ZipFile(jar_path) as z:
    for name in z.namelist():
        m = re.match(r'(?:res/)?gfx/kritter/([^/]+)/', name)
        if m:
            kritters.add(m.group(1))

print(f"Found {len(kritters)} kritters")

# Add to database
conn = sqlite3.connect(db_path)
conn.execute("CREATE TABLE IF NOT EXISTS Creatures (Name TEXT PRIMARY KEY)")
count = 0
for k in sorted(kritters):
    try:
        conn.execute("INSERT OR IGNORE INTO Creatures (Name) VALUES (?)", (k,))
        count += 1
    except Exception as e:
        print(f"Error inserting {k}: {e}")

conn.commit()
c = conn.execute("SELECT COUNT(*) FROM Creatures")
total = c.fetchone()[0]
print(f"Creatures table: {total} entries ({count} new)")
conn.close()
