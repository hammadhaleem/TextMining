import pymongo
from pymongo import MongoClient
import os
import xml.etree.ElementTree as ET
import threading
import time

threadLimiter = threading.BoundedSemaphore(5)

client = MongoClient()
db = client['text_mining']
data = db.converted_data


def GetDirList(cur_dir, list):
  for root, dirs, files in os.walk(cur_dir):
      for name in files:
        if name.endswith('.xml'):
          list.append(os.path.join(root, name))
      for name in dirs:
        GetDirList(os.path.join(root, name),list)
  return list


class ParseXml :
  def __init__(self,text):
    self.data_raw= " "
    tree = ET.parse(text)
    root = tree.getroot()
    self.file_data = dict()
    self.file_data['name'] = text
    for data in root.findall('metadata'):
      for e in data.findall('codes'):
        val = []
        for k in e.findall('code'):
            val.append(k.get('code'))
        self.file_data[str(e.get('class')).translate(None, '!@#$)(*&^%~`*!{}][;":/.,?><_-+=').lower()] = val
    self.file_data['paragraphs'] = []
    for data in root.findall('text'):
      for e in data.findall('p'):
        self.file_data['paragraphs'].append(e.text)

  def GetData(self):
    return self.file_data


class myThread (threading.Thread):
    def __init__(self,path):
        threading.Thread.__init__(self)
        self.path = path

    def write_db(self,path):
        data_id = data.insert(path)
        print ( data_id ,"\n")
    def run(self):
      threadLimiter.acquire()
      try:
        val = ParseXml(self.path)
        self.write_db(val.GetData())
      finally:
        threadLimiter.release()

list =[]
list = GetDirList(os.getcwd(),list)
print ( "Total Treads " + str(len(list)))
count = 0
for path in list :
    print ( path + "\t"+str(count))
    count =count +1
    threadLimiter.acquire()
    try :
      myThread(path).start()
    except Exception as e :
      print ( e)
      pass
    finally:
      threadLimiter.release()
    if count % 30000 == 0 :
        print "\t\t\tWaiting for 15 "
        time.sleep(15)






