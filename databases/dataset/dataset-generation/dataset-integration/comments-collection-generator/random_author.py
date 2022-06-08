import random
import json
import os



def random_author(users):

    #TEST 1
    print("LUNGHEZZA",len(users))
    author = random.choice(users)
    print("AUTHOR SELECTED: ",author['username'])

    #TEST 2
    # Extract the number of users per community (between 50 and 700)
    number_of_users = round(random.gauss(400, 150))
    number_of_users = max(50, min(number_of_users, 700))

    # Make a copy of the users and shuffle them
    temp_users = users.copy()
    random.shuffle(temp_users)

    #take the first user of the shuffle user list
    first_user = temp_users[0]
    print(first_user['username'])
    return first_user['username']



## Read list of users
with open(os.path.join('users','db-users.json'), 'r') as file:
    users=list(json.load(file))
## end with

random_author(users)
