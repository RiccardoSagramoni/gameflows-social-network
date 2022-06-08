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

#function to extract only top level comments
def extract_comments(post):
    topComments = []

    for comment in post['comments']:
        if not comment:
            return topComments
        comment = Comments(id=comment['id'],
                           text=comment['text'],
                           likes=comment['score'],
                           date=comment['date'],
                           author=random_author(users),
                           subcomments=comment['subcomments'])
        topComments.append(comment)
    return topComments





##----MAIN----##

# Read list of users
with open(os.path.join('users','db-users.json'), 'r') as file:
	users  = list(json.load(file))

# Read each .json document
for filename in os.listdir('posts'):
    if filename.endswith('.json'):
        # open current json
        with open(os.path.join('posts',filename),"r+") as json_file:

            # extract community_id (from json name)
            research = re.search('post(.+?)json',filename)
            community_id = (research.group(1))[:-1]

            #posts array (to write on new json)
            posts = []

            # reading the json
            document = json.load(json_file)
            # reading posts
            for post in document:
                currentPost = Post(id=post['id'],
                                   title=post['title'],
                                   likes=post['score'],
                                   community_name=post['subreddit'],
                                   community_id=community_id,
                                   author=random_author(users),
                                   text=post['selftext'],
                                   date=post['date'],
                                   comments=extract_comments(post))

                posts.append(currentPost)

            # writing on new json
            new_post_filename = filename
            with open(os.path.join('new_posts/',new_post_filename),'w') as file:
                file.write(json.dumps(posts, indent=2, cls=PostEncoder))
                #file.write(",\n")


