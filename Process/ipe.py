import os
import threading
import time
import _mysql,MySQLdb

def getTextSet(p):
	l=[]
	for i in p : 
		l.append(i[0])
	return l
def getsum1(t,p):
	sum1 = 0 
	for i in t :
		for j in p :	
			if i == j:
				sum1 =sum1+i[1]
	return sum1 

def getsum2(t,p,sum_intersection):
	for i in t : 
		sum1 =sum1+i[1]
	for i in p :
		sum1 =sum1+i[1]
	sum1 =sum1 - 2*sum_intersection
	return sum1 

def remove_pattern(NDP,p):
	NDP.remove(p)
	return NDP

def suffling(nd , Dnd , NDP,u ):
	for p in Dnd :
		termset = getTextset(p)
		flag = 1
		for t in termset : 
			if t in nd :
				flag = 0 
			else :
				flag = 1 
		if fal is 0 :
			new_NDP  = remove_pattern(NDP,p) #Remove complete confllict offenders 
		else : 
			var = getsum1( t, p ) 
			offering = ( 1 - 1/u )* var 
			base = getsum2(t,p,var)
			l = [] 
			for t in p :
				if t in n :
					t[1] = (1/u)*t[1] #shrink 
				else
					t[1] = t[1]*(1+offering / base ) 
				l.append(t)
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
				Dp.remove(pattern)
	return Dnd 

def composition ( np , pattern ):
	d =dict() 
	for term, weight in np :
		d[term] = int (weight)  
	for term,weight in pattern :
		if d.has_key(term):
			d[term] = int(d[term])+int(weight)
		else : 
			d[term] =int(weight)
	return d
def Threshold(DP):
	min= 9999 
	l =[]
	for pattern in DP :
		sum = 0 
		for weight in pattern :
			sum =sum +weight 
		if sum < min :
			min = sum 
			l = pattern
	return min

def IPE(D,DP,u):
	np = dict()
	NDP = DP #in our case, we have already normalized all D patterns 
	threshold =Threshold(DP)
	for nd  in D[neg]:
		if weight(nd) >= threshold:
			Dnd = find_Dnd(nd,DP)
			NDP = suffling(nd,Dnd,NDP,u)
			for pattern in NDP : 
				np = composition(np,pattern)
	return np 

# Format [list of list] 
#NDP =[ [[1,2],[2,3],[3]] , [[1,2],[2,3],[3]],  [[1,2],[2,3],[3]]]
def dict_to_list(dic):
	l=[]
	key = getkeys(dic)
	for i in keys :
		l.append([i,dic[i]])
	return l 
def convert (lis):
	l= []
	for i in lis : 
		for j in i :
			l = l.append(j)
	return l 
u=0.2
D= dict()
D['neg'] = []
D['pos'] = []
DP=[]
connection = MySQLdb.connect('localhost', 'root', 'kgggdkp2692', 'mining')
cursor = connection.cursor()
query= "select * from data"
q=cursor.execute(query)
rows = cursor.fetchall()
print q ,query
connection.commit()
connection.close()

for row in rows:
	if row['type'] == 'neg' : 
		D['neg'].append (convert(row[data]))
	else 
		D['pos'].append(convert(row[data]))
	if len(str(row['dp'])) > 2 :
		dp = dp.append ( dict_to_list(row['dp'])) 

print D , dp 
#IPE(D,DP,u)						
				
				
	
