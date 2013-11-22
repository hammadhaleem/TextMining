import pymongo
from pymongo import MongoClient
import os
import threading
import time
import nltk
from nltk import PorterStemmer
from nltk.stem.wordnet import WordNetLemmatizer
from nltk.corpus import stopwords

threadLimiter = threading.BoundedSemaphore(14)

client = MongoClient('192.184.92.23')
db = client['text_mining']
data = db.converted_data


db2 = client['text_mining_data']
data2 = db2.converted_data

lmtzr = WordNetLemmatizer()
porter_stem = PorterStemmer()
wordnet_tag ={'NN':'n','JJ':'a','VB':'v','RB':'r'}

class DataClean:

    def removeFreqone(self,list):
        new_list={}
        for a in list :
            try:
                new_list[a] += 1
            except:
                new_list[a] = 0
        return [a for a in new_list.keys() if new_list[a] > 0]

    def __init__(self,text):
        data = text.lower()
        tokens = nltk.word_tokenize(data)
        tagged = nltk.pos_tag(tokens)
        word_list = []
        for t in tagged:
            try:
                word_list.append(lmtzr.lemmatize(t[0],wordnet_tag[t[1][:2]]))
            except:
                word_list.append(porter_stem.stem_word(t[0]))

        self.filtered_words = [w for w in word_list if not w in stopwords.words('english')]

        #Now removal of terms with frequency =1  [ paper mentions about this ]
        print self.filtered_words 
        self.filtered_words = self.removeFreqone(self.filtered_words)


    def GetData(self):
        return self.filtered_words


class myThread (threading.Thread):
    def __init__(self,element):
        threading.Thread.__init__(self)
        self.element = element
        self.para = " "

    def write_db(self):
        for i in self.element['paragraphs']:
          self.para =self.para + " " + i

        self.element['keywords'] = DataClean(str(self.para)).GetData()
        data_id = data2.insert(self.element)
        #print self.element
        print data_id
    def run(self):
      threadLimiter.acquire()
      try:
        self.write_db()
      finally:
        threadLimiter.release()


count = 0
all_data =data.find()
for element in all_data:
    count =count +1
    if count > 0:
        threadLimiter.acquire()
        try :
          myThread(element).start()
        except Exception as e :
          print (e)
          pass
        finally:
          threadLimiter.release()
        if count % 300 == 0 :
            print "\t\t\tWaiting for 15 "
            time.sleep(15)
    print count
