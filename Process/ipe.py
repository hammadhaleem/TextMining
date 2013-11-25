import os
import threading
import time
import MySQLdb
import collections


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
	d={}
	var =999.0
	for pattern in DP :
		for a,weight in pattern.iteritems() :
			
			if d.has_key(a):
				d[a]=d[a]+weight
				if (var >= d[a]):
					var =d[a]
			else:
				d[a]=weight
				if (var >= d[a]):
					var =d[a]
	return var,d

def weight(lis,s):
	count = 0.0
	l=[]
	for word in lis :
		if word in l :
			pass
		else:
			l.append(word)
	for w in l :
		if w in s :
			count = count + s[w]			
	return count

def IPE(D,DP,u):
	np = dict()
	Dnd = []
	NDP = DP #in our case, we have already normalized all D patterns
	count = 0
	threshold =0.0
	threshold,Lis=Threshold(DP)
	Lisi=Lis
	print threshold

	for v in D['neg']:
		d = str(v)
		nd = d.replace('[', '').replace("]", '').replace(' ','').split(",")
		count =count + 1
		
		wei = weight(nd,Lis)
		if wei >=  threshold:
			Dnd = find_Dnd(nd,DP)
		print "Counter:" , count ,"Weight :" ,  wei , "Threshold : " , threshold
		
		NDP = suffling(nd,Dnd,NDP,u,)
		for pattern in NDP :
			np = composition(np,pattern)
		if (count % 20 )== 0 :
			fo = open("output.txt", "w+")
			print "Name of the file: ", fo.name
			od = collections.OrderedDict(sorted(np.items()))
			for key,value in od.iteritems():
				fo.write(str(key)+":"+str(value)+"\n")
			fo.close()

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
Lisi={}
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
		

pattern = IPE(D,dp,2)






