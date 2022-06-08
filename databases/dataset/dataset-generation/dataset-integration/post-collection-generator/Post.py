import json
from json import JSONEncoder

#OBJECT CLASS FOR JSON
class Post:
    def __init__(self, id=None, title=None, likes=None, author=None, text=None, date=None, community=None, comments=None):
        self.id = id
        self.title = title
        self.likes = likes
        self.author = author
        self.text = text
        self.date = date
        self.community = community
        self.comments = comments


# subclass JSONEncoder
class PostEncoder(JSONEncoder):
        def default(self, o):
            return o.__dict__