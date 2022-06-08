import os
import json
import random
import randomtimestamp as rt
import datetime as dt
import csv

with open(
			os.path.join(os.path.dirname(__file__), os.pardir, os.pardir, 'DATABASE-DUMP', 'user.json'), 
			'r',
			encoding="utf-8"
		) as file:
	users = json.load(file)

with open(
			os.path.join(os.path.dirname(__file__), os.pardir, os.pardir, os.pardir, 'DATABASE-DUMP', 'post.json'), 
			'r', 
			encoding="utf-8"
		) as file:
	posts = json.load(file)

i = 0

file = open(os.path.join(os.path.dirname(__file__), 'likes-post.csv'), 'w')
csv_writer = csv.writer(file)
csv_writer.writerow(['post', 'user', 'timestamp'])

for p in posts:
	likes = p['likes']
	temp_users = users.copy()
	random.shuffle(temp_users)

	for u in temp_users[:likes]:
		csv_writer.writerow([
			p['_id']['$oid'], \
			u['username'], \
			rt.randomtimestamp(\
				start = dt.datetime.strptime(p['date'], '%Y-%m-%d %H:%M:%S')) \
						.strftime('%Y-%m-%d %H:%M:%S')
		])
	
	if (i % 1000 == 0):
		print(i)
	i+=1
