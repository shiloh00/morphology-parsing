#!/usr/bin/python

import sys
import json
import pickle
import copy


def load_file(path):
    current_id = 0
    word_map = {}
    # work => { count: c, data: {}, words: [] }
    word_list = []
    with open(path) as fp:
        for line in fp:
            line = line.strip()[1:-1].lower()
            if len(line) == 0:
                continue
            wl = line.split(",")
            word_list.append(wl)
            if wl[0] in word_map:
                continue
            word_map[wl[0]] = { "word": wl[0], "id": current_id, "form": {}, "pos": []}
            current_id = current_id + 1
    for wl in word_list:
        ca = wl[2] + "," + wl[3]
        if wl[2] not in word_map[wl[0]]["pos"]:
            word_map[wl[0]]["pos"].append(wl[2])
        if False:
        #if wl[3] == "ID_ROOT":
            continue
        else:
            if ca not in word_map[wl[0]]["form"]:
                word_map[wl[0]]["form"][ca] = []
            found = False
            for w in word_map[wl[0]]["form"][ca]:
                if w["word"] == wl[1]:
                    found = True
                    break
            if not found:
                word_map[wl[0]]["form"][ca].append({"word": wl[1], "pos": [wl[2]], 
                    "form": {}, "id": current_id})
                current_id = current_id + 1
    # second pass to handle "DERIVED"
#    for wl in word_list:
#        ca = wl[2] + "," + wl[3]
#        if wl[2] == "DERIVED":
#            # do nothing in the first pass
#            if wl[1] not in word_map[wl[0]]["word_set"]:
#                if ca not in word_map[wl[0]]["data"]:
#                    word_map[wl[0]]["data"][ca] = []
#                if wl[1] not in word_map[wl[0]]["data"][ca]:
#                    word_map[wl[0]]["data"][ca].append(wl[1])
#            word_map[wl[0]]["word_set"][wl[1]] = 1
    return word_map

def filter_map(word_map):
    final_list = []
    no_child_count = 0
    for (k, v) in word_map.items():
        final_list.append(v)
        #if len(v["form"]) == 0:
        if not has_child(v):
            no_child_count = no_child_count + 1

    print "no child count:", no_child_count
    print "root count:", len(final_list)
    return final_list

def has_child(v):
    wd = v["word"]
    for itt in v["form"].values():
        for it in itt:
            if wd != it["word"]:
                return True
    return False

def track_word(cur, l, res):
    if cur["word"] not in res:
        res[cur["word"]] = []
    if len(l) == 1:
#        res[cur["word"]].append([l[0], "ID_ROOT"])
        pass
    else:
        res[cur["word"]].append(copy.deepcopy(l))
    for (k, v) in cur["form"].items():
        l.append(k)
        for vv in v:
            track_word(vv, l, res)
        l.pop()

def invert_map(words, wl):
    res = {}
    for wd in words:
        track_word(wd, [wd["word"]], res)
    return res


m = load_file(sys.argv[1])
final_list = filter_map(m)
print len(final_list)
im = invert_map(final_list, m)
with open("d.pickle", "w") as fp:
    pickle.dump(im, fp)
#for item in final_list:
#    print item["word"]
#    s = json.dumps(item)
with open(sys.argv[2], "w") as fp:
    json.dump(final_list, fp)
