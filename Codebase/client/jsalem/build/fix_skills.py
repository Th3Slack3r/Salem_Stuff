import sqlite3

DB = r"C:\Users\MikeM\AppData\Local\Haven Launcher\cache\https\game.salemthegame.com\java\items.db"

# Correct individual skill names from the wiki
skills = [
    "A Formal Education", "Acute Tree Harvesting", "Advanced Cooking", "Advanced Husbandry",
    "Aeolian Agriculture", "Alchemy", "Amateur Sculpting", "Ambitious Excavation", "Ample Storage",
    "Arcane Wisdom", "Aristotelian Logic", "Arson", "Artillery", "Ascent to the Summit",
    "Backalley Pugilism", "Baking", "Bark Gathering", "Beaver Skinning", "Beekeeping",
    "Bellows Operation", "Big Game Hunting", "Bioluminescent Applications", "Blacksmithing",
    "Blowdarts", "Boneworks", "Boobytraps", "Botany", "Brazier Forging", "Bug Hunting",
    "Bullying", "Burial Rights", "Butcher's Thrift", "Butchery", "Cabbage Growing",
    "Cabinet Maker", "Cannibalism", "Carpentry", "Catapult Construction", "Catapults",
    "Cattle Ranching", "Cauldronmaking", "Cementation", "Ceramics & Crucibles", "Cheese Making",
    "Childish Things", "Clubbing", "Coaling", "Cobbling", "Coffer Making", "Collector",
    "Colonial Tradesmanship", "Colony Capitol", "Compacts & Constitutions", "Cotton Planting",
    "Dairymaid", "Debasement", "Dressage", "Elaborate Gemcutting", "Elementary Gemcutting",
    "Embroidery & Silk", "Equestrian Mastery", "Essential Mineralogy", "Expeditions",
    "Expeditious Journeyman", "Experienced Traveler", "Exploration", "Eye for an Eye",
    "Fast Moves", "Fencing", "Field Dressing", "Fine Leathercraft", "Firearms", "Fishing",
    "Flags & Banners", "Floriculture", "Flowers & Berries", "Folk Medicine", "Foraging",
    "Foreign Theology", "Forestry", "French Cuisine", "French Tickler", "Fried Foods",
    "Friendly Wagers", "Frigid Journeys", "Fruit Orchards", "Fungiculture", "Game Meats",
    "Gardening", "Glass Blowing", "Goat Rearing", "Granite Excavation", "Grave Robbing",
    "Green Thumb", "Haberdashery", "Handheld Explosives", "Hideworking", "Hiking",
    "Hill Climbing", "His Majesty's Stall", "Hoarder", "Horse Riding", "Horticulture",
    "Humble Abodes", "I am the Darkness", "Igloo Construction", "Illicit Pharmacognosy",
    "Indian Tracking", "Inherent Rights", "Intermediate Cooking", "Iron Amalgamation",
    "Joinery & Finish", "Kiln Construction", "Labouring", "Lace & Fancywork", "Larceny",
    "Lepidopterology", "Literacy", "Locksmithing", "Long Distance Swimming",
    "Long Range Bombardment", "Lore of The Lumberwoods", "Loyalty Appreciation", "Lucky",
    "Maize Planting", "Mannequins", "Masonry", "Mechanics", "Mercantilism", "Metallurgy",
    "Metalsmithing", "Mineral Sifting", "Mining", "Monster Hunting", "Mortar Masonry",
    "Mountaineering", "Mushroom Hunting", "Musician", "Nut Orchards", "Nuts & Seeds",
    "Packrat", "Pajama Knitting", "Paper Making", "Patchwork & Rags", "Pharmacology Formal",
    "Pig Keeping", "Plague Handling", "Plantation Management", "Pockets", "Polearms",
    "Potato Growing", "Potions & Poultices", "Pottery", "Prospecting", "Psychotic Episodes",
    "Pulleys & Levers", "Pumpkin Planting", "Quarrying", "Really Lucky", "Revenge",
    "Ridiculously Lucky", "Rudimentary Triage", "Rustic Furniture", "Salem Store",
    "Sanctioned Mortification", "Scribing", "Seafood Chef", "Seamanship", "Self-Defense",
    "Settling", "Sewing", "Sheep Herding", "Shellfish Trapping", "Shrub Orchards",
    "Sight Seer", "Silversmithing", "Simple Cooking", "Simple Fences", "Slug Hunting",
    "Small Game Hunting", "Smoked Meats", "Sophisticated Furniture", "Stalls",
    "Steam Distillation", "Survival Skills", "Swimming", "Tanning", "Tasteful Gemcutting",
    "Tasty Pastries", "The Rights of Englishmen", "The Story of Cain & Abel", "Theology",
    "Three-field System", "Tobacco Planting", "Torches", "Towercraft", "Trespassing",
    "Tulip Mania", "Turkey Farming", "Vegetable Potting", "Venison Cuisine", "Veterinarian",
    "Vineyards", "Vintner", "Viscera & Bits", "Waterways", "Weapon Forging", "Weaving",
    "Whittling", "Windmill Theory", "Witchcraft", "Wrangling", "Yellow Belly"
]

conn = sqlite3.connect(DB)
conn.execute("DROP TABLE IF EXISTS Skills")
conn.execute("CREATE TABLE Skills (Name TEXT PRIMARY KEY, Description TEXT)")
count = 0
for s in skills:
    conn.execute("INSERT OR IGNORE INTO Skills (Name) VALUES (?)", (s,))
    count += 1
conn.commit()
c = conn.execute("SELECT COUNT(*) FROM Skills")
total = c.fetchone()[0]
print(f"Skills table rebuilt: {total} entries")
conn.close()
