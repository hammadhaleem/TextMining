# input : Positive documents D+ ; Minimum support , min_sup
# output: d-Patterns DP  , And support of terms 

DP=Null
foreach document d in D+ do :
	#let ps(d) be set of paragraph in d 
	SP= BIDE+(ps(d),min_sup)
	d`=Null
	foreach pattern pi in SP do 
		p = {(t,1)|t E pi }
		d`=d` @ p 
	end 
end 

T= {t|(t,f) E p , p E DP }
for each term t E T do 
	support(t) = 0 
end 

foreach d-Pattern p E DP do 
	foreach (t,w) E b(p) do 
		support(t)=support(t)+w
	end
end