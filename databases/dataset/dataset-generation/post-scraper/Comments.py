import json
from json import JSONEncoder

#OBJECT CLASS FOR JSON
class Comments:
    def __init__(self, id=None, text=None, score=None,date=None, subcomments=None):
        self.id = id
        self.text = text
        self.score = score
        self.date = date
        self.subcomments = subcomments



# subclass JSONEncoder
class CommentsEncoder(JSONEncoder):
        def default(self, o):
            return o.__dict__
