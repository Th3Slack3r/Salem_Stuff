import urllib.request, re

r = urllib.request.urlopen("https://web.archive.org/web/20260119061830/https://salemthegame.wiki/page/Baking", timeout=15)
t = r.read().decode("utf-8", errors="replace")

# Extract description
m = re.search(r'>([A-Z][^"]{20,}?)"', t, re.DOTALL)
if m:
    found = m.group(1).strip()
    if len(found) > 20 and "<" not in found and ">" not in found:
        print("DESC:", found[:150])
    else:
        print("DESC found but invalid:", repr(found[:100]))
else:
    print("DESC: no match")

# Extract requirements
rm = re.search(r"Skill\(s\) required</td>\s*<td[^>]*>\s*([^<]+)", t, re.IGNORECASE)
if rm:
    req = rm.group(1).strip()
    print("REQ:", req)
else:
    print("REQ: no match")
