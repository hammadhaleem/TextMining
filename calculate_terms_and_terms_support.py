import MySQLdb
import json
from Process.ptm import PTM

def get_normalized_dp(dp):
	ndp = {}
	s = 0.0
	for v in dp.values():
		s += v
	for k,v in dp.iteritems():
		ndp[k] = v/s
	return ndp

connection = MySQLdb.connect('localhost', 'root', 'root', 'mining')
cursor = connection.cursor()
query = "select * from data where type='pos'"
cursor.execute(query)
cursor.scroll(0, 'absolute')

while True:
	c = cursor.fetchone()
	if not c:
		break
	c2 = connection.cursor()
	dp = c[-1]
	# print c[0]
	# print dp
	try:
		print get_normalized_dp(json.loads(dp))
	except:
		pass