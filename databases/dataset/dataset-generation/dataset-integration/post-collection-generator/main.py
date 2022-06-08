import json, os
import datetime as dt
import random
from Post import *
from Comments import *
import re

'''
Post per community FABRI

Community: {community_id, name, genre}
Likes (int)
Author: {username}
Comment top level (only)
'''


#function to return post information dictionary
def get_community_info(post,community_genres):
    community = {}
    community['community_id'] = post['community_id']
    community['community_name'] = post['community_name']
    community['community_genre'] = community_genres
    return community


#function to extract only top level comments
def extract_comments(post):
    topComments = []

    for comment in post['comments']:
        if not comment:
            return topComments
        comment = Comments(id=comment['id'],
                           text=comment['text'],
                           likes=comment['likes'],
                           date=comment['date'],
                           author=comment['author'])
        topComments.append(comment)
    return topComments



##----MAIN----##


#Read list of communities
with open(os.path.join('communities','db-games-with-reddit-data.json'), 'r') as file:
	communities  = list(json.load(file))


# Read each .json document
for filename in os.listdir('posts'):
    if filename.endswith('.json'):
        # open current json
        with open(os.path.join('posts',filename),"r") as json_file:

            # extract community_id (from json name)
            research = re.search('post(.+?)json',filename)
            community_id = (research.group(1))[:-1]

            # extract community_genres (from communities document)
            counter_communities = 0
            community_genres = []
            while counter_communities < len(communities):
                if (communities[counter_communities]['id'] == int(community_id)):
                    community_genres = communities[counter_communities]['genres']
                counter_communities += 1

            # reading the json
            document = json.load(json_file)
            # reading posts
            posts = []
            for post in document:
                #CREATE OBJECT POST
                currentPost = Post(id=post['id'],
                                   title=post['title'],
                                   likes=post['likes'],
                                   author=post['author'],
                                   text=post['text'],
                                   date=post['date'],
                                   community=get_community_info(post,community_genres),
                                   comments=extract_comments(post))

                posts.append(currentPost)

            # writing on new json (comment directory)
            new_post_filename = filename
            with open(os.path.join('new_posts/',new_post_filename),'w') as file:
                file.write(json.dumps(posts, indent=2, cls=PostEncoder))
                #file.write(",\n")
