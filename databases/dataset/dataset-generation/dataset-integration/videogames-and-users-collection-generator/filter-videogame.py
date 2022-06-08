# Filter videogame that have been parsed from Reddit
import os
import os.path
import json


# Extract list of ids
raw_posts_filename = os.listdir(os.path.join(os.path.dirname(__file__), os.pardir, 'post-scraper', 'posts'))
reddit_ids = []

for p in raw_posts_filename:
	reddit_ids.append(p[4 : -5]) # these are strings!
## end for

print(reddit_ids)

# Extract videogame db
with open(
		os.path.join(os.path.dirname(__file__), os.pardir, 'idgb-videogame-dataset', 'python', 'db-games.json'), 
		"r", 
		encoding="utf-8"
		) as file:

	original_videogame_json_list = json.load(file)
## end with

videogames = []

for v in original_videogame_json_list:
	if str(v['id']) in reddit_ids:
		videogames.append(v)
	# end if
# end for

print(len(videogames))

with open(os.path.join(os.path.dirname(__file__), 'db-games-with-reddit-data.json'), 'w') as file:
	file.write(json.dumps(videogames, indent=2))
# end with
