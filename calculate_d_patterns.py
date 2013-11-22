from Process.spm import SPMining
import MySQLdb
import json

class DP(object):
	def __init__(self, min_sup):
		self.min_sup = min_sup

	def compose(self, dp, p):
		for k,v in p.iteritems():
			try:
				dp[k] += v
			except:
				dp[k] = v
		return dp

	def d_patterns(self, doc):
			sp = SPMining(doc, self.min_sup).spmine()
			print
			print sp
			print
			dp = {}
			for pat in sp:
				p = {}
				for t in pat:
					p[t] = 1
				dp = self.compose(dp, p)
			return dp

p=DP(0.75)
print p.d_patterns([[1,2,3,4],[2,4,5,3],[3,6,1],[5,1,2,7,3]])
# print p.get_terms_support()

connection = MySQLdb.connect('localhost', 'root', 'root', 'mining')
cursor = connection.cursor()
query = "select * from data where type='pos' and id<>25"
cursor.execute(query)
cursor.scroll(0, 'absolute')
dp = DP(0.3)
while True:
	c = cursor.fetchone()
	if not c:
		break
	print c[0]
	data = c[-1].replace('\'','"')
	print data
	print dp.d_patterns(json.loads(data))