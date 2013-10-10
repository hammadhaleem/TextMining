
import pymongo
from pymongo import MongoClient
import os
import xml.etree.ElementTree as ET
import threading

threadLimiter = threading.BoundedSemaphore(7)

def GetDirList(cur_dir, list):
  for root, dirs, files in os.walk(cur_dir):
      for name in files:
        if name.endswith('.xml'):
          list.append(os.path.join(root, name))
      for name in dirs:
        GetDirList(os.path.join(root, name),list)
  return list


class ParseXml :
  def __init__(self,text,name):
    self.data_raw= " "
    tree = ET.parse(text)
    root = tree.getroot()
    self.file_data = dict()
    self.file_data['name'] = name
    for data in root.findall('metadata'):
      for e in data.findall('codes'):
        val = []
        for k in e.findall('code'):
            val.append(k.get('code'))
        self.file_data[str(e.get('class'))] = val
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

    def write_db(self,path,list):
        client = MongoClient()

    def run(self):
      threadLimiter.acquire()
      try:
        val = ParseXml(self.path)
        print val
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

