class AssignWeight(object):
	def __init__(self, terms_sup):
		self.terms_sup = terms_sup

	def weigh(self, doc):
		weight = 0.0
		for term in doc:
			try:
				weight += self.terms_sup[term]
			except:
				pass
		return weight