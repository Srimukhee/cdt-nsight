/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.parser2.cpp;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;

/**
 * @author jcamelon
 */
public class CPPASTTranslationUnit extends CPPASTNode implements
        IASTTranslationUnit {
    private IASTDeclaration [] decls = null;
    private static final int DEFAULT_CHILDREN_LIST_SIZE = 8;
    private int currentIndex = 0;
    
    public void addDeclaration( IASTDeclaration d )
    {
        if( decls == null )
        {
            decls = new IASTDeclaration[ DEFAULT_CHILDREN_LIST_SIZE ];
            currentIndex = 0;
        }
        if( decls.length == currentIndex )
        {
            IASTDeclaration [] old = decls;
            decls = new IASTDeclaration[ old.length * 2 ];
            for( int i = 0; i < old.length; ++i )
                decls[i] = old[i];
        }
        decls[ currentIndex++ ] = d;
    }

    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getDeclarations()
     */
    public List getDeclarations() {
        if( decls == null ) return Collections.EMPTY_LIST;
        removeNullDeclarations();
        return Arrays.asList( decls );
    }

    /**
     * @param decls2
     */
    private void removeNullDeclarations() {
        int nullCount = 0; 
        for( int i = 0; i < decls.length; ++i )
            if( decls[i] == null )
                ++nullCount;
        if( nullCount == 0 ) return;
        IASTDeclaration [] old = decls;
        int newSize = old.length - nullCount;
        decls = new IASTDeclaration[ newSize ];
        for( int i = 0; i < newSize; ++i )
            decls[i] = old[i];
        currentIndex = newSize;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getScope()
     */
    public IScope getScope() {
        return null;
    }
    
    /* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getDeclarations(org.eclipse.cdt.core.dom.ast.IBinding)
	 */
	public List getDeclarations(IBinding binding) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getReferences(org.eclipse.cdt.core.dom.ast.IBinding)
	 */
	public List getReferences(IBinding binding) {
		// TODO Auto-generated method stub
		return null;
	}


}
