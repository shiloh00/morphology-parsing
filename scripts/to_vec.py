#/usr/bin/python

import sys
import pickle

id_map = {}

im = {}

with open(sys.argv[1]) as fp:
    im = pickle.load(fp)

print "Loading almost done"

word_count = 0

word_map = {}

for (word, lsts) in im.items():
    for lst in lsts:
        if lst[1] == 'ID_ROOT':
            if lst[0] not in word_map:
                word_map[lst[0]] = word_count
                word_count = word_count + 1
        else:
            for fid in lst[1:]:
                if fid not in id_map:
                    id_map[fid] = 0

for fid in id_map.keys():
    id_map[fid] = word_count
    word_count = word_count + 1

#with open('vec_id.txt', 'w') as fp:
#    for (k, v) in word_map.items():
#        fp.write(k + " " + str(v) + "\n")
#
#    fp.write('\n')
#
#    for (k, v) in id_map.items():
#        fp.write(k + " " + str(v) + "\n")

print "Loading completed! Please input:"

def to_vec(lsts):
    res = []
    for lst in lsts:
        cur = []
        cur.append(word_map[lst[0]])
        for fid in lst[1:]:
            if fid == 'ID_ROOT':
                continue
            cur.append(id_map[fid])
        res.append(cur)
    return res


while True:
    s = sys.stdin.readline()
    if s.strip() in im:
        res = im[s.strip()]
        print res
        print 'vec:', to_vec(res)
