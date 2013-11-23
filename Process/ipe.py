import os
import threading
import time
import MySQLdb

def getTextSet(p):
	l=[]
	for key,value  in p.iteritems():
		l.append(key)
	return l
def getsum1(t,p):
	sum1 = 0.0
	c= 0
	for i in t :
		c+=1
		for k,v in p.iteritems() :

			if i == k:
				sum1 =sum1+v
	return sum1*c

def getsum2(t,p,sum_intersection):
	sum1 = 0.0
	c= 0
	for i,j in p.iteritems() :
		sum1 =sum1+j
		c+=1

	sum1 =2.0*sum1*c - sum_intersection
	return sum1

def remove_pattern(NDP,p):
	try :
		NDP.remove(p)
	except :
		pass
	return NDP

def suffling(nd , Dnd , NDP,u ):
	new_NDP = []
	for p in Dnd :
		termset = getTextSet(p)
		flag = 1
		for t in termset :
			if t in nd :
				flag = 0
			else :
				flag = 1
		if flag is 0 :
			new_NDP  = remove_pattern(NDP,p) #Remove complete confllict offenders
		else :
			var = 0.0
			offering=0.0
			base = 0.0
			fl = 0.0
			var = getsum1( termset, p )
			fl = 1.0/u
			offering = ( 1.0 - fl )* var
			base = getsum2(t,p,var)
			l = []

			for key,val in p.iteritems() :
				if key in nd :
					p[key] = (1.0/u)*p[key] #shrink
				else:
					p[key] = p[key]*(1.0+offering / base )
				l.append(p)

			new_NDP.append(l)
	return new_NDP

def Threadhold(DP):
	value = 0
	return value

def find_Dnd(nd, DP ):
	Dnd = []

	for term in nd :
		for pattern in DP :
			if term in pattern :
				Dnd.append(pattern)
				DP.remove(pattern)
	return Dnd
import time
def composition ( np , pattern ):
	d =dict()
	count = 0
	for term, weight in np.iteritems() :
		d[term] = weight
	for i in pattern :
		try :
			for term,weight in i.iteritems() :
				if d.has_key(term):
					d[term] = d[term]+weight
				else :
					d[term] =weight
		except :
			pass

	return d



def Threshold(DP):
	min= 9999
	l =[]
	for pattern in DP :
		sum = 0
		for a,weight in pattern :
			sum =sum +weight
		if sum < min :
			min = sum
			l = pattern
	return min
def weight(list):
	d = dict()
	w=0
	count = 0
	for word in list :
		count =count + 1
		try:
			d[word]= d[word]+1
		except:
			d[word] = 1
	for key,value  in d.iteritems():
		w=w+value
	return w

def IPE(D,DP,u,threshold):
	np = dict()
	Dnd = []
	NDP = DP #in our case, we have already normalized all D patterns
	count = 0
	for v in D['neg']:
		d = str(v)
		nd = d.replace('[', '').replace("]", '').replace(' ','').split(",")
		count =count + 1
		print "Counter:" , count
		if weight(nd) >=  threshold:
			Dnd = find_Dnd(nd,DP)
		NDP = suffling(nd,Dnd,NDP,u)
		print weight(nd) , threshold
		for pattern in NDP :
			np = composition(np,pattern)
		if (count % 20 )== 0 :
			print np
	return np

connection = MySQLdb.connect('localhost', 'root', 'kgggdkp2692', 'mining')
cursor = connection.cursor()
query= "select * from data"
q=cursor.execute(query)
rows = cursor.fetchall()
connection.commit()
connection.close()

import json

u=0.002
D= dict()
D['neg'] = []
D['pos'] = []
dp=[]
threshold = 9999
for row in rows:

	d=dict()
	try :
		var = row[4].replace("'", "\"")
		d= json.loads(var)
	except :
		d = {}

	stri = row[3].replace('"', '').replace("'", '')
	stri.split(' ')
	if row[2] == 'neg' :
		D['neg'].append(stri)
	else:
		D['pos'].append(stri)

	if len(d) > 10 :
		nd = stri.replace('[', '').replace("]", '').replace(' ','').split(",")
		dp.append(d)
		count = 0
		for i in nd :
			count =count +1
		sum = 0.0
		for key,value in d.iteritems():
			c =value*count
			sum = c + sum
		if sum <  threshold :
			threshold = sum

pattern = IPE(D,dp,20,threshold)
print pattern





