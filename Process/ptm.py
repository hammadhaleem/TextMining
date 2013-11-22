from spm import SPMining
class PTM(object):
	def __init__(self, pos_doc, min_sup):
		'''pos_doc: D+
		   min_sup: Minimum (relative) Support required
		'''
		self.pos_doc = pos_doc
		self.min_sup = min_sup
		pass

	def compose(self, dp, p):
		for k,v in p.iteritems():
			try:
				dp[k] += v
			except:
				dp[k] = v
		return dp


	def d_patterns(self):
		DP = []
		for doc in self.pos_doc:
			sp = SPMining(doc, self.min_sup).spmine()
			#print sp
			dp = {}
			for pat in sp:
				p = {}
				for t in pat:
					p[t] = 1
				dp = self.compose(dp, p)
			DP.append(dp)
		return DP

	def get_terms(self, DPs=None):
		dp = DPs or self.d_patterns()
		Terms = []	#Terms
		for d in dp:
			for k in d.keys():
				if k not in Terms:
					Terms.append(k)
		return Terms

	def get_normalized_support(self, dp):
		beta = {}
		total = sum([f for f in dp.values()])*1.0
		for t,f in dp.iteritems():
			beta[t] = f/total
		return beta

	def get_terms_support(self, DPs=None):
		dp = DPs or self.d_patterns()
		terms = self.get_terms()
		sup = {}
		for d in dp:
			beta = self.get_normalized_support(d)
			for t,w in beta.iteritems():
				try:
					sup[t] += w
				except:
					sup[t] = w
		return sup

# p=PTM([[[1,2,3,4],[2,4,5,3],[3,6,1],[5,1,2,7,3],],], 0.75)
# print p.d_patterns()
# print p.get_terms_support()