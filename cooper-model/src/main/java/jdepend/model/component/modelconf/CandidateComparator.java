package jdepend.model.component.modelconf;

import java.util.Comparator;

public class CandidateComparator implements Comparator<Candidate> {

	@Override
	public int compare(Candidate o1, Candidate o2) {
		return o1.getId().compareTo(o2.getId());
	}

}
