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

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBPKeywordManager;
import org.jkiss.dbeaver.model.impl.EmptyKeywordManager;
import org.jkiss.dbeaver.ui.editors.sql.SQLConstants;
import org.jkiss.dbeaver.ui.editors.sql.syntax.tokens.*;
import org.jkiss.dbeaver.ui.editors.text.TextWhiteSpaceDetector;
import org.jkiss.utils.Pair;

import java.util.*;

/**
 * SQLSyntaxManager.
 * <p/>
 * Contains information about some concrete datasource underlying database syntax.
 * Support runtime change of datasource (reloads syntax information)
 */
public class SQLSyntaxManager extends RuleBasedScanner {

    public static final String DEFAULT_STATEMENT_DELIMITER = ";";

    public static final String CONFIG_COLOR_KEYWORD = "org.jkiss.dbeaver.sql.editor.color.keyword.foreground";
    public static final String CONFIG_COLOR_DATATYPE = "org.jkiss.dbeaver.sql.editor.color.datatype.foreground";
    public static final String CONFIG_COLOR_STRING = "org.jkiss.dbeaver.sql.editor.color.string.foreground";
    public static final String CONFIG_COLOR_NUMBER = "org.jkiss.dbeaver.sql.editor.color.number.foreground";
    public static final String CONFIG_COLOR_COMMENT = "org.jkiss.dbeaver.sql.editor.color.comment.foreground";
    public static final String CONFIG_COLOR_DELIMITER = "org.jkiss.dbeaver.sql.editor.color.delimiter.foreground";
    public static final String CONFIG_COLOR_PARAMETER = "org.jkiss.dbeaver.sql.editor.color.parameter.foreground";
    public static final String CONFIG_COLOR_TEXT = "org.jkiss.dbeaver.sql.editor.color.text.foreground";
    public static final String CONFIG_COLOR_BACKGROUND = "org.jkiss.dbeaver.sql.editor.color.text.background";

    private IThemeManager themeManager;

    private DBPKeywordManager keywordManager;
    private String quoteSymbol;
    private String structSeparator;
    private String statementDelimiter = DEFAULT_STATEMENT_DELIMITER;

    private TreeMap<Integer, SQLScriptPosition> positions = new TreeMap<Integer, SQLScriptPosition>();

    private Set<SQLScriptPosition> addedPositions = new HashSet<SQLScriptPosition>();
    private Set<SQLScriptPosition> removedPositions = new HashSet<SQLScriptPosition>();
    private char escapeChar;

    public SQLSyntaxManager()
    {
        themeManager = PlatformUI.getWorkbench().getThemeManager();
    }

    public void dispose()
    {
    }

    public DBPKeywordManager getKeywordManager()
    {
        return keywordManager;
    }

    public String getStructSeparator()
    {
        return structSeparator;
    }

    public String getStatementDelimiter()
    {
        return statementDelimiter;
    }

    public String getQuoteSymbol()
    {
        return quoteSymbol;
    }

    public Collection<? extends Position> getPositions(int offset, int length)
    {
        return positions.subMap(offset, offset + length).values();
    }

    public synchronized Set<SQLScriptPosition> getRemovedPositions(boolean clear)
    {
        Set<SQLScriptPosition> posList = removedPositions;
        if (clear) {
            removedPositions = new HashSet<SQLScriptPosition>();
        }
        return posList;
    }

    public synchronized Set<SQLScriptPosition> getAddedPositions(boolean clear)
    {
        Set<SQLScriptPosition> posList = addedPositions;
        if (clear) {
            addedPositions = new HashSet<SQLScriptPosition>();
        }
        return posList;
    }

    public void setDataSource(DBPDataSource dataSource)
    {
        if (dataSource == null) {
            keywordManager = EmptyKeywordManager.INSTANCE;
            quoteSymbol = null;
            structSeparator = ".";
            escapeChar = '\\';
            statementDelimiter = DEFAULT_STATEMENT_DELIMITER;
        } else {
            keywordManager = dataSource.getContainer().getKeywordManager();
            quoteSymbol = dataSource.getInfo().getIdentifierQuoteString();
            structSeparator = dataSource.getInfo().getStructSeparator();
            dataSource.getInfo().getSearchStringEscape();
            escapeChar = '\\';
            statementDelimiter = dataSource.getInfo().getScriptDelimiter();
            if (statementDelimiter == null) {
                statementDelimiter = DEFAULT_STATEMENT_DELIMITER;
            }
        }
    }

    public void refreshRules()
    {
        final Color backgroundColor = getColor(SQLSyntaxManager.CONFIG_COLOR_BACKGROUND, SWT.COLOR_WHITE);
        final IToken keywordToken = new Token(
            new TextAttribute(getColor(SQLSyntaxManager.CONFIG_COLOR_KEYWORD), backgroundColor, SWT.BOLD));
        final IToken typeToken = new Token(
            new TextAttribute(getColor(SQLSyntaxManager.CONFIG_COLOR_DATATYPE), backgroundColor, SWT.BOLD));
        final IToken stringToken = new Token(
            new TextAttribute(getColor(SQLSyntaxManager.CONFIG_COLOR_STRING)));
        final IToken quotedToken = new Token(
            new TextAttribute(getColor(SQLSyntaxManager.CONFIG_COLOR_DATATYPE)));
        final IToken numberToken = new Token(
            new TextAttribute(getColor(SQLSyntaxManager.CONFIG_COLOR_NUMBER)));
        final IToken commentToken = new SQLCommentToken(
            new TextAttribute(getColor(SQLSyntaxManager.CONFIG_COLOR_COMMENT)));
        final SQLDelimiterToken delimiterToken = new SQLDelimiterToken(
            new TextAttribute(getColor(SQLSyntaxManager.CONFIG_COLOR_DELIMITER, SWT.COLOR_RED)));
        final SQLParameterToken parameterToken = new SQLParameterToken(
            new TextAttribute(getColor(SQLSyntaxManager.CONFIG_COLOR_PARAMETER, SWT.COLOR_DARK_BLUE), backgroundColor, SWT.BOLD));
        final IToken otherToken = new Token(
            new TextAttribute(getColor(SQLSyntaxManager.CONFIG_COLOR_TEXT)));
        final SQLBlockBeginToken blockBeginToken = new SQLBlockBeginToken(
            new TextAttribute(getColor(SQLSyntaxManager.CONFIG_COLOR_KEYWORD), backgroundColor, SWT.BOLD));
        final SQLBlockEndToken blockEndToken = new SQLBlockEndToken(
            new TextAttribute(getColor(SQLSyntaxManager.CONFIG_COLOR_KEYWORD), backgroundColor, SWT.BOLD));

        setDefaultReturnToken(otherToken);
        List<IRule> rules = new ArrayList<IRule>();

        // Add rule for single-line comments.
        for (String lineComment : getKeywordManager().getSingleLineComments()) {
            rules.add(new EndOfLineRule(lineComment, commentToken)); //$NON-NLS-1$
        }

        // Add rules for delimited identifiers and string literals.
        if (quoteSymbol != null) {
            rules.add(new SingleLineRule(quoteSymbol, quoteSymbol, quotedToken, escapeChar));
        }
        if (quoteSymbol == null || !quoteSymbol.equals(SQLConstants.STR_QUOTE_SINGLE)) {
            rules.add(new NestedMultiLineRule(SQLConstants.STR_QUOTE_SINGLE, SQLConstants.STR_QUOTE_SINGLE, stringToken, escapeChar));
        }
        if (quoteSymbol == null || !quoteSymbol.equals(SQLConstants.STR_QUOTE_DOUBLE)) {
            rules.add(new SingleLineRule(SQLConstants.STR_QUOTE_DOUBLE, SQLConstants.STR_QUOTE_DOUBLE, quotedToken, escapeChar));
        }

        Pair<String, String> multiLineComments = getKeywordManager().getMultiLineComments();
        if (multiLineComments != null) {
            // Add rules for multi-line comments
            rules.add(new MultiLineRule(multiLineComments.getFirst(), multiLineComments.getSecond(), commentToken, (char) 0, true));
        }

        // Add generic whitespace rule.
        rules.add(new WhitespaceRule(new TextWhiteSpaceDetector()));

        // Add numeric rule
        rules.add(new NumberRule(numberToken));

        // Add word rule for keywords, types, and constants.
        WordRule wordRule = new WordRule(new SQLWordDetector(), otherToken, true);
        for (String reservedWord : keywordManager.getReservedWords()) {
            wordRule.addWord(reservedWord, keywordToken);
        }
        for (String function : keywordManager.getFunctions()) {
            wordRule.addWord(function, typeToken);
        }
        for (String type : keywordManager.getTypes()) {
            wordRule.addWord(type, typeToken);
        }
        wordRule.addWord(SQLConstants.BLOCK_BEGIN, blockBeginToken);
        wordRule.addWord(SQLConstants.BLOCK_END, blockEndToken);
        rules.add(wordRule);

        {
            // Delimiter rule
            WordRule delimRule = new WordRule(new IWordDetector() {
                @Override
                public boolean isWordStart(char c)
                {
                    return statementDelimiter.charAt(0) == c;
                }

                @Override
                public boolean isWordPart(char c)
                {
                    return statementDelimiter.indexOf(c) != -1;
                }
            });
            delimRule.addWord(statementDelimiter, delimiterToken);
            rules.add(delimRule);
        }

        {
            // Parameter rule
            IRule parameterRule = new IRule() {
                private StringBuilder buffer = new StringBuilder();

                @Override
                public IToken evaluate(ICharacterScanner scanner)
                {
                    int column = scanner.getColumn();
                    if (column  <= 0) {
                        return Token.UNDEFINED;
                    }
                    scanner.unread();
                    int prevChar = scanner.read();
                    if (Character.isJavaIdentifierPart(prevChar) ||
                        prevChar == ':' || prevChar == '?' || prevChar == '\\' || prevChar == '/')
                    {
                        return Token.UNDEFINED;
                    }
                    int c = scanner.read();
                    if (c != ICharacterScanner.EOF && (c == '?' || c == ':')) {
                        buffer.setLength(0);
                        do {
                            buffer.append((char) c);
                            c = scanner.read();
                        } while (c != ICharacterScanner.EOF && Character.isJavaIdentifierPart(c));
                        scanner.unread();

                        if ((buffer.charAt(0) == '?' && buffer.length() == 1) || (buffer.charAt(0) == ':' && buffer.length() > 1)) {
                            return parameterToken;
                        }

                        for (int i = buffer.length() - 1; i >= 0; i--) {
                            scanner.unread();
                        }
                    } else {
                        scanner.unread();
                    }
                    return Token.UNDEFINED;
                }
            };
            rules.add(parameterRule);
        }

        IRule[] result = new IRule[rules.size()];
        rules.toArray(result);
        setRules(result);
    }

    public Color getColor(String colorKey)
    {
        return getColor(colorKey, SWT.COLOR_BLACK);
    }

    public Color getColor(String colorKey, int colorDefault)
    {
        ITheme currentTheme = themeManager.getCurrentTheme();
        Color color = currentTheme.getColorRegistry().get(colorKey);
        if (color == null) {
            color = Display.getDefault().getSystemColor(colorDefault);
        }
        return color;
    }

}