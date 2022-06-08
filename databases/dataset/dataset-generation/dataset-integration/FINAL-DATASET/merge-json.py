import json
import os

dir_name = 'comments'

directory = os.listdir(os.path.join(os.path.dirname(__file__), dir_name))
final_doc = []

for f in directory:
	print(f)

	with open(os.path.join(os.path.dirname(__file__), dir_name, f), 'r') as file:
		doc = json.load(file)

		for d in doc:
			final_doc.append(d)
		## end for
	## end with
## end for

output_name = dir_name + '.json'
with open(os.path.join(os.path.dirname(__file__), output_name), 'w') as file:
	file.write(json.dumps(final_doc, indent=2))
