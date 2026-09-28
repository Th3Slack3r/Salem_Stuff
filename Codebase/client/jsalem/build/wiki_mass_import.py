"""
Mass wiki scraper for jsalem.
Scrapes every skill and item from the Wayback Machine archive,
extracts descriptions, and caches them in items.db.
Waits 2 minutes between requests to avoid being blocked.
"""

import urllib.request, urllib.error
import sqlite3, re, time, sys, os
from datetime import datetime

DB_PATH = r"C:\Users\MikeM\AppData\Local\Haven Launcher\cache\https\game.salemthegame.com\java\items.db"
WAYBACK_BASE = "https://web.archive.org/web/20260201121123/"
USER_AGENT = "jsalemBot/1.0"
DELAY_SECS = 120  # 2 minutes between requests

# Stats to track
stats = {"fetched": 0, "failed": 0, "cached": 0, "skipped": 0}

def fetch_page(url):
    """Fetch a URL with retries."""
    for attempt in range(3):
        try:
            req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
            resp = urllib.request.urlopen(req, timeout=30)
            return resp.read().decode("utf-8", errors="replace")
        except Exception as e:
            if attempt < 2:
                time.sleep(5)
            else:
                return None

def extract_skill_description(html):
    """Extract skill description from wiki page HTML."""
    if not html: return None
    # Description in <td ...>"..." pattern
    m = re.search(r'center">"([^"]+)"', html)
    desc = m.group(1).strip() if m else None
    # Requirements
    rm = re.search(r"Skill\(s\) required</td>\s*<td[^>]*>\s*([^<]+)", html, re.IGNORECASE)
    req = rm.group(1).strip() if rm else None
    result = ""
    if desc: result += desc
    if req: result += ". Requires: " + req
    return result if result else None

def extract_item_description(html, item_name):
    """Try to find item description from wiki page."""
    if not html: return None
    # Look for description paragraph
    m = re.search(r'<p>([^<]{30,}?)</p>', html)
    if m:
        text = m.group(1).strip()
        if len(text) > 30 and not text.startswith("<"):
            return text[:200]
    # Fallback: look for infobox data
    m = re.search(r'center">"([^"]{20,})"', html)
    if m: return m.group(1).strip()[:200]
    return None

def process_skills(conn):
    """Fetch descriptions for all skills without one."""
    cur = conn.execute("SELECT Name FROM Skills WHERE Description IS NULL OR Description = '' OR Description = '?'")
    skills = cur.fetchall()
    total = len(skills)
    print(f"\n=== SKILLS: {total} to fetch ===")
    
    for idx, (name,) in enumerate(skills, 1):
        wiki_name = name.replace(" ", "_")
        url = WAYBACK_BASE + "https://salemthegame.wiki/page/" + urllib.request.quote(wiki_name)
        
        print(f"[{idx}/{total}] Fetching skill: {name}...", end=" ", flush=True)
        html = fetch_page(url)
        
        if html:
            desc = extract_skill_description(html)
            if desc:
                conn.execute("UPDATE Skills SET Description = ? WHERE Name = ?", (desc, name))
                conn.commit()
                stats["cached"] += 1
                print(f"CACHED: {desc[:80]}...")
            else:
                # Page exists but no description found — mark so we don't retry
                conn.execute("UPDATE Skills SET Description = '-' WHERE Name = ?", (name,))
                conn.commit()
                stats["skipped"] += 1
                print("no description on page")
        else:
            # Network failure — leave as NULL so it retries on next run
            stats["failed"] += 1
            print("FAILED (fetch error)")
        
        if idx < total:
            next_ts = datetime.now().timestamp() + DELAY_SECS
            print(f"  Waiting {DELAY_SECS}s until {datetime.fromtimestamp(next_ts).strftime('%H:%M:%S')}...")
            time.sleep(DELAY_SECS)

def process_items(conn, table, name_col, desc_col, label):
    """Fetch descriptions for items in a table."""
    cur = conn.execute(f"SELECT {name_col} FROM {table} WHERE {desc_col} IS NULL OR {desc_col} = ''")
    items = cur.fetchall()
    total = len(items)
    print(f"\n=== {label}: {total} to fetch ===")
    
    for idx, (name,) in enumerate(items, 1):
        wiki_name = name.replace(" ", "_")
        url = WAYBACK_BASE + "https://salemthegame.wiki/page/" + urllib.request.quote(wiki_name)
        
        print(f"[{idx}/{total}] Fetching {label.lower()}: {name}...", end=" ", flush=True)
        html = fetch_page(url)
        
        if html:
            desc = extract_item_description(html, name)
            if desc:
                conn.execute(f"UPDATE {table} SET {desc_col} = ? WHERE {name_col} = ?", (desc, name))
                conn.commit()
                stats["cached"] += 1
                print(f"CACHED: {desc[:80]}...")
            else:
                conn.execute(f"UPDATE {table} SET {desc_col} = '-' WHERE {name_col} = ?", (name,))
                conn.commit()
                stats["skipped"] += 1
                print("no description on page")
        else:
            # Network failure — leave as NULL so it retries
            stats["failed"] += 1
            print("FAILED (fetch error)")
        
        if idx < total:
            next_ts = datetime.now().timestamp() + DELAY_SECS
            mins = DELAY_SECS // 60
            print(f"  Waiting {mins}min until {datetime.fromtimestamp(next_ts).strftime('%H:%M:%S')}...")
            time.sleep(DELAY_SECS)

def main():
    start = datetime.now()
    print(f"=== WIKI MASS IMPORT STARTED at {start.strftime('%Y-%m-%d %H:%M:%S')} ===")
    print(f"Delay between requests: {DELAY_SECS}s ({DELAY_SECS//60}min)")
    
    # Add description columns if missing
    conn = sqlite3.connect(DB_PATH)
    for table, col in [("Skills", "Description"), ("Artifacts", "Description"), ("Clothes", "Description"), ("Inspirationals", "Description"), ("Creatures", "Description")]:
        try:
            conn.execute(f"ALTER TABLE {table} ADD COLUMN {col} TEXT")
        except:
            pass  # Column already exists
    
    # Process in order: skills first (most useful), then items
    process_skills(conn)
    process_items(conn, "Artifacts", "Item", "Description", "Artifacts")
    process_items(conn, "Clothes", "Item", "Description", "Clothes")
    process_items(conn, "Inspirationals", "Item", "Description", "Inspirationals")
    process_items(conn, "Creatures", "Name", "Description", "Creatures")
    
    conn.close()
    elapsed = datetime.now() - start
    hours = elapsed.total_seconds() / 3600
    print(f"\n=== COMPLETE ===")
    print(f"Elapsed: {elapsed.total_seconds()/60:.1f} minutes ({hours:.1f} hours)")
    print(f"Fetched: {stats['fetched']}, Cached: {stats['cached']}, Failed: {stats['failed']}, Skipped: {stats['skipped']}")

if __name__ == "__main__":
    main()
