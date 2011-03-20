import re

f = open('human_rights.txt', 'r')
fulltext = f.read()

words = fulltext.split()
words = [word.lower() for word in words]


"""
Pseudocode:

For each word, generate a list of "nexts" that a directed line should be drawn to.

for each word
  save next_word in lookup table d

"""
nexts = {}
for i, word in enumerate(words):
    try:
        next_word = words[i+1]
    # This will fail when we hit the last word.
    except:
        break

    if word not in nexts:
        # This is a list representing the set of words that
        # follow this word.
        nexts[word] = [next_word]
    else:
        next_list = nexts[word]
        if next_word not in next_list:
            nexts[word].append(next_word)

print nexts

f.close()

