import re
with open('D:\\Salem\\sort\\Clients\\source\\codebase\\jsalem\\build\\test_page.html', 'r', encoding='utf-8') as f:
    html = f.read()
# Look for any meaningful text
idx = html.find('salemthegame')
if idx > 0:
    print('=== Content near salemthegame mention ===')
    print(html[max(0,idx-200):idx+1000])
# Try to find "A Face in the Dross" in the content
if 'Face in the Dross' in html:
    idx2 = html.find('Face in the Dross')
    print('\n=== Around item name ===')
    print(html[max(0,idx2-500):idx2+500])
# Check if it's a redirect/interstitial
if 'class="wm-background"' in html or '__wm' in html[:1000]:
    print('\n=== Wayback interstitial detected ===')
# Look for any description-like text near <p> tags near the content
ps = re.findall(r'<p[^>]*>(.*?)</p>', html, re.DOTALL)
print('\n=== All <p> tags with long text ===')
for p in ps:
    clean = re.sub(r'<[^>]+>', '', p).strip()
    if len(clean) > 30:
        print(f'  [{len(clean)}] {clean[:200]}')
