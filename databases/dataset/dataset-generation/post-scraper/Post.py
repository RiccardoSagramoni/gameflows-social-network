from Comments import Comments
from json import JSONEncoder

#OBJECT CLASS FOR JSON
class Post:
    def __init__(self, id, title, score, subreddit, selftext, date, comments=[]):
        self.id = id
        self.title = title
        self.score = score
        self.subreddit = subreddit
        self.selftext = selftext
        self.date = date
        self.comments = comments

# subclass JSONEncoder
class PostEncoder(JSONEncoder):
        def default(self, o):
            return o.__dict__