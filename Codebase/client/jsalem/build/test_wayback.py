import urllib.request, re

url = "https://web.archive.org/web/20260201121123/https://salemthegame.wiki/page/Baking"
req = urllib.request.Request(url, headers={"User-Agent": "jsalemBot/1.0"})
resp = urllib.request.urlopen(req, timeout=30)
html = resp.read().decode("utf-8", errors="replace")

m = re.search(r'center">"([^"]+)"', html)
if m:
    print("DESC:", m.group(1)[:100])

rm = re.search(r"Skill\(s\) required</td>\s*<td[^>]*>\s*([^<]+)", html, re.IGNORECASE)
if rm:
    print("REQ:", rm.group(1).strip())
print("OK")
