import praw
import prawcore
import datetime as dt
import json
import os
from Post import *
from Comments import *

#### Extract in a recursive manner at most 5 levels of comments
def extract_comments (parent, current_level = 1):
	subComments = []

	if current_level > 5:
		return subComments

	for r in parent.replies:
		subc = extract_comments(r, current_level+1)
		comment = Comments(id=r.id,
							text=r.body,
							score=r.score,
							date=str(dt.datetime.fromtimestamp(r.created)),
							subcomments=subc)
		subComments.append(comment)
	## end for
	
	return subComments
####

# Reddit user info (test app on reddit profile)
reddit = praw.Reddit(
			client_id='YG29jnzECTQXmP4Lkgwznw', 
			client_secret='8N1suT8JMRO5wX3JUuv15Ct5B_I9WQ', 
			user_agent='user_agent')


# tmp data structure
posts = []

# Extract subreddit urls
with open("reddit.json", "r") as f:
	reddit_urls = json.load(f)
## end with

with open("selected_games.json", "r") as f:
	selected_games = json.load(f)
## end with


POST_LIMIT = 500 # max number of posts to scrape
first_subreddit_index = 195 # point where to start
parsed_subreddits = 0 # counter
MAX_SUBREDDITS = 120

# subreddit submission
for url in reddit_urls [first_subreddit_index:]:
	if parsed_subreddits >= MAX_SUBREDDITS:
		break

	# Check if subreddit url has been chosen
	if url['id'] not in selected_games:
		continue
	## end if
	

	this_subreddit = reddit.subreddit(url['url'].split("/")[-1])
	# Check number of subscribers
	try:
		if this_subreddit.subscribers < 1000:
			continue
		## end if
	except:
		# subreddit doesn't exist
		continue
	## end try except

	print(url['id'])

	# Open json file
	filename = 'posts/post' + str(url['id']) + '.json'
	file = open(filename, 'w')
	file.write("[\n")

	parsed_subreddits += 1
	comment_inserted = 0

	for post in this_subreddit.hot(limit=POST_LIMIT):
		topComments = []

		# retrieving post date
		time = post.created
		date = dt.datetime.fromtimestamp(time)

		#comment submission (2 levels depth)
		submission = reddit.submission(id=post.id)
		submission.comments.replace_more(limit=None)
		for top_level_comment in submission.comments:
			# creating object comment with subcomments
			comment = Comments(id=top_level_comment.id,
							text=top_level_comment.body,
							score=top_level_comment.score,
							date=str(dt.datetime.fromtimestamp(top_level_comment.created)),
							subcomments = extract_comments(top_level_comment))

			# Build comment array
			topComments.append(comment)
		## end for

		# create Post object with comments and subcomments
		currentPost = Post(post.id,
						post.title,
						post.score,
						str(post.subreddit),
						post.selftext,
						str(date),
						comments=topComments)

		#writing on json
		file.write(json.dumps(currentPost, indent=2,  cls=PostEncoder))
		file.write(",\n")
		posts.append(currentPost)
		comment_inserted += 1
	## end for

	file.close()

	print('Videogame:' + str(url['id']) + ": " + str(comment_inserted) + " comments")

	# Remove last comma
	with open(filename, "rb+") as f:
		f.seek(-3, os.SEEK_END)
		f.truncate()

	# Close JSON printed on file
	with open(filename, "a") as f:
		f.write("\n]")
## end for
