def getTextSet(p):
#input pi = [[t1,f1],[t2,f2]...[tn,fn]]
	l=[]
	for i in p : 
		l.append(i[0])
	return l
#output ti = { t1, t2 ,t3 ..tn }

# Format [list of list] 
#NDP =[ [[1,2],[2,3],[3]] , [[1,2],[2,3],[3]],  [[1,2],[2,3],[3]]]

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
	new_NDP=[]
	for pi in NDP : 
		if pi is p :
			print "Removed"
		else :
			new_NDP.append(pi)
	return new_NDP 

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







						
				
				
	
