import time
class SPMining(object):
	def __init__(self, seq, min_support):
		'''seq: List of Sequence in Paragraphs.
		   min_support: Min absolute support.
		'''
		self.freq_paterns = []
		self.seq = seq
		self.min_support = min_support
		self.freq_1 = self.get_1term_fsp()

	def projected_DB(self, pat):
		pdb = []
		p = pat[-1]	#Last term
		for s in self.seq:
			if self.test_for_sup(pat, s): 
				pdb.append(s[s.index(p)+1:])
		return pdb

	def projected_DB_terms(self, pat):
		pdts = []
		pdb = self.projected_DB(pat)
		for pd in pdb:
			for p in pd:
				if [p] in self.freq_1 and p not in pdts: # and p not in self.freq_1:
					pdts.append(p)
		return pdts

	def test_for_sup(self, pat, seq):
		i,j = 0,0
		# print len(seq)
		while j<len(seq):
			if pat[i] == seq[j]:
				i += 1
			if i == len(pat):
				return True
			j += 1
		return False

	def get_abs_support(self, pat):
		sup = 0
		for s in self.seq:
			if self.test_for_sup(pat, s):
				sup += 1
		return sup

	def get_rel_support(self, pat):
		sup = self.get_abs_support(pat)*1.0
		return sup/len(self.seq)

	def get_1term_fsp(self):
		fsp = []	#1term fsp
		for s in self.seq:
			for t in s:
				# print t, self.get_abs_support([t])
				if [t] not in fsp and self.get_rel_support([t]) >= self.min_support:
					fsp.append([t])
		return fsp

	def spmine(self):
		if len(self.seq) == 1:
			return self.seq
		return self.mine(self.freq_1)

	def mine(self, PL):
		self.prune(PL)
		self.freq_paterns += PL #SP <- SP U PL
		PLnew = [] #PL'
		#print PL if len(PL) < 200 else len(PL)
		for p in PL:
			pdb = self.projected_DB_terms(p)
			for pd in pdb:
				q = p+[pd]
				if self.get_rel_support(q) >= self.min_support:
					PLnew.append(q)
		if len(PLnew) == 0:
			return self.freq_paterns
		else:
			return self.mine(PLnew)

	def prune(self, PL):
		if len(PL) == 0:
			return
		elem_len = len(PL[0])-1
		self.eliminate([s for s in self.freq_paterns if len(s) == elem_len], PL)

	def eliminate(self, PLOld, PL):
		print len(PL), len(PLOld)
		for s in PL:
			for p in PLOld:
				#print p,s,self.get_abs_support(p), self.get_abs_support(s)
				if self.test_for_sup(p, s) and self.get_abs_support(p) == self.get_abs_support(s):
					try:
						self.freq_paterns.remove(p)
					except:
						break
