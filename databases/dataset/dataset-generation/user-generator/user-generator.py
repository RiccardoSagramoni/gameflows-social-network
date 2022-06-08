# This script is useless. Use the link in user-generator.txt


import requests
import json
import math

N = 3
MAX_USERS_PER_REQUEST = min(N, 5000)
NUMBER_OF_REQUESTS = math.ceil(float(N) / 5000.0)

# api-endpoint
URL = "https://randomuser.me/api/"
PARAMS = \
	{\
		'results': MAX_USERS_PER_REQUEST,\
		'inc': 'gender, name, email, login, dob, cell, picture'\
	}

# Open json file
file = open('users.json', 'w')
file.write("[\n")

# sending get request and saving the response as response object
for i in range (0, (N / 5000) + 1):
	r = requests.get(url = URL, params=PARAMS)

	# dump data in json format
	file.write(json.dumps(r.json(), indent=2))

	if i != (N / 5000):
		file.write(",\n")
	## end if
## end for

file.write("]")
# Close json file
file.close()
