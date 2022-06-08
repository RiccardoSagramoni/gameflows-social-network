# dataset-integration
## FUNZIONAMENTO SCRIPT PYTHON

1. post-author-associator (main.py)<br>
Crea il formato corretto .json (aggiungendo author per post e commenti, aggiungendo le info sulla community e cambiando qualche nome di attributo), usati per la creazione delle collections COMMENTS e POSTS.<br>
FILES OUTPUT: directory "new_posts"
	
2. comments_collection_generator<br>
Crea la collection COMMENTS (usando come input il risultato dello SCRIPT 1)<br>
FILES OUTPUT: directory "new_comments"
	
3. post_collection_generator<br>
Crea la collection POSTS (usando come input il risultato dello SCRIPT 1)<br>
FILES OUTPUT: directory "new_posts"
	
	
### NOTE:
- nel deploy finale non sono presenti i risultati intermedi della generazione del dataset (i.e. l'output dei script python di questa cartella) per non appesantire il repository
- community 227 eliminata (incongruenza con genres)
