import json, os
import datetime as dt
import random
from Comment import *
import re


'''
1)post: {community_id, community_name, post_id} #mancante author
2)Author: {Author_id, username}
3)Comment: (id,text, likes, date, Author(random), post, Subcomments)
4)Subcomments: (id,text,likes,date,auhtor,subcomments)
'''

#function to return post information dictionary
def get_post_info(posts):
    post = {}
    post['post_id'] = posts['id']
    post['author'] = posts['author']
    post['community_id'] = posts['community_id']
    #post['community_name'] = posts['community_name']
    return post


#function to associate random authors to comments
def random_author(users):
    #author = {} #if we wants more fields for author
    author = random.choice(users)
    return author['username']


#function to create subcomment field
def extract_subcomments(comments):
    subComments = []

    for res in comments['subcomments']:
        if not res:
            return subComments
        sub = extract_subcomments(res)
        comment = Comments(id=res['id'],
                           text=res['text'],
                           likes=res['score'],
                           date=res['date'],
                           author=random_author(users),
                           subcomments=sub)
        delattr(comment,'post')
        subComments.append(comment)
    return subComments



##----MAIN----##

# Read list of users
with open(os.path.join('users','db-users.json'), 'r') as file:
	users  = list(json.load(file))



# Read each .json document
for filename in os.listdir('posts'):
    if filename.endswith('.json'):
        # open current json
        with open(os.path.join('posts',filename),"r") as json_file:
            topComments = []

            # reading the json
            document = json.load(json_file)
            # reading posts
            for posts in document:
                # reading comments
                for comments in posts['comments']:
                    # creating object comment (with subcomments, post_info, random_author)
                    comment = Comments(id=comments['id'],
                                       text=comments['text'],
                                       likes=comments['likes'],
                                       date=comments['date'],
                                       author=comments['author'],
                                       post=get_post_info(posts),
                                       subcomments=extract_subcomments(comments))
                    # Build comment array
                    topComments.append(comment)

                # writing on new json (comment directory)
                comment_filename = "comments_" + filename
                with open(os.path.join('new_comments/',comment_filename),'w') as file:
                    file.write(json.dumps(topComments, indent=2, cls=CommentsEncoder))
                    #file.write(",\n")
