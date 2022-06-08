import json
from json import JSONEncoder

#OBJECT CLASS FOR JSON
class Post:
    def __init__(self,id=None,title=None,likes=None,community_name=None,community_id=None,text=None,date=None, author=None, comments=None ):
        self.id=id
        self.title =title
        self.likes =likes
        self.community_name =community_name
        self.community_id = community_id
        self.text =text
        self.date =date
        self.author = author
        self.comments = comments



# subclass JSONEncoder
class PostEncoder(JSONEncoder):
        def default(self, o):
            return o.__dict__