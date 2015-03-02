/*
 * Copyright (C) 2010-2012 Serge Rieder
 * serge@jkiss.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.jkiss.dbeaver.ui.editors.sql.syntax;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.jkiss.utils.CommonUtils;

/**
 * SQL Completion proposal
 */
public class SQLCompletionProposal implements ICompletionProposal, ICompletionProposalExtension2 {

    static final Log log = LogFactory.getLog(SQLCompletionProposal.class);

    private SQLSyntaxManager syntaxManager;

    /** The string to be displayed in the completion proposal popup. */
    private String displayString;
    /** The replacement string. */
    private String replacementString;
    private String replacementLower;
    /** The replacement offset. */
    private int replacementOffset;
    /** The replacement length. */
    private int replacementLength;
    /** The cursor position after this proposal has been applied. */
    private int cursorPosition;
    /** The image to be displayed in the completion proposal popup. */
    private Image image;
    /** The context information of this proposal. */
    private IContextInformation contextInformation;
    /** The additional info of this proposal. */
    private String additionalProposalInfo;

    public SQLCompletionProposal(SQLSyntaxManager syntaxManager, String displayString, String replacementString, SQLWordPartDetector wordDetector, int cursorPosition, Image image, IContextInformation contextInformation, String additionalProposalInfo)
    {
        this.syntaxManager = syntaxManager;
        this.displayString = displayString;
        this.replacementString = replacementString;
        this.replacementLower = replacementString.toLowerCase();
        this.cursorPosition = cursorPosition;
        this.image = image;
        this.contextInformation = contextInformation;
        this.additionalProposalInfo = additionalProposalInfo;

        setPosition(wordDetector);
    }

    private void setPosition(SQLWordPartDetector wordDetector)
    {
        String wordPart = wordDetector.getWordPart();
        int divPos = wordPart.lastIndexOf(syntaxManager.getStructSeparator());
        if (divPos == -1) {
            replacementOffset = wordDetector.getOffset();
            replacementLength = wordDetector.getLength();
        } else {
            replacementOffset = wordDetector.getOffset() + divPos + 1;
            replacementLength = wordDetector.getLength() - divPos - 1;
        }
    }

    @Override
    public void apply(IDocument document) {
        try {
            document.replace(replacementOffset, replacementLength, replacementString);
        } catch (BadLocationException e) {
            // ignore
            log.debug(e);
        }
    }

    /*
     * @see ICompletionProposal#getSelection(IDocument)
     */
    @Override
    public Point getSelection(IDocument document) {
        return new Point(replacementOffset + cursorPosition, 0);
    }

    @Override
    public String getAdditionalProposalInfo()
    {
        return additionalProposalInfo;
    }

    @Override
    public String getDisplayString()
    {
        return displayString;
    }

    @Override
    public Image getImage()
    {
        return image;
    }

    @Override
    public IContextInformation getContextInformation()
    {
        return contextInformation;
    }

    //////////////////////////////////////////////////////////////////
    // ICompletionProposalExtension2

    @Override
    public void apply(ITextViewer viewer, char trigger, int stateMask, int offset)
    {
        apply(viewer.getDocument());
    }

    @Override
    public void selected(ITextViewer viewer, boolean smartToggle)
    {

    }

    @Override
    public void unselected(ITextViewer viewer)
    {

    }

    @Override
    public boolean validate(IDocument document, int offset, DocumentEvent event)
    {
        final SQLWordPartDetector wordDetector = new SQLWordPartDetector(document, syntaxManager, offset);
        String wordPart = wordDetector.getWordPart();
        int divPos = wordPart.lastIndexOf(syntaxManager.getStructSeparator());
        if (divPos != -1) {
            wordPart = wordPart.substring(divPos + 1);
        }
        if (!CommonUtils.isEmpty(wordPart) && replacementLower.startsWith(wordPart.toLowerCase())) {
            setPosition(wordDetector);
            return true;
        } else {
            return false;
        }
    }
}