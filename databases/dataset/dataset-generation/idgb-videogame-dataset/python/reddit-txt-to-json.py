import json

# Read subreddit
with open("reddit.txt", "r", encoding="utf-8") as file:
	reddit = file.readlines()

out = []
id = 0

for line in reddit:
	doc = {}
	doc['id'] = id
	doc['url'] = line[:-1]
	out.append(doc)
	id += 1
## end for

with open("reddit.json", "w", encoding="utf-8") as file:
	file.write(json.dumps(out, indent=2))
## end with