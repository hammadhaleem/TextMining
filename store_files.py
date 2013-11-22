import MySQLdb

connection = MySQLdb.connect('localhost', 'root', 'kgggdkp2692', 'mining')
cursor = connection.cursor()
type="neg"	#For Negative Documents
# type="pos"	#For Positive Documents
did = 0
statements = []
while True:
	try:
		temp = raw_input()
		if len(temp) == 0:
			try:
				query = "INSERT INTO `data`(`did`, `data`, `type`) VALUES ("+str(did)+",'"+str(statements).replace("'","\\'")+"','"+type+"')"
				# print query
				cursor.execute(query)
				connection.commit()
			except Exception, e:
				print str(e)
		elif temp[0:2] == '.I':
			did = int(temp[3:])
		elif temp[0:2] == '.W':
			statements = []
		else:
			statements.append(temp.split())
	except:
		break
