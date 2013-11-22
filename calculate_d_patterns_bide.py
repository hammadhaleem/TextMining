import os
import threading
import time
import _mysql,MySQLdb


threadLimiter = threading.BoundedSemaphore(2)
path = "/home/engineer/htdocs/pankaj/TextMining/data/"
dirlist = os.listdir(path)
count = 0

class DP(object):
  def __init__(self):
    pass

  def compose(self, dp, p):
    for k,v in p.iteritems():
      try:
        dp[k] += v
      except:
        dp[k] = v
    return dp

  def d_patterns(self, sp):
      dp = {}
      for pat in sp:
        p = {}
        for t in pat:
          p[t] = 1
        dp = self.compose(dp, p)
      return dp

class myThread (threading.Thread):
    def __init__(self,d,name,dp):
        threading.Thread.__init__(self)
        self.d= d
        self.name = name
        self.dp = dp

    def get_normalized_dp(self,dp):
      ndp = {}
      s = 0.0
      for v in dp.values():
        s += v
      for k,v in dp.iteritems():
        ndp[k] = v/s
      return ndp

    def run(self):
      threadLimiter.acquire()
      try:
        dp = self.dp
        os.system("java -jar spmf.jar run BIDE+_with_strings data/"+self.d+" file/temp_output"+self.name+" 20% > file/temp"+self.name+"")
        f = open('file/temp_output'+self.name+'', 'r')
        ft = open('file/temp'+self.name+'' , 'r')
        sp = []
        while True:
          l = f.readline()
          if not l:
            break
          l = l.split('SUP')[0].replace('-1 ', '')
          sp.append(l.split())
        #print sp
        ft.readline()
        exec_time = ft.readline().split()[3]
        dpps = str(self.get_normalized_dp(dp.d_patterns(sp)))
        id = self.d.split('.')[0]
        query = 'Update data set `dp` = \''+dpps.replace('\'', '"')+'\' where id = '+id
        try :
          self.connection = MySQLdb.connect('localhost', 'root', 'kgggdkp2692', 'mining')
          self.cursor = self.connection.cursor()
          q=self.cursor.execute(query)
          print q ,query
          self.connection.commit()
          self.connection.close()
        except Exception as e :
          self.connection =None
          print "Unable to write to DB:" + str(e)
      finally:
        threadLimiter.release()

for d in dirlist:
  threadLimiter.acquire()
  try :
    myThread(d,count,DP()).start()
    print("Processing")
    #time.sleep(2)
    #if count % 50 == 0 :
    print count
  finally:
    count = count+1
    threadLimiter.release()



