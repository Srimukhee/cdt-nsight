/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui;

import org.eclipse.cdt.ui.text.ICCompletionInvocationContext;


public interface ICCompletionContributor {
	
	/**
	 * Initialize the completion contributor class
	 */
	void initialize();
	
	/**
	 * get the matching function of a given name
	 */
	IFunctionSummary getFunctionInfo(ICCompletionInvocationContext context, String name);
	
	/**
	 * Get array of matching functions starting with this prefix
	 */
	IFunctionSummary[] getMatchingFunctions(ICCompletionInvocationContext context, String prefix);
}

