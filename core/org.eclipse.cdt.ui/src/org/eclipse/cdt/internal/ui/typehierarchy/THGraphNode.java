/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.ui.typehierarchy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.model.ICElement;

class THGraphNode {
	private List<THGraphEdge> fOutgoing= Collections.emptyList();
	private List<THGraphEdge> fIncoming= Collections.emptyList();
	private ICElement fElement;
	private ICElement[] fMembers= null;
	
	THGraphNode(ICElement element) {
		fElement= element;
	}
	
	void startEdge(THGraphEdge outgoing) {
		fOutgoing= addElement(fOutgoing, outgoing);
	}

	void endEdge(THGraphEdge incoming) {
		fIncoming= addElement(fIncoming, incoming);
	}
	
	ICElement getElement() {
		return fElement;
	}

	private List<THGraphEdge> addElement(List<THGraphEdge> list, THGraphEdge elem) {
		switch (list.size()) {
		case 0:
			return Collections.singletonList(elem);
		case 1:
			list= new ArrayList<THGraphEdge>(list);
			list.add(elem);
			return list;
		}
		list.add(elem);
		return list;
	}

	List<THGraphEdge> getOutgoing() {
		return fOutgoing;
	}
	
	List<THGraphEdge> getIncoming() {
		return fIncoming;
	}

	public void setMembers(ICElement[] array) {
		fMembers= array;
	}
	
	public ICElement[] getMembers(boolean addInherited) {
		if (!addInherited) {
			return fMembers;
		}
		ArrayList<ICElement> list= new ArrayList<ICElement>();
		collectMembers(new HashSet<THGraphNode>(), list);
		return list.toArray(new ICElement[list.size()]);
	}

	private void collectMembers(HashSet<THGraphNode> visited, List<ICElement> list) {
		if (visited.add(this)) {
			if (fMembers != null) {
				list.addAll(Arrays.asList(fMembers));
			}
			List<THGraphEdge> bases= getOutgoing();
			for (Iterator<THGraphEdge> iterator = bases.iterator(); iterator.hasNext();) {
				THGraphEdge edge = iterator.next();
				edge.getEndNode().collectMembers(visited, list);
			}
		}
	}
}
