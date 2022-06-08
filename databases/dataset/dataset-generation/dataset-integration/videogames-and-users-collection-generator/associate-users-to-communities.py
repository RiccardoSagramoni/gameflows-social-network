# Generate the list of followers for each videogame community
import os
import os.path
import json
import random

videogames = []
users = []

# Read list of videogames
with open(os.path.join(os.path.dirname(__file__), 'db-games-with-reddit-data.json'), 'r') as file:
	videogames = list(json.load(file))
## end with

## Read list of users
with open(os.path.join(os.path.dirname(__file__), 'db-users.json'), 'r') as file:
	users  = list(json.load(file))
## end with

print(len(videogames))

for v in videogames:
	# Extract the number of users per community (between 50 and 700)
	number_of_users = round(random.gauss(400, 150))
	number_of_users = max(50, min(number_of_users, 700))
	#print(number_of_users)

	# Make a copy of the users and shuffle them
	temp_users = users.copy()
	random.shuffle(temp_users)

	v['followers'] = []

	# Read the users data
	for u in temp_users[0:number_of_users-1]:
		embedded_user_info = {}
		embedded_user_info['username'] = u['username']
		embedded_user_info['date-of-birth'] = u['date-of-birth']

		v['followers'].append(embedded_user_info)
	## end for
## end for

with open(os.path.join(os.path.dirname(__file__), 'FINAL-videogames-with-users.json'), 'w') as file:
	file.write(json.dumps(videogames, indent=2))
## end with
