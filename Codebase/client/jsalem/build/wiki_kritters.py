import sqlite3, urllib.request, re, time

db_path = r"C:\Users\MikeM\AppData\Local\Haven Launcher\cache\https\game.salemthegame.com\java\items.db"
conn = sqlite3.connect(db_path)

# Add description column
try:
    conn.execute("ALTER TABLE Creatures ADD COLUMN Description TEXT")
except:
    pass

# Get creatures without descriptions
creatures = conn.execute("SELECT Name FROM Creatures WHERE Description IS NULL OR Description = ''").fetchall()

if not creatures:
    print("All creatures already have descriptions")
    exit(0)

for (name,) in creatures:
    wiki_name = name.replace(" ", "_")
    url = f"https://salemthegame.wiki/index.php?title={wiki_name}&action=edit"
    try:
        req = urllib.request.Request(url, headers={'User-Agent': 'jsalemBot/1.0'})
        resp = urllib.request.urlopen(req, timeout=10)
        html = resp.read().decode('utf-8', errors='replace')
        
        # Try to find first paragraph or description
        desc = ""
        m = re.search(r'<textarea[^>]*>(.*?)</textarea>', html, re.DOTALL)
        if m:
            text = m.group(1)
            text = re.sub(r'<[^>]+>', '', text)
            lines = [l.strip() for l in text.split('\n') if l.strip() and not l.startswith('|') and not l.startswith('{{') and not l.startswith('}}')]
            for line in lines:
                if len(line) > 20 and not line.startswith('#'):
                    desc = line[:200]
                    break
        
        if desc:
            conn.execute("UPDATE Creatures SET Description = ? WHERE Name = ?", (desc, name))
            print(f"{name}: {desc[:60]}...")
        else:
            print(f"{name}: no description found")
        
        time.sleep(0.5)
    except Exception as e:
        print(f"{name}: error - {e}")

conn.commit()
conn.close()
print("Done")
