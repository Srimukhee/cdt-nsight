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
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;

/**
 * @author jcamelon
 */
public class CPPASTName extends CPPASTNode implements IASTName {

    private char[] name;
    private static final char[] EMPTY_CHAR_ARRAY = { };
    private IBinding binding = null;

    /**
     * @param name 
     */
    public CPPASTName(char [] name ) {
        this.name = name;
    }

    /**
     * 
     */
    public CPPASTName() {
        name = EMPTY_CHAR_ARRAY;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTName#resolveBinding()
     */
    public IBinding resolveBinding() {
    	if( binding == null )
    		binding = CPPVisitor.createBinding( this ); 
    	
        return binding;
    }
    
	public IBinding[] resolvePrefix() {
		return CPPSemantics.prefixLookup(this);
	}
	
    protected void setBinding( IBinding binding ){
    	this.binding = binding;
    }
    protected IBinding getBinding(){
    	return binding;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        if( name == EMPTY_CHAR_ARRAY ) return null;
        return new String( name );
    }
    
    public char[] toCharArray() {
    	return name;
    }
    
    public void setName( char [] name )
    {
        this.name = name;
    }

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitNames ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTName#isDeclaration()
	 */
	public boolean isDeclaration() {
		IASTNode parent = getParent();
		if (parent instanceof IASTNameOwner) {
			int role = ((IASTNameOwner) parent).getRoleForName(this);
			if( role == IASTNameOwner.r_reference ) return false;
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTName#isReference()
	 */
	public boolean isReference() {
		IASTNode parent = getParent();
		if (parent instanceof IASTNameOwner) {
			int role = ((IASTNameOwner) parent).getRoleForName(this);
			if( role == IASTNameOwner.r_reference ) return true;
			return false;
		}
		return false;
	}
}
