import json
import datetime

####
def serialize_field (json_array, field):
	array = []
	for a in json_array:
		array.append(a[field])
	return array
####

# Read games
with open("games.json", "r", encoding="utf-8") as file:
	games = json.load(file)
## end with

# Read subreddit
with open("reddit.json", "r", encoding="utf-8") as file:
	reddit = json.load(file)
## end with


new_games = []
selected_games = []
selected_subreddits = []

for game in games:
	new_doc = {}
	matches = False

	# Extract name of Reddit websited
	for w in game['websites']:
		if w['category'] == 14:
			url_name = w['url'].split("/")[-1]
		## end if
	## end for

	# Check if matches
	for r in reddit:
		reddit_name = r['url'].split("/")[-1]
		if (reddit_name == url_name):
			# Filter out duplicates (different videogames with same subreddit)
			if (reddit_name in selected_subreddits):
				break

			matches = True
			id = r['id']
			selected_games.append(r['id'])
			selected_subreddits.append(reddit_name)
			break
		## end if
	## end for

	if matches == False:
		continue

	## Parse json
	new_doc['id'] = id
	new_doc['name'] = game['name']

	if 'summary' in game:
		new_doc['summary'] = game['summary']
	if 'game_modes' in game:
		new_doc['game_modes'] = serialize_field(game['game_modes'], 'name')
	if 'platforms' in game:
		new_doc['platforms'] = serialize_field(game['platforms'], 'name')
	if 'cover' in game:
		new_doc['cover'] = game['cover']['url']
	if 'genres' in game:
		new_doc['genres'] = serialize_field(game['genres'], 'name')
	if 'collection' in game:
		new_doc['collection'] = game['collection']['name']
	if 'aggregated_rating' in game:
		new_doc['aggregated_rating'] = game['aggregated_rating']
	if 'first_release_date' in game:
		new_doc['release_date'] = datetime.datetime.utcfromtimestamp(int(game['first_release_date'])).strftime('%Y-%m-%d %H:%M:%S')

	
	publishers = []
	developers = []
	for involved_company in game['involved_companies']:
		if involved_company['publisher']:
			publishers.append(involved_company['company']['name'])
		## end if

		if involved_company['developer']:
			developers.append(involved_company['company']['name'])
		## end if
	## end for

	new_doc['developers'] = developers
	new_doc['publishers'] = publishers

	## TODO age ratings
	new_games.append(new_doc)
## end for

with open("db-games.json", "w") as file:
	file.write(json.dumps(new_games, indent=2))
## end with

with open("selected_games.json", "w") as file:
	selected_games.sort()
	selected_games = list(dict.fromkeys(selected_games))
	file.write(json.dumps(selected_games, indent=4))
## end with
