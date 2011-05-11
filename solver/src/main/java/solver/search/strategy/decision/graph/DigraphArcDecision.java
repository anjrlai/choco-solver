/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.search.strategy.decision.graph;

import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.search.strategy.assignments.Assignment;
import solver.search.strategy.decision.AbstractDecision;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.graph.GraphVar;

public class DigraphArcDecision extends AbstractDecision<GraphVar> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	int branch;
	Assignment<GraphVar> assignment;
	int fromTo;
	GraphVar g;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************
	
    public DigraphArcDecision(GraphVar variable, int fromTo, Assignment<GraphVar> graph_ass) {
		g = variable;
		this.fromTo = fromTo;
		assignment = graph_ass;
		branch = 0;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
    public boolean hasNext() {
        return branch < 2;
    }

    @Override
    public void buildNext() {
        branch++;
    }

	@Override
	public void apply() throws ContradictionException {
		 if (branch == 1) {
			 assignment.apply(g, fromTo, this);
	     } else if (branch == 2) {
	    	 assignment.unapply(g, fromTo, this);
	     }
	}

	@Override
	public void free() {
		// TODO
	}

	@Override
	public Explanation explain(IntVar v, Deduction d) {
		return null;
	}

	@Override
	public boolean reactOnPromotion() {
		return false;
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.VOID.mask;
	}

	@Override
	@Deprecated
	public void set(GraphVar var, int value, Assignment<GraphVar> assignment) {
		throw new UnsupportedOperationException();		
	}
}
