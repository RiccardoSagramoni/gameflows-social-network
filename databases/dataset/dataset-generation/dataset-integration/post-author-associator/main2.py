#NOT USED (TOO SLOW)

import json, os
import datetime as dt
import random
import re
from Post import *
from Comments import *


#function to associate random authors to comments
def random_author(users):
    #author = {} #if we wants more fields for author
    author = random.choice(users)
    return author['username']



##----MAIN----##

# Read list of users
with open(os.path.join('users','db-users.json'), 'r') as file:
	users  = list(json.load(file))

# Read each .json document
for filename in os.listdir('posts'):
    if filename.endswith('.json'):
        # open current json
        with open(os.path.join('posts',filename),"r+") as json_file:

            # reading the json
            document = json.load(json_file)

            # reading posts
            for post in document:
                post_author = random_author(users)
                post["author"] = post_author
                for comment in post["comments"]:
                    comment_author = random_author(users)
                    comment["author"] = comment_author

                with open(os.path.join('new_posts',filename),"w") as json_file_out:
                    json.dump(document, json_file_out, indent=2)


#NOTE ESECUZIONE
#post10.json: 1:30 minuti per esecuzione


#METHOD 2 (INSIDE EACH FOR WITHOUT THE FINAL DUMP) - metodo più lento
'''
post["author"] = post_author
#post["author"].append(post_author)
json_file.seek(0)
json.dump(document,json_file, indent=2)


comment["author"] = comment_author
#comment["author"].append(comment_author)
json_file.seek(0)
json.dump(document,json_file, indent=2)
'''

