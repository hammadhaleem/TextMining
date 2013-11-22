import nltk
import threading
import _mysql,MySQLdb
import os
import xml.etree.ElementTree as ET
from nltk import PorterStemmer
from nltk.stem.wordnet import WordNetLemmatizer
from nltk.corpus import stopwords

#Globals
threadLimiter = threading.BoundedSemaphore(1)
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
        data = text.translate(None, '!@#$)(*&^%~`*!{}][;":/.,?><_-+=').lower()
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
        #self.filtered_words = self.removeFreqone(self.filtered_words)


    def GetData(self):
        return self.filtered_words


class ParseXml :
  def __init__(self,text):
    self.data_raw = []
    tree = ET.parse(text)
    root = tree.getroot()
    try:
      self.data_raw.append(root.findall('headline')[0].text)
    except Exception, e:
      print str(e)
    for data in root.findall('text'):
      for e in data.findall('p'):
        self.data_raw.append(e.text)
  def GetData(self):
    return self.data_raw


def GetDirList(cur_dir, list):
  for root, dirs, files in os.walk(cur_dir):
      for name in files:
        if name.endswith('.xml'):
          list.append(os.path.join(root, name))
      for name in dirs:
        GetDirList(os.path.join(root, name),list)
  return list



class myThread (threading.Thread):
    def __init__(self,path):
        threading.Thread.__init__(self)
        self.path = path
        try :
          self.connection = MySQLdb.connect('localhost', 'root', 'root', 'mining')
        except Exception as e :
          self.connection =None
          print "Unable to connect   :" + str(e)

    def write_db(self,list,list1):
      x = self.connection.cursor()
      try:
        q= "INSERT INTO `data_store`( `path`,`data`) VALUES ('"+self.addslashes(str(list))+"','"+self.addslashes(str(list1))+"')"
        #print q
        x.execute(q)
        self.connection.commit()
      except Exception as e :
        print e
        self.connection.rollback()
      self.connection.close()

    def addslashes(self,s):

        l = ["\\", '"', "'", "\0", ]
        for i in l:
           if i in s:
             s = s.replace(i, '\\'+i)

        return s

    def run(self):
      threadLimiter.acquire()
      cleaned_data = []
      try:
        data = ParseXml(self.path).GetData()
        #print data
        #print Data.GetData()
        for d in data:
          #print d
          cleaned_data.append(DataClean(d).GetData()) 
        try :
          self.write_db(self.path, cleaned_data)
        except MySQLdb.IntegrityError as e :
          print "Error :: "+Str(e)
          pass
        print "Done \n"
      except Exception,e:
        print str(e)
      finally:
        threadLimiter.release()


list =[]
list = GetDirList(os.getcwd(),list)
print "Total Treads " + str(len(list))
count = 0
for path in list :
    print path + "\t"+str(count)
    count =count +1
    threadLimiter.acquire()
    try :
      myThread(path).start()
    finally:
      threadLimiter.release()